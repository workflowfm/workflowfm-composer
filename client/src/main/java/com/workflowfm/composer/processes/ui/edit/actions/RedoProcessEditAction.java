package com.workflowfm.composer.processes.ui.edit.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.processes.ui.edit.EditProcessPanel;

import com.workflowfm.composer.ui.UIAction;

public class RedoProcessEditAction extends UIAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5250456567306514734L;
	private EditProcessPanel dialog;
	
	public RedoProcessEditAction(EditProcessPanel dialog) {
		super("Redo", "silk_icons/arrow_redo.png", KeyEvent.VK_R, KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);
		this.dialog = dialog;
	}

	public void actionPerformed(ActionEvent event)
	{
		dialog.getUndoManager().redo();
		dialog.updateUndoRedoButtonStatus();
	}
}
