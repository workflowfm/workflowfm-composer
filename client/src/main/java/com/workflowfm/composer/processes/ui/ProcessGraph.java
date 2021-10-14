package com.workflowfm.composer.processes.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.graph.CellVisitor;
import com.workflowfm.composer.graph.ComposableCell;
import com.workflowfm.composer.graph.GraphEngine;
import com.workflowfm.composer.graph.JGraph;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.utils.Log;

/**
 * This class extends the mxGraph class to provide functions for constructing
 * graphs that represents processes. It also provides helper functions for
 * traversing and manipulating mxGraphs more easily.
 */
public class ProcessGraph implements Serializable {
	private static final long serialVersionUID = -1008249073606669346L;
	
	private GraphEngine engine;
	
	private Object selectedCell = null;
	private transient CopyOnWriteArrayList<ProcessGraphSelectionListener> selectionListeners = new CopyOnWriteArrayList<ProcessGraphSelectionListener>();



	public ProcessGraph() {
		this.engine = new JGraph();
	}

	public ProcessGraph(GraphEngine g) {
		this.engine = g;
	}

	public GraphEngine getGraphEngine() { return this.engine; }

	public void insertGraph (ProcessGraph graph) {
		engine.insertCells(graph.getGraphEngine().cloneAllCells());
	}
	
	public Object getSelectedCell() {
		return selectedCell;
	}

	public void setSelectedCell(Object cell) {
		this.selectedCell = cell;
		if (cell == null) 
			for (ProcessGraphSelectionListener l : selectionListeners)
				l.deselected();
		else {
			Object value = getValue(cell);
			
			if (!(value instanceof ComposableCell))
				for (ProcessGraphSelectionListener l : selectionListeners)
					l.selectedUnknown(cell);
			else if (value instanceof PortEdge)
				for (ProcessGraphSelectionListener l : selectionListeners)
					l.selected((PortEdge)value);
			else if (value instanceof PortVertex)
				for (ProcessGraphSelectionListener l : selectionListeners)
					l.selected((PortVertex)value);
			else if (value instanceof ProcessVertex)
				for (ProcessGraphSelectionListener l : selectionListeners)
					l.selected((ProcessVertex)value);
			else
				for (ProcessGraphSelectionListener l : selectionListeners)
					l.selected((ComposableCell)value);
		}
	}

	public void addSelectionListener(ProcessGraphSelectionListener listener) {
		this.selectionListeners.add(listener);
	}
	
	public void removeSelectionListener(ProcessGraphSelectionListener listener) {
		this.selectionListeners.remove(listener);
	}
	
	
	public Object createProcessVertex(CProcess process) {

		ProcessVertex v = new ProcessVertex(process,process.getName());
		Object c;
		if (process.isCopier()) {
			c = engine.createRoundVertex(v,
					ComposerProperties.processCopierNodeRadius(),ComposerProperties.processColour(true));
		} else {
			c = engine.createRectangleVertex(v,
					ComposerProperties.processNodeWidth(),
					ComposerProperties.processNodeHeight(),
					ComposerProperties.processColour(!process.isComposite()));
			// if (ComposerProperties.processNodeAutoResize())
			// engine.autoResizeCell(c, v.toString(),
			// ComposerProperties.processNodeHeight());
		}

		Log.d("Adding process node with name "
				+ process.getName() + " and label " + v.toString());
		return c;
	}
	
	public Object createJoinVertex(CProcess process,String bundle) {

		ProcessVertex v = new ProcessVertex(process,bundle,ProcessVertex.TERMINATOR_VERTEX);
		Object c = engine.createTriangleVertex(v,
				ComposerProperties.processNodeHeight(),ComposerProperties.processColour(true));

		Log.d("Adding join node with name "
				+ process.getName() + " and label " + v.toString());
		return c;
	}
	
	public Object createMergeVertex(CProcess process, String bundle) {

		ProcessVertex v = new ProcessVertex(process.getName(),bundle,process.getOutput());
		Object c = engine.createRhombusVertex(v,
				ComposerProperties.processNodeHeight(),ComposerProperties.processColour(true));

		Log.d("Adding merge node with name "
				+ CProcess.nameOfMerge(process.getName()) + " and label " + v.toString());
		return c;
	}

	/**
	 * Adds a vertex that acts as a box the user can click on to select process
	 * messages.
	 */
	public Object addMessagePortVertex(PortVertex v, Object vertex) {
		Object portVertex = engine.createPortVertex(v, v.isClickable(), ComposerProperties.portEdgeColour());
		addMessagePortEdge(v.isInput(), v.getEdge(), portVertex, vertex);
		return portVertex;
	}

