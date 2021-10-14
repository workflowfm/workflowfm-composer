package com.workflowfm.composer.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.undo.UndoableEdit;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellMarker;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.utils.Log;

public class JGraph extends mxGraph implements GraphEngine {

	private mxGraphComponent graphComponent; // this includes scrollpanes by default! 
	private mxGraph graph;
	private mxGraphModel graphModel;
	private mxUndoableEdit previousUndoableGraphEdit;
	
	private HashMap<Object, mxCellMarker> markers = new HashMap<Object, mxCellMarker>();

	public JGraph() {
		graphComponent = new mxGraphComponent(this);
		graphComponent.setTextAntiAlias(true);
		graphComponent.setAntiAlias(true); //false
		graph = getGraphComponent().getGraph();
		//graph.setHtmlLabels(true);
		graphModel = (mxGraphModel) (graph.getModel());
		setAsUneditable();
		graphComponent.getHorizontalScrollBar().setUnitIncrement(16); // TODO put these as properties
		graphComponent.getVerticalScrollBar().setUnitIncrement(16);
		//getGraphComponent().setPreferredSize(new Dimension(1000, 1000));
		// Shift the viewport slightly so initially added nodes aren't cropped
		getView().setTranslate(new mxPoint(15, 15));
		setCollapseToPreferredSize(false); //TODO remove this but re-layout on collapse!
		mxIEventListener undoHandler2 = new mxIEventListener() {
			@Override
			public void invoke(Object source, mxEventObject evt)
			{
				previousUndoableGraphEdit = (mxUndoableEdit) evt.getProperty("edit");
				//System.err.println("Graph edit event occured. (" + (previousUndoableGraphEdit == null?"null":previousUndoableGraphEdit.getChanges().size()) + ")");
				//for (mxUndoableChange change : previousUndoableGraphEdit.getChanges()) {
				//	System.err.println("- " + change.getClass().getCanonicalName());
				//}
			}
		};

		// Adds the command history to the model and view
		graphModel.addListener(mxEvent.UNDO, undoHandler2);
		graph.getView().addListener(mxEvent.UNDO, undoHandler2);
	}	

	public mxGraphComponent getGraphComponent() {
		return graphComponent;
	}

	/**
	 * Set the graph so that the user cannot edit it by e.g. resizing nodes and
	 * adding edges.
	 */

	protected void setAsUneditable() {
		setCellsResizable(false);
		setCellsEditable(false);
		setEdgeLabelsMovable(false);
		setCellsDisconnectable(false);
		setCellsEditable(false);
		setConnectableEdges(false);
		setVertexLabelsMovable(false);
		setKeepEdgesInForeground(true);
		setCellsBendable(false);
		setAllowDanglingEdges(false);
		setCellsCloneable(false);
		setDropEnabled(false);
		setSplitEnabled(false);
		getGraphComponent().setDragEnabled(false);
		getGraphComponent().setConnectable(false);
		getGraphComponent().setCenterPage(true);
		// setAutoSizeCells(true);
	}

	public void startEdit() {
		super.getModel().beginUpdate();
		previousUndoableGraphEdit = null;
	}

	public void endEdit() {
		super.getModel().endUpdate();
	}


	public Object[] getAllVertices() {
		return getAllChildVertices(getDefaultParent()).toArray();
	}

	public Collection<Object> getAllChildVertices (Object parent) {
		Collection<Object> c = new ArrayList<Object>();
		//if (parent != getDefaultParent())
			c.add(parent);
		for (Object o : getChildVertices(parent)) {
			c.addAll(getAllChildVertices(o));
		}
		return c;
	}
	
	public Object[] getAllEdges() {
		return getChildEdges(getDefaultParent());
	}

	public Object[] getAllCells() {
		return getAllChildCells(getDefaultParent()).toArray();
	}

	public Collection<Object> getAllChildCells (Object parent) {
		Collection<Object> c = new ArrayList<Object>();
		c.add(parent);
		for (Object o : getChildCells(parent,true,true)) {
			c.addAll(getAllChildCells(o));
		}
		return c;
	}

	
	@Override
	public mxGraph getGraph() {
		return this;
	}

	public String[] cellsToValues(Object c[]) {
		String[] v = new String[c.length];
		for (int i = 0; i < c.length; ++i)
			v[i] = (String) (((mxCell) c[i]).getValue());

		return v;
	}

	public void clear() {
		getGraph().removeCells(getGraph().getChildCells(getGraph().getDefaultParent(),true,true));
		//removeCells(getAllVertices()); // This causes a bug when adding new cells afterwards. Perhaps some edges are not being deleted?
		refresh();
		//layout();
	}

