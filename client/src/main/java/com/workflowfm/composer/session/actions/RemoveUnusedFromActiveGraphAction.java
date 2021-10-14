package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.edit.graph.RemoveUnusedGraphEdit;
import com.workflowfm.composer.ui.GraphWindow;
import com.workflowfm.composer.ui.WindowManager;

public class RemoveUnusedFromActiveGraphAction extends CompositionSessionAction {

	private static final long serialVersionUID = -1459483366842727126L;
	
	private WindowManager manager;
	
	public RemoveUnusedFromActiveGraphAction(WindowManager manager, CompositionSession session, ExceptionHandler handler) {
		super("Clear Unused Processes", session, handler, "silk_icons/page_white_swoosh.png", KeyEvent.VK_C, KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
		this.manager = manager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (manager.getActiveWindow() instanceof GraphWindow)
			new RemoveUnusedGraphEdit(getSession(),getExceptionHandler(),((GraphWindow)manager.getActiveWindow()).getGraph()).apply(); 
	}

}