	/** Adds an edge that represents a message. */
	public Object addMessagePortEdge(boolean input, PortEdge m,
			Object portVertex, Object vertex) {
		Object v1 = input ? portVertex : vertex;
		Object v2 = input ? vertex : portVertex;
		addEdge(m, v1, v2);
		return portVertex;
	}

	public Object addEdge(PortEdge m, Object from, Object to) {	
		// only show an arrow if the edge is not pointing to a port vertex (ie. points to a process vertex) 
		// or if the port vertex is clickable
		boolean arrow = !(getValue(to) instanceof PortVertex) || ((PortVertex)getValue(to)).isClickable();
		if (m == null)
			return engine.insertEdge(m, from, to);
		return engine.insertEdge(m, from, to, arrow, m.isBuffer(), m.isOptional());
	}

	public void rerouteEdgeSource(Object edge, Object newSource) {
		PortEdge m = getMessage(edge); // (PortEdge)engine.valueOfCell(edge);
		addEdge(m, newSource, engine.getTarget(edge));
		engine.deleteCells(new Object[] { edge });
	}

	public void rerouteEdgeTarget(Object edge, Object newTarget) {
		PortEdge m = getMessage(edge); // (PortEdge)engine.valueOfCell(edge);
		addEdge(m, engine.getSource(edge), newTarget);
		engine.deleteCells(new Object[] { edge });
	}
	
	public void rerouteEdgeTargetAsBuffer(Object edge, Object newTarget) {
		PortEdge m = new PortEdge(getMessage(edge),true); // (PortEdge)engine.valueOfCell(edge);
		addEdge(m, engine.getSource(edge), newTarget);
		engine.deleteCells(new Object[] { edge });
	}
	
	public void removeEdges(Object processVertex, boolean removeInputs) {
		Stack<Object> toDelete = new Stack<Object>();
		Object[] edges = removeInputs ? engine.getIncomingEdges(processVertex)
				: engine.getOutgoingEdges(processVertex);
		toDelete.addAll(Arrays.asList(edges));
		while (!toDelete.isEmpty()) {
			Object edge = toDelete.pop();
			Object vertex = engine.getTerminal(edge, removeInputs);

			Object[] edges2 = removeInputs ? engine.getIncomingEdges(vertex)
					: engine.getOutgoingEdges(vertex);
			toDelete.addAll(Arrays.asList(edges2));

			engine.deleteCells(new Object[] { edge, vertex });
		}
	}
	
	public void removeInputStartingFrom(Object startingEdge) {
		Collection<Object> toRemove = new LinkedList<Object>();
		toRemove.add(startingEdge);
		LinkedList<Object> vertices = new LinkedList<Object>();
		vertices.add(engine.getSource(startingEdge));
		
		while (!vertices.isEmpty()) {
			Object v = vertices.pop();
			if (engine.valueOfCell(v) instanceof ProcessVertex)
				continue;
			
			toRemove.add(v);
			Object[] edges =  engine.getIncomingEdges(v);
			for (Object e : edges) {
				toRemove.add(e);
				vertices.add(engine.getSource(e));
			}
		}
		engine.deleteCells(toRemove.toArray());
	}

	/**
	 * Removes all graph elements from a process that represent a specified
	 * input channel.
	 */
	public Object removeInputChannelFromProcessBundle(Object processVertex,
			String channelToFind) throws NotFoundException {
		Object vertex = getProcessVertexFromInputChannel(processVertex,channelToFind);
		removeInputChannelFromProcess(vertex, channelToFind);
		return vertex;
		
//		for (Object vertex : engine.getConnectedVertices(processVertex)) {
//			if (!(isInputVarVertex(vertex))) continue;
//			
//			Object val = engine.valueOfCell(vertex);
//			if (!((PortVertex)val).getChannel().equals(channelToFind)) continue;
//			
//			Object[] outgoingEdges = engine.getOutgoingEdges(vertex);
//
//			if (outgoingEdges.length == 0)
//				continue;
//
//			Object edge = outgoingEdges[0];
//			
//			Object sv = engine.getTarget(edge);
//			if (isProcessVertex(sv)) {
//				removeInputStartingFrom(edge);
//				return sv;
//			}
//		}
//		throw new NotFoundException("process vertex with channel", channelToFind, "graph bundle");
	}

