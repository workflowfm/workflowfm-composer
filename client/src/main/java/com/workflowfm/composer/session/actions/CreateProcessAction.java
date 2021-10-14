package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.ui.EditProcessWindow;
import com.workflowfm.composer.ui.WindowManager;

public class CreateProcessAction extends CompositionSessionAction {

	private static final long serialVersionUID = -2111947361115569347L;	

	private WindowManager manager;

	public CreateProcessAction(WindowManager manager, CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Create Process", session, exceptionHandler, "silk_icons/brick_add.png", KeyEvent.VK_A, KeyEvent.VK_A, ActionEvent.ALT_MASK);
		this.manager = manager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		EditProcessWindow window = new EditProcessWindow(getSession(), manager, getExceptionHandler(), getSession().getFreshProcessName());
		window.show();	
	}	

}