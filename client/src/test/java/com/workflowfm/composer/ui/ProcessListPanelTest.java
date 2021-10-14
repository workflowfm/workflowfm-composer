package com.workflowfm.composer.ui;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JFrame;

import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.CompositionSessionUI;
import com.workflowfm.composer.ui.ProcessListPanel;

public class ProcessListPanelTest {
	private CompositionSession session;
	private Prover prover;

	public ProcessListPanelTest()
	{
		prover = new HolLight();
		session = new CompositionSession(prover);
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
		
		CompositionSessionUI sUI = new CompositionSessionUI(session);
		sUI.setup();
		ProcessListPanel pPanel = new ProcessListPanel(session, sUI);
		pPanel.setup();
		pPanel.getPanel().setPreferredSize(new Dimension(600,600));

		frame.getContentPane().add(pPanel.getPanel());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public void addStuff() {		
		ProcessPort input = new ProcessPort("c0", new CllTerm("X"));
		ProcessPort output = new ProcessPort("c1", new CllTerm("X").times(new CllTerm("Y").plus(new CllTerm("Z"))));
		Vector<ProcessPort> inputs = new Vector<ProcessPort>();
		inputs.add(input);
		CProcess process = new CProcess("Q",inputs,output,false,false,true,true);
		
		session.addProcess(process);
		
		ProcessPort input2 = new ProcessPort("c2", new CllTerm("X"));
		ProcessPort output2 = new ProcessPort("c3", new CllTerm("Y"));
		Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
		inputs2.add(input2);
		CProcess process2 = new CProcess("P",inputs2,output2,false,false,true,false);
		session.addProcess(process2);
		
		ProcessPort input3a = new ProcessPort("c4", new CllTerm("X"));
		ProcessPort input3b = new ProcessPort("c5", new CllTerm("Y"));
		ProcessPort output3 = new ProcessPort("c6", new CllTerm("Z"));
		Vector<ProcessPort> inputs3 = new Vector<ProcessPort>();
		inputs3.add(input3a);
		inputs3.add(input3b);
		Vector<ComposeAction> actions = new Vector<ComposeAction>();
		actions.add(new ComposeAction("TEST", "Q", "lsel", "P", "rsel", "R"));
		CProcess process3 = new CProcess("R",inputs3,output3,false,false,true,false,actions);
		session.addProcess(process3);
		
		try {
			session.removeProcess(process2.getName());
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		setLookAndFeel();
		ProcessListPanelTest gui = new ProcessListPanelTest();
		gui.setup();
		gui.addStuff();
	}
}