	public void removeInputChannelFromProcess(Object processVertex,
			String channelToFind) throws NotFoundException {
		for (Object edge : engine.getIncomingEdges(processVertex)) {
			Object vertex = getConnectionSource(edge);
			if (!(isInputVarVertex(vertex))) continue;
			
			Object val = engine.valueOfCell(vertex);
			if (!((PortVertex)val).getChannel().equals(channelToFind)) continue;
			
			removeInputStartingFrom(edge);
			return;
		}
		throw new NotFoundException("process vertex with channel", channelToFind, "graph bundle");
	}
	
	public void removeConnectionStartingFrom(Object startingEdge) {
		Collection<Object> toRemove = new LinkedList<Object>();
		toRemove.add(startingEdge);
		LinkedList<Object> vertices = new LinkedList<Object>();
		vertices.add(engine.getTarget(startingEdge));
		
		while (!vertices.isEmpty()) {
			Object v = vertices.pop();
			if (engine.valueOfCell(v) instanceof ProcessVertex)
				continue;
			
			toRemove.add(v);
			Object[] edges =  engine.getOutgoingEdges(v);
			for (Object e : edges) {
				toRemove.add(e);
				vertices.add(engine.getTarget(e));
			}
		}
		engine.deleteCells(toRemove.toArray());
	}
	
	public void removeEntireConnection(Object edge) {
		Object e = edge;
		while (true) {
			Object v = engine.getSource(e);
			if (engine.valueOfCell(v) instanceof ProcessVertex) {
				removeConnectionStartingFrom(e);
				return;
			}
			Object[] incoming = engine.getIncomingEdges(v);
			if (incoming.length == 0) return;
			e = incoming[0];
		}
	}
	
	public void removeConnectionForTerm(Object edge,CllTerm term) {
		Object e = edge;
		while (true) {
			Object value = engine.valueOfCell(e);
			if (value instanceof PortEdge && ((PortEdge)value).getTerm().equals(term)) {
				removeConnectionStartingFrom(e);
				return;
			}
			Object v = engine.getSource(e);
			if (engine.valueOfCell(v) instanceof ProcessVertex) {
				Log.w("Removing entire connection because we failed to find given subterm.");
				removeConnectionStartingFrom(e);
				return;
			}
			Object[] incoming = engine.getIncomingEdges(v);
			if (incoming.length == 0) return;
			e = incoming[0];
		}
	}
	
	public Object getConnectionSource(Object edge) throws NotFoundException {
		Object e = edge;
		while (true) {
			Object v = engine.getSource(e);
			if (engine.valueOfCell(v) instanceof ProcessVertex) {
				return v;
			}
			Object[] incoming = engine.getIncomingEdges(v);
			if (incoming.length == 0) return v; //throw new NotFoundException("process vertex of connection source");
			e = incoming[0];
		}
	}
	
	public Object getConnectionTarget(Object edge) throws NotFoundException {
		Object e = edge;
		while (true) {
			Object v = engine.getTarget(e);
			if (engine.valueOfCell(v) instanceof ProcessVertex) {
				return v;
			}
			Object[] outgoing = engine.getOutgoingEdges(v);
			if (outgoing.length == 0) return v; //throw new NotFoundException("process vertex of connection target");
			e = outgoing[0];
		}
	}
	
	public Collection<Object> getOutputsOfProcessBundle(Object processVertex) {
		Collection<Object> vars = new HashSet<Object>();
		for (Object vertex : engine.getConnectedVertices(processVertex)) {
			if (isOutputVarVertex(vertex))
				vars.add(engine.getIncomingEdges(vertex)[0]);
		}

		return vars;
	}

	public Collection<Object> getInputEdgesOfProcessBundle(Object processVertex) {
		Collection<Object> vars = new HashSet<Object>();
		for (Object vertex : engine.getConnectedVertices(processVertex)) {
			if (isInputVarVertex(vertex))
				vars.add(engine.getOutgoingEdges(vertex)[0]);
		}

		return vars;
	}
	
	public Collection<Object> getInputsOfProcessBundle(Object processVertex) {
		Collection<Object> vs = new HashSet<Object>();
		for (Object vertex : engine.getConnectedVertices(processVertex)) {
			if (isInputVarVertex(vertex))
				vs.add(vertex);
		}

		return vs;
	}

