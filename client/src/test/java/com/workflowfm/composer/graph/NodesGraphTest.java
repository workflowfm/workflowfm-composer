package com.workflowfm.composer.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllTermPath;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.ui.PortEdge;
import com.workflowfm.composer.processes.ui.PortVertex;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.properties.ComposerProperties;

public class NodesGraphTest extends JFrame
{
	private static final long serialVersionUID = -7168287239804761025L;

	private JPanel panel = new JPanel();
	private ProcessGraph graph = new ProcessGraph();

	public NodesGraphTest()
	{
	}

	public void setupGraph()
	{
		graph.getGraphEngine().createPortVertex(null, true, ComposerProperties.processColour(true));
		graph.getGraphEngine().createRectangleVertex(null, ComposerProperties.processNodeWidth(), ComposerProperties.processNodeHeight(), ComposerProperties.processColour(true));
		graph.getGraphEngine().createRectangleVertex(null, ComposerProperties.processNodeWidth(), ComposerProperties.processNodeHeight(), ComposerProperties.processColour(false));
		graph.getGraphEngine().createRoundVertex(null, ComposerProperties.processNodeHeight() / 2, ComposerProperties.processColour(true));
		graph.getGraphEngine().createRoundVertex(null, 100, ComposerProperties.processColour(true));
		graph.getGraphEngine().createTriangleVertex(null, ComposerProperties.processNodeHeight(), ComposerProperties.processColour(true));
		graph.getGraphEngine().createTriangleVertex(null, 100, ComposerProperties.processColour(true));
		graph.getGraphEngine().createRhombusVertex("&", ComposerProperties.processNodeHeight(), ComposerProperties.processColour(true));
		graph.getGraphEngine().createRhombusVertex(null, 100, ComposerProperties.processColour(true));
		
		CProcess p = new CProcess("P", new Vector<ProcessPort>(), null);
		CProcess q = new CProcess("ThisIsALongNameForACompositeProcess", new Vector<ProcessPort>(), null);

		PortEdge pe1 = new PortEdge(new CllTerm("A"), new CllTerm("A"), new CllTermPath(), false, false);
		Object v2 = graph.createProcessVertex(p);
		Object v1 = graph.addMessagePortVertex(new PortVertex(p, 0, pe1, "P", true), v2);
		Object v3 = graph.createProcessVertex(new CProcess("Q", new Vector<ProcessPort>(), null, false, true));
		new CllTerm("B").joinVertices(graph, p, "P", false, true, v2, true, v3, true);
		PortEdge pe2 = new PortEdge(new CllTerm("C"), new CllTerm("C"), new CllTermPath(), false, true);

		Object v4 = graph.addMessagePortVertex(new PortVertex(q, pe2, "P", false), v3);
		Object v5 = graph.createJoinVertex(q,q.getName());
		new CllTerm("D").joinVertices(graph, q, "P", true, true, v4, false, v5, true);
		
		graph.layout();
}

	private static void setLookAndFeel()
	{
		// Enable antialiasing for Swing
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");

		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setup()
	{
		System.err.println("Setting up GUI.");

		final JFrame frame = this;
		frame.setSize(600, 600);

		setupGraph();

		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(1200, 800));
		panel.setBackground(Color.WHITE);
		panel.add(graph.getGraphEngine().getGraphComponent());
		panel.setVisible(true);

		frame.add(panel);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent winEv)
			{
				System.exit(0);
			}
		});
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String args[])
	{
		setLookAndFeel();
		NodesGraphTest gui = new NodesGraphTest();
		gui.setup();
	}


}
