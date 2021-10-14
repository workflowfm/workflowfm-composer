package com.workflowfm.composer.prover.response;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ComposeActionState;

public class ComposeResponse extends ProverResponse {

	private CProcess process;
	private ComposeActionState state;
	private ComposeAction action;
	
	public CProcess getProcess() {
		return process;
	}

	public ComposeActionState getState() {
		return state;
	}
	
	public ComposeAction getAction() {
		return action;
	}

	@Override
	public boolean isException() { return false; }

	@Override
	public String debugString() { return "ComposeResponse[" + action.debugString() + ": " + process.debugString() + " (" + state.debugString() + ")]"; }
}
