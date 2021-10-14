package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ui.ProcessGraphSelectionListener;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.ui.GraphWindow;
import com.workflowfm.composer.ui.StoreProcessWindow;
import com.workflowfm.composer.ui.WindowManager;
import com.workflowfm.composer.ui.WindowManagerChangeListener;
import com.workflowfm.composer.ui.WorkspaceUI;

public class StoreSelectionProcessAction extends GraphSelectionAction<WorkspaceUI> implements ProcessGraphSelectionListener, WindowManagerChangeListener {

	private static final long serialVersionUID = 1739838196919169662L;

	public StoreSelectionProcessAction(WindowManager manager, CompositionSession session, ExceptionHandler handler) {
		super("Store Composition", manager, session, handler, "silk_icons/bricks.png", KeyEvent.VK_N, KeyEvent.VK_N, ActionEvent.ALT_MASK);
	}

	@Override
	protected boolean isValidWindow(GraphWindow window) {
		return (window instanceof WorkspaceUI);
	}

	@Override
	protected boolean isValidSelection(CProcess selection) {
		return selection != null; //selection.isIntermediate(); TODO to limit or not to limit?
	}

	@Override
	protected CProcess getSelectionByName(String name) throws NotFoundException {
		return window.getWorkspace().getProcess(name); // If isValidSelection is limited to intermediates, this can also become getComposition
	}

	@Override
	protected void actionPerformed(ActionEvent e, CProcess process, String bundle) {
		try {
			new StoreProcessWindow(window.getWorkspace(), getExceptionHandler(), getManager(), window.getWorkspace().getProcess(bundle)).show();
		} catch (NotFoundException e1) {
			getExceptionHandler().handleException(e1);
			return;
		}
	}

}