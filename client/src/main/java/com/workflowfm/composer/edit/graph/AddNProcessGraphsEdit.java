package com.workflowfm.composer.edit.graph;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ProcessStore;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.utils.Log;

public class AddNProcessGraphsEdit extends UndoableGraphEdit {

	private static final long serialVersionUID = -7306627851140035089L;
	private CProcess process;
	private ProcessStore store;

	private String name;
	private int n;

	public AddNProcessGraphsEdit(String name, int n, ProcessStore store,
			CompositionSession session, ExceptionHandler handler,
			ProcessGraph graph) {
		super("Add graph of " + name, session, handler, graph, true);
		this.name = name;
		this.n = n;
	}

	public AddNProcessGraphsEdit(CProcess process, int n,
			CompositionSession session, ExceptionHandler handler,
			ProcessGraph graph) {
		super("Add graph of " + process.getName(), session, handler, graph, true);
		this.name = process.getName();
		this.process = process;
		this.n = n;
	}


	@Override
	protected void doGraph() {
		if (this.process == null) {
			try {
				if (this.store != null)
					this.process = store.getProcess(name);
				else
					this.process = getSession().getProcess(name);
			} catch (NotFoundException ex) {
				getExceptionHandler().handleException(ex);
				return;
			}
		}

		if (isVisible()) {
			for (int i = 0; i < n; i++)
				getGraph().insertGraph(process.getCompositionGraph());
		}
	}

	@Override
	protected void undoGraph() {
		if (isVisible()) {
			for (int i = 0; i < n; i++)
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
