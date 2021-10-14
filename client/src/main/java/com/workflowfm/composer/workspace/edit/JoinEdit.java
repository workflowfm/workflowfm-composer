package com.workflowfm.composer.workspace.edit;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.exceptions.InvalidProvenancePathException;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllTermPath;
import com.workflowfm.composer.processes.CllTermVisitor;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ComposeActionState;
import com.workflowfm.composer.processes.ComposeProvenance;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.ui.PortEdge;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.processes.ui.ProcessVertex;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.Workspace;

public class JoinEdit extends CompositionEdit {

	private static final long serialVersionUID = 8306130450468084478L;

	private boolean newStuff = true;
	
	public JoinEdit(Workspace workspace, ExceptionHandler handler, CProcess process,
			ComposeAction action, ComposeActionState state, boolean visible) {
		super(workspace, handler, process, action, state, visible);
	}
	
	@Override
	public void doGraph(ProcessGraph graph, Object lvertex, CProcess larg, String lsel, Object rvertex, CProcess rarg, String rsel) {
		try {
			Log.d("Joining " + larg.getName() + " to " + rarg.getName());
		
			// TODO we assume ProcessVertices with composite processes are JOIN nodes. We'll need to do better than that if we add more composite nodes.
			
			Object processVertex1 = graph.getBottomMostProcess(lvertex);
			Object bufferVertex1 = processVertex1;
			boolean deleteBufferVertex1 = false;
			if (((ProcessVertex)graph.getValue(processVertex1)).isTerminator()) { // JOIN node - delete connection but keep buffers 
				Log.d("Removing join node from " + larg.getName());
				Object source = null;
				for (Object edge : graph.getGraphEngine().getIncomingEdges(processVertex1)) {
					Object vEdge = graph.getValue(edge);
					if (vEdge instanceof PortEdge && ((PortEdge)vEdge).isBuffer()) {
						if (source == null) source = graph.getConnectionSource(edge);
					} else {
						source = graph.getConnectionSource(edge);
						graph.removeEntireConnection(edge);
					}
				}
				if (source != null) { // this should always be the case, but you never know!
					// ((ProcessVertex)graph.getValue(source)).setBottom(true); Don't do that here because side-effects with original graph!
					bufferVertex1 = processVertex1;
					deleteBufferVertex1 = true;
					processVertex1 = source;
				}
			}		
			
			Object processVertex2 = graph.getBottomMostProcess(rvertex);
			Object nullTargetVertex = processVertex2; // This handles the special optional cases
			if (state.getOutputProvenance(process.getName()).getSources().contains(CProcess.nameOfMerge(process.getName()))) { // Special MERGE situation
				Log.d("Adding merge node to " + rarg.getName());
				// We create a merge vertex
				// We make sure it has the name of the right process bundle, so that search starting from this vertex finds items of the right process
				Object mergeVertex = graph.createMergeVertex(process,CompositionEdit.RIGHT_BUNDLE_NAME);
				
				if (((ProcessVertex)graph.getValue(processVertex2)).isTerminator()) { // we need to replace the join node with the new merge node
					graph.removeUnconnectedOutputsFromBundle(processVertex2);
					for (Object edge : graph.getGraphEngine().getIncomingEdges(processVertex2))
						graph.rerouteEdgeTarget(edge, mergeVertex);
					graph.getGraphEngine().deleteCells(new Object[] { processVertex2 });
				} else {
					((ProcessVertex)graph.getValue(processVertex2)).getProcess().getOutput().joinVertices(graph, rarg, true, false, processVertex2, mergeVertex);
				}

				// TODO Cloning graph cells does not clone cell values. Perhaps setBottom should be in ProcessGraph like updateBundle and renew ProcessVertices? 
				((ProcessVertex)graph.getValue(processVertex2)).setBottom(false);
				((ProcessVertex)graph.getValue(mergeVertex)).setBottom(true);
				processVertex2 = mergeVertex;
				nullTargetVertex = mergeVertex;
			}
			else if (!((ProcessVertex)graph.getValue(processVertex2)).isTerminator() && !(process.getOutputCll().equals(rarg.getOutputCll()))) {
				Log.d("Adding join node to " + rarg.getName());
				// We create a join vertex
				// We make sure it has the name of the right process bundle, so that search starting from this vertex finds items of the right process
				Object joinVertex = graph.createJoinVertex(process,CompositionEdit.RIGHT_BUNDLE_NAME);
				graph.removeUnconnectedOutputsFromBundle(processVertex2);
				((ProcessVertex)graph.getValue(processVertex2)).getProcess().getOutput().joinVertices(graph, rarg, false, false, processVertex2, joinVertex);

				// TODO Cloning graph cells does not clone cell values. Perhaps setBottom should be in ProcessGraph like updateBundle and renew ProcessVertices? 
				((ProcessVertex)graph.getValue(processVertex2)).setBottom(false);
				((ProcessVertex)graph.getValue(joinVertex)).setBottom(true);
				processVertex2 = joinVertex;
			}
			
			HashMap<String, Object> selectedTargets = new HashMap<String,Object>();
			
			Log.d("Join vertices: left=[" + graph.getValue(processVertex1) + "] right=[" + graph.getValue(processVertex2) + "] leftBuffersAreOn=[" + graph.getValue(bufferVertex1) + "] nullGoesTo=[" + graph.getValue(nullTargetVertex) + "]");
			
			try {
				for (JoinedResource resource : analyseConnection(larg, state.getInputProvenance(larg.getOutputCll()))) {
					join(graph, processVertex1, bufferVertex1, processVertex2, nullTargetVertex, resource, larg.getOutputCll(), selectedTargets);
				} 
			} catch (NotFoundException nfe) {
				nfe.printStackTrace();
			}			

			//deleteJoinedInputs(graph, processVertex2);
			
			// The two bundles are connected now so all nodes should be updated properly
			graph.updateBundle(processVertex1, process.getName()); 
			((ProcessVertex)graph.getValue(processVertex1)).setBottom(false);

			//addReportedBufferedEdges(graph, processVertex1, processVertex2, buffered);

			if (deleteBufferVertex1) {
				graph.removeUnconnectedOutputsFromBundle(bufferVertex1);
				graph.getGraphEngine().deleteCells(new Object[] { bufferVertex1 });
			}
			
			// Replace outputs of bundle with outputs of composite process - also removes outputs of processVertex1
			graph.removeUnconnectedOutputsFromBundle(processVertex2);
			process.getOutput().addAsOutputToVertex(graph, process, false, false, processVertex2);

			//addInferredBufferedEdges(graph, processVertex1, processVertex2);
			//optimiseBufferedEdgeRouting(graph, processVertex1);
		} catch (Exception e) {
			getExceptionHandler().handleException(e);
		}
	}
	