	public Object getProcessVertexFromInputChannel(Object processVertex,
			String channelToFind) throws NotFoundException {
		String bundle;
		Object value = engine.valueOfCell(processVertex);
		if (value != null && value instanceof ComposableCell)
			bundle = ((ComposableCell)value).getBundle();
		else 
			throw new NotFoundException("bundle for process vertex", value==null?"(null)":value.toString());
		
		Collection<Object> vertices = engine.getConnectedVertices(processVertex);

		for (Object vertex : vertices) {
			if (!(isInputVarVertex(vertex))) continue;
			
			Object val = engine.valueOfCell(vertex);
			if (!((PortVertex)val).getChannel().equals(channelToFind)) continue;
			if (!((PortVertex)val).getBundle().equals(bundle)) continue;
			
			Object[] outgoingEdges = engine.getOutgoingEdges(vertex);

			if (outgoingEdges.length == 0) continue;

			Object result = getConnectionTarget(outgoingEdges[0]);
			if (getValue(result) instanceof ProcessVertex)
				return result;
		}

		throw new NotFoundException("process with input channel", channelToFind, "bundle " + bundle);
	}

//	public Object getProcessVertexFromOutputChannelBROKEN(Object processVertex,
//			String channelToFind) throws NotFoundException {
//		Object found = null;
//		String bundle = engine.valueOfCell(processVertex) == null ? "(null)"
//				: engine.valueOfCell(processVertex).toString();
//		Collection<Object> vertices = getOutputsOfProcessBundle(processVertex);
//
//		if (vertices.size() == 0)
//			throw new NotFoundException("process with output channel", channelToFind, "bundle " + bundle);
//
//		for (Object vertex : vertices) {
//			Object[] edges = engine.getIncomingEdges(vertex);
//
//			if (edges.length == 0) {
//				Log.w("Output PortVertex " + engine.valueOfCell(vertex)
//						+ " has no incoming edge!");
//				continue;
//			}
//
//			Object edge = edges[0];
//			PortEdge m = getMessage(edge);
//			if (m == null)
//				continue;
//
//			if (m.channelEquals(channelToFind)) {
//				Object sv = engine.getSource(edge);
//
//				if (isProcessVertex(sv))
//					found = sv;
//			}
//		}
//
//		if (found != null)
//			return found;
//
//		throw new NotFoundException("process with output channel", channelToFind, "bundle " + bundle);
//	}

	public void removeAllOutputsFromProcessBundle(Object processVertex) {
		removeEdges(processVertex, false);
	}

	public void removeAllInputs(Object processVertex) {
		removeEdges(processVertex, true);
	}

	/**
	 * Given a vertex from a subgraph, this returns the first vertex found from
	 * this subgraph that has a specified value.
	 */
	public Object getConnectedProcessVertex(Object startingVertex, final String name) throws NotFoundException {
		CellVisitor<Object> visitor = new CellVisitor<Object>(null) {
			@Override
			public boolean visit(Object vertex, Object edge) {
				Object oValue = engine.valueOfCell(vertex);
				if (!(oValue instanceof ProcessVertex)) return true;
				ProcessVertex pValue = (ProcessVertex)oValue;
				if (pValue.getProcess().getName().equals(name)) {
					result = vertex;
					return false;
				}
				return true;
			}
		};
		engine.traverseGraph(startingVertex, false, visitor);
		if (visitor.getResult() == null)
			throw new NotFoundException("connected process vertex for process", name);
		return visitor.getResult();
	}

	public Object getFirstProcessFoundFromTraversal(Object startingVertex) {
		CellVisitor<Object> visitor = new CellVisitor<Object>(null) {
			@Override
			public boolean visit(Object vertex, Object edge) {
				if (isProcessVertex(vertex)) {
					result = vertex;
					return false;
				}
				return true;
			}
		};
		engine.traverseGraph(startingVertex, false, visitor);
		return visitor.getResult();
	}

	/** Returns the vertex that represents a process of a specified label. */
	public Object getProcessVertexByLabel(final String label)
			throws NotFoundException {
		CellVisitor<Object> visitor = new CellVisitor<Object>(null) {
			@Override
			public boolean visit(Object vertex, Object edge) {
				Object value = engine.valueOfCell(vertex);

				if (value instanceof ProcessVertex
						&& ((ProcessVertex) value).toString().equals(label))
					if (isProcessVertex(vertex)) {
						result = vertex;
						return false;
					}

				return true;
			}
		};
		engine.searchVertices(visitor);
		Object result = visitor.getResult();

		if (result == null)
			throw new NotFoundException("process vertex with label", label);

		return result;
	}

