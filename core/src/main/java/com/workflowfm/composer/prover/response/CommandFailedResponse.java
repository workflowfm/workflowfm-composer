package com.workflowfm.composer.prover.response;

public class CommandFailedResponse extends ExceptionResponse {

	public CommandFailedResponse() {
		super();
	}

	public CommandFailedResponse(String content) {
		super(content);
		this.response = "CommandFailed";
	}

	@Override
	public String errorMessage() {
		return "Prover command failed";
	}

	@Override
	public String debugString() { return "CommandFailedResponse[" + content + "]"; }

}
