package com.workflowfm.composer.edit.graph;

import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ProcessStore;
import com.workflowfm.composer.utils.Log;

public class AddRemovedProcessGraphEdit extends UndoableGraphEdit {

	private static final long serialVersionUID = 2969566322768619245L;

	private RemoveProcessGraphEdit removeEdit;
	private ProcessStore store;
	private String newName;
	
	private CProcess process;
	
	public AddRemovedProcessGraphEdit(String newName, RemoveProcessGraphEdit removeEdit, ProcessStore store) {
		super("Restore graphs of " + removeEdit.getName(), removeEdit.getSession(), removeEdit.getExceptionHandler(), removeEdit.getGraph(), true);
		this.removeEdit = removeEdit;
		this.store = store;
		this.newName = newName;
	}

	@Override
	protected void doGraph() {
		try {
			this.process = store.getProcess(newName);
		} catch (NotFoundException e) {
			getExceptionHandler().handleException(e);
			return;
		}
		if (isVisible()) {
			for (int i = 0; i < removeEdit.getCount(); i++)
				getGraph().insertGraph(process.getCompositionGraph());
		}
	}

	@Override
	protected void undoGraph() {
		if (isVisible() && process != null) {
			for (int i = 0; i < removeEdit.getCount(); i++)
				try {
					getGraph().deleteBundle(process.getName());
				} catch (NotFoundException e) {
					Log.e("Tried to undo adding a graph, but failed: " + process.getName());
					e.printStackTrace();
				}
		}
	}

	@Override
	protected void redoGraph() {
		doGraph();
	}

}
