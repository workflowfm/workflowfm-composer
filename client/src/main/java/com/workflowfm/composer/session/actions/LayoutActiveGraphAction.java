package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.ui.GraphWindow;
import com.workflowfm.composer.ui.WindowManager;

public class LayoutActiveGraphAction extends CompositionSessionAction {

	private static final long serialVersionUID = -6947924167309943677L;

	private WindowManager manager;
	
	public LayoutActiveGraphAction(WindowManager manager, CompositionSession session, ExceptionHandler handler) {
		super("Layout Graph", session, handler, "silk_icons/arrow_out.png", KeyEvent.VK_L, KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK);
		this.manager = manager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (manager.getActiveWindow() instanceof GraphWindow)
			((GraphWindow)manager.getActiveWindow()).getGraph().layout();
	}

}
