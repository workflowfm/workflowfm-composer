package com.workflowfm.composer.edit;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.workspace.Workspace;

public class RemoveWorkspaceEdit extends UndoableSessionEdit {

	private static final long serialVersionUID = -6225363043088069225L;
	private Workspace workspace;
	
	public RemoveWorkspaceEdit(Workspace workspace, CompositionSession session, ExceptionHandler handler) {
		super("Remove " + workspace.getName(),session,handler);
		this.workspace = workspace;
	}

	@Override
	protected void doSession() {
		getSession().removeWorkspace(workspace);
	}

	@Override
	protected void undoSession() {
		getSession().addWorkspace(workspace);
	}

	@Override
	protected void redoSession() {
		getSession().removeWorkspace(workspace);
	}
}
