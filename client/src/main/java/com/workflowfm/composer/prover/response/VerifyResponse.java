package com.workflowfm.composer.prover.response;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ComposeActionState;

public class VerifyResponse extends ProverResponse {

	private CProcess process;
	private ComposeActionState state;
	
	public CProcess getProcess() {
		return process;
	}

	public ComposeActionState getState() {
		return state;
	}

	@Override
	public boolean isException() { return false; }

	@Override
	public String debugString() { return "VerifyResponse[ " + process.debugString() + " : " + state.debugString() + "]"; }
}
