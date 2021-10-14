package com.workflowfm.composer.workspace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.edit.graph.RemoveBundleGraphEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.workspace.Workspace;

public class RemoveBundleGraphAction extends WorkspaceAction {

	private static final long serialVersionUID = 6414010328074158184L;
	
	public RemoveBundleGraphAction(Workspace workspace, ExceptionHandler exceptionHandler) {
		super("Remove Graph", workspace, exceptionHandler, "silk_icons/chart_organisation_delete.png", KeyEvent.VK_DELETE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object member = getWorkspace().getGraph().getSelectedCell();
		if (member != null)
			new RemoveBundleGraphEdit(getWorkspace().getSession(), getExceptionHandler(), getWorkspace().getGraph(), member).apply();
	}

}
