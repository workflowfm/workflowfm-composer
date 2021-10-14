package com.workflowfm.composer.prover.command;

import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.prover.response.ProverResponse;
import com.workflowfm.composer.prover.response.UnknownResponse;
import com.workflowfm.composer.utils.Completable;
import com.workflowfm.composer.utils.CompletionListener;
import com.workflowfm.composer.utils.CustomGson;

/**
 * A command sent to the prover. 
 */
public class ProverCommand implements Completable {
	
	protected String command;
	protected boolean succeeded = false;
	private transient ExceptionHandler handler;
	
	protected transient Vector<ProverResponse> responses = new Vector<ProverResponse>();

	protected transient CopyOnWriteArrayList<CompletionListener> completionListeners = new CopyOnWriteArrayList<CompletionListener>();
	@Override
	public void addCompletionListener(CompletionListener a) { completionListeners.add(a); }
	@Override
	public void removeCompletionListener(CompletionListener a) { completionListeners.remove(a); }
	protected void notifyCompletion() { for (CompletionListener a : completionListeners) a.completed(); }
	
	public ProverCommand(String command, ExceptionHandler handler) {
		this.command = command;
		this.handler = handler;
		this.succeeded = false;
	}
	
	public String getCommand() {
		return command;
	}
	
	public ExceptionHandler getExceptionHandler() {
		return handler;
	}

	public boolean succeeded() {
		return succeeded;
	}
	
	public void setResponses(Vector<ProverResponse> responses) {
    	this.responses = responses;
		this.succeeded = true;
		notifyCompletion();
	}
	
	public Vector<ProverResponse> getResponses() {
		return this.responses;
	}
	
	public void proverError(UserError e) {
		handler.handleException(e);
		this.succeeded = false;
		notifyCompletion();
	}

	public String debugString() { return "ProverCommand[" + command + "]"; }
}
