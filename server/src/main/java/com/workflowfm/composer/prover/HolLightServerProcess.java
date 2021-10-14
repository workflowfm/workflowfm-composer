package com.workflowfm.composer.prover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.prover.response.ExceptionResponse;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.utils.Utils;


/** Used to launch a HOL Light process and communicate with it via pipes. */
public class HolLightServerProcess {

	private Thread executionThread;
	
	private LinkedBlockingQueue<ProverRawCommand> commandQueue = new LinkedBlockingQueue<ProverRawCommand>();
	private ProverRawCommand executingCommand = null;
	
	private Process holLightProcess;
	private BufferedReader pipeIn;
	private BufferedWriter pipeOut;
	private BufferedReader pipeErr;
	
	final public static String ocamlPrompt = "\n#";
	private static Pattern jsonPattern = Pattern.compile(
			"JSON_START(.*?)JSON_END", Pattern.DOTALL);
	private String commandResponse = "";
	private boolean removeNewlinesFromJsonOutput = true;
	
	private boolean started = false;

	public void start() {
		if (holLightProcess != null) return;
		
    String command = ComposerProperties.launchProverCommand();

		Log.d("Starting HOL Light via [" + command + "]");

		try {
			holLightProcess = Runtime.getRuntime().exec(command);
			
			pipeIn = new BufferedReader(new InputStreamReader(
					holLightProcess.getInputStream()));
			pipeOut = new BufferedWriter(new OutputStreamWriter(
					holLightProcess.getOutputStream()));
			pipeErr = new BufferedReader(new InputStreamReader(
					holLightProcess.getErrorStream()));
			// As we parse the output of OCaml to find out if a command succeed
			// or not, we first need to ignore any output generated when OCaml
			// is started up so this output doesn't confuse the parser. We do
			// this by looking for a string that we have told OCaml to print
			// once setup is complete.
			Log.d("Waiting for HOL Light to finish setting up.");
			waitOutputUntil("Prover ready");
      Log.d(scanOutputUntil(ocamlPrompt));
			//if (pipeErr.ready()) while (pipeErr.readLine() != null); // empty error stream - LOOPS!

			Log.d("HOL Light started.");// Running startup tests."); //TODO come up with another test
			//testHolLightIsWorking();
			//Log.d("HOL Light startup test passed.");
		} catch (UserError|IOException err) {
			err.printStackTrace();
			return;
		}
		
		executionThread = new Thread(new Runnable() {
        private boolean isExecuting = false;

        @Override
        public void run()
        {
          while (!Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()) {
            try {
              if (isExecuting && commandQueue.isEmpty()) {
                isExecuting = false;
                executingCommand = null;
                //for (ProverStateListener l : stateListeners) {
								//l.executionStopped(HolLight.this);
                //}
              }

              executingCommand = commandQueue.take();
              Log.d("Executing next command...");
              if (!isExecuting) {
                isExecuting = true;
                //for (ProverStateListener l : stateListeners) {
								//l.executionStarted(HolLight.this);
                //}
              }

              sendCommand(executingCommand);

            } catch (InterruptedException ex) {
              Thread.currentThread().interrupt();
              stop();
            }
          }
          Log.d("Prover execution thread closing.");
        }
      });
		executionThread.start();
		started = true;
	}

	public void stop() {
		Log.d("Stopping HOL Light.");
		started = false;
		
		executionThread.interrupt();
		try {
			pipeIn.close();
			pipeOut.close();
			pipeErr.close();	
		} catch (IOException e){
			//e.printStackTrace();
		}
		
		if (holLightProcess != null) {
			holLightProcess.destroy();
			holLightProcess = null;
		}
	}
	
	public void execute(ProverRawCommand command) {
		if (executingCommand != null && executingCommand.equals(command)) {
			Log.d("Command already executing at prover...");
			for (CommandCompletionListener listener : command.getCompletionListeners()) {
				executingCommand.addCompletionListener(listener);
			}
			return;
		}
		else { 
			for (ProverRawCommand queued : commandQueue) {
				if (queued.equals(command)) {
					Log.d("Command already queued...");
					for (CommandCompletionListener listener : command.getCompletionListeners()) {
						queued.addCompletionListener(listener);
					}
					return;
				}
			}
		}
		Log.d("Adding command to queue...");
		commandQueue.add(command);
	}
	
