package com.workflowfm.composer.processes;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.exceptions.InvalidProvenancePathException;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.ui.PortEdge;
import com.workflowfm.composer.processes.ui.PortVertex;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.processes.ui.ProcessVertex;
import com.workflowfm.composer.utils.Log;

/** Represents a CLL term such as A ++ B ** C. */
public class CllTerm implements Serializable {
	private static final long serialVersionUID = -7479893251612940973L;

	private String type;
	private String name = "";
	private Vector<CllTerm> args;

	public CllTerm(String type, String name, Vector<CllTerm> args) {
		this.type = type;
		this.name = name;
		this.args = args;
	}

	public CllTerm(String var) {
		this("var",var,new Vector<CllTerm>());
	}

	public CllTerm(CllTerm other) {
		this.type = other.getType();
		this.name = other.getName();
		this.args = new Vector<CllTerm>(other.getArgs().size());
		for (CllTerm arg : other.getArgs()) {
			this.args.add(new CllTerm(arg));
		}
	}

	// Constructor used by GSON so that args is not null.
	@SuppressWarnings("unused")
	private CllTerm() {
		this("","",new Vector<CllTerm>());
	}

	public boolean isAtomic() {
		return args.size() == 0;
	}

	public boolean isUnary() {
		return args.size() == 1;
	}


	/** Returns all the variables that occur in the CLL term. */
	public Set<String> getVars() {
		HashSet<String> res = new HashSet<String>();
		if (isAtomic()) res.add(name);
		else {
			for (CllTerm c : args) {
				res.addAll(c.getVars());
			}
		}
		return res;
	}

	/**
	 * Given a service vertex and a CLL term that represents a message, this
	 * function adds edges to the vertex that represent that message.
	 */
	private Object addProcessPorts(
			ProcessGraph graph, CProcess process, String bundle, int inputIndex, 
			CllTerm root, CllTermPath initialPath, 
			boolean optional, boolean buffer, boolean muted, boolean clickableNodes,
			Object vertex, boolean vertexIsProcess, String previousType,
			Object target) {

//		Log.d("Process:" + process.getName() + " vertexIsProcess:" + vertexIsProcess + " type:" + type + " name:" + name + " previousType:" + previousType);

		//vertexType can be:
		// "" = process, "var", "times", "plus"
		
		//final String arrow = (vertexIsProcess && input ? "" : ";endArrow=none");

		//		 System.out.println("Processing CLL: " + vertexIsService + " " + type
		//		 +
		//		 " " + previousType + "(" + optional + ")");
		PortEdge message = new PortEdge(this, root, initialPath, buffer, optional);
		message.setMuted(muted);
		
		if (type.equals("var")) {
			Object result;
			if (target == null) {
				PortVertex portVertex = new PortVertex(process,inputIndex,message,bundle,true);
				result = graph.addMessagePortVertex(portVertex, vertex); // style
			} else {
				result = graph.addMessagePortEdge(inputIndex >= 0, message, target, vertex);
			}
			return result;
		}

		if (type.equals("times")) {	
			// Fork unless we are connecting directly to the process.

			// e.g. for A**B, the A and B will be attached directly to the
			// service vertex instead of a being attached to a new vertex.
			// e.g. for A**(B**C), the A, B and C will be attached to the
			// same node instead of a branch being created for B and C. <-- this is taken care of by HOL Light now
			// Update: Restored this functionality even though HOL Light takes care of it to an extent.
			// In a lot of situations HOL Light produces left associative outputs such as (A ** B) ** C (see Cristina's example 28/06/2016) 
			if (/*buffer ||*/ (!vertexIsProcess && !"times".equals(previousType))) {
				PortVertex portVertex = new PortVertex(process,inputIndex,message,bundle,clickableNodes);
				vertex = graph.addMessagePortVertex(portVertex, vertex);
			}
			optional = false;

		} else if (type.equals("plus")) {
			// Fork unless we are connecting directly to the process.

			// We only avoid forking for outputs because we need extra
			// logic to make sure input rendering is unambiguous.
			if ((inputIndex >= 0) /*|| buffer*/ || (!vertexIsProcess && !"plus".equals(previousType))) {
				PortVertex portVertex = new PortVertex(process,inputIndex,message,bundle,clickableNodes);
				vertex = graph.addMessagePortVertex(portVertex, vertex); 
			}
			optional = true;

		} else if (type.equals("neg")) {
			// We ignore this and move straight to the argument. Under normal circumstances we should never encounter this.
			//TODO this ends up happening at JoinEdit because HOL Light reports connections as negated terms (inputs)
			// input = !input; // We flip the input parameter. This should never happen though.
		} else
			throw new RuntimeException("Failed to parse type: " + type);
		
		int argcount = 0;
		for (CllTerm c : args) {
			CllTermPath path = initialPath.clone();
			path.add(argcount);
			c.addProcessPorts(graph, process, bundle, inputIndex, root, path, optional, buffer, muted, clickableNodes, vertex, false, type, target);
			argcount++;
		}
		return target;
	}

