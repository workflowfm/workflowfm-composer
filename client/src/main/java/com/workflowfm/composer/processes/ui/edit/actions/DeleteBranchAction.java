package com.workflowfm.composer.processes.ui.edit.actions;

import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ui.PortVertex;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.processes.ui.edit.EditProcessPanel;
import com.workflowfm.composer.utils.Log;

public class DeleteBranchAction extends ProcessEditAction {

	private static final long serialVersionUID = -389416604850988010L;

	public DeleteBranchAction(EditProcessPanel dialog) {
		super(dialog, "Delete branch", "silk_icons/delete.png", KeyEvent.VK_DELETE);
	}

	@Override
	protected boolean doEdit() {
		Object selectedCell = getDialog().getSelectedCell();
		if (selectedCell == null) return false;

		ProcessGraph graph = getDialog().getGraph();
		if (!graph.isPortVertex(selectedCell)) return false;

		Object val = graph.getValue(selectedCell);
		PortVertex port = (PortVertex)val;
		CProcess process = getDialog().getProcess();

		if (port.getTermPath().isRoot()) {
			if (port.isInput() && process.getInputs().size() > 1) { // don't delete the last input!
				// Root input = one of the inputs (normally atomic or optional)
				Log.d("Delete root input.");
				try {
					process.removeInput(port.getChannel());
				} catch (NotFoundException e) {
					throw new RuntimeException("Failed to find selected input to remove", e);
				}
			} else {
				// Root output = single atomic output
				Log.d("Delete root output. (denied)");
				return false;
			}
		} else {
			Log.d("Delete branch. Target: [" + getDialog().getProver().cllResourceString(port.getTerm()) + 
					"] Path: [" + port.getTermPath() +
					"] Root: [" + getDialog().getProver().cllResourceString(port.getRootTerm()) + "].");

			try {
				port.getRootTerm().deletePath(port.getTermPath());
			} catch (InvalidCllPathException | NotFoundException e) {
				throw new RuntimeException("Failed to delete path from root term", e);
			} 
		}		
		return true;
	} 
}
