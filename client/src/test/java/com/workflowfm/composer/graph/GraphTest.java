package com.workflowfm.composer.graph;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.SwingConstants;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class GraphTest extends JFrame
{
	private static final long serialVersionUID = 1L;

	public GraphTest()
	{
		super("Hello, World!");

		mxGraph graph1 = new mxGraph();
		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph1,SwingConstants.WEST);

		graph1.getModel().beginUpdate();
		try
		{
			mxCell v1 = (mxCell) graph1.insertVertex(graph1.getDefaultParent(), null, "test", 240, 150, 115, 30, "");
			mxCell in = (mxCell) graph1.insertVertex(graph1.getDefaultParent(), null, null, 240, 150, 15, 15, "");

			mxCell v2 = (mxCell) graph1.insertVertex(graph1.getDefaultParent(), null, "test2", 240, 150, 115, 30, "");
			mxCell out = (mxCell) graph1.insertVertex(graph1.getDefaultParent(), null, null, 240, 150, 15, 15, "");
			
			graph1.insertEdge(graph1.getDefaultParent(), "", "", in, v1, "");
			graph1.insertEdge(graph1.getDefaultParent(), "", "", v1, v2, "");
			graph1.insertEdge(graph1.getDefaultParent(), "", "", v2, out, "");
			
			Collection<mxCell> c = new ArrayList<mxCell>();
			c.add((mxCell) v1);
			c.add((mxCell) v2);
			c.add((mxCell) out);
			c.add((mxCell) in);
		    
		    mxCell group = (mxCell) graph1.groupCells(null, 12, c.toArray());
		    
		    //group.setCollapsed(true);
		    group.setConnectable(true);
		    graph1.setCollapseToPreferredSize(false);
	    
		    mxCell gout1 = (mxCell) graph1.insertVertex(graph1.getDefaultParent(), null, null, 240, 150, 15, 15, "");
		    graph1.insertEdge(graph1.getDefaultParent(), "", null, group, gout1, "");
		    
		    layout.execute(graph1.getDefaultParent());	    
		}
		finally
		{
			graph1.getModel().endUpdate();
		    graph1.refresh();
		}

		mxGraphComponent graphComponent = new mxGraphComponent(graph1);
		getContentPane().add(graphComponent);
	}

	public static void main(String[] args)
	{
		GraphTest frame = new GraphTest();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1400, 1320);
		frame.setVisible(true);
	}

}
