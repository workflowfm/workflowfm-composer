package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.edit.CreateWorkspaceEdit;

public class AddWorkspaceAction extends CompositionSessionAction {

	private static final long serialVersionUID = 1652740495456332988L;

	public AddWorkspaceAction(CompositionSession session, ExceptionHandler handler) {
		super("Create Workspace", session, handler, "silk_icons/application_add.png", KeyEvent.VK_T, KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new CreateWorkspaceEdit(getSession(),getExceptionHandler()).apply();
	}

}