	public Object addProcessPorts(
			ProcessGraph graph, CProcess process, String bundle, int inputIndex,
			boolean optional, boolean clickableNodes,
			Object vertex, boolean vertexIsProcess) {
		return this.addProcessPorts(graph, process, bundle, inputIndex, this, new CllTermPath(), optional, false, false, clickableNodes, vertex, vertexIsProcess, "", null);
	}
	
	public Object joinVertices(
			ProcessGraph graph, CProcess process, String bundle,
			boolean optional, boolean buffer, 
			Object vertex1, boolean vertex1IsProcess,
			Object vertex2, boolean vertex2IsProcess) {
		//LinkedList<Object> tipVertices = addProcessPorts(graph, bundle, channel, "", false, optional, vertex1, vertex1IsProcess, null);
		boolean muted = false;
		if (vertex1IsProcess) {
			muted = ((ProcessVertex)graph.getValue(vertex1)).getProcess().isCopier();
		}
		return addProcessPorts(graph, process,  bundle, -1, this, new CllTermPath(), optional, buffer, muted, false, vertex1, vertex1IsProcess, "", vertex2);
	}
	
	private Object addConnections(
			ProcessGraph graph, CProcess process, String bundle, 
			CllTerm root, CllTermPath initialPath, 
			boolean optional, boolean clickableNodes,
			Object vertex, boolean vertexIsProcess, String previousType,
			Object targetBottom, Object nullTargetVertex, 
			ComposeProvenance targets, Map<String, Object> selectedTargets) throws NotFoundException { //, InvalidProvenancePathException {

		ComposeProvenance targetProv = initialPath.follow(targets);
		boolean buffer = targetProv.isBuffered();
		
		PortEdge edge = new PortEdge(this, root, initialPath, buffer, optional);
		Optional<String> targetChannel = targetProv.getSingleSource(); 
		Log.d("Connecting path [" + initialPath + "] - type [" + type + "] - name [" + name + "] - targets: [" + targetProv.toString() + "].");
		
		
		if (targetChannel.isPresent()) { // Single target
			Object target;
			String tChannel = targetChannel.get();
			if (tChannel.isEmpty() || tChannel.equals(CProcess.nameOfMerge(process.getName()))) {
				// This happens in the special cases of WITH where things get connected even though there is no input
				target = nullTargetVertex;
				Log.d("Connecting to null target vertex: " + graph.getValue(nullTargetVertex));
			}
			else if (tChannel.equals(ComposeProvenance.BUFFER_SOURCE)) { // Buffered input
				target = targetBottom;
				Log.d("Buffering to target vertex: " + graph.getValue(targetBottom));
			}
			else { // otherwise, input should be channel:identifier
				String[] parts = tChannel.split(ComposeProvenance.IDENTIFIER_SEPARATOR);
				if (parts.length < 2) {
					Log.w("Invalid input provenance received from prover [" + tChannel + "] - no identifier");
					try { // try to find the owner of the channel anyway
						target = graph.getProcessVertexFromInputChannel(targetBottom, tChannel);
						Log.d("Connecting to target vertex: " + graph.getValue(target));					
					} catch (NotFoundException e) {
						target = nullTargetVertex;
						Log.w("Failed to find target channel [" + tChannel + "]");
					}
				} else {
					String channel = parts[0];
					// When more than one inputs have the same channel name, we use a different identifier for each input.
					// The prover reports the same identifier if the same input was used for different connections, or a different identifier otherwise
					if (selectedTargets.containsKey(tChannel)) { // We have already encountered this identifier, so we will resuse the same input
						target = selectedTargets.get(tChannel);
						Log.d("Connecting to pre-selected target vertex (" + tChannel +"): " + graph.getValue(target));					
					} else { // This is the first time we encounter this identifier so we will use a new input with that channel name
						target = graph.removeInputChannelFromProcessBundle(targetBottom, channel); // remove the input so we never re-use it again
						selectedTargets.put(tChannel, target);
						Log.d("Connecting to target vertex (" + tChannel +"): " + graph.getValue(target));					
					}
				}
			}
			return this.joinVertices(graph, process, bundle, optional, buffer, vertex, vertexIsProcess, target, true);
		}

		if (type.equals("var")) {
			Log.w("Found multiple targets for single resource [" + name + "] - Targets: " + targetProv.getSources());
			return this.joinVertices(graph, process, bundle, optional, buffer, vertex, vertexIsProcess, nullTargetVertex, true);
		}

//			if (initialPath.isLeaf(targets)) {
//				String tChannel = targetChannel.get();
//				if (!tChannel.isEmpty() && !tChannel.equals(ComposeProvenance.BUFFER_SOURCE)) {
//					Log.d("Removing input channel [" + tChannel + "] while connecting path [" + initialPath + "].");
//					graph.removeInputChannelFromProcess(target, targetChannel.get());
//				}
//			}
		
		if (type.equals("times")) {	
			// Fork unless we are connecting directly to the process.

			// e.g. for A**B, the A and B will be attached directly to the
			// service vertex instead of a being attached to a new vertex.
			// e.g. for A**(B**C), the A, B and C will be attached to the
			// same node instead of a branch being created for B and C. <-- this is taken care of by HOL Light now
			// Update: Restored this functionality even though HOL Light takes care of it to an extent.
			// In a lot of situations HOL Light produces left associative outputs such as (A ** B) ** C (see Cristina's example 28/06/2016) 
			if (buffer || (!vertexIsProcess && !"times".equals(previousType))) {
				PortVertex portVertex = new PortVertex(process,edge,bundle,clickableNodes);
				vertex = graph.addMessagePortVertex(portVertex, vertex);
				vertexIsProcess = false;
			}
			optional = false;
		} else if (type.equals("plus")) {
			// Fork unless we are connecting directly to the process.

			// We only avoid forking for outputs because we need extra
			// logic to make sure input rendering is unambiguous.
			if (buffer || (!vertexIsProcess && !"plus".equals(previousType))) {
				PortVertex portVertex = new PortVertex(process,edge,bundle,clickableNodes);
				vertex = graph.addMessagePortVertex(portVertex, vertex); 
				vertexIsProcess = false;
			}
			optional = true;

		} else if (type.equals("neg")) {
			// We ignore this and move straight to the argument. Under normal circumstances we should never encounter this.
			//TODO this ends up happening at JoinEdit because HOL Light reports connections as negated terms (inputs)
			// input = !input; // We flip the input parameter. This should never happen though.
		} else
			throw new RuntimeException("Failed to parse type: " + type);

//		Object target = null;
//		boolean leaf = initialPath.isLeaf(targets) && !targetChannel.get().isEmpty() && !targetChannel.get().equals(ComposeProvenance.BUFFER_SOURCE);

		Object result = targetBottom;
		int argcount = 0;
		for (CllTerm c : args) {
			CllTermPath path = initialPath.clone();
			path.add(argcount);
			result = c.addConnections(graph, process, bundle, root, path, optional, clickableNodes, vertex, vertexIsProcess, type, targetBottom, nullTargetVertex, targets, selectedTargets);
//			if (leaf && target != null) { //avoid search 
//				c.addProcessPorts(graph, bundle, channel, root, path, false, optional, false, clickableNodes, vertex, false, type, target);
//			} else {
//				target = c.addConnections(graph, bundle, channel, root, path, optional, clickableNodes, vertex, false, type, targetBottom, nullTargetVertex, targets);
//			}
			argcount++;
		}
//		if (leaf) {
//			Log.d("Removing input channel [" + targetChannel.get() + "] while connecting path [" + initialPath + "].");
//			graph.removeInputChannelFromProcess(target, targetChannel.get());
//		}
		return result;
	}
	
//	private Object addConnections(
//			ProcessGraph graph, String bundle,
//			CllTermPath initialPath, Prover prover,
//			boolean clickableNodes, String previousType,
//			ComposeProvenance sources,
//			Object targetBottom, Object nullTargetVertex, 
//			ComposeProvenance targets) throws NotFoundException { //, InvalidProvenancePathException {
//
//		ComposeProvenance sourceProv = initialPath.follow(sources);
//		Optional<String> source = sourceProv.getSingleSource(); 
//		
//		Optional<String> targetChannel = initialPath.follow(targets).getSingleSource(); 
//		boolean leaf = initialPath.isLeaf(targets) && !targetChannel.get().isEmpty() && !targetChannel.get().equals(ComposeProvenance.BUFFER_SOURCE);
//		Object target = null;
//		
//		if (source.isPresent()) {
//			Log.d("Connecting term [" + prover.cllResourceString(this) + "](" + initialPath + ") - source: [" + source.get() + "] - targets: [" + initialPath.follow(targets).getSources() + "].");
//			target = addConnections(graph, bundle, null, initialPath.follow(this), new CllTermPath(), previousType.equals("plus"), clickableNodes, sourceVertex, true, "", targetBottom, nullTargetVertex, initialPath.follow(targets));
//		} else {
//			int argcount = 0;
//			
//			for (CllTerm c : args) {
//				CllTermPath path = initialPath.clone();
//				path.add(argcount);
//				target = c.addConnections(graph,bundle,path,prover,clickableNodes,type,sources,targetBottom,nullTargetVertex,targets);
//				argcount++;
//			}
//		}
//		if (leaf) {
//			Log.d("Removing input channel [" + targetChannel.get() + "] while connecting path [" + initialPath + "].");
//			graph.removeInputChannelFromProcess(target, targetChannel.get());
//		}
//		return target;
//	}
	
