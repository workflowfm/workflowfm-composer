package com.workflowfm.composer.ui;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ProcessStoreChangeListener;
import com.workflowfm.composer.session.CompositionSessionChangeListener;
import com.workflowfm.composer.workspace.Workspace;

public class ShowIntermediateGraphWindow extends ShowCompositionGraphWindow implements ProcessStoreChangeListener, CompositionSessionChangeListener {

	private Workspace workspace;
	
	public ShowIntermediateGraphWindow (CProcess process, Workspace workspace, WindowManager manager) {
		super(process,workspace.getSession(),manager);
		this.workspace = workspace;
	}

	@Override
	public void show() {
		super.show();
		workspace.addChangeListener(this);
		workspace.getSession().addChangeListener((CompositionSessionChangeListener) this);
	}
	
	@Override
	public void dispose() {
		workspace.removeChangeListener(this);
		workspace.getSession().removeChangeListener((CompositionSessionChangeListener) this);
		super.dispose();
	}

	@Override
	public void processAdded(CProcess process) { }

	@Override
	public void processUpdated(String previousName, CProcess process) {
		if (previousName.equals(this.process.getName())) {
			getGraph().getGraphEngine().clear();
			getGraph().insertGraph(process.getFullGraph());
			getGraph().layout();
			this.process = process;
		}	
	}

	@Override
	public void processRemoved(CProcess process) {
		dispose();
	}

	@Override
	public void workspaceRemoved(Workspace workspace) {	
		if (workspace.getName().equals(this.workspace.getName())) {
			dispose();
		}
	}

	@Override
	public void workspaceAdded(Workspace workspace) { }

	@Override
	public void workspaceActivated(Workspace workspace) { }

	@Override
	public void undoRedoUpdate() { }

	@Override
	public void sessionReset() { }

	@Override
	public void sessionSaved() { }
}
