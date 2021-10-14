package com.workflowfm.composer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.CompositionListPanel;
import com.workflowfm.composer.ui.CompositionSessionUI;
import com.workflowfm.composer.ui.ProcessListPanel;
import com.workflowfm.composer.ui.WorkspaceUI;
import com.workflowfm.composer.workspace.Workspace;

public class WorkspaceUITest
{
	private Workspace workspace;
	private Prover prover;

	public WorkspaceUITest()
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
		WorkspaceUI wUI = new WorkspaceUI(workspace,sUI);
		wUI.show();
		
		ProcessListPanel pPanel = new ProcessListPanel(workspace.getSession(), sUI);
		pPanel.setup();
		CompositionListPanel cPanel = new CompositionListPanel(workspace, sUI);
		cPanel.setup();

		JSplitPane processesSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pPanel.getPanel(), cPanel.getPanel());
		processesSplitPane.setDividerLocation(150);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(processesSplitPane,BorderLayout.WEST);
		panel.add(wUI.getPanel(),BorderLayout.CENTER);
		panel.setBackground(Color.WHITE);
		panel.setPreferredSize(new Dimension(800, 600));
		
		frame.getContentPane().add(panel);
		frame.setBackground(Color.WHITE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void addStuff() {
		{
			ProcessPort output = new ProcessPort("c1", new CllTerm("X").times(new CllTerm("Y").plus(new CllTerm("Z"))));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c0", new CllTerm("X")));
			CProcess process = new CProcess("Q",inputs,output);
			workspace.addProcess(process);
		}

		{
			ProcessPort output = new ProcessPort("c3", new CllTerm("Y"));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c2", new CllTerm("X")));
			CProcess process = new CProcess("P",inputs,output,false,false,true,false);
			workspace.addProcess(process);
		}

		{
			ProcessPort output = new ProcessPort("c6", new CllTerm("Z"));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c4", new CllTerm("X")));
			inputs.add(new ProcessPort("c5", new CllTerm("Y")));
			Vector<ComposeAction> actions = new Vector<ComposeAction>();
			actions.add(new ComposeAction("TEST", "Q", "lsel", "P", "rsel", "[R]"));
			CProcess process = new CProcess("[R]",inputs,output,true,false,false,false,actions);
			workspace.addProcess(process);
		}

		{
			ProcessPort output = new ProcessPort("c6", new CllTerm("Z"));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c4", new CllTerm("X")));
			inputs.add(new ProcessPort("c5", new CllTerm("Y")));
			Vector<ComposeAction> actions = new Vector<ComposeAction>();
			actions.add(new ComposeAction("TEST", "Q", "lsel", "P", "rsel", "R"));
			CProcess process = new CProcess("R",inputs,output,false,false,false,false,actions);
			workspace.addProcess(process);
		}
	}

	public static void main(String args[])
	{
		setLookAndFeel();
		WorkspaceUITest gui = new WorkspaceUITest();
		gui.setup();
		gui.addStuff();
	}


}
