package com.workflowfm.composer.workspace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.edit.graph.AddProcessGraphEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.workspace.Workspace;

public class AddProcessGraphAction extends WorkspaceAction {

	private static final long serialVersionUID = -1911386611017428078L;
	private CProcess process;
	
	public AddProcessGraphAction(Workspace workspace, ExceptionHandler exceptionHandler, CProcess process) {
		super("Add Graph", workspace, exceptionHandler, "silk_icons/add.png", KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
		this.process = process;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new AddProcessGraphEdit(process, getWorkspace().getSession(), getExceptionHandler(), getWorkspace().getGraph()).apply();
	}

}
