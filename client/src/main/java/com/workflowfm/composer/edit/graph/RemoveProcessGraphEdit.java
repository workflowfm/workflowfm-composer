package com.workflowfm.composer.edit.graph;

import java.util.Collection;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.session.CompositionSession;

public class RemoveProcessGraphEdit extends UndoableGraphEdit {

	private static final long serialVersionUID = -3679572765776958525L;
	private String name;	
	
	private Collection<Collection<Object>> bundles;
	
	public RemoveProcessGraphEdit(CProcess process, CompositionSession session, ExceptionHandler handler, ProcessGraph graph) {
		super("Remove " + process.getName(),session, handler, graph, true);
		this.name = process.getName();
	}
	
	public RemoveProcessGraphEdit(String name, CompositionSession session, ExceptionHandler handler, ProcessGraph graph) {
		super("Remove " + name,session,handler,graph,true);
		this.name = name;
	}
	
	@Override
	protected void doGraph() {
		if (isVisible()) {
			this.bundles = getGraph().deleteAllBundles(name);
		}
	}
	
	@Override
	public boolean hadEffect() { 
		return bundles != null && bundles.size() > 0; 
	}
	
	public int getCount() {
		return bundles==null?0:bundles.size();
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	protected void undoGraph() {
		if (isVisible() && hadEffect()) {
			for (Collection<Object> bundle : bundles) {
				getGraph().getGraphEngine().insertCells(bundle.toArray());
			}
		}
	}

	@Override
	protected void redoGraph() {
		doGraph();
	}
}
