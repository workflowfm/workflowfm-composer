package com.workflowfm.composer.prover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.utils.Log;

/** Used to launch a HOL Light process and communicate with it via pipes. */
public class HolLightSocket {
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	
	final public static String ocamlPrompt = "\n#";
	private static Pattern jsonPattern = Pattern.compile(
			"JSON_START(.*?)JSON_END", Pattern.DOTALL);
	private String commandResponse = "";
  private boolean removeNewlinesFromJsonOutput = true;

  private String host;
  private int port;
  
  protected HolLightSocket(String host, int port) {
    this.host = host;
    this.port = port;
  }

 	public void start() throws UserError {
		if (isConnected()) return;
		
//		String workingDirectory = ComposerProperties.proverDirectory();
//	    String command = ComposerProperties.launchProverCommand();

		Log.d("Connecting to HOL Light server: " + host + ":" + port + " ..."); 

		try {
      socket = new Socket(host, port);
			writer = new PrintWriter(socket.getOutputStream(),true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			Log.d("Connection successful.");// Running startup tests."); //TODO come up with another test
			//testHolLightIsWorking();
            //Log.d("HOL Light startup test passed.");

      ComposerProperties.set("server", host);
      ComposerProperties.set("port", port);      
		} catch (IOException err) {
			throw new UserError("Unable to connect to HOL Light server: " + host + ":" + port, err);
		}
	}

	public void stop() {
		Log.d("Closing connection to HOL Light.");
		
		try {
			if (writer != null) writer.close();
			if (reader != null) reader.close();
			if (socket != null) socket.close();
		} catch (IOException e){
			e.printStackTrace();
		}
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

	protected void pipeStringToOCaml(String m) {
		Log.d("Sending HOL Light command: " + m);
		writer.println(m);
	}

	@SuppressWarnings("resource")
	protected String scanOutputUntil(String s) throws UserError {
		try {
			Scanner scanner = new Scanner(reader);
			scanner.useDelimiter(s);
			return scanner.next();
		} catch (Exception e) {
			throw new UserError("Failed to read HOL Light response.", e);
		}
	}

	protected Vector<String> parseJsonOutputs() throws UserError {
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
	
	protected Vector<String> sendJsonString(String json) throws UserError {
		int max = ComposerProperties.serverMaxAttempts();
		for (int i = 0; i < (max > 0?max:Integer.MAX_VALUE); i++) {
			try {
				pipeStringToOCaml(json);
				Vector<String> jsonOutputs = parseJsonOutputs();
				return jsonOutputs;
			} catch (Exception e) {
				Log.d("Failed to read HOL Light response. Attempting to reconnect...");
				stop();
				socket = null;
				start();
			}
		}
		throw new UserError("Failed to get response from HOL Light server.");		
	}

	public String getCommandResponse() {
		return commandResponse;
	}
  
  public boolean isConnected() {
    return (socket != null && socket.isConnected() && !socket.isClosed());
  }
}
