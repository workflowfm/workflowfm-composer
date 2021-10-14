package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.session.CompositionSessionChangeListener;

import com.workflowfm.composer.workspace.Workspace;

public class UndoAction extends CompositionSessionAction implements CompositionSessionChangeListener {

	private static final long serialVersionUID = 7270103541791749591L;
	
	public UndoAction(CompositionSession session, ExceptionHandler handler) {
		super("Undo", session, handler, "silk_icons/arrow_undo.png", KeyEvent.VK_U, KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
	}

	public void actionPerformed(ActionEvent event)
	{
		if (getSession().getUndoManager().canUndo()) {
			getSession().getUndoManager().undo();
			getSession().updateUndoRedoStatus();
		}
	}

	@Override
	public void workspaceAdded(Workspace workspace) { }

	@Override
	public void workspaceActivated(Workspace workspace) { }

	@Override
	public void workspaceRemoved(Workspace workspace) { }

	@Override
	public void undoRedoUpdate() {
		setDescription(getSession().getUndoManager().getUndoPresentationName());
		setEnabled(getSession().getUndoManager().canUndo());	
	}

	@Override
	public void sessionReset() {
		setDescription("Undo");
		setEnabled(false);		
	}

	@Override
	public void sessionSaved() { }
}