	private boolean isOptional(CllTerm source, CllTermPath path) {
		return isOptionalParent(source,path);
		//return isOptionalAny(source,path);
	}
	
	private boolean isOptionalAny(CllTerm source, CllTermPath path) {
		final Vector<String> types = new Vector<String>();
		CllTermVisitor visitor = new CllTermVisitor() {
			@Override
			public boolean visit(CllTerm term,CllTermPath path) {
				types.add(term.getType());
				return false;
			}
		};
		
		try {
			path.traverseParent(source, visitor);
		} catch (InvalidCllPathException | NotFoundException e) {
			e.printStackTrace();
		}
		
		return types.contains("plus");
	}

	private boolean isOptionalParent(CllTerm source, CllTermPath path) {
		try {
			return path.followParent(source).getType().equals("plus");
		} catch (InvalidCllPathException | NotFoundException e) {
			return false;
		}
	}
	
	private boolean originalWasOptional(final ProcessGraph graph, Object processVertex1, CllTerm term) throws InvalidCllPathException, NotFoundException {
		// Get the original process - otherwise optimiseBufferEdgeRouting should take care of it
		// if the term exists in the output and has any optional (grand)parent then it is treated as optional
		// This may be overly sensitive and there are probably better ways to represent stuff (see "compound optional" unit test)
		// It is limited by the fact that HOL Light reports connected INPUTS and there is no way currently to find out which output corresponds to a connection
		// TODO the most problematic cases are when a connected term appears in the output multiple times. e.g. (A ++ B) ** B - is B optional?
		// Interestingly this also depends on lsel!

		CllTerm outputOfOriginalProcess = ((ProcessVertex)graph.getValue(processVertex1)).getProcess().getOutputCll();
		Optional<CllTermPath> res = outputOfOriginalProcess.hasSubterm(term);

		if (res.isPresent() && !res.get().isRoot()) {
			return isOptional(outputOfOriginalProcess, res.get());
		}
		else
			return false;
	}

