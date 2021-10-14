package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.ui.ShowCompositionGraphWindow;
import com.workflowfm.composer.ui.WindowManager;

public class ShowProcessGraphAction extends CompositionSessionAction {

	private static final long serialVersionUID = 3644420043336600231L;
	private CProcess process;
	private WindowManager manager;
	
	public ShowProcessGraphAction(CProcess process, WindowManager manager, CompositionSession session, ExceptionHandler exceptionHandler) {
		// TODO icon used to be magnifier.png ?
		super("Show Graph", session, exceptionHandler, "silk_icons/chart_organisation.png", KeyEvent.VK_G, KeyEvent.VK_G, ActionEvent.ALT_MASK);
		this.process = process;
		this.manager = manager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new ShowCompositionGraphWindow(process, getSession(), manager).show();
	}
}