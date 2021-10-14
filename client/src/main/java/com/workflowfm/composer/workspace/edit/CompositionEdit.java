package com.workflowfm.composer.workspace.edit;

import java.util.Collection;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ComposeActionState;
import com.workflowfm.composer.processes.ComposeProvenance;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.Workspace;

public class CompositionEdit extends UndoableWorkspaceEdit {

	private static final long serialVersionUID = -5624038728187028171L;
	
	protected static final String LEFT_BUNDLE_NAME = "_LEFT_";
	protected static final String RIGHT_BUNDLE_NAME = "_RIGHT_";

	private ComposeAction action;

	protected ComposeActionState state;
	protected CProcess process;
	private CProcess previous;
	
	private Collection<Object> largCells;
	private Collection<Object> rargCells;
	
	public CompositionEdit(Workspace workspace, ExceptionHandler handler, CProcess process, ComposeAction action, ComposeActionState state, boolean visible) {
		super(action.getAction() + " " + action.getLarg() + " with " + action.getRarg() + " (" + action.getResult() + ")", workspace, handler, visible);
		this.action = action;
		this.process = process;
		this.state = state;
		try {
			ComposeProvenance prov = state.getOutputProvenance(process.getName());
			prov.clarifyOutputProvenance(workspace);
			this.process.setProvenance(prov);
		} catch (NotFoundException ex) { 
			Log.w("Prover did not send provenance for output of: " + process.getName());
		}
	}

	@Override
	protected void doExec() {
		if (getWorkspace().processExists(process.getName())) {
			try {
				previous = getWorkspace().getProcess(process.getName());
				getWorkspace().updateProcess(previous.getName(),process);
			} catch (NotFoundException e) { }
			
		} else {
			getWorkspace().addProcess(process);
		}
		
	}

	@Override
	protected void undoWorkspace() {
		if (previous != null)
			try {
				getWorkspace().updateProcess(process.getName(), previous);
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		else
			try {
				getWorkspace().removeProcess(process.getName());
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
	}

	@Override
	protected void redoWorkspace() {
		doExec();
	}
	
	@Override
	public void doGraph() {
		CProcess larg;
		CProcess rarg;
		try {
			larg = getWorkspace().getProcess(action.getLarg());
			rarg = getWorkspace().getProcess(action.getRarg());
		} catch (NotFoundException e1) {
			getExceptionHandler().handleException(e1);
			return;
		}
		

		ProcessGraph graph = new ProcessGraph();

		try {
			graph.insertGraph(larg.getCompositionGraph());
			// Updating the bundle name allows 2 things
			// 1) Cloning values in the graph so that operations do not affect previous graphs
			// 2) Setting separate bundle names so we can operate on bundles that have the same name
			graph.updateBundle(larg.getName(), LEFT_BUNDLE_NAME);
			Object lvertex = graph.getBundleProcessVertexByLabel(LEFT_BUNDLE_NAME);
			
			graph.insertGraph(rarg.getCompositionGraph());
			graph.updateBundle(rarg.getName(), RIGHT_BUNDLE_NAME);
			Object rvertex = graph.getBundleProcessVertexByLabel(RIGHT_BUNDLE_NAME);
				
			doGraph(graph, lvertex, larg, action.getLsel(), rvertex, rarg, action.getRsel());
		} catch (NotFoundException e1) { // This should never happen! We insert the graph, but can't find it?
			getExceptionHandler().handleException(e1);
			return;
		}
			
		process.setCompositeGraph(graph);

		if (isVisible()) {
			try {
				largCells = getWorkspace().getGraph().deleteBundle(action.getLarg());
			} catch (NotFoundException e) {	}
			try {
				rargCells = getWorkspace().getGraph().deleteBundle(action.getRarg());
			} catch (NotFoundException e) {	}	
		
			getWorkspace().getGraph().insertGraph(graph);
		}
	}
	

	@Override
	protected void undoGraph() {
		try {
			getWorkspace().getGraph().deleteBundle(process.getName());
		} catch (NotFoundException e) {	
			Log.e("Tried to undo adding a graph, but failed: " + process.getName());
			e.printStackTrace();
		}
		if (largCells != null) try { // we used to re-insert the deleted cells, but that is bug-gy! 
			CProcess larg = getWorkspace().getProcess(action.getLarg());
			getWorkspace().getGraph().insertGraph(larg.getCompositionGraph());
		} catch (NotFoundException e1) {
			getExceptionHandler().handleException(e1);
			return;
		}
		if (rargCells != null) try {
			CProcess rarg = getWorkspace().getProcess(action.getRarg());
			getWorkspace().getGraph().insertGraph(rarg.getCompositionGraph());
		} catch (NotFoundException e1) {
			getExceptionHandler().handleException(e1);
			return;
		}
	}

	@Override
	protected void redoGraph() {
		if (isVisible()) {
			try {
				getWorkspace().getGraph().deleteBundle(action.getLarg());
			} catch (NotFoundException e) {	}
			try {
				getWorkspace().getGraph().deleteBundle(action.getRarg());
			} catch (NotFoundException e) {	}	
		
			getWorkspace().getGraph().insertGraph(process.getCompositionGraph());
		}
	}
	
	public void doGraph(ProcessGraph graph, Object lvertex, CProcess larg, String lsel, Object rvertex, CProcess rarg, String rsel) {
		graph.insertGraph(process.getAtomicGraph());
	}
}
