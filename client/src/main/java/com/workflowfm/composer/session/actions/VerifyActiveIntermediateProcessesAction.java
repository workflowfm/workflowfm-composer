package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.workspace.Workspace;
import com.workflowfm.composer.workspace.actions.VerifyAllIntermediateProcessesAction;

public class VerifyActiveIntermediateProcessesAction extends CompositionSessionAction {

	private static final long serialVersionUID = 1998683812816303908L;

	public VerifyActiveIntermediateProcessesAction(CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Verify All Intermediates", session, exceptionHandler, "silk_icons/shield_cog.png", KeyEvent.VK_B, KeyEvent.VK_B, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Workspace workspace = getSession().getActiveWorkspace(); 
		if (workspace == null) return;
		
		new VerifyAllIntermediateProcessesAction(workspace, getExceptionHandler()).actionPerformed(e);
	}

}	