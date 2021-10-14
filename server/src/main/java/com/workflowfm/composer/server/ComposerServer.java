package com.workflowfm.composer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.prover.HolLightServerProcess;
import com.workflowfm.composer.utils.Log;

public class ComposerServer {
	private HolLightServerProcess hol;
	private ServerSocket sock;

	public ComposerServer() {
		this.hol = new HolLightServerProcess();
	}

	public void start(int port) {
		try
		{
			this.sock = new ServerSocket(port,10);
			//this.sock.setSoTimeout(555055000);
		} catch(IOException ex) {
			Log.e("Unable to listen on port:" + port);
			Log.e(ex.getLocalizedMessage());
		}
		
		this.hol.start();
		if (!hol.isStarted())
			return;
		
		Log.d("Server socket created. Waiting for connection...");
		while(true) {
			try {
				Socket connection = sock.accept();
				Log.d("Connection received from " + connection.getInetAddress().getHostName() + " (" + connection.getInetAddress().getHostAddress() + ":" + connection.getPort() + ")");
				//create new thread to handle client

				new ComposerServerSession(connection, hol).start();
			} catch(IOException ex) {
				Log.e("Failed to accept connection on port: " + port);
				Log.e(ex.getLocalizedMessage());
			}
		}
		
		//this.hol.stop();
	}
	
	public static void main(String args[])
	{
		// if (args.length > 0) ComposerProperties.readProperties(args[0]);
		ComposerServer server = new ComposerServer();
		server.start(ComposerProperties.serverPort());
	}
}












