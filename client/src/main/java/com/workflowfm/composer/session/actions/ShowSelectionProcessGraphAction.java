package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.ui.GraphWindow;
import com.workflowfm.composer.ui.ShowIntermediateGraphWindow;
import com.workflowfm.composer.ui.WindowManager;
import com.workflowfm.composer.ui.WorkspaceUI;

public class ShowSelectionProcessGraphAction extends GraphSelectionAction<WorkspaceUI> {

	private static final long serialVersionUID = 3644420043336600231L;
	
	public ShowSelectionProcessGraphAction(WindowManager manager, CompositionSession session, ExceptionHandler exceptionHandler) {
		// TODO icon used to be magnifier.png ?
		super("Show Graph", manager, session, exceptionHandler, "silk_icons/chart_organisation.png", KeyEvent.VK_G, KeyEvent.VK_G, KeyEvent.ALT_DOWN_MASK);
	}

	@Override
	public void actionPerformed(ActionEvent e, CProcess process, String bundle) {
		new ShowIntermediateGraphWindow(process, window.getWorkspace(), getManager()).show();
	}
	
	@Override
	protected boolean isValidWindow(GraphWindow window) {
		return window instanceof WorkspaceUI;
	}

	@Override
	protected boolean isValidSelection(CProcess process) {
		return (process != null);
	}

	@Override
	protected CProcess getSelectionByName(String name) throws NotFoundException {
		return window.getWorkspace().getProcess(name);
	}
}