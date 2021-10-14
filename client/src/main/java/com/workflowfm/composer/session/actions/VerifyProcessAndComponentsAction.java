package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;

public class VerifyProcessAndComponentsAction extends VerifyMultipleProcessesAction {

	private static final long serialVersionUID = 2791724865388950719L;
	
	public VerifyProcessAndComponentsAction(CProcess process, CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Verify Process&Parents", session, exceptionHandler, "silk_icons/shield_go.png", KeyEvent.VK_B, KeyEvent.VK_B, ActionEvent.ALT_MASK);
		try {
			setProcesses(session.getProcessWithOrderedDependencies(process));
		} catch (NotFoundException e) {
			getExceptionHandler().handleException(e);
			return;
		}
	}

}	
