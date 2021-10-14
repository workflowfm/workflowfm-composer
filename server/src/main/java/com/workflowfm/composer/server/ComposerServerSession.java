package com.workflowfm.composer.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.workflowfm.composer.prover.CommandCompletionListener;
import com.workflowfm.composer.prover.HolLightServerProcess;
import com.workflowfm.composer.prover.ProverRawCommand;
import com.workflowfm.composer.utils.Log;

public class ComposerServerSession extends Thread implements CommandCompletionListener {

	private final static int OPEN = 0;
	private final static int AUTHED = 1;

	private int state;
	private HolLightServerProcess hol;
	private Socket socket;

	private PrintWriter writer;

	public ComposerServerSession(Socket socket, HolLightServerProcess hol) {
		this.socket = socket;
		this.hol = hol;
	}

	public void run() {
		try {
			this.socket.setSoTimeout(400000); // 5 minutes
			this.writer = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							socket.getInputStream()));
			this.state = OPEN;

			// TODO authenticate
			this.state = AUTHED;

			String input;
			try {
				while ((input = in.readLine()) != null) {
					Log.d("Data received on socket [" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() +"]: " + input);
					if (handleInput(input) < 0) 
						break;
				} 
			} catch (SocketTimeoutException e) {
				Log.d("Timeout on socket [" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() +"]");
			}
			
			Log.d("Closing socket [" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() +"]");
			socket.close();
			
		} catch (IOException e) {
			Log.e("IO Error in socket [" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() +"]: " + e.getLocalizedMessage());
			//e.printStackTrace();
		}
	}

	protected int handleInput(String input) {
		if (this.state != AUTHED) return -2;
		
		ProverRawCommand command = new ProverRawCommand(input);

		// TODO validate
		if (false) 
			return -1;

		command.addCompletionListener(this);
		hol.execute(command);
		return 0;
	}

	public void sendOutput(String output) {
		if (socket == null) {
			Log.d("Tried to send [" + output.length() + "] characters to null socket.");	
		}
		else if (!socket.isConnected() || socket.isClosed()) {
			Log.d("Tried to send [" + output.length() + "] characters to [" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "] but it was closed.");			
		}
		else {
			Log.d("Sending [" + output.length() + "] characters to [" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]");
			writer.println(output);
			writer.flush();
		}
	}
	
	@Override
	public void completed(ProverRawCommand command) {
		sendOutput(HolLightServerProcess.wrapResponses(command.getResponses()));
	}
}
