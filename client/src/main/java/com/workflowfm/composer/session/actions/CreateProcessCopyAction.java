package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.EditProcessWindow;
import com.workflowfm.composer.ui.WindowManager;

public class CreateProcessCopyAction extends CompositionSessionAction {

	private static final long serialVersionUID = 8056464674958090165L;
	private WindowManager manager;
	private String processName;

	public CreateProcessCopyAction(String processName, WindowManager manager, CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Copy Process", session, exceptionHandler, "silk_icons/brick_copy.png", KeyEvent.VK_C, KeyEvent.VK_C, ActionEvent.ALT_MASK);
		this.manager = manager;
		this.processName = processName;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		EditProcessWindow window = new EditProcessWindow(getSession(), manager, getExceptionHandler(), processName, true);
		window.show();	
	}	

}