	public void joinGraphsFromSingleSource(
			ProcessGraph graph, CProcess process, String bundle, 
			boolean optional, Object vertex, boolean vertexIsProcess, 
			Object targetBottom, Object nullTargetVertex, ComposeProvenance targets 
			) throws NotFoundException, InvalidProvenancePathException {
		addConnections(graph, process, bundle, this, new CllTermPath(), optional, false, vertex, vertexIsProcess, "", targetBottom, nullTargetVertex, targets, new HashMap<String,Object>());
	}

	public void joinGraphs(
			ProcessGraph graph, CProcess process, String bundle, 
			boolean optional, Object vertex, boolean vertexIsProcess, 
			Object targetBottom, Object nullTargetVertex, 
			ComposeProvenance targets, Map<String,Object> selectedTargets
			) throws NotFoundException, InvalidProvenancePathException {
		addConnections(graph, process, bundle, this, new CllTermPath(), optional, false, vertex, vertexIsProcess, "", targetBottom, nullTargetVertex, targets, selectedTargets);
	}
	
	public String getType() { return type; }

	public String getName() { return name; }

	public void setName(String name) { this.name = name; }

	public Vector<CllTerm> getArgs() { return args; }

	@Override 
	public boolean equals(Object o) {
		if (!(o instanceof CllTerm)) return false;
		CllTerm other = (CllTerm) o;
		return (other.getType().equalsIgnoreCase(type)
				&& other.getName().equalsIgnoreCase(name)
				&& args.equals(other.getArgs()));
	}