	private void join(final ProcessGraph graph, 
			Object processVertex1, Object bufferVertex1, 
			Object processVertex2, Object nullTargetVertex,
			JoinedResource resource, CllTerm parent,
			HashMap<String,Object> selectedTargets) throws InvalidCllPathException, NotFoundException
	{
		CllTerm term = resource.getPath().follow(parent); // find the sub-term returned by analyseConnections
		
		boolean optional = isOptional(parent, resource.getPath());
		Log.d("Connection: " + resource.debugString(getWorkspace().getProver(), parent) + " - Optional: [" + optional + "]");
	
		Object sourceVertex = null; // We need to find the (unique source) in the graph
		
		if (((ProcessVertex)graph.getValue(processVertex1)).getProcess().getName().equals(resource.getSource())) {
			// The (left) process we are joining is the source
			Log.d("Source found by direct connection: " + getWorkspace().getProver().cllResourceString(term) + " - source: " + resource.getSource());
			sourceVertex = processVertex1;
		}
		else {
			// The source is some other vertex further left. This means the sub-term must have be buffered up till now
			Object sourceEdge = null;
			Object[] incomingEdges = graph.getGraphEngine().getIncomingEdges(bufferVertex1);

			Vector<Object> buffers = new Vector<Object>();
			
			// Gather all incoming buffers with a source that matches the resource's source
			for (Object incomingEdgeCell : incomingEdges)
			{ 
				Object val = graph.getValue(incomingEdgeCell);
				if (!(val instanceof PortEdge)) continue;
				PortEdge pe = (PortEdge)val;
				if (!(pe.isBuffer())) continue;
				
				Object sV = graph.getConnectionSource(incomingEdgeCell);
				Object s = graph.getValue(sV);
				if (s instanceof ProcessVertex && ((ProcessVertex)s).getProcess().getName().equals(resource.getSource()))
				{
					buffers.add(incomingEdgeCell);
				}
			}
			
			// Heuristics to pick the right source
			// Note that matching a buffer to the output can be tricky because of filtering.
			
			// Only 1 matching vertex: this will be the most common.
			if (buffers.size() == 1) {
				Log.d("Source found by single option: " + getWorkspace().getProver().cllResourceString(term) + " - source: " + resource.getSource());
				sourceEdge = buffers.elementAt(0);
			}
			
			if (sourceEdge == null)
				// Perfect match: the output of the buffer is exactly the same as the joining sub-term
				for (Object edge : buffers)
				{ 
					PortEdge pe = (PortEdge)graph.getValue(edge);
					if (pe.getRootTerm().equals(term)) {
						Log.d("Source found by root term match: " + getWorkspace().getProver().cllResourceString(term) + " - source: " + resource.getSource());
						sourceEdge = edge;
						break;
					}
				}
			
			if (sourceEdge == null)
				// Vars match: the set of variables of the buffer match exactly the set of variables in the joining sub-term 
				for (Object edge : buffers)
				{ 
					PortEdge pe = (PortEdge)graph.getValue(edge);
					if (pe.getRootTerm().getVars().equals(term.getVars())) {
						Log.d("Source found by vars match: " + getWorkspace().getProver().cllResourceString(term) + " - source: " + resource.getSource());
						sourceEdge = edge;
						break;
					}
				}
			
			if (sourceEdge == null)
				// Subterm match: a subterm of the buffer is the same as the joining sub-term
				for (Object edge : buffers)
				{ 
					PortEdge pe = (PortEdge)graph.getValue(edge);
					if (pe.getRootTerm().hasSubterm(term).isPresent()) {
						Log.d("Source found by subterm: " + getWorkspace().getProver().cllResourceString(term) + " - source: " + resource.getSource());
						sourceEdge = edge;
						break;
					}
				}

			if (sourceEdge == null)
				// Subset vars match: the variables in the joining sub-term are all contained in the set of variables of the buffer
				for (Object edge : buffers)
				{ 
					PortEdge pe = (PortEdge)graph.getValue(edge);
					if (pe.getRootTerm().getVars().containsAll(term.getVars())) {
						Log.d("Source found by vars subset: " + getWorkspace().getProver().cllResourceString(term) + " - source: " + resource.getSource());
						sourceEdge = edge;
						break;
					}
				}
			
			if (sourceEdge == null)
				// Nothing worked - just pick one
				if (buffers.size() > 0) {
					Log.d("None of the source heuristic worked for joined term: " + getWorkspace().getProver().cllResourceString(term) + " - source: " + resource.getSource());
					sourceEdge = buffers.elementAt(0);
				}
				
			if (sourceEdge != null) {
				sourceVertex = graph.getConnectionSource(sourceEdge);
				graph.removeConnectionForTerm(sourceEdge,term);
			}
			
			if (sourceVertex == null) {
				// Failure - just pick any connected process with that name
				Log.w("Could not find source of joined term: " + getWorkspace().getProver().cllResourceString(term) + " - source: " + resource.getSource());
				try {
					sourceVertex = graph.getConnectedProcessVertex(processVertex1, resource.getSource());
				} catch (NotFoundException e) {
					// Failure - just use the rightmost process
					sourceVertex = nullTargetVertex; //processVertex1;
				}
			}
		}
		
		Log.d("Joining from vertex " + graph.getValue(sourceVertex) + " to " + graph.getValue(processVertex2));

		try {
			term.joinGraphs(graph, process, process.getName(), optional, sourceVertex, true, processVertex2, nullTargetVertex, resource.getTargets(), selectedTargets);
		} catch (InvalidProvenancePathException e) {
			getExceptionHandler().handleException("Failed to match input provenance when joining graphs.", e);
		} catch (NotFoundException e1) {
			getExceptionHandler().handleException(e1);
		}
		//term.joinVertices(graph, process.getName(), null, optional, resource.isBuffered(), sourceVertex, true, processVertex2, true); 
	}
	
