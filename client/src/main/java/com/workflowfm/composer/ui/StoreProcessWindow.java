package com.workflowfm.composer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.workflowfm.composer.edit.CompositionBuilder;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllValidator;
import com.workflowfm.composer.processes.ProcessStoreChangeListener;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.prover.command.ComposeCommand;
import com.workflowfm.composer.utils.CompletionListener;
import com.workflowfm.composer.utils.UIUtils;
import com.workflowfm.composer.utils.validate.ValidationException;
import com.workflowfm.composer.workspace.Workspace;

public class StoreProcessWindow implements GraphWindow, ProcessStoreChangeListener, WindowManagerChangeListener, CompletionListener {

	public static final ImageIcon STORECOMPOSITEPROCESS_ICON = UIUtils.getIcon("silk_icons/bricks.png");

	private WindowManager manager;
	private ExceptionHandler handler;
	private Workspace workspace;
	private CProcess process;

	private ComposeCommand command;
	private CompositionBuilder builder;

	private ProcessGraph graph;
	private JPanel panel;


	public StoreProcessWindow (Workspace workspace, ExceptionHandler handler, WindowManager manager, CProcess process) {
		this.manager = manager;
		this.workspace = workspace;
		this.process = process;
		this.handler = handler;
	}

	@Override
	public void show() {
		if (process.handleInvalid(handler)) return;

		graph = process.getCompositionGraph();

		panel = new JPanel(new BorderLayout());

		JPanel labelPanel = new JPanel(new FlowLayout());
		labelPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		JLabel processLabel = UIUtils.createImageLabel("Enter process name:", "silk_icons/bricks.png");
		labelPanel.add(processLabel);

		final JTextField jTextField = new JTextField(workspace.getSession().getFreshProcessName("Composition"));
		processLabel.setLabelFor(jTextField);
		jTextField.setPreferredSize(new Dimension(300,28)); // TODO avoid preferred size?
		labelPanel.add(jTextField);

		panel.add(labelPanel,BorderLayout.NORTH);
		panel.add(graph.getGraphEngine().getGraphComponent(),BorderLayout.CENTER);
		graph.layout();

		JButton jButton = new JButton("Done");
		jButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				final String name = jTextField.getText().trim();

				try {
					workspace.getSession().getValidator().validate(name,CllValidator.PROCESS);
				} catch (ValidationException e2) {
					handler.handleException(e2);
					return;
				}
				
				if (workspace.processExists(name))
				{
					int result = JOptionPane.showConfirmDialog(panel,"Process '" + name + "' already exists. Overwrite?","Existing process",JOptionPane.YES_NO_CANCEL_OPTION);
					switch(result){
					case JOptionPane.YES_OPTION:
						break;
					case JOptionPane.NO_OPTION:
						return;
					case JOptionPane.CLOSED_OPTION:
						return;
					case JOptionPane.CANCEL_OPTION:
						return;
					}
				}

				try {
					command = new ComposeCommand(workspace, name, workspace.getActionsForProcess(process, name), handler);
					builder = new CompositionBuilder(command.getName(), workspace.getSession(), handler, true);
					builder.addAsListener(command);
					builder.addCompletionListener(StoreProcessWindow.this);
					workspace.getSession().getProver().execute(command);
				} catch (NotFoundException e1) {
					handler.handleException(e1);
				}
			}


		});

		panel.add(jButton, BorderLayout.SOUTH);
		panel.setName(getName());

		manager.addWindow(this);

		workspace.addChangeListener(this);
		manager.addChangeListener(this);
	}

	@Override
	public void dispose() {
		workspace.removeChangeListener(this);
		manager.removeChangeListener(this);
		manager.removeWindow(this);
	}

	@Override
	public String getName() {
		return "Store " + process.getName() + " (" + workspace.getName() + ")";
	}

	@Override
	public JPanel getPanel() {
		return this.panel;
	}

	@Override
	public ProcessGraph getGraph() {
		return this.graph;
	}

	@Override
	public Icon getIcon() {
		return StoreProcessWindow.STORECOMPOSITEPROCESS_ICON;
	}

	@Override
	public void processAdded(CProcess process) { }

	@Override
	public void processUpdated(String previousName, CProcess process) {
		if (previousName.equals(this.process.getName())) {
			panel.remove(graph.getGraphEngine().getGraphComponent());
			panel.add(process.getCompositionGraph().getGraphEngine().getGraphComponent(),BorderLayout.CENTER);
			graph.layout();
			this.process = process;
		}	
	}

	@Override
	public void processRemoved(CProcess process) {
		//if (process.getName().equals(this.process.getName()))
		// TODO need to also check dependencies! leave as is?
		dispose();
	}

	@Override
	public void windowAdded(Window window) { }

	@Override
	public void windowActivated(Window window) { }

	@Override
	public void windowRemoved(Window window) { 
		if (window.getName().equals(workspace.getName())) {
			dispose();
		}
	}

	@Override
	public void completed() {
		if (builder.succeeded())
			dispose();
	}

	@Override
	public WindowManager getManager() {
		return this.manager;
	}
}