	public Optional<CllTermPath> hasSubterm(CllTerm tm) {
		if (type.equals(tm.getType()) && name.equals(tm.getName()) && args.containsAll(tm.getArgs()))
			return Optional.of(new CllTermPath());
		for (int i = 0 ; i < args.size() ; i++) {
			CllTerm arg = args.elementAt(i);
			Optional<CllTermPath> res = arg.hasSubterm(tm);
			if (res.isPresent()) {
				CllTermPath respath = res.get();
				respath.push(i);
				return Optional.of(respath);
			}
		}
		return Optional.empty();
	}

	public CllTerm bfs(CllTermVisitor visitor) {
		LinkedList<CllTerm> frontier = new LinkedList<CllTerm>();
		LinkedList<CllTermPath> pathFrontier = new LinkedList<CllTermPath>(); 
		frontier.add(this);
		pathFrontier.add(new CllTermPath());
		while (!frontier.isEmpty()) {
			CllTerm head = frontier.pop();
			CllTermPath path = pathFrontier.pop();
			if (visitor.visit(head,path)) return head;
			for (int i = 0 ; i < args.size() ; i++) {
				frontier.add(args.elementAt(i));
				CllTermPath childpath = path.clone();
				childpath.add(i);
				pathFrontier.add(childpath);
			}
		}
		return this;
	}

