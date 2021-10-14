package com.workflowfm.composer.edit;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.workspace.Workspace;

public class CreateWorkspaceEdit extends UndoableSessionEdit {

	private static final long serialVersionUID = -9133423284591166499L;
	private Workspace workspace;
	
	public CreateWorkspaceEdit(CompositionSession session, ExceptionHandler handler) {
		super("Create Workspace",session,handler);
	}

	@Override
	protected void doSession() {
		this.workspace = getSession().createWorkspace();
		setDescription("Create " + workspace.getName());
	}

	@Override
	protected void undoSession() {
		getSession().removeWorkspace(workspace);
	}

	@Override
	protected void redoSession() {
		getSession().addWorkspace(workspace);
	}
	
	public Workspace getWorkspace() {
		return this.workspace;
	}
}
