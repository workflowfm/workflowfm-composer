package com.workflowfm.composer.prover.response;

public abstract class ProverResponse {
	protected String response;
	
	public String getResponse() { return response; } 
	
	public abstract boolean isException();
	public abstract String debugString();
}