	public Object[] cloneConnectedSubgraph(Object vertex) {
		//return super.cloneCells(cells);
		// HACK because cloneCells does not clone edges :/
		JGraph clonegraph = new JGraph();
		clonegraph.addCells(cloneAllCells());

		Set<Object> toRemove = new HashSet<Object>();
		toRemove.addAll(Arrays.asList(clonegraph.getChildVertices(clonegraph.getDefaultParent())));
		Collection<Object> c = clonegraph.getConnectedVertices(vertex); // doesn't work because vertex has been cloned! 
		toRemove.removeAll(c);
		clonegraph.removeCells(toRemove.toArray());
		return clonegraph.cloneAllCells();
	}


	public Object[] cloneCells(Object[] cells) {
		return super.cloneCells(cells);
	}

	public Object[] cloneAllCells() {
		return cloneCells(getChildCells(getDefaultParent()));
	}

	public Object valueOfCell(Object c) {
		return ((mxCell) c).getValue();

	}

	public void autoResizeCell(Object c, String s, int height) {
		FontMetrics metrics = mxUtils.getFontMetrics(mxUtils.getFont(this
				.getCellStyle(c)));
		// System.out.println("STRINGWIDTH: " +
		// metrics.stringWidth(s.toString()));
		this.resizeCell(c, new mxRectangle(240, 150,
				metrics.stringWidth(s) * 1.2 + 20, height));
	}

	public Object createRectangleVertex(Object value, int width, int height, String color) {
		return (mxCell) insertVertex(defaultParent, null, value, 240, 150,
				width, height,
				"rounded=1;fontSize=12;strokeWidth=1.5;fontColor=#000000;fontStyle=" + mxConstants.FONT_BOLD + ";fillColor=" + color + ";whiteSpace=wrap"); //fillColor=#F78400;

	}

	public Object createRoundVertex(Object value, int radius, String color) {
		return (mxCell) insertVertex(defaultParent, null, value, 240, 150,
				radius * 2, radius * 2,
				"shape=ellipse;fontSize=20;strokeWidth=1.2;fontColor=#000000;fontStyle=" + mxConstants.FONT_BOLD + ";fillColor=" + color + ";spacingTop=4"); //fillColor=#F78400;
	}

	public Object createTriangleVertex(Object value, int width, String color) {
		return (mxCell) insertVertex(defaultParent, null, value, 240, 150,
				width * 0.5, width * 1.1,
				"shape=triangle;fontSize=20;strokeWidth=1.5;fontColor=#000000;align=left;spacingTop=3;fontFamily=arial;fillColor=" + color); //fillColor=#F78400;
	}
	
	public Object createRhombusVertex(Object value, int width, String color) {
		return (mxCell) insertVertex(defaultParent, null, value, 240, 150,
				width * 1.0, width * 1.0,
				"shape=rhombus;fontSize=18;strokeWidth=1.2;fontColor=#000000;spacingTop=3;fontFamily=arial;fillColor=" + color); //fillColor=#F78400;
	}
	
	/**
	 * Adds a vertex that acts as a box the user can click on to select process
	 * messages.
	 */
	public Object createPortVertex(Object value, boolean clickable, String color) {
		int size = clickable ? 15 : 0;

		return insertVertex(defaultParent, null, value, 240, 150, size, size,
				"shape=ellipse;strokeWidth=2;fillColor=" + color + ";rounded=0"); //fillColor=#F78400;
	}

	public Object insertEdge(Object value, Object source, Object target) {
		//System.out.println ("Inserting edge: " + value + " from " + source + " to " + target);
		return insertEdge(value,source,target,false,false,false);
	}
	
	public Object insertEdge(Object value, Object source, Object target,
			boolean arrow, boolean buffered, boolean optional) {
		//System.out.println ("Inserting edge: " + value + " from " + source + " to " + target + " buffer:" + buffered + " optional:" + optional);
		String strokeColor = ComposerProperties.edgeColour();
		if (buffered) strokeColor = ComposerProperties.bufferColour();
		
		String style = "fontSize=11;fontColor=black;strokeWidth=2;fontStyle=" + mxConstants.FONT_BOLD + ";strokeColor=" + strokeColor + ";verticalAlign=bottom;";
		
		if (!arrow) style += "endArrow=none;";
		if (optional) style += "dashed=1;";
		
		Object o = super
				.insertEdge(
						defaultParent,
						null,
						value,
						source,
						target,
						style
						);
		//+ arg5);
		return (mxCell) o;
	}

	public Object[] getIncomingEdges(Object vertex) {
		return super.getIncomingEdges(vertex);
	}

