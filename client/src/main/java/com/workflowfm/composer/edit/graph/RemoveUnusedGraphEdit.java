package com.workflowfm.composer.edit.graph;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.session.CompositionSession;

public class RemoveUnusedGraphEdit extends UndoableGraphEdit {

	private static final long serialVersionUID = 3436205093989034421L;
	private Object[] cells;

	public RemoveUnusedGraphEdit(CompositionSession session, ExceptionHandler handler, ProcessGraph graph) {
		super("Remove Unused Processes",session,handler,graph,true);
	}
	
	@Override
	protected void doGraph() {
		if (isVisible()) {
			this.cells = getGraph().removeAtomicBundles().toArray();
		}
	}
	
	@Override
	public boolean hadEffect() { return cells != null && cells.length > 0; }

	@Override
	protected void undoGraph() {
		if (isVisible() && hadEffect()) {
			getGraph().getGraphEngine().insertCells(cells);
		}
	}

	@Override
	protected void redoGraph() {
		doGraph();
	}
}
