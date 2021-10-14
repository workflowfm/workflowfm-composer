package com.workflowfm.composer.workspace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.utils.CompletionListener;
import com.workflowfm.composer.workspace.Workspace;

public class VerifyIntermediateProcessAction extends WorkspaceAction implements CompletionListener {

	private static final long serialVersionUID = 6998458176173554316L;
	private CProcess process;
	
	private WorkspaceComposeAction action;
	private boolean succeeded = false;
	
	public VerifyIntermediateProcessAction(CProcess process, Workspace workspace, ExceptionHandler exceptionHandler) {
		super("Verify Composition", workspace, exceptionHandler, "silk_icons/shield.png", KeyEvent.VK_V, KeyEvent.VK_V, ActionEvent.ALT_MASK);
		this.process = process;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (process.isChecked() && process.isValid()) {
			notifyCompletion();
			return;
		}
		if (!process.isIntermediate()) {
			notifyCompletion();
			return;
		}
		try {
			action = new WorkspaceComposeAction(getWorkspace(), getExceptionHandler(), process.getActions().lastElement(), false);
			action.addCompletionListener(this);
			action.actionPerformed(e);
		// TODO lose the assumption that an intermediate composition only has 1 composition action (although it's true for now)
		} catch (Exception ex) {
			process.setInvalid();
			succeeded = false;
			notifyCompletion();
		}
	}

	@Override
	public void completed() {
		succeeded = action.succeeded();
		notifyCompletion();
	}

	public boolean succeeded() {
		return this.succeeded;
	}
}
