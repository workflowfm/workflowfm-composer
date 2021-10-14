package com.workflowfm.composer.prover;

import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllTermPath;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.ui.PortEdge;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.prover.command.ProverCommand;
import com.workflowfm.composer.prover.response.ProverResponse;
import com.workflowfm.composer.prover.response.UnknownResponse;
import com.workflowfm.composer.utils.CustomGson;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.utils.Utils;
import com.workflowfm.composer.utils.validate.NonemptyStringValidator;
import com.workflowfm.composer.utils.validate.RegexStringValidator;
import com.workflowfm.composer.utils.validate.Validator;
import com.workflowfm.composer.utils.validate.ValidatorGroup;

/**
 * Used to communicate to a HOL Light prover process while keeping track of the
 * proof script needed to represent the state that the prover is in.
 */

public class HolLight extends HolLightSocket implements Prover {

	private static final String regex = "[a-zA-Z][a-zA-Z0-9_]*";
	private static final String validatorMessage = "Must start with a letter.\nAllowed characters: a-z A-Z 0-9 _";
	private Validator<String> resourceValidator;
	private Validator<String> channelValidator;
	private Validator<String> processValidator;

	/** Stores all strings sent to the prover. Useful for debug purposes. */
	private StringBuffer log = new StringBuffer();
	private CopyOnWriteArrayList<ProverStateListener> stateListeners = new CopyOnWriteArrayList<ProverStateListener>();

	private LinkedBlockingQueue<ProverCommand> commandQueue = new LinkedBlockingQueue<ProverCommand>();

  private Thread executionThread;


  public HolLight() {
    this(ComposerProperties.serverURL(), ComposerProperties.serverPort());
  }

  public HolLight(String host, int port) {
    super(host, port);

		{
			String fieldType = "resource name";
			ValidatorGroup<String> v = new ValidatorGroup<>(2);
			v.add(new NonemptyStringValidator(fieldType));
			v.add(new RegexStringValidator(fieldType, regex, validatorMessage));
			this.resourceValidator = v;
		}
		{
			String fieldType = "channel name";
			ValidatorGroup<String> v = new ValidatorGroup<>(2);
			v.add(new NonemptyStringValidator(fieldType));
			v.add(new RegexStringValidator(fieldType, regex, validatorMessage));
			this.channelValidator = v;
		}
		{
			String fieldType = "process name";
			ValidatorGroup<String> v = new ValidatorGroup<>(2);
			v.add(new NonemptyStringValidator(fieldType));
			v.add(new RegexStringValidator(fieldType, regex, validatorMessage));
			this.processValidator = v;
		}
	}

	public String cllOperatorString(CllTerm cll) {
		String type = cll.getType();

		if (type.equals("var")) return cll.getName();
		else if (type.equals("neg")) return "NEG"; 
		else if (type.equals("times")) return "**";
		else if (type.equals("plus")) return "++";
		else {
			Log.e("Unknown CLL type encountered: [" + type + "]");
			return type;
		}
	}	

	@Override
	public String cllResourceString(CllTerm cll) {
		String op = cllOperatorString(cll);
		final Vector<CllTerm> args = cll.getArgs();

		if (cll.isAtomic()) return op;
		else if (cll.isUnary()) return "(" + op + " " + cllResourceString(args.get(0)) + ")";
		else {
			Vector<String> argStrings = new Vector<String>(args.size());
			for (CllTerm c : args) {
				argStrings.add(cllResourceString(c));
			}
			return "(" + String.join(" " + op + " ", argStrings) + ")";
		}
	}

	@Override
	public String portResourceString(ProcessPort port) {
		return portResourceString(port.getChannel(), port.getCllTerm());
	}

	@Override 
	public String portResourceString(String channel, CllTerm term) {
		return cllResourceString(term) + " <> " + channel;
	}

	@Override
	public String cllPath(PortEdge e) throws InvalidCllPathException {
		return cllPath(e.getRootTerm(), e.getTermPath());
	}		

	@Override
	public String cllPath(CllTerm tm, CllTermPath path) throws InvalidCllPathException {
		if (path.isRoot()) return "";

		StringBuffer res = new StringBuffer();
		CllTerm tmi = tm;

		for (Integer it : path.getCollection()) { 
			//System.out.println("Term: " + cllResourceString(tmi) + " - Args: " + tmi.getArgs().size() + " - it: " + it);
			if (it == tmi.getArgs().size() - 1) res.append(Utils.repeatString("r", it));
			else res.append(Utils.repeatString("r", it)).append("lr");
			tmi = new CllTermPath(it.toString()).follow(tmi);

		}
		return res.toString();
	}

	@Override
	public void execute(ProverCommand command) {
		commandQueue.add(command);
	}

	private void sendCommand(ProverCommand command) { 
		sendCommand(command,false);
	}

	
	private void sendCommand(ProverCommand command, boolean reconnect) { 
		try {
			if (reconnect) restart();
			
			String json = Utils.getJsonString(command);
	
			Log.d("****\nsendCommand: " + command.debugString() + "\n****\n");
	
			long startTime = System.currentTimeMillis();
	
			Vector<String> jsonOutputs = super.sendJsonString(json);
	
			String s = "SENT {" + json + "}\n"
					+ " - Time: " + (System.currentTimeMillis() - startTime) + "ms.\n";
	
			for (String out : jsonOutputs) {
				s += "RESPONSE {" + out + "}\n";
			}
			addToLog(s);
			
			try {
				command.setResponses(parseResponses(jsonOutputs));
			} catch (UserError e) {
				command.getExceptionHandler().handleException(e);
			}
			
		} catch (UserError e) {
			if (reconnect) {
				Log.d("sendCommand failed");
				command.proverError(e);
			}
			else {
				Log.d("sendCommand failed - attempting to restart prover");
				sendCommand(command, true);
			}
		}
	}
	
