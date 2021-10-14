package com.workflowfm.composer.graph;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.view.mxGraph;

public class mxProcessLayout extends mxHierarchicalLayout {

	public mxProcessLayout(mxGraph arg0) {
		super(arg0);
		traverseAncestors = false;
	}

	public mxProcessLayout(mxGraph arg0, int arg1) {
		super(arg0, arg1);
		traverseAncestors = false;
	}
	

}
