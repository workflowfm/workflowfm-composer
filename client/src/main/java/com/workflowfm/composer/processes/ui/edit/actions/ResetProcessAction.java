package com.workflowfm.composer.processes.ui.edit.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.processes.ui.edit.EditProcessPanel;

public class ResetProcessAction extends ProcessEditAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7560025932150410836L;

	public ResetProcessAction(EditProcessPanel dialog) {
		super(dialog,"Reset Process", "silk_icons/arrow_refresh.png", KeyEvent.VK_R, KeyEvent.VK_R, ActionEvent.CTRL_MASK);
	}

	@Override
	protected boolean doEdit() {
		// TODO CProcess equality check!! 
		//if (!getDialog().getProcess().equals(getDialog().getOriginalProcess())) {
		//	Log.d("Resetting process [" + getDialog().getProcess().getName() + "] to original.");
		//	getDialog().setProcess(new CProcess(getDialog().getOriginalProcess()));
		//	return true;
		//}
		return false;
	}
}
