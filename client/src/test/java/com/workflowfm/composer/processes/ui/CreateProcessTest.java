package com.workflowfm.composer.processes.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.workflowfm.composer.exceptions.LogExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.ui.edit.EditProcessPanel;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.Prover;

public class CreateProcessTest
{
	private EditProcessPanel panel;
	private CProcess process;
	private Prover prover;

	public CreateProcessTest()
	{
		prover = new HolLight();

		ProcessPort input = new ProcessPort("c", new CllTerm("X"));
		//ProcessPort output = new ProcessPort("c", new CllTerm("X"));
		ProcessPort output = new ProcessPort("c", new CllTerm("X").times(new CllTerm("Y").plus(new CllTerm("Z"))));
		Vector<ProcessPort> inputs = new Vector<ProcessPort>();
		inputs.add(input);
		process = new CProcess("P",inputs,output);
		//workspace.addAction("CreateAtomicProcessAction", new CreateAtomicProcessAction());
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

		final JFrame frame = new JFrame();
		frame.setSize(800, 800);

		panel = new EditProcessPanel(prover, process, new LogExceptionHandler());
		panel.setup();
		
		JButton button = new JButton("Done");
		final JDialog dialog = new JDialog(frame);
		
		button.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				CProcess process = panel.getProcess();
				System.out.println("Finished editing process: " + process.getName());
				System.out.println("= Inputs = ");
				for (CllTerm tm : process.getInputCll()) {
					System.out.println(prover.cllResourceString(tm));
				}
				System.out.println("= Output =");
				System.out.println(prover.cllResourceString(process.getOutputCll()));
				dialog.dispose();
			}
		});

		dialog.setLayout(new BorderLayout());
		dialog.getContentPane().add(panel.getPanel(),BorderLayout.CENTER);
		dialog.getContentPane().add(button,BorderLayout.SOUTH);
		dialog.setPreferredSize(new Dimension(800, 800));;
		dialog.pack();
		dialog.setVisible(true);
		
		//frame.add(dialog);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.addWindowListener(new java.awt.event.WindowAdapter() {
		//	@Override
		//	public void windowClosing(WindowEvent winEv)
		//	{
		//		System.exit(0);
		//	}
		//});
		//frame.pack();
		frame.setVisible(true);
	}

	public static void main(String args[])
	{
		setLookAndFeel();
		CreateProcessTest gui = new CreateProcessTest();
		gui.setup();
	}


}
