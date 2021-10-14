package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.ui.EditProcessWindow;
import com.workflowfm.composer.ui.WindowManager;

public class EditProcessAction extends CompositionSessionAction {

	private static final long serialVersionUID = -2793766366795867847L;
	private WindowManager manager;
	private String processName;

	public EditProcessAction(String processName, WindowManager manager, CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Edit Process", session, exceptionHandler, "silk_icons/brick_edit.png", KeyEvent.VK_E, KeyEvent.VK_E, ActionEvent.ALT_MASK);
		this.manager = manager;
		this.processName = processName;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		EditProcessWindow window = new EditProcessWindow(getSession(), manager, getExceptionHandler(), processName);
		window.show();	
	}	

}