	public class JoinedResource {
		private CllTermPath path;
		//private boolean optional;
		private String source;
		private ComposeProvenance targets;
		
		public JoinedResource(CllTermPath path, /*boolean optional, */String source, ComposeProvenance targets) {
			super();
			this.path = path;
			//this.optional = optional;
			this.source = source;
			this.targets = targets;
		}
		
		public CllTermPath getPath() {
			return path;
		}
//		public boolean isOptional() {
//			return optional;
//		}
		public ComposeProvenance getTargets() {
			return targets;
		}		
		public String getSource() {
			return source;
		}
		public String debugString(Prover prover, CllTerm term) {
			try {
				return "[" + prover.cllResourceString(path.follow(term)) + "] Source: [" + source + "] Targets: [" + targets.getSources() + "] .";
			} catch (InvalidCllPathException e) {
				return "[" + prover.cllResourceString(term) + "] INVALID PATH:[" + path.toString() + "] Source: [" + source + "] Target: [" + targets.getSources() + "] .";
			}
		}
	}
	
	// Split the output to sub-terms that have a unique source and the targets either (a) are buffers or (b) have no buffers in them
	public Collection<JoinedResource> analyseConnection(CProcess lproc, final ComposeProvenance iprov) {
		final Vector<JoinedResource> connected = new Vector<JoinedResource>();
		final ComposeProvenance oprov = lproc.getProvenance();
		CllTerm output = lproc.getOutputCll();
		
		CllTermVisitor visitor = new CllTermVisitor() {	
			@Override
			public boolean visit(CllTerm term, CllTermPath path) {
//				try {
				Optional<String> source = path.follow(oprov).getSingleSource(); // Does the entire sub-term come from the same source?
				if (!source.isPresent()) return false; // No! -> keep looking
				else { // Yes! 
					ComposeProvenance targets = path.follow(iprov);
					if (targets.hasNoBuffers() || targets.isBuffered()) { // split buffers from targets with no buffers - if this sub-term is neither, keep looking
						connected.add(new JoinedResource(path, source.get(), targets));
						return true;
					} else {
						return false;
					}
				}
//				} catch (InvalidProvenancePathException e) {
//					return false;
//				}
			}
		};
		
		output.bfsAll(visitor, true);
		return connected;
	}
	
}
