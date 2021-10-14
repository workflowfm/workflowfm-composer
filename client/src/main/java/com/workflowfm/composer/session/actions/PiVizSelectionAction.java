package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.ui.GraphWindow;
import com.workflowfm.composer.ui.WindowManager;
import com.workflowfm.composer.ui.WorkspaceUI;

public class PiVizSelectionAction extends GraphSelectionAction<WorkspaceUI> {

	private static final long serialVersionUID = 1879283953663037261L;

	public PiVizSelectionAction(WindowManager manager, CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Inspect \u03C0-calculus", manager, session, exceptionHandler, "silk_icons/report_picture.png", KeyEvent.VK_I, KeyEvent.VK_I, KeyEvent.ALT_DOWN_MASK);
	}

	@Override
	public void actionPerformed(ActionEvent e, CProcess process, String bundle) {
		new PiVizAction(process, getManager(), getSession(), getExceptionHandler()).actionPerformed(e);
	}
	
	@Override
	protected boolean isValidWindow(GraphWindow window) {
		return window instanceof WorkspaceUI;
	}

	@Override
	protected boolean isValidSelection(CProcess process) {
		return (process != null && process.isChecked() && process.isValid());
	}

	@Override
	protected CProcess getSelectionByName(String name) throws NotFoundException {
		return window.getWorkspace().getProcess(name);
	}
}