package com.workflowfm.composer.processes.ui.edit.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllValidator;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.ui.PortVertex;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.processes.ui.edit.EditProcessPanel;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.utils.validate.ValidationException;

public class ExpandParentBranchAction extends ProcessEditAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3660005338069874566L;

	public ExpandParentBranchAction(EditProcessPanel dialog) {
		super(dialog, "Expand branch", "silk_icons/arrow_divide.png", KeyEvent.VK_E, KeyEvent.VK_E, ActionEvent.CTRL_MASK);
	}

	@Override
	protected boolean doEdit() {
		Object selectedCell = getDialog().getSelectedCell();
		if (selectedCell == null) return false;
		
		ProcessGraph graph = getDialog().getGraph();
		Object val = graph.getValue(selectedCell);
		if (!(val instanceof PortVertex)) return false;
		
		PortVertex port = (PortVertex)val;
		
		CProcess process = getDialog().getProcess();
		CllTerm tm = getNewTerm();
		
		if (tm == null) return false;
			
		if (port.getTermPath().isRoot()) {
			if (port.isInput()) {
				// Root input = one of the inputs (normally atomic or optional)
				Log.d("Expand branch of root input.");
				process.addInput(new ProcessPort(getDialog().getFreshChannel(), tm));
			} else {
				// Root output = single atomic output
				Log.d("Expand branch of root output.");
				CllTerm result = port.getTerm().times(tm);
				ProcessPort newPort = new ProcessPort(getDialog().getFreshChannel(), result);
				process.setOutput(newPort);
			}
		} else {
			Log.d("Expand branch. Target: [" + getDialog().getProver().cllResourceString(port.getTerm()) + 
					"] Path: [" + port.getTermPath() +
					"] Root: [" + getDialog().getProver().cllResourceString(port.getRootTerm()) + "].");

			try {
				CllTerm parent = port.getTermPath().followParent(port.getRootTerm());
				parent.expand(tm); 
			} catch (InvalidCllPathException | NotFoundException e) {
				throw new RuntimeException("Failed to find parent to expand", e);
			}
			
		}		
		return true;
	} 
	
	private CllTerm getNewTerm() {
		String result = "X";
		do {
			result = (String)JOptionPane.showInputDialog(getDialog().getPanel(),"Enter new resource name:","Expand branch",JOptionPane.QUESTION_MESSAGE,new ImageIcon(),null,result); 
		} while (result != null && !validateName(result));
		
		if (result != null) {
			return new CllTerm(result);
		}
		else return null;
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
