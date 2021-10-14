package com.workflowfm.composer.processes.ui;

import com.workflowfm.composer.graph.ComposableCell;

public interface ProcessGraphSelectionListener {
	public void selectedUnknown (Object cell);
	public void selected (ComposableCell cell);
	public void selected (PortEdge edge);
	public void selected (PortVertex port);
	public void selected (ProcessVertex process);
	public void deselected ();
}
