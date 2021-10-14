package com.workflowfm.composer.ui;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JFrame;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.CompositionListPanel;
import com.workflowfm.composer.ui.CompositionSessionUI;
import com.workflowfm.composer.workspace.Workspace;

public class CompositionListPanelTest {
	private Workspace workspace;
	private Prover prover;

	public CompositionListPanelTest()
	{
		prover = new HolLight();
		CompositionSession session = new CompositionSession(prover);
		workspace = session.createWorkspace();
	}

	private static void setLookAndFeel()
	{
		// Enable antialiasing for Swing
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
	}

	public void setup()
	{
		System.err.println("Setting up GUI.");

		final JFrame frame = new JFrame();
		
		CompositionSessionUI sUI = new CompositionSessionUI(workspace.getSession());
		sUI.setup();
		CompositionListPanel cPanel = new CompositionListPanel(workspace, sUI);
		cPanel.setup();
		cPanel.getPanel().setPreferredSize(new Dimension(600,600));

		frame.getContentPane().add(cPanel.getPanel());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void addStuff() {
		ProcessPort output = new ProcessPort("c1", new CllTerm("X").times(new CllTerm("Y").plus(new CllTerm("Z"))));
		Vector<ProcessPort> inputs = new Vector<ProcessPort>();
		inputs.add(new ProcessPort("c0", new CllTerm("X")));
		Vector<ComposeAction> actions = new Vector<ComposeAction>();
		actions.add(new ComposeAction("TEST", "Q", "lsel", "P", "rsel", "R"));
		CProcess process = new CProcess("Q",inputs,output,true,false,true,true,actions);
		
		workspace.addProcess(process);
		
		ProcessPort output2 = new ProcessPort("c3", new CllTerm("Y"));
		Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
		inputs2.add(new ProcessPort("c2", new CllTerm("X")));
		Vector<ComposeAction> actions2 = new Vector<ComposeAction>();
		actions2.add(new ComposeAction("TEST", "Q", "lsel", "P", "rsel", "R"));
		CProcess process2 = new CProcess("P",inputs2,output2,true,false,true,false,actions2);
		workspace.addProcess(process2);
		
		ProcessPort output3 = new ProcessPort("c6", new CllTerm("Z"));
		Vector<ProcessPort> inputs3 = new Vector<ProcessPort>();
		inputs3.add(new ProcessPort("c4", new CllTerm("X")));
		inputs3.add(new ProcessPort("c5", new CllTerm("Y")));
		Vector<ComposeAction> actions3 = new Vector<ComposeAction>();
		actions3.add(new ComposeAction("TEST", "Q", "lsel", "P", "rsel", "R"));
		CProcess process3 = new CProcess("R",inputs3,output3,true,false,false,false,actions3);
		workspace.addProcess(process3);
	}

	public static void main(String args[])
	{
		setLookAndFeel();
		CompositionListPanelTest gui = new CompositionListPanelTest();
		gui.setup();
		gui.addStuff();
	}
}
