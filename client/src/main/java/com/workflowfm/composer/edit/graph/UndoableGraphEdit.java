package com.workflowfm.composer.edit.graph;

import com.workflowfm.composer.edit.UndoableSessionEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.session.CompositionSession;

public abstract class UndoableGraphEdit extends UndoableSessionEdit { 

	private static final long serialVersionUID = -6990905316911085333L;

	private ProcessGraph graph;
	private boolean visible;

	//private UndoableEdit graphEdit = null;	

	public UndoableGraphEdit(String description, CompositionSession session, ExceptionHandler handler, ProcessGraph graph, boolean visible) {
		super(description,session,handler);
		this.graph = graph;
		this.visible = visible;
	}

	public ProcessGraph getGraph() { 
		return this.graph; 
	}

	protected abstract void doGraph();
	protected abstract void undoGraph();
	protected abstract void redoGraph();

//	private void undoGraphEdit() {
//		if (graphEdit != null) graphEdit.undo();
//		else Log.d("No graph edit to undo.");
//	}
//
//	private void redoGraphEdit() {
//		if (graphEdit != null) graphEdit.redo();
//	}

	@Override
	protected void doSession() { 
		doGraph();
		graph.layout();
	}

	@Override
	protected void undoSession() { 
		undoGraph();
		graph.layout();
	}

	@Override
	protected void redoSession() { 
		redoGraph();
		graph.layout();
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
