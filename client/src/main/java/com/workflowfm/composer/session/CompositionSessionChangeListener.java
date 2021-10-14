package com.workflowfm.composer.session;

import com.workflowfm.composer.workspace.Workspace;

/** Observer that is used to react to changes in the composition session. */
public interface CompositionSessionChangeListener {

	public void workspaceAdded(Workspace workspace);
	public void workspaceActivated(Workspace workspace);
	public void workspaceRemoved(Workspace workspace);

	public void undoRedoUpdate();
	
	public void sessionReset();
	public void sessionSaved();
}