	public Object[] getOutgoingEdges(Object vertex) {
		return super.getOutgoingEdges(vertex);
	}

	public Object getTerminal(Object edge, boolean source) {
		return ((mxCell)edge).getTerminal(source);
	}

	public void insertCells(Object[] cells) {
		super.addCells(cells);
	}

	public void deleteCells(Object[] cells) {
		Log.d("Deleting " + cells.length + " cells.");
		graph.removeCells(cells);
	}

	/**
	 * Given a vertex from a subgraph, this returns all the vertices from that
	 * subgraph.
	 */
	public Collection<Object> getConnectedVertices(Object vertex) {
		final Set<Object> connected = new HashSet<Object>();

		graph.traverse(vertex, false, new mxICellVisitor() {
			@Override
			public boolean visit(Object vertex, Object edge) {
				if (vertex != null)
					connected.add((mxCell) vertex);
				return true;
			}
		}, null, null);

		return connected;
	}

	public Collection<Object> getConnectedCells(Object vertex) {
		final Set<Object> connected = new HashSet<Object>();

		graph.traverse(vertex, false, new mxICellVisitor() {
			@Override
			public boolean visit(Object vertex, Object edge) {
				if (vertex != null)
					connected.add((mxCell) vertex);
				if (edge != null)
					connected.add((mxCell) edge);
				return true;
			}
		}, null, null);

		return connected;
	}

//	public Object findConnectedVertex(Object g, Object startingVertex, boolean directed, final CellVisitor v) {
//		mxGraph graph = (mxGraph) g;
//		final mxCell[] result = new mxCell[1];
//
//		graph.traverse(startingVertex, directed, new mxICellVisitor() {
//			@Override
//			public boolean visit(Object vertex, Object edge) {
//				if (v.visit(vertex,edge)) {
//					mxCell c = (mxCell) vertex;
//					result[0] = c;
//					return false;
//				}
//
//				return true;
//			}
//		}, null, null);
//
//		return result[0];
//	}

//	public Object findConnectedVertex(Object startingVertex, boolean directed, final CellVisitor v) {
//		return findConnectedVertex(this,startingVertex,directed,v);
//	}

	public void searchVertices(CellVisitor<?> v) {
		Object[] cells = getChildCells(getDefaultParent());
		for (Object c : cells) {
			if (!v.visit(c, null)) break;
		}
	}

	public void traverseGraph(Object graph, Object startingVertex, boolean directed, final CellVisitor<?> v) {
		traverse(startingVertex, directed, new mxICellVisitor() {
			@Override
			public boolean visit(Object vertex, Object edge) {
				return v.visit(vertex,edge);
			}
		}, null, null);
	}

	public void traverseGraph(Object startingVertex, boolean directed, final CellVisitor<?> v) {
		traverseGraph(this,startingVertex,directed,v);
	}

	public void layout() {
		mxProcessLayout layout = new mxProcessLayout(this);

		// Horizontal layout
		layout.setOrientation(SwingConstants.WEST);

		// Vertical layout
		// layout.setOrientation(SwingConstants.NORTH);

		// Controls vertical spacing of nodes within connected graph?
		layout.setIntraCellSpacing(ComposerProperties.intraCellSpacing());

		// Controls spacing between non-connected graphs
		layout.setInterHierarchySpacing(ComposerProperties.interHierarchySpacing());

		// Controls edge length?
		// layout.setInterRankCellSpacing(70);
		// layout.setInterRankCellSpacing(90);
		layout.setInterRankCellSpacing(ComposerProperties.interRankCellSpacing());

		layout(layout);
	}
	
	protected Set<Object> getCellsWithChildren(Object parent) {
		Set<Object> result = new LinkedHashSet<Object>();
		mxIGraphModel model = graph.getModel();
		
		int childCount = model.getChildCount(parent);
		
		if (childCount > 0) {	
			for (int i = 0; i < childCount; i++)
			{
				Object child = model.getChildAt(parent, i);
				result.addAll(getCellsWithChildren(child));
			}
			result.add(parent);
		}
		return result;
	}