	private void sendCommand(ProverRawCommand command) { // TODO possible exceptions?? What if the prover fails/disconnects?
		String json = command.getCommand();
		
		long startTime = System.currentTimeMillis();

		Vector<String> jsonOutputs = sendJsonString(json);

		executingCommand = null;
		
		//String s = "SENT {" + json + "}\n"
		//		+ " - Time: " + (System.currentTimeMillis() - startTime) + "ms.\n";
		String s = " - Time: " + (System.currentTimeMillis() - startTime) + "ms.\n";
		Log.d(s);
		
		//for (String out : jsonOutputs) {
		//	s += "RESPONSE {" + out + "}\n";
		//}
		//addToLog(s);

		command.setResponses(jsonOutputs);;
	}

	// TODO Need another way to test HOL Light now.
//	public void testHolLightIsWorking() {
//		if (!sendCommand("true"))
//			throw new RuntimeException(
//					"Command failed when it was meant to succeed.");
//
//		// The command should be recognised but should fail as this command
//		// backtracks in a proof and we haven't started a proof yet.
//		if (sendCommand("b()"))
//			throw new RuntimeException(
//					"The HOL Light functions don't seem to be available.");
//
//		boolean expectedBehaviour = false;
//		try {
//			// OCaml should reply "Error: Unbound value..."
//			sendCommand("a b c d");
//		} catch (RuntimeException e) {
//			expectedBehaviour = true;
//		}
//		if (!expectedBehaviour)
//			throw new RuntimeException(
//					"An invalid command which was expected to fail succeeded.");
//	}

	private void pipeStringToOCaml(String m) throws UserError {
		Log.d("Sending HOL Light command: " + m);
		try {
			pipeOut.write(m + "\n");
			pipeOut.flush();
		} catch (IOException e) {
			//e.printStackTrace();
			throw new UserError("Could not pipe command to HOL Light.",e);
		}
	}
    
  private void waitOutputUntil(String m) throws UserError {
    try {
      String line;
      while ((line = pipeIn.readLine()) != null) {
        int index = -1;
        Log.d(">>> >>> " + line);
        if(line.indexOf(m,index+1) != -1)
          return;
      }
      throw new UserError("Reached EOF");
    } catch (Exception e) {
      throw new UserError("Failed to read HOL Light response: " + m,e);
    }
  }

	@SuppressWarnings("resource")
	private String scanOutputUntil(String s) throws UserError {
		try {
			Scanner scanner = new Scanner(pipeIn);
			scanner.useDelimiter(s);
			return scanner.next();
		} catch (Exception e) {
			throw new UserError("Failed to read HOL Light response.",e);
		}
	}

	private Vector<String> parseJsonOutputs() throws UserError {
		Log.d("Waiting for OCaml response.");
		commandResponse = scanOutputUntil(ocamlPrompt);

		Log.d("OCaml response: (" + commandResponse + ")");

		Vector <String> jsonOutputs = new Vector<String>();

		Matcher matcher = jsonPattern.matcher(commandResponse);
		while (matcher.find()) {
			if (removeNewlinesFromJsonOutput)
				jsonOutputs.add(matcher.group(1).replace("\n", " "));
			else 
				jsonOutputs.add(matcher.group(1));
		}
		return jsonOutputs;
	}
	
	private Vector<String> sendJsonString(String json) {
		Vector<String> jsonOutputs = new Vector<String>();
		try {
			String command = "Json_composer_io.execute \"" + json + "\";;";
			pipeStringToOCaml(command);
			jsonOutputs = parseJsonOutputs();
		} catch (UserError e) {
			Log.e("Error contacting prover.");
			e.printStackTrace();
			
			String sdump;
			Log.d("=== Error stream: ===");
			try {
				while ((sdump = pipeErr.readLine()) != null)
					Log.d(sdump);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Log.d("=== ===");

			jsonOutputs.add(getJsonExceptionResponse("Prover failure. Please retry or contact the server administrator."));
			
			new Thread(new Runnable() {
          @Override
          public void run()
          {
            if (!started) return;
            Log.e("Attempting to restart prover.");
            stop();
            start();
          }
        }).start();
		}
		return jsonOutputs;
	}

	public boolean isStarted() {
		return started;
	}
	
	public String getJsonExceptionResponse(String content) {
		return Utils.getJsonString(new ExceptionResponse(content),false); // Do NOT escape the result!
	}
	
	public static String wrapResponses(Collection<String> responses) {
		//TODO strings should be customizable not hardcoded
		StringBuffer out = new StringBuffer();
		for (String json : responses) {
			out.append("JSON_START\n");
			out.append(json);
			out.append("JSON_END\n");
		}
		out.append("\n#");
		return out.toString();
	}
}
