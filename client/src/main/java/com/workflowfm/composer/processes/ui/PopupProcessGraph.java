package com.workflowfm.composer.processes.ui;

import java.awt.Dimension;
import java.awt.event.MouseEvent;

import com.workflowfm.composer.processes.CProcess;

import com.workflowfm.composer.utils.PopupFrame;

public class PopupProcessGraph extends PopupFrame {

	private CProcess process;

	private ProcessGraph graph;

	public PopupProcessGraph(ProcessGraph graph) {
		super();
		this.graph = graph;
	}

	public void addListeners() {
		this.graph.getGraphEngine().getControlComponent().addMouseListener(this);
		this.graph.getGraphEngine().getControlComponent().addMouseMotionListener(this);
	}

	private ProcessVertex getProcessVertex(int x, int y) {
		Object selectedCell = graph.getGraphEngine().getCellAt(x, y);
		if (graph.isProcessVertex(selectedCell))
			return (ProcessVertex)graph.getValue(selectedCell);
		else
			return null;
	}

	private void mouseController(MouseEvent e) {
		ProcessVertex vertex = getProcessVertex(e.getX(),e.getY());
		if (vertex != null && !vertex.isTerminator() && vertex.getProcess().isComposite()) {
			this.process = vertex.getProcess();
			mouseInHighlightArea(e);
		} else 
			mouseOutsideHighlightArea();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseController(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseController(e);
	}

	@Override
	public void init() {
		if (process != null) {
			ProcessGraph processGraph = new ProcessGraph();
			processGraph.getGraphEngine().insertCells(process.getFullGraph().getGraphEngine().cloneAllCells());
			processGraph.layout();
			processGraph.getGraphEngine().getControlComponent().addMouseListener(getPopupMouseListener());
			processGraph.getGraphEngine().getGraphComponent().setPreferredSize(new Dimension(300,100)); // TODO what should this size be?
			getWindow().getContentPane().add(processGraph.getGraphEngine().getGraphComponent());
		}
	}


}
