package com.workflowfm.composer.processes.ui.edit.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.processes.ui.edit.EditProcessPanel;

import com.workflowfm.composer.ui.UIAction;

public class UndoProcessEditAction extends UIAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 615335397482716727L;
	private EditProcessPanel dialog;
	
	public UndoProcessEditAction(EditProcessPanel dialog) {
		super("Undo", "silk_icons/arrow_undo.png", KeyEvent.VK_U, KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
		this.dialog = dialog;
	}

	public void actionPerformed(ActionEvent event)
	{
		dialog.getUndoManager().undo();
		dialog.updateUndoRedoButtonStatus();
	}
}
