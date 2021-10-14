package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;

public class VerifyAllProcessesAction extends VerifyMultipleProcessesAction {

	private static final long serialVersionUID = 2791724865388950719L;

	public VerifyAllProcessesAction(CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Verify All", session, exceptionHandler, "silk_icons/shield_bricks.png", KeyEvent.VK_B, KeyEvent.VK_B, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Vector<CProcess> processes = new Vector<CProcess>();

		for (CProcess p : getSession().getProcesses()) {
			try {
				processes.addAll(getSession().getProcessWithOrderedDependencies(p));
			} catch (NotFoundException e1) { 
				getExceptionHandler().handleException(e1);
				return;
			}
		}
		setProcesses(processes);
		super.actionPerformed(e);
	}

}	