	protected void layout(mxGraphLayout layout) {
		final mxGraph graph = getGraphComponent().getGraph();
		graph.getModel().beginUpdate();

		Set<Object> parents = getCellsWithChildren(graph.getDefaultParent());
		for (Object parent: parents) {
			layout.execute(parent);
		}

//		mxMorphing morph = new mxMorphing(getGraphComponent(), 20, 1.2, 50);
//
//		morph.addListener(mxEvent.DONE, new mxIEventListener() {
//			@Override
//			public void invoke(Object arg0, mxEventObject arg1) {
//				// TODO Auto-generated method stub
//
//			}
//		});

		// Stops edges that join the same nodes from overlapping
		mxParallelEdgeLayout layout2 = new mxParallelEdgeLayout(graph);
//		layout2.execute(graph.getDefaultParent());
		for (Object parent: parents) {
			layout2.execute(parent);
		}
		
		
//		if (true)
			// No animation
			graph.getModel().endUpdate();
//		else
			// Animation TODO: Will break undo/redo
//			morph.startAnimation();

		graph.refresh();

	}

	public void centerViewport(Component parent) {
		graph.getView().setTranslate(new mxPoint(50, 50));

		/*
		 * // TODO: This should work but getGraphBounds sometimes returns huge
		 * bounds that appear wrong Dimension d = parent.getSize(); mxRectangle
		 * b = graph.getGraphBounds(); Log.d("rect " + b); Log.d("dddddd " + d);
		 * double x = -b.getX() - (b.getWidth() - d.getWidth()) / 2; double y =
		 * -b.getY() - (b.getHeight() - d.getHeight()) / 2;
		 * graph.getView().setTranslate(new mxPoint(x, y));
		 */
	}

	public Object createGroup(Object[] cells, String color) {
		return createGroup(cells, false, color);
	}

	public Object createGroup(Object[] cells, boolean dashed, String color) {
		String style = "";
		if (dashed) style = "dashed=1";

		mxCell group = (mxCell) createGroupCell(null);
		group.setStyle("fillColor=" + color + ";strokeWidth=3;" + style);
		groupCells(group, ComposerProperties.groupPadding(), cells);
		
		return group;
	}

	@Override
	public void updateValue(Object cell, Object value) {
		// For some reason you have to set the value via the graph model object
		// for undo/redo of graph edits to work. Undo/redo does not work if you
		// set the value via mxCell.setValue
		graphModel.setValue(cell, value); 	
	}

	@Override
	public Object getSource(Object edge) {
		return model.getTerminal(edge, true);
	}

	@Override
	public Object getTarget(Object edge) {
		return model.getTerminal(edge, false);
	}

	@Override
	public JComponent getControlComponent() {
		return getGraphComponent().getGraphControl();
	}

	@Override
	public Object getCellAt(int x, int y) {
		return getGraphComponent().getCellAt(x,y);
	}

	@Override
	public boolean isEdge(Object cell) {
		return ((mxICell)cell).isEdge();
	}
	
	@Override
	public boolean isVertex(Object cell) {
		return ((mxICell)cell).isVertex();
	}

	@Override
	public Object getSelectedCell() {
		return getSelectionCell();
	}

	@Override
	public UndoableEdit getLastUndoableEdit() {
		if (previousUndoableGraphEdit == null) return null;
		else return new JGraphUndoableEdit(previousUndoableGraphEdit);
	}

	@Override
	public void highlightCell(Object cell, Color color) {
		if (markers.containsKey(cell)) return;
		mxCellMarker marker = new mxCellMarker(getGraphComponent());
		marker.highlight(getView().getState(cell), color);
		markers.put(cell, marker);
		//Log.d("Highlighted cell.");
	}

	@Override
	public void deHighlightCell(Object cell) {
		mxCellMarker marker = markers.get(cell);
		if (marker != null) marker.unmark();
		markers.remove(cell);
		//Log.d("DeHighlighted cell.");
	}
	
	@Override
	public void deHighlightByColor(Color color) {
		for (Iterator<Entry<Object, mxCellMarker>> it = markers.entrySet().iterator(); it.hasNext() ;) {
			mxCellMarker m = it.next().getValue();
			if (m.getCurrentColor().equals(color)) {
				m.unmark();
				it.remove();
				//Log.d("DeHighlighted cell by color.");
			}
		}
	}
	
	@Override
	public void deHighlightAll() {
		for (Object key : markers.keySet()) {
			markers.get(key).unmark();
		}
		markers.clear();
		//Log.d("Highlighted all cells.");
	}
	
	@Override
	public boolean isHighlighted(Object cell) {
		return markers.containsKey(cell);
	}
	

	@Override
	public int getGraphWidth() {
		return getGraphComponent().getWidth();
		//(int) Math.round(b.getWidth())
	}

	@Override
	public int getGraphHeight() {
		return getGraphComponent().getHeight();
	}
	
	@Override
	public JScrollBar getHorizontalScrollBar() {
		return graphComponent.getHorizontalScrollBar();
	}

	@Override
	public JScrollBar getVerticalScrollBar() {
		return graphComponent.getVerticalScrollBar();
	}
}
