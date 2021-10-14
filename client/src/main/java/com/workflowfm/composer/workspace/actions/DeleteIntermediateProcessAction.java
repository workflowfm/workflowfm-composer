package com.workflowfm.composer.workspace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.edit.DeleteProcessEdit;
import com.workflowfm.composer.edit.graph.RemoveProcessGraphEdit;
import com.workflowfm.composer.edit.group.UndoableSessionEditGroup;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.workspace.Workspace;

public class DeleteIntermediateProcessAction extends WorkspaceAction {

	private static final long serialVersionUID = -943522672188004621L;
	private CProcess process;

	public DeleteIntermediateProcessAction(CProcess process, Workspace workspace, ExceptionHandler exceptionHandler) {
		super("Delete Composition", workspace, exceptionHandler, "silk_icons/delete.png", KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.ALT_MASK);
		this.process = process;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		UndoableSessionEditGroup edit = new UndoableSessionEditGroup(new RemoveProcessGraphEdit(process, getWorkspace().getSession(), getExceptionHandler(), getWorkspace().getGraph()));
		edit.append(new DeleteProcessEdit(process, getWorkspace(), getWorkspace().getSession(), getExceptionHandler()));
		edit.apply();
	}	

}
