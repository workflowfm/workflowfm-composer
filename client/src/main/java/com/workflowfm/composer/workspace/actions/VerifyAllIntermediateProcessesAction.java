package com.workflowfm.composer.workspace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.workspace.Workspace;

public class VerifyAllIntermediateProcessesAction extends VerifyMultipleIntermediateProcessesAction {

	private static final long serialVersionUID = 1998683812816303908L;

	// TODO build another icon?
	public VerifyAllIntermediateProcessesAction(Workspace workspace, ExceptionHandler exceptionHandler) {
		super("Verify All Intermediates", workspace, exceptionHandler, "silk_icons/shield_cog.png", KeyEvent.VK_B, KeyEvent.VK_B, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Vector<CProcess> processes = new Vector<CProcess>();

		for (CProcess p : getWorkspace().getCompositions()) {
			try {
				processes.addAll(getWorkspace().getProcessWithOrderedDependencies(p));
			} catch (NotFoundException e1) {
				getExceptionHandler().handleException(e1);
				return;
			}
		}
		setProcesses(processes);
		super.actionPerformed(e);
	}

}	
