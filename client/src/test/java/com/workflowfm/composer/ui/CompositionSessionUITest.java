package com.workflowfm.composer.ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.CompositionSessionUI;
import com.workflowfm.composer.workspace.Workspace;

public class CompositionSessionUITest {
	
	private Prover prover;
	private CompositionSessionUI ui;
	private boolean useProver;
	
	public CompositionSessionUITest()
	{
		this(true);
	}
	
	public CompositionSessionUITest(boolean useProver)
	{
		prover = new HolLight();
		this.useProver = useProver;
	}
		
	public void setup()
	{
		System.err.println("Setting up GUI.");

		if (useProver)
			try {
				prover.start();
			} catch (UserError e1) {
				e1.printStackTrace();
				return;
			}
		
		final JFrame frame = new JFrame();
		
		ui = new CompositionSessionUI(new CompositionSession(prover));
		ui.setup();
		ui.getPanel().setPreferredSize(new Dimension(900, 600));
		
		frame.getContentPane().add(ui.getPanel());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
		    	//frame.dispose();
		    	if (useProver) prover.stop();
		        System.exit(0);
		    }
		});
		frame.pack();
		frame.setVisible(true);
	}
	
	public void addStuff() {
		{
			ProcessPort output = new ProcessPort("c1", new CllTerm("X").times(new CllTerm("Y").plus(new CllTerm("Z"))));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c0", new CllTerm("X")));
			CProcess process = new CProcess("Q",inputs,output);
			ui.getSession().addProcess(process);
		}

		Workspace workspace = ui.getSession().createWorkspace();
		//ui.getSession().addWorkspace(workspace);
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
		
		workspace = ui.getSession().createWorkspace();
		//ui.getSession().addWorkspace(workspace);
		{
			ProcessPort output = new ProcessPort("c3", new CllTerm("Y"));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c2", new CllTerm("X")));
			CProcess process = new CProcess("P",inputs,output,true,false,true,false);
			workspace.addProcess(process);
		}

		{
			ProcessPort output = new ProcessPort("c6", new CllTerm("Z"));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(new ProcessPort("c4", new CllTerm("X")));
			inputs.add(new ProcessPort("c5", new CllTerm("Y")));
			Vector<ComposeAction> actions = new Vector<ComposeAction>();
			actions.add(new ComposeAction("TEST", "Q", "lsel", "P", "rsel", "R"));
			CProcess process = new CProcess("R",inputs,output,true,false,false,false,actions);
			workspace.addProcess(process);
		}
		
	}
	
	public JTabbedPane getTabbedPane() {
		return ui.getTabbedPane();
	}
	
	private static void setLookAndFeel()
	{
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		// Enable antialiasing for Swing
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
	}
	
	public static void main(String args[])
	{
		setLookAndFeel();
		CompositionSessionUITest gui = new CompositionSessionUITest();
		gui.setup();
		gui.addStuff();
		for (int i = 0; i < gui.getTabbedPane().getTabCount(); i++) {
			System.out.println(">> " +gui.getTabbedPane().getComponentAt(i).getName() ); 
		}
	}
}