	/** Returns a vertex that belongs to a specified bundle. */
	public Object getBundleVertexByLabel(final String label)
			throws NotFoundException {
		CellVisitor<Object> visitor = new CellVisitor<Object>(null) {
			@Override
			public boolean visit(Object vertex, Object edge) {
				Object value = engine.valueOfCell(vertex);

				if (value instanceof ComposableCell
						&& ((ComposableCell) value).getBundle().equals(label)) {
					result = vertex;
					return false;
				}

				return true;
			}
		};
		engine.searchVertices(visitor);
		Object result = visitor.getResult();

		if (result == null)
			throw new NotFoundException("bundle", label);

		return result;
	}

	/** Returns a ProcessVertex vertex that belongs to a specified bundle. */
	public Object getBundleProcessVertexByLabel(final String label)
			throws NotFoundException {
		CellVisitor<Object> visitor = new CellVisitor<Object>(null) {
			@Override
			public boolean visit(Object vertex, Object edge) {
				Object value = engine.valueOfCell(vertex);

				if (value instanceof ProcessVertex
						&& ((ProcessVertex) value).getBundle().equals(label)) {
					result = vertex;
					return false;
				}
				return true;
			}
		};
		engine.searchVertices(visitor);
		Object result = visitor.getResult();

		if (result == null)
			throw new NotFoundException("bundle", label);

		return result;
	}

	/** Returns all vertexes that belong to a specified bundle. */
	public Collection<Object> getBundleVerticesByLabel(final String label)
			throws NotFoundException {
		Object vertex = getBundleVertexByLabel(label);
		return engine.getConnectedVertices(vertex);
	}

	/** Returns all cells that belong to a specified bundle. */
	public Collection<Object> getBundleCellsByLabel(final String label)
			throws NotFoundException {
		Object vertex = getBundleVertexByLabel(label);
		return engine.getConnectedCells(vertex);
	}
	
	public boolean bundleExists(final String label) {
		try {
			getBundleVertexByLabel(label);
			return true;
		} catch (NotFoundException e) {
			return false;
		}
	}

	// /** Returns all vertexes that belong to a specified bundle. */
	// public Collection<Object> getAllBundleVertexesByLabel(final String label)
	// {
	// Collection<Object> result = engine.findVertexes(
	// new CellVisitor() {
	//
	// @Override
	// public boolean visit(Object vertex, Object edge) {
	// Object value = engine.valueOfCell(vertex);
	// if (value == null) {
	// System.out.println("NULL value!");
	// } else {
	// System.out.println("Value : " + value.toString() + " - class: " +
	// value.getClass().toString());
	// }
	// if (value instanceof ComposableVertex) {
	// System.out.println("Bundle: " + ((ComposableVertex) value).getBundle());
	// }
	// if (value instanceof ComposableVertex
	// && ((ComposableVertex) value).getBundle().equals(label))
	// return true;
	//
	// return false;
	// }});
	// if (result.size() == 0)
	// throw new NotFoundException("Bundle label " + label + " not found.");
	//
	// return result;
	// }

	/** Prints the values and IDs of all graph elements. Useful for debugging. */
	// public void printValuesAndIds() {
	// Object[] cells = getChildCells(getDefaultParent());
	// for (Object c : cells) {
	// mxCell cell = (mxCell) c;
	// System.out.println("Value: " + cell.getValue() + ", Id: "
	// + cell.getId());
	// }
	// }

	public void removeUnconnectedOutputsFromBundle(Object bundleMember) {
		Collection<Object> c = engine.getConnectedVertices(bundleMember);
		for (Object o : c) {
			if (isOutputVarVertex(o)) {
				// Remove all nodes between this port and the process it is
				// connected to
				// Note: If we delete a node as it is found during graph
				// traversal, the
				// graph traversal will end so removal is only done as the final
				// step.
				
				CellVisitor<ArrayList<Object>> visitor = new CellVisitor<ArrayList<Object>>(new ArrayList<Object>()) {
					@Override
					public boolean visit(Object vertex, Object edge) {
						if (isProcessVertex(vertex)) return false;
						else if (vertex != null) {
							result.add(vertex);
						}
						else if (edge != null) {
							result.add(edge);
						}
						return true;
					}
				};
				engine.traverseGraph(o, false, visitor);

				ArrayList<Object> toRemove = visitor.getResult();
				toRemove.add(o);
				engine.deleteCells(toRemove.toArray());
			}
		}
	}

