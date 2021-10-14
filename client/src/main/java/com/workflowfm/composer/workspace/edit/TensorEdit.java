package com.workflowfm.composer.workspace.edit;

import java.util.ListIterator;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ComposeActionState;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.ui.PortEdge;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.processes.ui.ProcessVertex;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.Workspace;

public class TensorEdit extends CompositionEdit {

	private static final long serialVersionUID = -1153024385524521940L;

	public TensorEdit(Workspace workspace, ExceptionHandler handler, CProcess process,
			ComposeAction action, ComposeActionState state, boolean visible) {
		super(workspace, handler, process, action, state, visible);
	}

	@Override
	public void doGraph(ProcessGraph graph, Object lvertex, CProcess larg, String lsel, Object rvertex, CProcess rarg, String rsel) {
		try {
			Object processVertex1 = graph.getBottomMostProcess(lvertex);
			Object processVertex2 = graph.getBottomMostProcess(rvertex);
			graph.removeUnconnectedOutputsFromBundle(processVertex1);
			graph.removeUnconnectedOutputsFromBundle(processVertex2);
			// No longer needed. Doing this at CompositionEdit.doGraph
			//graph.updateBundle(larg.getName(), process.getName());
			//graph.updateBundle(rarg.getName(), process.getName()); 
			((ProcessVertex)graph.getValue(processVertex1)).setBottom(false);
			((ProcessVertex)graph.getValue(processVertex2)).setBottom(false);
			
			Object joinVertex = graph.createJoinVertex(process,process.getName());

			if (((ProcessVertex)graph.getValue(processVertex1)).isTerminator()) { // JOIN node - delete connection but keep buffers 
				Log.d("Removing join node from " + larg.getName());
				for (Object edge : graph.getGraphEngine().getIncomingEdges(processVertex1)) {
					graph.rerouteEdgeTargetAsBuffer(edge, joinVertex);
				}
				graph.getGraphEngine().deleteCells(new Object[] { processVertex1 });
			} else {
				larg.getOutput().joinVertices(graph, process, false, true, processVertex1, joinVertex);
			}

			if (((ProcessVertex)graph.getValue(processVertex2)).isTerminator()) { // JOIN node - delete connection but keep buffers 
				Log.d("Removing join node from " + rarg.getName());
				for (Object edge : graph.getGraphEngine().getIncomingEdges(processVertex2)) {
					graph.rerouteEdgeTargetAsBuffer(edge, joinVertex);
				}
				graph.getGraphEngine().deleteCells(new Object[] { processVertex2 });
			} else {
				rarg.getOutput().joinVertices(graph, process, false, true, processVertex2, joinVertex);
			}
						
			process.getOutput().addAsOutputToVertex(graph, process, false, false, joinVertex);
			graph.updateBundle(joinVertex, process.getName());
			
		} catch (Exception e) {
			getExceptionHandler().handleException(e);
		}
	}

	public void doGraphOld(ProcessGraph graph, CProcess larg, String lsel,
			CProcess rarg, String rsel) {
		try {
			Object group = graph.groupProcesses(larg, rarg, process, false);

			process.getOutput().addAsOutputToVertex(graph, process, false, false, group);
			for (ListIterator<ProcessPort> it = process.getInputs().listIterator() ; it.hasNext();) {
				int i = it.nextIndex();
				it.next().addAsInputToVertex(graph, process, i, false, false, group);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
}
