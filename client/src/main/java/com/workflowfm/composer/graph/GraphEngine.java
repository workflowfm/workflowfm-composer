package com.workflowfm.composer.graph;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.undo.UndoableEdit;

public interface GraphEngine {
	public Object getGraph();
	public JComponent getGraphComponent();
	public JComponent getControlComponent();
	public JScrollBar getHorizontalScrollBar();
	public JScrollBar getVerticalScrollBar();
	
	public void startEdit();
	public void endEdit();
	public UndoableEdit getLastUndoableEdit();
	
	public Object valueOfCell(Object c);
	public String[] cellsToValues(Object c[]);
	public void updateValue(Object cell, Object value);
	
	public Object[] getAllVertices();
	public Object[] getAllEdges();
	public Object[] getAllCells();
	
	public Object[] getIncomingEdges(Object vertex);
	public Object[] getOutgoingEdges(Object vertex);
	public Object getTerminal(Object edge, boolean source);
	public Object getSource(Object edge);
	public Object getTarget(Object edge);
	public Collection<Object> getConnectedVertices(Object vertex);
	public Collection<Object> getConnectedCells(Object vertex);
	
	//public Object findConnectedVertex(Object graph, Object startingVertex, boolean directed, final CellVisitor v);
	//public Object findConnectedVertex(Object startingVertex, boolean directed, final CellVisitor v);
	public void searchVertices(final CellVisitor<?> v);
	
	//public void traverseGraph(Object graph, Object startingVertex, boolean directed, final CellVisitor<?> v);
	public void traverseGraph(Object startingVertex, boolean directed, final CellVisitor<?> v);
	
	public Object createRectangleVertex(Object value, int width, int height, String color);
	public Object createRoundVertex(Object value, int radius, String color);
	public Object createTriangleVertex(Object value, int width, String color);
	public Object createRhombusVertex(Object value, int width, String color);
	/**
	 * Adds a vertex that acts as a box the user can click on to select process
	 * messages.
	 */
	public Object createPortVertex(Object value, boolean clickable, String color);
	public Object createGroup(Object[] cells, String color);
	public Object createGroup(Object[] cells, boolean dashed, String color);
	
	public Object insertEdge(Object value, Object source, Object target);
	public Object insertEdge(Object value, Object source, Object target, boolean arrow, boolean buffered, boolean optional);

		
	public void clear();
	
	public void insertCells(Object[] cells);
	public void deleteCells(Object[] cells);
	//public Object cloneCell(Object cell);
	public Object[] cloneConnectedSubgraph(Object vertex);
	public Object[] cloneCells(Object[] cells);
	public Object[] cloneAllCells();
	
	public void autoResizeCell (Object c, String s, int height);
	public void layout();
	public void centerViewport(Component parent);
	
	public int getGraphWidth();
	public int getGraphHeight();

	public boolean isVertex(Object cell);
	public boolean isEdge(Object cell);
	
	public Object getCellAt(int x, int y);
	public Object getSelectedCell();
	
	public void highlightCell(Object cell, Color color);
	public boolean isHighlighted(Object cell);
	public void deHighlightCell(Object cell);
	public void deHighlightByColor(Color color);
	public void deHighlightAll();
}