	/**
	 * Given a vertex of a subgraph, this finds the process vertex from this
	 * subgraph that the connectable outputs exit from.
	 */
	public Object getBottomMostProcess(Object v2) throws NotFoundException {
		CellVisitor<Object> visitor = new CellVisitor<Object>(null) {
			@Override
			public boolean visit(Object vertex, Object edge) {
				//System.out.println("Vertex:" + vertex + " Value:" + engine.valueOfCell(vertex) + " (" + engine.valueOfCell(vertex).getClass().getName() + ") IsPort:" + isPortVertex(vertex));
				//				if (engine.valueOfCell(vertex) instanceof PortVertex) System.out.println("Message:" + ((PortVertex)engine.valueOfCell(vertex)).getMessage());				
				//				if (!isPortVertex(vertex)) // it is either a processvertex or a group
				//				{
				//					boolean isBottom = true;
				//					for (Object e : engine.getOutgoingEdges(vertex)) {			
				//						Object target = engine.getTarget(e);
				//						if (!isPortVertex(target)) isBottom = false;
				//					}
				//					if (isBottom) {
				//						result[0] = vertex;
				//						return false;
				//					}
				//				}
				if (isProcessVertex(vertex) && ((ProcessVertex)engine.valueOfCell(vertex)).isBottom()) {
					result = vertex;
					return false;
				}
				return true;
			}
		};

		engine.traverseGraph(v2, true, visitor);

		Object result = visitor.getResult();
		if (result == null)
			throw new NotFoundException("bottom most process");

		return result;
	}

	
	public void createProcessGraph(CProcess process) {
		createProcessGraph(process,false);
	}
	
	public void createProcessGraph(CProcess process, boolean clickableNodes) {
		
		Object v = createProcessVertex(process);
		for (ListIterator<ProcessPort> it = process.getInputs().listIterator() ; it.hasNext();) {
			int i = it.nextIndex();
			it.next().addAsInputToVertex(this, process, i, false, clickableNodes, v);
		}
		process.getOutput().addAsOutputToVertex(this, process, false, clickableNodes, v);
	}

	public boolean isProcessVertex(Object o) {
		if (o == null)
			return false;

		Object v = engine.valueOfCell(o);
		return v instanceof ProcessVertex;
	}

	public boolean isPortVertex(Object o) {
		if (o == null)
			return false;

		Object v = engine.valueOfCell(o);
		return v instanceof PortVertex;
	}

	public boolean isInputVarVertex(Object cell) {
		return engine.isVertex(cell) && getValue(cell) instanceof PortVertex //c.getValue() == null
				&& engine.getIncomingEdges(cell).length == 0;
	}

	public boolean isOutputVarVertex(Object cell) {
		return engine.isVertex(cell) && getValue(cell) instanceof PortVertex //c.getValue() == null
				&& engine.getOutgoingEdges(cell).length == 0;
	}

	public boolean isVarVertex(Object cell) {
		return isInputVarVertex(cell) || isOutputVarVertex(cell);
	}
	
	public boolean isOutputPortVertex(Object o) {
		CellVisitor<Boolean> visitor = new CellVisitor<Boolean>(true) {		
			@Override
			public boolean visit(Object vertex, Object edge) {
				if (isOutputVarVertex(vertex)) {
					result = true;
					return false;
				}
				if (isProcessVertex(vertex)) {
					result = false;
					return false;
				}
				return true;				
			}
		};
		engine.traverseGraph(o, true, visitor);
		return visitor.getResult().booleanValue();	
	}
	
	
	// public ProcessVertex getCellAsProcessVertex(Object o) {
	// if (isProcessVertex(o)) {
	// Object v = engine.valueOfCell(o);
	// return (ProcessVertex) v;
	// }
	//
	// return null;
	// }

	/**
	 * Removes all atomic process vertices that have not been connected to any
	 * other process.
	 */
	public Collection<Object> removeAtomicBundles() {
		/*
		 * Removes processes that are not part of a different bundle.
		 */ 
		 final Set<Object> toRemove = new HashSet<Object>(); 
		 for (Object v : engine.getAllVertices()) { 
			 Object value = getValue(v);
			 if (value instanceof ProcessVertex) {
				 ProcessVertex vertex = (ProcessVertex)value;
				 if (vertex.getBundle().equals(vertex.getProcess().getName()))
					 toRemove.addAll(engine.getConnectedVertices(v)); 
			 }
		 } 
		 // Cells need to be removed at the end or it interferes with the detection process
		 if (toRemove.size() > 0) {
			 engine.deleteCells(toRemove.toArray());
		 }
		 return toRemove;
	}

