package com.workflowfm.composer.prover.command;

import java.util.Collection;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;

public class CreateProcessCommand extends ProverCommand {

	private String name;
	private Collection<CllTerm> inputs;
	private CllTerm output;

	public CreateProcessCommand(String name, Collection<CllTerm> inputs,
			CllTerm output, ExceptionHandler handler) {
		super("create", handler);
		this.name = name;
		this.inputs = inputs;
		this.output = output;
	}
	
	public CreateProcessCommand(CProcess process, ExceptionHandler handler) {
		this(process.getName(), process.getInputCll(), process.getOutputCll(), handler);
	}

	public String getProcessName() {
		return name;
	}

	public Collection<CllTerm> getInputs() {
		return inputs;
	}

	public CllTerm getOutput() {
		return output;
	}
}

// private boolean responseReceived = false;
// @Override
// protected Vector<UndoableAction> handleResponse(ProverResponse response) {
// Vector<UndoableAction> actions = new Vector<UndoableAction>();
//
// if (response instanceof CreateProcessResponse && !responseReceived) {
// actions.add(new
// CreateProcessAction(((CreateProcessResponse)response).getProcess(),visible));
// responseReceived = true;
// } else {
// actions.add(new
// UnexpectedResponseAction(response.debugString(),"CreateProcessResponse"));
// }
//
// return actions;
// }

