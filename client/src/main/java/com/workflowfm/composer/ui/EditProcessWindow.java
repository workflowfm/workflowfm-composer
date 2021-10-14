package com.workflowfm.composer.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.workflowfm.composer.edit.AddProcessEdit;
import com.workflowfm.composer.edit.group.UndoableSessionEditGroup;
import com.workflowfm.composer.edit.group.UpdateProcessEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.processes.ui.edit.EditProcessPanel;
import com.workflowfm.composer.prover.command.CreateProcessCommand;
import com.workflowfm.composer.prover.command.ProverCommand;
import com.workflowfm.composer.prover.response.CreateProcessResponse;
import com.workflowfm.composer.prover.response.ExceptionResponse;
import com.workflowfm.composer.prover.response.ProverResponse;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.dialogs.ProverExceptionResponseDialog;
import com.workflowfm.composer.utils.CompletionListener;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.utils.UIUtils;
import com.workflowfm.composer.utils.validate.ValidationException;

public class EditProcessWindow implements CompletionListener, GraphWindow {

	public static final ImageIcon PROCESSEDIT_ICON = UIUtils.getIcon("silk_icons/cog_edit.png");

	private WindowManager manager;
	private CompositionSession session;
	private ExceptionHandler handler;
	private String processName;

	private EditProcessPanel editPanel;
	private ProverCommand command;
	private JPanel panel;

	private String name;

	private boolean copy = false;

	public EditProcessWindow (CompositionSession session, WindowManager manager, ExceptionHandler handler, String processName) {
		this(session,manager,handler,processName,false);
	}

	public EditProcessWindow (CompositionSession session, WindowManager manager, ExceptionHandler handler, String processName, boolean copy) {
		this.manager = manager;
		this.session = session;
		this.handler = handler;
		this.processName = processName;
		this.copy = copy;
	}
	
	@Override
	public void show() {
		if (session.processExists(processName)) {
			if (!copy) {
				this.name = "Edit " + processName;
				Log.d("Edit panel for process " + processName);
				try {
					this.editPanel = new EditProcessPanel(session.getProver(),session.getProcess(processName),session.getValidator(),handler);
					editPanel.setup();
				} catch (NotFoundException e1) { } // never happens
			} else {
				try {
					CProcess proc = new CProcess(session.getProcess(processName));
					this.processName = processName + "_copy";
					proc.setName(processName);
					this.name = "Create " + proc.getName();
					Log.d("Create copy panel for process " + processName);
					
					this.editPanel = new EditProcessPanel(session.getProver(),proc,session.getValidator(),handler);
					editPanel.setup();
				} catch (NotFoundException e1) { } // never happens
			}
		}
		else {
			Log.d("Create panel for process " + processName);
			this.editPanel = new EditProcessPanel(session.getProver(),session.getValidator(),handler);
			editPanel.setup();
			editPanel.getProcess().setName(processName);
			this.name = "Create " + processName;
		}

		editPanel.getGraph().layout();

		JButton button = new JButton("Done");
		button.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				CProcess process = editPanel.getProcess();

				try {
					session.getValidator().validate(process);
				} catch (ValidationException e2) {
					handler.handleException(e2);
					return;
				}

				if (!process.getName().equals(processName) && session.processExists(process.getName())) {
					int result = JOptionPane.showConfirmDialog(editPanel.getPanel(),"Process '" + process.getName() + "' already exists. Overwrite?","Existing process",JOptionPane.YES_NO_CANCEL_OPTION);
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

				command = new CreateProcessCommand(process,handler);
				command.addCompletionListener(EditProcessWindow.this);
				session.getProver().execute(command);
			}
		});

		panel = new JPanel(new BorderLayout());
		panel.add(editPanel.getPanel(),BorderLayout.CENTER);
		panel.add(button,BorderLayout.SOUTH);
		panel.setName(getName());

		manager.addWindow(this);
	}

	@Override
	public void dispose() {
		manager.removeWindow(this);
	}

	@Override
	public void completed() {
		if (command == null) {
			throw new RuntimeException("Null command completed");
		}
		if (!command.succeeded()) {
			Log.e("Command failed: " + command.debugString());
			return;
		}

		CreateProcessResponse response = null;
		for (ProverResponse r : command.getResponses()) {
			if (r.isException()) {
				new ProverExceptionResponseDialog((ExceptionResponse)r).show(panel);
				return;
			}
			if (r instanceof CreateProcessResponse)
				response = (CreateProcessResponse)r;
		}

		if (response != null) {
			Log.d("Creating update edit for process " + processName + " (new: " + response.getProcess().getName() + ")");
			AddProcessEdit addEdit = new AddProcessEdit(processName, response.getProcess(), session, handler, true);
			UpdateProcessEdit edit = new UpdateProcessEdit(processName, response.getProcess().getName(), session, handler, new UndoableSessionEditGroup(addEdit), true);

			// Close the tab.
			dispose();
			
			// Update the process after the window is closed so that it can be added to the last active workspace (if any)
			edit.apply();
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public JPanel getPanel() {
		return this.panel;
	}

	@Override
	public ProcessGraph getGraph() {
		return this.editPanel.getGraph();
	}

	@Override
	public Icon getIcon() {
		return EditProcessWindow.PROCESSEDIT_ICON;
	}


	@Override
	public WindowManager getManager() {
		return this.manager;
	}
}