	/**
	 * Converts the JSON reply from the prover into a Response object. 
	 */
	public Vector<ProverResponse> parseResponses(Vector<String> jsonOutputs) throws UserError {
		//if (jsonOutputs.isEmpty()) return null;
		
		Vector<ProverResponse> responses = new Vector<ProverResponse>();
		for (String json : jsonOutputs) {
			System.err.println("JSON reply: " + json);
			if (json == "") { //probably not needed any more!
				continue; //return new ProverResponse();
			} else {
				//System.out.println(json);
				JsonObject object = new JsonParser().parse(json)
						.getAsJsonObject();
	
				if (object.has("error")) {
					String error = object.get("error").getAsString();
					throw new UserError(error);
				}
	
				String responseName = object.get("response").getAsString();
				System.out.println("Response: " + responseName);
	
				try {
					String classPath = "com.workflowfm.composer.prover.response." + responseName + "Response";
					ProverResponse response = (ProverResponse) CustomGson.getGson().fromJson(object,
							(Class<?>) Class.forName(classPath));

					responses.add(response);

				} catch (ClassNotFoundException e) {
					responses.add(new UnknownResponse(responseName));
				}
			}
		}
		return responses;
	}

	@Override
	public void addToLog(String c) {
		log.append(c);
		notifyLogChanged();
	}

	@Override
	public void start() throws UserError {
		super.start();
		executionThread = new Thread(new Runnable() {
			private boolean isExecuting = false;

			@Override
			public void run()
			{
				while (!Thread.currentThread().isInterrupted()) {
					try {
						if (isExecuting && commandQueue.isEmpty()) {
							isExecuting = false;
							for (ProverStateListener l : stateListeners) {
								l.executionStopped(HolLight.this);
							}
						}

						ProverCommand command = commandQueue.take();
						Log.d("Executing next command...");
						if (!isExecuting) {
							isExecuting = true;
							for (ProverStateListener l : stateListeners) {
								l.executionStarted(HolLight.this);
							}
						}

						sendCommand(command);

					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
						break;
					}
				}
				Log.d("Prover execution thread closing.");
			}
		});
		executionThread.start();
	}

	@Override
	public void stop() {
		executionThread.interrupt();
		super.stop();
	}

	@Override
	public void restart() throws UserError {
		super.stop();
		super.start();
		log = new StringBuffer();
		notifyLogChanged();
	}

	@Override
	public void addStateListener(ProverStateListener p) {
		stateListeners.add(p);
	}

	@Override
	public void removeStateListener(ProverStateListener p) {
		stateListeners.remove(p);
	}

	private void notifyLogChanged() {
		for (ProverStateListener p : stateListeners) {
			p.logUpdated(this);
		}
	}


	//    public Queue<String> getCommandsFromFile(File file) throws FileNotFoundException, IOException {
	//		return getScriptCommands(readFileAsString(file));  
	//	}

	//	/** Returns the contents of a file as a string. */
	//	private String readFileAsString(final File file)
	//			throws FileNotFoundException, IOException {
	//		FileInputStream stream = null;
	//		String allLines = "";
	//		try {
	//			stream = new FileInputStream(file);
	//			DataInputStream in = new DataInputStream(stream);
	//			BufferedReader br = new BufferedReader(new InputStreamReader(in));
	//			String read;
	//			while ((read = br.readLine()) != null) {
	//				allLines += read + "\n";
	//			}
	//			in.close();
	//		} finally {
	//			if (stream != null)
	//				stream.close();
	//		}
	//		return allLines;
	//	}

	// TODO FIX THIS FOR JSON!
	//	public Queue<String> getScriptCommands(String script) {
	//		String commands[] = script.split(";;");
	//		Queue<String> result = new LinkedList<String>();
	//		
	//		for (String line : commands) {
	//			line = line.trim();
	//			if (line.length() < 3)
	//				continue; // "p()" is the smallest possible command!
	//			
	//			result.add(line);
	//			
	//			// We edit the commands to adjust them to the local session/environment. 
	//			//line += " ";
	//			//int i = line.indexOf(' ');
	//			//line = MODULE + "." + line.substring(0, i) + " " + gui.getWorkspace().getSessionid() + line.substring(i);
	//
	//			//System.err.println("Executing script command: " + line);
	//
	//			//gui.executeProverCommandInBackground(line);
	//			// generateAndExecuteCommandObject(workspace, gui, line);
	//		}
	//		return result;
	//	}

	@Override
	public String getLog() {
		return log.toString();
	}


	@Override
	public Validator<String> getResourceValidator() {
		return resourceValidator;
	}

	@Override
	public Validator<String> getChannelValidator() {
		return channelValidator;
	}

	@Override
	public Validator<String> getProcessValidator() {
		return processValidator;
	}
}
