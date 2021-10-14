package com.workflowfm.composer.workspace.edit;

import java.util.Vector;

import javax.swing.JFrame;

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
import com.workflowfm.composer.ui.WorkspaceUI;
import com.workflowfm.composer.workspace.Workspace;


public class AddProcessGraphEditTest {
	private Workspace workspace;
	private Prover prover;
	
	private CProcess p1;
	private CProcess p2;
	private CProcess p3;

	public AddProcessGraphEditTest()
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
		
		CompositionSessionUI sUI = new CompositionSessionUI(workspace.getSession());
		sUI.setup();
		WorkspaceUI wUI = new WorkspaceUI(workspace,sUI);
		wUI.show();
		ExceptionHandler exceptionHandler = new ComponentExceptionHandler(wUI.getPanel());
				
		frame.getContentPane().add(wUI.getPanel());
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
		AddProcessGraphEditTest gui = new AddProcessGraphEditTest();
		gui.setup();
	}

}
