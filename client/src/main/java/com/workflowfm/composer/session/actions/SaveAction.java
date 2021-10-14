package com.workflowfm.composer.session.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.session.ComposerSaveState;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.session.CompositionSessionChangeListener;
import com.workflowfm.composer.workspace.Workspace;

public class SaveAction extends CompositionSessionAction implements CompositionSessionChangeListener {

	private static final long serialVersionUID = 4244978433957488184L;
	private Component parent;

	public SaveAction(Component parent, CompositionSession session, ExceptionHandler handler) {
		super("Save", session, handler, "silk_icons/disk.png", KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		File file = getSession().getSaveFile();
		if (file == null) {
			new SaveAsAction(parent, getSession(), getExceptionHandler()).actionPerformed(e);
			return;
		}
		try
		{
			if (file != null && file.getParentFile() != null && file.getParentFile().exists() && file.getParentFile().isDirectory())
				ComposerProperties.set("proofScriptDirectory", file.getParent());

			new ComposerSaveState(getSession()).saveToFile(file);
			getSession().setSaveFile(file);
			getSession().notifySessionSaved();
		}
		catch (Exception e1)
		{
			getExceptionHandler().handleException("Unable to save project.", e1);
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
		this.setEnabled(true);
	}

	@Override
	public void sessionReset() {
		this.setEnabled(false);
	}

	@Override
	public void sessionSaved() {
		this.setEnabled(false);
	}
}