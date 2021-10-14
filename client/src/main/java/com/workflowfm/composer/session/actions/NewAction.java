package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;

public class NewAction extends CompositionSessionAction {

	private static final long serialVersionUID = 6853464392059662035L;

	public NewAction(CompositionSession session, ExceptionHandler handler) {
		super("New", session, handler, "silk_icons/page.png", KeyEvent.VK_N, KeyEvent.VK_N,  KeyEvent.CTRL_DOWN_MASK);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Ask to save first?
		getSession().reset();
	}

}
