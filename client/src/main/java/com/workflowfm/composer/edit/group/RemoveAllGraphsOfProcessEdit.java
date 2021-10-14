package com.workflowfm.composer.edit.group;

import com.workflowfm.composer.edit.graph.RemoveProcessGraphEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.workspace.Workspace;

public class RemoveAllGraphsOfProcessEdit extends UndoableSessionEditGroup {

	private static final long serialVersionUID = -646256275419397188L;
	private String name;	
	
	public RemoveAllGraphsOfProcessEdit(CProcess process, CompositionSession session, ExceptionHandler handler) {
		super("Remove graphs of " + process.getName(), session, handler);
		this.name = process.getName();
		setEdits();
	}
	
	public RemoveAllGraphsOfProcessEdit(String name, CompositionSession session, ExceptionHandler handler) {
		super("Remove graphs of " + name,session,handler);
		this.name = name;
		setEdits();
	}

	private void setEdits() {
		for (Workspace workspace : getSession().getWorkspaces()) {
			if (workspace.getGraph().bundleExists(name))
				edits.add(new RemoveProcessGraphEdit(name, getSession(), getExceptionHandler(), workspace.getGraph()));
		}
	}
	
	public String getName() {
		return this.name;
	}
}