	public Collection<CllTerm> bfsAll(CllTermVisitor visitor) {
		return bfsAll(visitor,false);
	}
	
	public Collection<CllTerm> bfsAll(CllTermVisitor visitor,boolean prune) {
		Vector<CllTerm> results = new Vector<CllTerm>();

		LinkedList<CllTerm> frontier = new LinkedList<CllTerm>();
		LinkedList<CllTermPath> pathFrontier = new LinkedList<CllTermPath>(); 
		frontier.add(this);
		pathFrontier.add(new CllTermPath());
		while (!frontier.isEmpty()) {
			CllTerm head = frontier.pop();
			CllTermPath path = pathFrontier.pop();
			if (visitor.visit(head,path)) {
				results.add(head);
				if (prune) continue;
			}
			for (int i = 0 ; i < head.getArgs().size() ; i++) {
				frontier.add(head.getArgs().elementAt(i));
				CllTermPath childpath = path.clone();
				childpath.add(i);
				pathFrontier.add(childpath);
			}
		}
		return results;
	}

	// Not working and probably not needed anyway.
	//	public CllTerm removeSubterm(CllTerm tm) {
	//		if (this.equals(tm)) return null;
	//		
	//		Vector<CllTerm> newargs = new Vector<CllTerm>();
	//		for (CllTerm arg: args) {
	//			if (!arg.equals(tm))
	//				newargs.add(new CllTerm(arg));
	//		}
	//		Vector<CllTerm> newargs2 = new Vector<CllTerm>();
	//		for (CllTerm arg : newargs) {
	//			CllTerm newarg = arg.removeSubterm(tm);
	//			if (newarg != null) newargs2.add(newarg);
	//		}
	//		
	//		CllTerm result = new CllTerm(type, name, newargs2);
	//		result.flatten();
	//		return result;
	//	}

	private CllTerm merge(CllTerm c, String type) {
		Vector<CllTerm> args = new Vector<CllTerm>();
		if (this.type.equals(type)) {
			args = this.args;
		} else {
			args.add(this);
		}
		if (c.getType().equals(type)) {
			args.addAll(c.getArgs());
		} else {
			args.add(c);
		}
		return new CllTerm(type,"",args);
	}

	public CllTerm tensor(CllTerm c) {
		return merge(c,"times");
	}

	public CllTerm times(CllTerm c) {
		return this.tensor(c);
	}

	public CllTerm plus(CllTerm c) {
		return merge(c,"plus");
	}

	public CllTerm merge(CllTerm c) { 
		return merge(c,type); 
	}

	public void expand(CllTerm c) {
		//TODO check if atomic or unary?
		this.args.add(c);
	}

	public CllTerm neg() {
		Vector<CllTerm> args = new Vector<CllTerm>();
		args.add(this);
		return new CllTerm("neg","",args);
	}

	public CllTerm unneg() {
		if (this.type.equals("neg"))
			return args.get(0);
		else 
			return this;
	}
	
	public void copy(CllTerm c) {
		this.name = c.getName();
		this.type = c.getType();
		this.args = c.getArgs();
	}

	public void setAt(CllTermPath path, CllTerm term) throws InvalidCllPathException, NotFoundException {
		if (path.isRoot()) copy(term);
		else {
			CllTerm immediateParent;
			immediateParent = path.followParent(this);
			immediateParent.getArgs().set(path.lastIndex(),term);
		}
	}

	public void deletePath(CllTermPath path) throws InvalidCllPathException, NotFoundException {
		if (path.isRoot()) return;

		CllTerm immediateParent = path.followParent(this);
		immediateParent.getArgs().remove(path.lastIndex());
	}

	// TODO
	//	public void expandAt(CllTermPath path, CllTerm c) {
	//		//TODO check if atomic or unary?
	//		CllTerm immediateParent = path.followParent(this);
	//		immediateParent.getArgs().set(path.lastIndex(),term);
	//	}

	// WARNING: This breaks semantics of verified processes!
	public void flatten() {
		Vector<CllTerm> res = new Vector<CllTerm>();
		for (CllTerm tm : this.args) {
			tm.flatten();
			if (tm.getType().equals(type)) {
				res.addAll(tm.getArgs());
			} else {
				res.add(tm);
			}
		}
		this.args = res;
		if (args.size() == 1 && (this.type.equals("times") || this.type.equals("plus"))) {
			this.copy(args.firstElement());
		}
	}
}