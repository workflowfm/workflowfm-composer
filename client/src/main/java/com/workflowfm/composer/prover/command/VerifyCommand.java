package com.workflowfm.composer.prover.command;

import java.util.Collection;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ComposeActionState;

import com.workflowfm.composer.workspace.Workspace;

public class VerifyCommand extends ProverCommand {
	private String name;
	private Collection<ComposeAction> actions;
	@SuppressWarnings("unused")
	private Collection<CProcess> components;
	private String result;
	private ComposeActionState state;
	
	public VerifyCommand(Workspace workspace, String name, Collection<ComposeAction> actions, String result, ExceptionHandler handler) throws NotFoundException {
		this(name, actions, workspace.getComponents(actions), result, workspace.getName(),handler);
	}

	public VerifyCommand(String name, Collection<ComposeAction> actions, Collection<CProcess> components, String result, String label, ExceptionHandler handler) {		
		super("verify",handler);
		this.name = name;
		this.actions = actions;
		this.components = components;
		this.result = result;
		this.state = new ComposeActionState(label);
	}
	
	@Override
	public String debugString() { 
		StringBuffer acts = new StringBuffer();
		for (ComposeAction act : actions) {
			acts.append(act.debugString());
			acts.append(">");
		}
		return "VerifyCommand[" + name + " (" + result + "): " + acts + " (" + state.getCtr() + ")]"; }
}
