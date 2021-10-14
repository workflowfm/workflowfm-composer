package com.workflowfm.composer.prover.command;

import java.util.Collection;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ComposeActionState;

import com.workflowfm.composer.workspace.Workspace;

public class ComposeCommand extends ProverCommand {
	private String name;
	private Collection<ComposeAction> actions;
	private Collection<CProcess> components;
	private ComposeActionState state;

	public ComposeCommand(Workspace workspace, String name, Collection<ComposeAction> actions, ExceptionHandler handler) throws NotFoundException {
		this(name, actions, workspace.getComponents(actions), handler);
	}

	public ComposeCommand(String name, Collection<ComposeAction> actions, Collection<CProcess> components, ExceptionHandler handler) {		
		super("compose",handler);
		this.name = name;
		this.actions = actions;
		this.components = components;
		this.state = new ComposeActionState(name,components);
	}

	@Override
	public String debugString() { 
		StringBuffer acts = new StringBuffer();
		for (ComposeAction act : actions) {
			acts.append(act.debugString());
			acts.append(" > ");
		}
		return "ComposeCommand[" + name + ": " + acts + " (" + state.getCtr() + ")]"; 
	}
	
	public String getName() {
		return this.name;
	}
	
	public Collection<CProcess> getComponents() { 
		return this.components;
	}

//	@Override
//	protected Vector<UndoableAction> handleResponse(ProverResponse response) {
//		Vector<UndoableAction> actions = new Vector<UndoableAction>();
//
//		if (response instanceof ComposeResponse) {
//			ComposeResponse r = (ComposeResponse) response;
//			switch (r.getAction().getAction().toUpperCase()) {
//			case "JOIN":
//				actions.add(new JoinAction(r.getProcess(),r.getAction(),r.getState()));
//				break;
//			case "WITH":
//				actions.add(new WithAction(r.getProcess(),r.getAction(),r.getState()));
//				break;
//			case "TENSOR":
//				actions.add(new TensorAction(r.getProcess(),r.getAction(),r.getState()));
//				break;
//			default:
//				Log.w("Unknown compose action: " + r.getAction().getAction());
//				actions.add(new CompositionAction(r.getProcess(),r.getAction(),r.getState()));
//				break;
//			}
//		} else { 
//			actions.add(new UnexpectedResponseAction(response.debugString(),"ComposeResponse"));
//		}
//		return actions;
//	}
}
