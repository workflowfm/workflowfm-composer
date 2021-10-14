package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.edit.DeleteProcessEdit;
import com.workflowfm.composer.edit.group.RemoveAllGraphsOfProcessEdit;
import com.workflowfm.composer.edit.group.UndoableSessionEditGroup;

public class DeleteProcessAction extends CompositionSessionAction {

	private static final long serialVersionUID = 7959385832927889362L;
	private CProcess process;

	public DeleteProcessAction(CProcess process, CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Delete Process", session, exceptionHandler, "silk_icons/delete.png", KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.ALT_MASK);
		this.process = process;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		UndoableSessionEditGroup edit = new RemoveAllGraphsOfProcessEdit(process, getSession(), getExceptionHandler());
		edit.append(new DeleteProcessEdit(process, getSession(), getSession(), getExceptionHandler()));
		edit.apply();
	}	

}