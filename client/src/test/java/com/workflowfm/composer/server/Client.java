package com.workflowfm.composer.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.commons.lang3.StringEscapeUtils;

public class Client {
	public static void main(String args[])
	{ 
		try
		{
			Socket sock = new Socket("localhost",7000);
			System.out.println("Connected. Now sending data.");
			PrintWriter out = new PrintWriter(sock.getOutputStream(),true);
			out.println(StringEscapeUtils.escapeJava("{\"name\":\"P1\",\"inputs\":[{\"type\":\"var\",\"name\":\"X\",\"args\":[]}],\"output\":{\"type\":\"var\",\"name\":\"Y\",\"args\":[]},\"command\":\"create\",\"succeeded\":false}"));
			//out.flush();
			System.out.println("Data sent.");
			//			System.out.println("Message sent to the server : "+sendMessage);

			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		
			String input;
			System.out.println("Waiting for input.");
			while ((input = in.readLine()) != null && !input.equals("#")) {
				System.out.println("Message received from the server : " + input);
			}
			sock.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
