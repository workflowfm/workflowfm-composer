package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.PiLibDeployWindow;
import com.workflowfm.composer.ui.WindowManager;

public class PiLibAction extends CompositionSessionAction {

	private static final long serialVersionUID = -4624062231394682174L;

	private String processName;
	private WindowManager manager;

	public PiLibAction(String processName, WindowManager manager, CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Deploy in Scala", session, exceptionHandler, "silk_icons/application_cascade.png", KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.ALT_DOWN_MASK);
		this.processName = processName;
		this.manager = manager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		PiLibDeployWindow window = new PiLibDeployWindow(processName, getSession(), getExceptionHandler(), manager);
		window.show();	
	}
}