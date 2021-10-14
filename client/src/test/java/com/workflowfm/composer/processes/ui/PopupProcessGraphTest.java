package com.workflowfm.composer.processes.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.ui.PopupProcessGraph;
import com.workflowfm.composer.processes.ui.ProcessGraph;


public class PopupProcessGraphTest {
	
	private ProcessGraph graph = new ProcessGraph();
	
	public void setup()
	{		
		System.err.println("Setting up GUI.");
		
		new PopupProcessGraph(graph).addListeners();
		
		graph.getGraphEngine().getGraphComponent().setPreferredSize(new Dimension(800, 600));
		
		JPanel panel = new JPanel(new BorderLayout());
		
		panel.add(graph.getGraphEngine().getGraphComponent(),BorderLayout.CENTER);	
		panel.setPreferredSize(new Dimension(800, 600));

		final JFrame frame = new JFrame();
		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void addStuff() {
		{
			ProcessPort output = new ProcessPort("c1", new CllTerm("X").times(new CllTerm("Y").plus(new CllTerm("Z"))));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c0", new CllTerm("X")));
			Vector<ComposeAction> actions = new Vector<ComposeAction>();
			actions.add(new ComposeAction("X", "X", "X", "X", "X", "X"));
			CProcess process = new CProcess("Q",inputs,output,true,false,true,true,actions);
			
			ProcessGraph graph = new ProcessGraph();
			CProcess pa = new CProcess("A",new Vector<ProcessPort>(), null);
			CProcess pb = new CProcess("B",new Vector<ProcessPort>(), null);
			Object v1 = graph.createProcessVertex(pa);
			Object v2 = graph.createProcessVertex(pb);
			ProcessPort port = new ProcessPort("zzz", new CllTerm("ZZZ"));
			port.joinVertices(graph, process, false, false, v1, v2);
			graph.layout();
			
			process.setCompositeGraph(graph);
			this.graph.createProcessGraph(process);
		}

		{
			ProcessPort output = new ProcessPort("c3", new CllTerm("Y"));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c2", new CllTerm("X")));
			CProcess process = new CProcess("P",inputs,output);
			graph.createProcessGraph(process);
		}
		
		{
			ProcessPort output = new ProcessPort("c4", new CllTerm("X").plus(new CllTerm("Y").plus(new CllTerm("Z"))));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c5", new CllTerm("X")));
			Vector<ComposeAction> actions = new Vector<ComposeAction>();
			actions.add(new ComposeAction("X", "X", "X", "X", "X", "X"));
			CProcess process = new CProcess("Q",inputs,output,true,false,true,true,actions);
			
			ProcessGraph graph = new ProcessGraph();
			CProcess pa = new CProcess("AA",new Vector<ProcessPort>(), null);
			CProcess pb = new CProcess("BB",new Vector<ProcessPort>(), null);
			Object v1 = graph.createProcessVertex(pa);
			Object v2 = graph.createProcessVertex(pb);
			Object v3 = graph.createProcessVertex(pb);
			Object v4 = graph.createProcessVertex(pb);
			Object v5 = graph.createProcessVertex(pb);
			ProcessPort port = new ProcessPort("zzzz", new CllTerm("X").plus(new CllTerm("Y")).times(new CllTerm("Z")).plus(new CllTerm("K")));
			port.joinVertices(graph, process, false, false, v1, v2);
			port.joinVertices(graph, process, false, false, v2, v3);
			port.joinVertices(graph, process, false, false, v3, v4);
			port.joinVertices(graph, process, false, false, v4, v5);
			graph.layout();
			
			process.setCompositeGraph(graph);
			this.graph.createProcessGraph(process);
		}
		
		graph.layout();
	}
	
	private static void setLookAndFeel()
	{
		// Enable antialiasing for Swing
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
	}

	public static void main(String args[])
	{
		setLookAndFeel();
		PopupProcessGraphTest gui = new PopupProcessGraphTest();
		gui.setup();
		gui.addStuff();
	}
}
