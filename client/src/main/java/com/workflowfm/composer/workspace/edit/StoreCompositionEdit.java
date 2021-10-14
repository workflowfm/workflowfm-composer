package com.workflowfm.composer.workspace.edit;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.workspace.Workspace;

public class StoreCompositionEdit extends UndoableWorkspaceEdit {

	private static final long serialVersionUID = 4234780460478259894L;
	private CProcess process;
	private CProcess previous;

	private String name;

	public StoreCompositionEdit(String name, Workspace workspace,
			ExceptionHandler handler, boolean visible) {
		super("Store " + name, workspace, handler, visible);
		this.name = name;
	}

	@Override
	protected void doExec() {
		try {
			process = getWorkspace().getCompositeProcess(name);

			if (getSession().processExists(process.getName())) {
				previous = getSession().getProcess(process.getName());
				getSession().updateProcess(previous.getName(), process);
			}
			else {
				getSession().addProcess(process);
			}
		} catch (NotFoundException e) {
			getExceptionHandler().handleException(e);
		}
	}

	@Override
	protected void undoWorkspace() {
		try {
			if (previous != null)
				getSession().updateProcess(process.getName(), previous);
			else
				getSession().removeProcess(process.getName());
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void redoWorkspace() {
		try {
			if (previous != null)
				getSession().updateProcess(previous.getName(), process);
			else
				getSession().addProcess(process);
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doGraph() {
		// TODO manage active workspace that is not active while storing?
//		if (isVisible() && getSession().getActiveWorkspace() != null) {
//			new AddProcessGraphEdit(process, getSession(), getExceptionHandler(), getSession().getActiveWorkspace().getGraph()).apply(false);
//		}
	}

	@Override
	protected void undoGraph() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void redoGraph() {
		// TODO Auto-generated method stub
		
	}
}
