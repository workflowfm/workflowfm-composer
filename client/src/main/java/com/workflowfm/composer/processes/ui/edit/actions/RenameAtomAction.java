package com.workflowfm.composer.processes.ui.edit.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllValidator;
import com.workflowfm.composer.processes.ui.PortEdge;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.processes.ui.edit.EditProcessPanel;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.utils.validate.ValidationException;

public class RenameAtomAction extends ProcessEditAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -811767813324836101L;

	public RenameAtomAction(EditProcessPanel dialog) {
		super(dialog,"Rename Atom", "silk_icons/page_white_edit.png", KeyEvent.VK_R, KeyEvent.VK_R, ActionEvent.CTRL_MASK);
	}

	@Override
	protected boolean doEdit() {
		Object selectedCell = getDialog().getSelectedCell();
		if (selectedCell == null) return false;

		ProcessGraph graph = getDialog().getGraph();
		Object val = graph.getValue(selectedCell);

		if (!(val instanceof PortEdge)) return false;

		PortEdge edge = (PortEdge)val;
		CllTerm term = edge.getTerm();

		if (!term.isAtomic()) {
			Log.d("ERROR: Tried to rename non-atomic CLL term: [" + getDialog().getProver().cllResourceString(term) + "]");
			return false;
		}
		
		String result = term.getName();
		do { 
			result = (String)JOptionPane.showInputDialog(getDialog().getPanel(),"Enter new resource name:","Rename resource",JOptionPane.QUESTION_MESSAGE,new ImageIcon(),null,result); 
		} while (result != null && !validateName(result));
		
		if (result != null && !result.equals(term.getName())) {
			Log.d("Renaming atom [" + term.getName() + "] of process [" + getDialog().getProcess().getName() + "] to [" + result + "].");
			term.setName(result);
			return true;
		}
		return false;
	}

	private boolean validateName(String name) {
		try {
			getDialog().getValidator().validate(name,CllValidator.RESOURCE);
			return true;
		} catch (ValidationException e2) {
			getDialog().getExceptionHandler().handleException(e2);
			return false;
		}
	}
}
