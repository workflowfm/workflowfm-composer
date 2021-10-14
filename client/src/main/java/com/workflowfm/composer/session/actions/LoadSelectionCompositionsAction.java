package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.ui.GraphWindow;
import com.workflowfm.composer.ui.WindowManager;

public class LoadSelectionCompositionsAction extends GraphSelectionAction<GraphWindow> {
	
	private static final long serialVersionUID = -4790233233153267434L;

	public LoadSelectionCompositionsAction(WindowManager manager, CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Load Compositions", manager, session, exceptionHandler, "silk_icons/table_go.png", KeyEvent.VK_L);
	}
	
	@Override
	public void actionPerformed(ActionEvent e, CProcess process, String bundle) {
		new LoadCompositionsAction(getSession(), getExceptionHandler(), process).actionPerformed(e);;
	}

	@Override
	protected boolean isValidWindow(GraphWindow window) {
		return true;
	}

	@Override
	protected boolean isValidSelection(CProcess process) {
		return (process != null && process.isComposite() && !process.isIntermediate());
	}

	@Override
	protected CProcess getSelectionByName(String name) throws NotFoundException {
		return getSession().getProcess(name);
	}
	
}