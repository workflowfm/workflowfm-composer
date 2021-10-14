package com.workflowfm.composer.prover.response;

public class ExceptionResponse extends ProverResponse {
	protected String content;
	
	public ExceptionResponse() { }
	
	public ExceptionResponse(String content) {
		this.response = "Exception";
		this.content = content;
	}
	
	public String getContent() { 
		return this.content;
	}
	
	@Override
	public boolean isException() { return true; }

	@Override
	public String debugString() { return "ExceptionResponse[" + content + "]"; }

	public String errorMessage() {
		return "Exception thrown from prover"; 
	}
	
}
