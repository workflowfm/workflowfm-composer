package com.workflowfm.composer.prover;

import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A command sent to the prover. 
 */
public class ProverRawCommand {
	
	protected String command;
	
	protected transient Vector<String> responses = new Vector<String>();

	protected transient CopyOnWriteArrayList<CommandCompletionListener> completionListeners = new CopyOnWriteArrayList<CommandCompletionListener>();
	public void addCompletionListener(CommandCompletionListener a) { completionListeners.add(a); }
	public void removeCompletionListener(CommandCompletionListener a) { completionListeners.remove(a); }
	protected void notifyCompletion() { for (CommandCompletionListener a : completionListeners) a.completed(this); }
	
	public ProverRawCommand(String command) {
		this.command = command;
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setResponses(Vector<String> jsonResults) {
		this.responses = jsonResults;
		notifyCompletion();
	}
	
	public Vector<String> getResponses() {
		return this.responses;
	}
	
	public CopyOnWriteArrayList<CommandCompletionListener> getCompletionListeners() {
		return completionListeners;
	}	

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ProverRawCommand)) return false;
		ProverRawCommand other = (ProverRawCommand)o;
		return other.command.equals(command);
	}
	
	public String debugString() { return "ProverCommand[" + command + "]"; }
}
