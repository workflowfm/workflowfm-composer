package com.workflowfm.composer.prover.command;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ComposeActionState;

import com.workflowfm.composer.workspace.Workspace;

public class Compose1Command extends ProverCommand {
	private ComposeAction action;
	private CProcess lhs;
	private CProcess rhs;
	private ComposeActionState state;

	public Compose1Command(Workspace workspace, ComposeAction action, ExceptionHandler handler) throws NotFoundException {
		this(action, workspace.getProcess(action.getLarg()), workspace.getProcess(action.getRarg()), workspace.getName(), handler);
	}

	public Compose1Command(ComposeAction action, CProcess lhs, CProcess rhs, String label, ExceptionHandler handler) {		
		super("compose1",handler);
		this.action = action;
		this.lhs = lhs;
		this.rhs = rhs;
		this.state = new ComposeActionState(label,lhs,rhs);
	}
	
	public CProcess getLhs() {
		return this.lhs;
	}
	
	public CProcess getRhs() {
		return this.rhs;
	}

	@Override
	public String debugString() { return "Compose1Command[" + action.debugString() + " (" + state.getCtr() + ")]"; }
}
