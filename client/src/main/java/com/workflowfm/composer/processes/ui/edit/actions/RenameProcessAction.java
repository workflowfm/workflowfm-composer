package com.workflowfm.composer.processes.ui.edit.actions;

import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.workflowfm.composer.processes.CllValidator;
import com.workflowfm.composer.processes.ui.edit.EditProcessPanel;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.utils.validate.ValidationException;

public class RenameProcessAction extends ProcessEditAction {

	private static final long serialVersionUID = 8321323449475297316L;
	
	public RenameProcessAction(EditProcessPanel dialog) {
		super(dialog,"Rename Process", "silk_icons/page_white_edit.png", KeyEvent.VK_E, KeyEvent.VK_F2, 0);
	}

	@Override
	protected boolean doEdit() {
		String result = getDialog().getProcess().getName();
		do {
			result = (String)JOptionPane.showInputDialog(getDialog().getPanel(),"Enter new process name:","Rename process",JOptionPane.QUESTION_MESSAGE,new ImageIcon(),null,result); 
		} while (result != null && !validateName(result));
		
		if (result != null && !result.equals(getDialog().getProcess().getName())) {
			
			Log.d("Renaming process [" + getDialog().getProcess().getName() + "] to [" + result + "].");
			getDialog().getProcess().setName(result);
			return true;
		}
		return false;
	}
	
	private boolean validateName(String name) {
		try {
			getDialog().getValidator().validate(name,CllValidator.PROCESS);
			return true;
		} catch (ValidationException e2) {
			getDialog().getExceptionHandler().handleException(e2);
			return false;
		}
	}
}
