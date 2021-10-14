package com.workflowfm.composer.ui;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.workflowfm.composer.edit.graph.AddProcessGraphEdit;
import com.workflowfm.composer.exceptions.ComponentExceptionHandler;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.CompositionSessionUI;
import com.workflowfm.composer.ui.SessionToolbar;
import com.workflowfm.composer.ui.WorkspaceUI;
import com.workflowfm.composer.workspace.Workspace;

public class SessionToolbarTest {
	private Workspace workspace;
	private Prover prover;
	
	private CProcess p1;
	private CProcess p2;
	private CProcess p3;

	public SessionToolbarTest()
	{
		prover = new HolLight();
		CompositionSession session = new CompositionSession(prover);
		workspace = session.createWorkspace();

		{
			ProcessPort output = new ProcessPort("c1", new CllTerm("X").times(new CllTerm("Y").plus(new CllTerm("Z"))));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c0", new CllTerm("X")));
			p1 = new CProcess("Q",inputs,output);
			workspace.addProcess(p1);
		}


		{
			ProcessPort output = new ProcessPort("c6", new CllTerm("Z"));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c4", new CllTerm("X")));
			//inputs.add(new ProcessPort("c5", new CllTerm("Y")));
			Vector<ComposeAction> actions = new Vector<ComposeAction>();
			actions.add(new ComposeAction("TEST", "Q", "lsel", "P", "rsel", "[R]"));
			p2 = new CProcess("[R]",inputs,output,true,false,false,false,actions);
			workspace.addProcess(p2);
		}

		{
			ProcessPort output = new ProcessPort("c6", new CllTerm("Z"));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c4", new CllTerm("X")));
			inputs.add(new ProcessPort("c5", new CllTerm("Y")));
			Vector<ComposeAction> actions = new Vector<ComposeAction>();
			actions.add(new ComposeAction("TEST", "Q", "lsel", "P", "rsel", "R"));
			p3 = new CProcess("R",inputs,output,false,false,false,false,actions);
			workspace.addProcess(p3);
		}
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
		
		JPanel panel = new JPanel(new BorderLayout());
		
		CompositionSessionUI sUI = new CompositionSessionUI(workspace.getSession());
		sUI.setup();
		WorkspaceUI wUI = new WorkspaceUI(workspace,sUI);
		wUI.show();
		panel.add(wUI.getPanel(),BorderLayout.CENTER);
		ExceptionHandler exceptionHandler = new ComponentExceptionHandler(wUI.getPanel());
		
		SessionToolbar toolbar = new SessionToolbar(workspace.getSession());
		toolbar.setup();
		panel.add(toolbar.getToolbar(),BorderLayout.PAGE_START);
		
		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		new AddProcessGraphEdit(p1, workspace.getSession(), exceptionHandler, workspace.getGraph()).apply();
		new AddProcessGraphEdit(p2, workspace.getSession(), exceptionHandler, workspace.getGraph()).apply();
		new AddProcessGraphEdit(p3, workspace.getSession(), exceptionHandler, workspace.getGraph()).apply();
	}

	public static void main(String args[])
	{
		setLookAndFeel();
		SessionToolbarTest gui = new SessionToolbarTest();
		gui.setup();
	}

}