package com.workflowfm.composer.prover.response;

public class UnknownResponse extends ExceptionResponse {

	public UnknownResponse(String content) {
		super(content);
	}

	@Override
	public String errorMessage() {
		return "Prover returned an unknown response.";
	}

	@Override
	public String debugString() { return "UnknownResponse[" + content + "]"; }

}