	/*
	 * public void removeEverythingButSingleBundle(String bundleName) {
	 * Set<Object> toRemove = new HashSet<Object>();
	 * toRemove.addAll(Arrays.asList(getAllVertices())); Collection<mxCell> c =
	 * getConnectedVertices(getProcessVertexById(bundleName));
	 * toRemove.removeAll(c); graph.removeCells(toRemove.toArray()); }
	 */

	public PortEdge getMessage(Object cell) {
		Object v = engine.valueOfCell(cell);

		if (v instanceof PortEdge)
			return (PortEdge) v;

		return null;
	}

	public void updateBundle(String oldBundle, String newBundle) throws NotFoundException {
		updateBundle(getBundleVerticesByLabel(oldBundle), newBundle);
	}

	public void updateBundle(Object vertex, String newBundle) throws NotFoundException {
		updateBundle(engine.getConnectedVertices(vertex), newBundle);
	}
	
	public void updateBundle(Collection<Object> bundle, String newBundle) {
		for (Object cell : bundle) {
			Object v = engine.valueOfCell(cell);
			if (v instanceof ComposableCell) {
				engine.updateValue(cell,
						((ComposableCell) v).newBundle(newBundle));
			}
		}
	}

	public Object groupProcesses(CProcess process1, CProcess process2, CProcess composition, boolean dashed) throws NotFoundException {
		Collection<Object> c = new ArrayList<Object>();

		Collection<Object> bundle1 = getBundleVerticesByLabel(process1
				.getName());
		updateBundle(bundle1, composition.getName());
		Collection<Object> bundle2 = getBundleVerticesByLabel(process2
				.getName());
		updateBundle(bundle2, composition.getName());

		c.addAll(bundle1);
		c.addAll(bundle2);
		System.out.println("Grouping " + c.size() + " (" + bundle1.size() + "+"
				+ bundle2.size() + ") vertices.");

		// for (Object o : engine.getAllVertices())
		// c.add(o);
		// System.out.println("Grouping " + c.size() + " vertices.");

		Object group = engine.createGroup(c.toArray(), dashed, ComposerProperties.processColour(false));
		engine.updateValue(group, new ProcessVertex(composition,composition.getName()));

		return group;
	}

	public Collection<Object> deleteBundle(String bundleName) throws NotFoundException {
		Collection<Object> bundle = getBundleCellsByLabel(bundleName);
		engine.deleteCells(bundle.toArray());
		return bundle;
	}

	public Collection<Collection<Object>> deleteAllBundles(String bundleName) {
		Vector<Collection<Object>> bundles = new Vector<Collection<Object>>();
		try {
			while (true) {
				bundles.add(deleteBundle(bundleName));
			}
		} catch (NotFoundException e) { }
		return bundles;
	}

	public void layout() {
		if (ComposerProperties.processNodeAutoResize()) {
			Object[] cells = engine.getAllVertices();
			for (int i = 0; i < cells.length; i++) {
				Object v = engine.valueOfCell(cells[i]);
				if (v instanceof ProcessVertex) {
					ProcessVertex p = (ProcessVertex) v;
					if (!p.getProcess().isCopier() && !p.isTerminator() && !p.isMerge()) {
						engine.autoResizeCell(cells[i], p.getProcess()
								.getLabel(), ComposerProperties
								.processNodeHeight());
					}
				}
			}
		}
		engine.layout();
		engine.deHighlightAll();
	}

	public Object getValue(Object cell) {
		return engine.valueOfCell(cell);
	}

	public Object getPortCellOfEdge(Object edge) {
		if (!engine.isEdge(edge)) return null;

		PortEdge m = (PortEdge)getValue(edge);
		if (m == null) return null;
		Object source = engine.getSource(edge);
		Object sourceValue = getValue(source);

		if (sourceValue instanceof PortVertex && ((PortVertex)sourceValue).getTerm().equals(m.getTerm()))
			return source;
		else 
		{
			Object target = engine.getTarget(edge);
			Object targetValue = getValue(target);
			if (targetValue instanceof PortVertex && ((PortVertex)targetValue).getTerm().equals(m.getTerm()))
				return target;
			else
			{
				//Log.d("Unable to find matching PortVertex connected to the selected edge!");
				return null;
			}
		}
	}

	public void highlightCorrespondingPorts(Object selectedCell, Color color) {
		highlightCorrespondingPorts(selectedCell, color, false);
	}
	
