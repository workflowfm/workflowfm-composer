package com.workflowfm.composer.edit.graph;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.session.CompositionSession;

public class RemoveBundleGraphEdit extends UndoableGraphEdit {

	private static final long serialVersionUID = 1338911344697918587L;
	private Object member;
	private Object[] cells;

	public RemoveBundleGraphEdit(CompositionSession session, ExceptionHandler handler, ProcessGraph graph, Object member) {
		super("Remove Graph",session,handler,graph,true);
		this.member = member;
	}
	
	@Override
	protected void doGraph() {
		if (isVisible()) {
			cells = getGraph().getGraphEngine().getConnectedCells(member).toArray();
			getGraph().getGraphEngine().deleteCells(cells);
		}
	}

	@Override
	protected void undoGraph() {
		if (cells != null) {
			getGraph().getGraphEngine().insertCells(cells);
		}
	}

	@Override
	protected void redoGraph() {
		doGraph();
	}
}
