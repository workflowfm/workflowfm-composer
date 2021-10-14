package com.workflowfm.composer.processes.ui.edit.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ui.edit.EditProcessPanel;
import com.workflowfm.composer.processes.ui.edit.UndoableProcessEdit;

import com.workflowfm.composer.ui.UIAction;

public abstract class ProcessEditAction extends UIAction { 

	/**
	 * 
	 */
	private static final long serialVersionUID = 6988720727645531563L;
	private EditProcessPanel panel;
	
	public ProcessEditAction(EditProcessPanel panel, String actionName, String iconFilename,
			int mnemonicKey, int acceleratorKey, int acceleratorKeyModifier) {
		super(actionName, iconFilename, mnemonicKey, acceleratorKey,
				acceleratorKeyModifier);
		this.panel = panel;
	}

	public ProcessEditAction(EditProcessPanel panel, String actionName, String iconFilename,
			int mnemonicKey) {
		super(actionName, iconFilename, mnemonicKey);
		this.panel = panel;
	}

	public ProcessEditAction(EditProcessPanel panel, String actionName, URL iconURL,
			int mnemonicKey, int acceleratorKey, int acceleratorKeyModifier) {
		super(actionName, iconURL, mnemonicKey, acceleratorKey, acceleratorKeyModifier);
		this.panel = panel;
	}

	public ProcessEditAction(EditProcessPanel panel, String actionName, URL iconURL,
			int mnemonicKey) {
		super(actionName, iconURL, mnemonicKey);
		this.panel = panel;
	}
	
	public EditProcessPanel getDialog() { return this.panel; }
	
	protected abstract boolean doEdit();
	
	public void actionPerformed(ActionEvent e) {
		CProcess old = new CProcess(panel.getProcess()); // TODO This means the process is copied every single time the action is invoked, even if it fails!
		if (doEdit() == true) {
			panel.refresh();
			panel.addToUndoManager(new UndoableProcessEdit(getDescription(), panel, old));
			
		}
	}
	
	
}

