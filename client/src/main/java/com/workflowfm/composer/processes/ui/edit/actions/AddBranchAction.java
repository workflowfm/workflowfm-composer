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

public class AddBranchAction extends ProcessEditAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7156652171945986525L;

	public AddBranchAction(EditProcessPanel dialog) {
		super(dialog, "Add branch", "silk_icons/arrow_divide.png", KeyEvent.VK_B, KeyEvent.VK_B, ActionEvent.CTRL_MASK);
	}

	@Override
	protected boolean doEdit() {
		Object selectedCell = getDialog().getSelectedCell();
		if (selectedCell == null) return false;
		
		ProcessGraph graph = getDialog().getGraph();
		if (!graph.isPortVertex(selectedCell)) return false;
		
		Object val = graph.getValue(selectedCell);
		PortVertex port = (PortVertex)val;
		boolean optional = false;
		if (!port.isOptional() || (port.isInput() && port.getTermPath().isRoot())) optional = true;
		
		CProcess process = getDialog().getProcess();
		CllTerm tm = getNewTerm(port, optional);

		if (tm == null) return false;

		if (port.getTermPath().isRoot()) {
			if (port.isInput()) {
				// Root input = one of the inputs (normally atomic or optional)
				Log.d("Add branch to root input.");
				ProcessPort newPort = new ProcessPort(getDialog().getFreshChannel(), tm);
				try {
					process.setInput(port.getChannel(),newPort);
				} catch (NotFoundException e) {
					throw new RuntimeException("Failed to find selected input", e);
				}
			} else {
				// Root output = single atomic output
				Log.d("Add branch to root output.");
				ProcessPort newPort = new ProcessPort(getDialog().getFreshChannel(), tm);
				process.setOutput(newPort);
			}
		} else {
			Log.d("Add branch to branch. Target: [" + getDialog().getProver().cllResourceString(port.getTerm()) + 
					"] Path: [" + port.getTermPath() +
					"] Root: [" + getDialog().getProver().cllResourceString(port.getRootTerm()) + "].");

			try {
				port.getRootTerm().setAt(port.getTermPath(), tm);
			} catch (InvalidCllPathException | NotFoundException e) {
				throw new RuntimeException("Failed to set root term", e);
			} 
		}		
		return true;
	} 

	private CllTerm getNewTerm(PortVertex port, boolean optional) {
		CllTerm term;
		String result = "X";
		do {
			result = (String)JOptionPane.showInputDialog(getDialog().getPanel(),"Enter new resource name:","Add branch",JOptionPane.QUESTION_MESSAGE,new ImageIcon(),null,result); 
		} while (result != null && !validateName(result));
		
		if (result != null) {
			term = new CllTerm(result);
		}
		else return null;
		
		if (optional)
			return port.getTerm().plus(term);
		else
			return port.getTerm().times(term);
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