	public void highlightCorrespondingPorts(Object selectedCell, Color color, boolean overwrite)
	{		
		//TODO highlight the edge as well??

		if (engine.isEdge(selectedCell))
		{
			selectedCell = getPortCellOfEdge(selectedCell);
			if (selectedCell == null) return;
		}

		boolean input; 

		if (isOutputVarVertex(selectedCell)) {
			input = false;
		} else if (isInputVarVertex(selectedCell)) {
			input = true;
		} else {
			return;
		}

		PortVertex selectedPort = (PortVertex)getValue(selectedCell);
		String bundle = selectedPort.getBundle();
		CllTerm message = selectedPort.getTerm();

		Object[] vertices = engine.getAllVertices();

		engine.deHighlightByColor(color);
		if (overwrite) engine.deHighlightCell(selectedCell);
		engine.highlightCell(selectedCell, color);

		for (Object vertex : vertices) {
			if ((input && isOutputVarVertex(vertex)) || !input
					&& isInputVarVertex(vertex)) {
				PortVertex v = (PortVertex)getValue(vertex);
				CllTerm m = v.getTerm();
				if (m == null) 
					continue;
				if (m.equals(message) && !v.getBundle().equals(bundle)) {
					if (overwrite) engine.deHighlightCell(vertex);
					engine.highlightCell(vertex, color);
				}
			}
		}
	}

	public void exportGraphAsImage(File file) throws IOException
	{
		JComponent component = engine.getControlComponent();
		Container parent = component.getParent();

		Dimension size = component.getSize();
		if (size.width <= 0 || size.height <= 0) {
			size = component.getPreferredSize();
		}

		Log.d("Component width:[" + size.width + "] height:[" + size.height + "].");

		// Only what can be seen in the current viewport will be exported so
		// large graphs will be cropped.
		//mxGraphComponent c = workspace.getGraphComponent();
		//mxRectangle b = workspace.getGraph().getGraphBounds();

		// TODO: Make the image bigger to compensate for the .setTranslate call on
		// the viewport. Shouldn't need this? // (int) Math.round(b.getWidth()) + 1 + 15
		//BufferedImage bi = new BufferedImage(engine.getGraphWidth(), engine.getGraphHeight(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage bi = new BufferedImage(size.width, size.height + 30, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setBackground(Color.white);
		g.clearRect(0, 0, size.width, size.height + 30);
		g.setColor(Color.black);
		g.setFont(new Font( "SansSerif", Font.BOLD, 11 ));
		//Graphics g = bi.getGraphics();

		// Translate so the border isn't visible.
		//g.translate(-2, -2);

		//component.paintAll(g); //contentPane
		SwingUtilities.paintComponent(g, component, component.getParent(), 0, 30, size.width, size.height);

		//g.drawString("Process: " + processName, 5, 5);
		String filename = file.getName();
		if (filename.lastIndexOf(".") != -1)
			filename = filename.substring(0,filename.lastIndexOf("."));
		g.drawString("File: " + filename, 5, 15);

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		g.drawString("Date: " + sdf.format(cal.getTime()), 5, 30);

		g.dispose();

		final String formatName = "png";

		for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
			ImageWriter writer = iw.next();
			ImageWriteParam writeParam = writer.getDefaultWriteParam();
			ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
			IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
			if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
				continue;
			}

			setDPI(metadata,300);

			final ImageOutputStream stream = ImageIO.createImageOutputStream(file);
			try {
				writer.setOutput(stream);
				writer.write(metadata, new IIOImage(bi, null, metadata), writeParam);
			} finally {
				stream.close();
			}
			break;
		}

		//ImageIO.write(bi, "png", file);

		// HACKHACK paintComponent changes the parent because it assumes the component is not in the hierarchy
		parent.add(component); 
	}

	private void setDPI(IIOMetadata metadata, int DPI) throws IIOInvalidTreeException {

		// for PMG, it's dots per millimeter
		double dotsPerMilli = 1.0 * DPI / 0.393700787; // / 10 

		IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
		horiz.setAttribute("value", Double.toString(dotsPerMilli));

		IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
		vert.setAttribute("value", Double.toString(dotsPerMilli));

		IIOMetadataNode dim = new IIOMetadataNode("Dimension");
		dim.appendChild(horiz);
		dim.appendChild(vert);

		IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
		root.appendChild(dim);

		metadata.mergeTree("javax_imageio_1.0", root);
	}
}
