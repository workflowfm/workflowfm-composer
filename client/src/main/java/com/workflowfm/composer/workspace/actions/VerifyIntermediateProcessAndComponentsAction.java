package com.workflowfm.composer.workspace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.workspace.Workspace;

public class VerifyIntermediateProcessAndComponentsAction extends VerifyMultipleIntermediateProcessesAction {

	private static final long serialVersionUID = -5588756848423914370L;

	public VerifyIntermediateProcessAndComponentsAction(CProcess process, Workspace workspace, ExceptionHandler exceptionHandler) {
		super("Verify Process&Parents", workspace, exceptionHandler, "silk_icons/shield_go.png", KeyEvent.VK_B, KeyEvent.VK_B, ActionEvent.ALT_MASK);
		try {
			setProcesses(workspace.getProcessWithOrderedDependencies(process));
		} catch (NotFoundException e) {
			getExceptionHandler().handleException(e);
			return;
		}
	}

}	
