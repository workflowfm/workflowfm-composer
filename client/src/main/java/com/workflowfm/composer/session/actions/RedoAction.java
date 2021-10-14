package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.session.CompositionSessionChangeListener;

import com.workflowfm.composer.workspace.Workspace;

public class RedoAction extends CompositionSessionAction implements CompositionSessionChangeListener {

	private static final long serialVersionUID = 1280094356732666131L;
	
	public RedoAction(CompositionSession session, ExceptionHandler handler) {
		super("Redo", session, handler, "silk_icons/arrow_redo.png", KeyEvent.VK_R, KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);
	}

	public void actionPerformed(ActionEvent event)
	{
		if (getSession().getUndoManager().canRedo()) {
			getSession().getUndoManager().redo();
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
		setDescription(getSession().getUndoManager().getRedoPresentationName());
		setEnabled(getSession().getUndoManager().canRedo());	
	}

	@Override
	public void sessionReset() {
		setDescription("Redo");
		setEnabled(false);		
	}

	@Override
	public void sessionSaved() { }
}
