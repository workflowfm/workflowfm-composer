package com.workflowfm.composer.prover.response;

import com.workflowfm.composer.processes.CProcess;

public class CreateProcessResponse extends ProverResponse {
	private CProcess process;

	public CProcess getProcess() { return process; }
	
	@Override
	public String debugString() { return "CreateProcessResponse[" + process.debugString() + "]"; }

	@Override
	public boolean isException() { return false; }
}