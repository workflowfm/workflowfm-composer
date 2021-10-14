package com.workflowfm.composer.workspace.edit;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ComposeActionState;
import com.workflowfm.composer.processes.MergedInput;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.processes.ui.ProcessVertex;
import com.workflowfm.composer.workspace.Workspace;

public class WithEdit extends CompositionEdit {

	private static final long serialVersionUID = 6221032526210935247L;

	public WithEdit(Workspace workspace, ExceptionHandler handler, CProcess process,
			ComposeAction action, ComposeActionState state, boolean visible) {
		super(workspace, handler, process, action, state, visible);
	}

	@Override
	public void doGraph(ProcessGraph graph, Object lvertex, CProcess larg, String lsel, Object rvertex, CProcess rarg, String rsel)  {
		try {
			mergeInputs(graph,lvertex,larg,rvertex,rarg);
			mergeOutput(graph,lvertex,larg,rvertex,rarg);
			graph.updateBundle(lvertex, process.getName());
		} catch (NotFoundException ex) { 
			this.getExceptionHandler().handleException(ex);
		}
	}

	private void mergeInputs (ProcessGraph graph, Object lvertex, CProcess larg, Object rvertex, CProcess rarg) {

		Object mergeNode = graph.createMergeVertex(process,process.getName());
		
		for (MergedInput m : state.getMerged()) try {
			Object leftNode = graph.removeInputChannelFromProcessBundle(lvertex,m.getLeft());
			Object rightNode = graph.removeInputChannelFromProcessBundle(rvertex,m.getRight());
			ProcessPort leftPort = larg.getInput(m.getLeft());
			ProcessPort rightPort = rarg.getInput(m.getRight());	
			
			Object lValue = graph.getValue(leftNode);
			Object rValue = graph.getValue(rightNode);
			
			boolean leftIsMerge = lValue instanceof ProcessVertex && ((ProcessVertex)lValue).isMerge();
			boolean rightIsMerge = rValue instanceof ProcessVertex && ((ProcessVertex)rValue).isMerge();

			if (leftIsMerge) {
				for (Object edge : graph.getGraphEngine().getIncomingEdges(leftNode))
					graph.rerouteEdgeTarget(edge, mergeNode);
				for (Object edge : graph.getGraphEngine().getOutgoingEdges(leftNode))
					graph.rerouteEdgeSource(edge, mergeNode);
				graph.getGraphEngine().deleteCells(new Object[] { leftNode });
			} else {
				leftPort.joinVertices(graph, process, true, false, mergeNode, leftNode);
			}
			
			if (rightIsMerge) {
				for (Object edge : graph.getGraphEngine().getIncomingEdges(rightNode))
					graph.rerouteEdgeTarget(edge, mergeNode);
				for (Object edge : graph.getGraphEngine().getOutgoingEdges(rightNode))
					graph.rerouteEdgeSource(edge, mergeNode);
				graph.getGraphEngine().deleteCells(new Object[] { rightNode });

			} else {
				rightPort.joinVertices(graph, process, true, false, mergeNode, rightNode);
			}
			
//			for (ListIterator<ProcessPort> it = process.getInputs().listIterator() ; it.hasNext();) {
//				int i = it.nextIndex();
//				ProcessPort p = it.next();
//				if (!larg.hasChannel(p.getChannel()) && !rarg.hasChannel(p.getChannel())) 
//					p.addAsInputToVertex(graph, process, i, false, false, mergeNode);
//			}	
			
			ProcessPort p = m.getTerm();
			p.unneg(); // The server reports these inputs negated, so we need to get rid of the negation
			p.addAsInputToVertex(graph, process, process.getInputIndex(p.getChannel()), false, false, mergeNode);

		} catch (NotFoundException ex) { 
			this.getExceptionHandler().handleException(ex);
		}
	}

	
	private void mergeOutput(ProcessGraph graph, Object lvertex, CProcess larg, Object rvertex, CProcess rarg) throws NotFoundException {
		graph.removeUnconnectedOutputsFromBundle(lvertex);
		graph.removeUnconnectedOutputsFromBundle(rvertex);

		Object leftBottom = graph.getBottomMostProcess(lvertex);
		Object rightBottom = graph.getBottomMostProcess(rvertex);
		((ProcessVertex)graph.getValue(leftBottom)).setBottom(false);
		((ProcessVertex)graph.getValue(rightBottom)).setBottom(false);
		
		Object lBottomValue = graph.getValue(leftBottom);
		Object rBottomValue = graph.getValue(rightBottom);

		boolean leftBottomIsSpecial = lBottomValue instanceof ProcessVertex && (((ProcessVertex)lBottomValue).isMerge() || ((ProcessVertex)lBottomValue).isTerminator());
		boolean rightBottomIsSpecial = rBottomValue instanceof ProcessVertex && (((ProcessVertex)rBottomValue).isMerge() || ((ProcessVertex)rBottomValue).isTerminator());
			
		Object mergeBottomNode = graph.createMergeVertex(process,process.getName());
							
		if (leftBottomIsSpecial) {
			for (Object edge : graph.getGraphEngine().getIncomingEdges(leftBottom))
				graph.rerouteEdgeTarget(edge, mergeBottomNode);
			graph.getGraphEngine().deleteCells(new Object[] { leftBottom });
		} else {
			larg.getOutput().joinVertices(graph, process, false, false, leftBottom, mergeBottomNode);
		}

		if (rightBottomIsSpecial) {
			for (Object edge : graph.getGraphEngine().getIncomingEdges(rightBottom)) 
				graph.rerouteEdgeTarget(edge, mergeBottomNode);
			graph.getGraphEngine().deleteCells(new Object[] { rightBottom });
		} else {
			rarg.getOutput().joinVertices(graph, process, false, false, rightBottom, mergeBottomNode);
		}

		process.getOutput().addAsOutputToVertex(graph, process, false, false, mergeBottomNode);
		((ProcessVertex)graph.getValue(mergeBottomNode)).setBottom(true);
	}	
}
