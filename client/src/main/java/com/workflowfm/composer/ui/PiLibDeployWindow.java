package com.workflowfm.composer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ProcessStoreChangeListener;
import com.workflowfm.composer.processes.deploy.DeploymentFile;
import com.workflowfm.composer.processes.deploy.DeploymentLogListener;
import com.workflowfm.composer.processes.ui.ProcessCellRenderer;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.prover.command.PiLibCommand;
import com.workflowfm.composer.prover.response.DeployResponse;
import com.workflowfm.composer.prover.response.ExceptionResponse;
import com.workflowfm.composer.prover.response.ProverResponse;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.session.CompositionSessionChangeListener;
import com.workflowfm.composer.utils.*;
import com.workflowfm.composer.workspace.Workspace;

public class PiLibDeployWindow implements Window, CompletionListener, CompositionSessionChangeListener, ProcessStoreChangeListener , DeploymentLogListener {

	public static final ImageIcon PILIB_ICON = UIUtils.getIcon("silk_icons/application_cascade.png");
	private static final String labelRegex = "[a-zA-Z_][a-zA-Z0-9_]*";
	
	private WindowManager manager;
	private ExceptionHandler handler;
	private CompositionSession session;
	
	private PiLibCommand command;

	private SortedComboBoxModel<String> processesListModel = new SortedComboBoxModel<String>();
	private JComboBox<String> processList;
	
	private JTextPane logArea;
	private String selectedProcess;

	private final JPanel panel = new JPanel();

	private int i = 0; // file iterator
	private int files = 0;
	private int successes = 0; // successful files
	private int failures = 0; // failed files
	private int skipped = 0; // skipped files
	
	public PiLibDeployWindow (String selectedProcess, CompositionSession session, ExceptionHandler handler, WindowManager manager) {
		this.manager = manager;
		this.session = session;
		this.handler = handler;
		this.selectedProcess = selectedProcess;
	}

	@Override
	public void show() {		
		Dimension buttonDimension = new Dimension(28,28);
		Dimension labelDimension = new Dimension(150,28);
		Dimension logAreaDimension = new Dimension(200,200);

	
		//JPanel panel = new JPanel(new GridLayout(5, 3));
		final JPanel mainPanel = new JPanel(new SpringLayout());

		JLabel projectLabel = new JLabel("Project name:");
		projectLabel.setPreferredSize(labelDimension);
		mainPanel.add(projectLabel);

		final JTextField projectField = new JTextField(ComposerProperties.projectName());
		projectLabel.setLabelFor(projectField);
		mainPanel.add(projectField);


		JLabel processLabel = new JLabel("Process:");
		processLabel.setPreferredSize(labelDimension);
		mainPanel.add(processLabel);

		for (CProcess p : session.getProcesses())
			if (p.isComposite())
				processesListModel.addElement(p.getName());
		
		processList = new JComboBox<String>(processesListModel);
		processList.setRenderer(new ProcessCellRenderer(session));
		processList.setSelectedItem(selectedProcess);
		processLabel.setLabelFor(processList);
		mainPanel.add(processList);

		JLabel folderLabel = new JLabel("Target directory:");
		folderLabel.setPreferredSize(labelDimension);
		mainPanel.add(folderLabel);


		final JTextField folderField = new JTextField(ComposerProperties.deployFolder());
		folderLabel.setLabelFor(folderField);

		JButton folderButton = new JButton("Browse");
		folderButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser(ComposerProperties.deployFolder());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				if (fc.showSaveDialog(mainPanel) == JFileChooser.APPROVE_OPTION)
				{
					File file = fc.getSelectedFile();
					if (file != null && file.isDirectory())
						folderField.setText(file.getPath());
				}
			}
		});

		JPanel folderPanel = new JPanel();
		folderPanel.add(folderField);
		folderPanel.add(folderButton);
		mainPanel.add(folderPanel);

		JLabel packageLabel = new JLabel("Package:");
		packageLabel.setPreferredSize(labelDimension);
		mainPanel.add(packageLabel);

		final JTextField packageField = new JTextField(ComposerProperties.deployPackageName());
		packageLabel.setLabelFor(packageField);
		mainPanel.add(packageField);


		mainPanel.add(new JLabel(""));

		JPanel checkPanel = new JPanel(new GridLayout(1, 2));
		final JCheckBox statefulCheckBox = new JCheckBox("Use Stateful library");
		final JCheckBox mainCheckBox = new JCheckBox("Create Main class");

		statefulCheckBox.setSelected(ComposerProperties.deployStateful());
		mainCheckBox.setSelected(ComposerProperties.deployMain());

		checkPanel.add(statefulCheckBox);
		checkPanel.add(mainCheckBox);
		mainPanel.add(checkPanel);

		//Lay out the panel.
		SpringUtilities.makeCompactGrid(mainPanel, //parent
				5, 2,
				5, 5,  //initX, initY
				5, 5); //xPad, yPad


		JButton jButton = new JButton("Deploy");
		jButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				final String name = (String) processList.getSelectedItem();

				if (!session.processExists(name))
				{
					handler.handleException(new NotFoundException("process", name));
					return;
				}

				// TODO validate fields

				ComposerProperties.set("deployFolder", folderField.getText());
				ComposerProperties.set("deployPackageName", packageField.getText());
				ComposerProperties.set("projectName", projectField.getText());
				ComposerProperties.set("deployStateful", statefulCheckBox.isSelected());
				ComposerProperties.set("deployMain", mainCheckBox.isSelected());
				PiLibDeployWindow.this.selectedProcess = name;

				try {
					CProcess process = session.getProcess(name);
					if (process.handleInvalid(handler)) return;
				} catch (NotFoundException e2) {
					// This shouldn't happen. We've checked!
					e2.printStackTrace();
				}
						
				try {
					command = new PiLibCommand(session, name, folderField.getText(), packageField.getText(), projectField.getText(), mainCheckBox.isSelected(), statefulCheckBox.isSelected(), handler);
					command.addCompletionListener(PiLibDeployWindow.this);
					session.getProver().execute(command);
				} catch (NotFoundException e1) {
					handler.handleException(e1);
				}
			}
		});

//		JButton closeButton = new JButton("Close");
//		closeButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e)
//			{
//				dispose();
//			}
//		});

		JPanel buttonPanel = new JPanel (new GridLayout(1, 2)); 
		buttonPanel.add(jButton);
//		buttonPanel.add(closeButton);


		JPanel logPanel = new JPanel(new BorderLayout());

		//logArea = new JTextArea();
		logArea = new JTextPane();
		JPanel noWrapPanel = new JPanel( new BorderLayout() );
		noWrapPanel.add( logArea );

		JScrollPane sp2 = new JScrollPane(noWrapPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp2.setPreferredSize(logAreaDimension);
		JLabel label = new JLabel("Deployment Log");
		label.setIcon(UIUtils.getIcon("silk_icons/script.png"));
		label.setLabelFor(logArea);

		logPanel.add(label,BorderLayout.NORTH);
		logPanel.add(sp2,BorderLayout.CENTER);

		panel.setLayout(new BorderLayout());
		panel.add(buttonPanel, BorderLayout.SOUTH);
		panel.add(mainPanel, BorderLayout.NORTH);
		panel.add(logPanel,BorderLayout.CENTER);
		panel.setName(getName());
		
		this.session.addChangeListener((CompositionSessionChangeListener)this);
		this.session.addChangeListener((ProcessStoreChangeListener)this);
		
		manager.addWindow(this);
	}
	
	@Override
	public void dispose() {
		session.removeChangeListener((CompositionSessionChangeListener)this);
		session.removeChangeListener((ProcessStoreChangeListener)this);

		manager.removeWindow(this);
	}
	
	@Override
	public String getName() {
		return "Scala Deploy";
	}

	@Override
	public JPanel getPanel() {
		return this.panel;
	}

	@Override
	public Icon getIcon() {
		return PiLibDeployWindow.PILIB_ICON;
	}

	@Override
	public WindowManager getManager() {
		return this.manager;
	}

	@Override
	public void workspaceAdded(Workspace workspace) { }

	@Override
	public void workspaceActivated(Workspace workspace) { }

	@Override
	public void workspaceRemoved(Workspace workspace) { }

	@Override
	public void processAdded(CProcess process) {
		processesListModel.addElement(process.getName());		
	}

	@Override
	public void processUpdated(String previousName, CProcess process) {
		if (!previousName.equals(process.getName())) { 
			processesListModel.removeElement(previousName);
			processesListModel.addElement(process.getName());
		}
		panel.repaint();
	}

	@Override
	public void processRemoved(CProcess process) {
		processesListModel.removeElement(process.getName());
	}

	@Override
	public void sessionReset() {
		processesListModel.removeAllElements();		
	}

	@Override
	public void undoRedoUpdate() { }

	@Override
	public void sessionSaved() { }

	@Override
	public void success(String file) {
		i++;
		successes++;
		addLogIcon("silk_icons/application_go.png");
		addLog(" Deployed file (" + i + " of " + files +"): [" + file + "]\n");
	}


	@Override
	public void skipped(String file) {
		i++;
		skipped++;
		addLogIcon("silk_icons/application_error.png");
		addLog(" Skipped file (" + i + " of " + files +"): [" + file + "]\n");
	}

	@Override
	public void start(int numberOfFiles) {
		this.files = numberOfFiles;
		this.i = 0;
		this.successes = 0;
		this.failures = 0;
		this.skipped = 0;
		addLog("Deploying [" + numberOfFiles + "] files...\n");
	}

	@Override
	public void finish() {
		addLog("Deployment of " + this.selectedProcess + " completed: " + successes + " succeeded, " + skipped + " skipped, " + failures + " failed.\n");
	}

	@Override
	public void failure(String file, Exception exception) {
		i++;
		failures++;
		addLogIcon("silk_icons/application_delete.png");
		addLog(" Failed to deploy file (" + i + " of " + files +"): [" + file + "]\nException: " + exception.getMessage()  + "\n");	
	}

	private void addLogIcon(String iconPath) {
		logArea.insertIcon(UIUtils.getIcon(iconPath));
	}

	private void addLog(String text) {
		StyledDocument doc = logArea.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), text, new SimpleAttributeSet() );
		} catch (BadLocationException e) {}
		Utils.scrollToBottomOfTextArea(logArea);
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
		for (ProverResponse r : command.getResponses()) {
			if (r.isException()) {
				handler.handleException((ExceptionResponse)r);
				return;
			}
			if (r instanceof DeployResponse) {
				DeployResponse d = (DeployResponse)r;
				if (!d.getType().equalsIgnoreCase(command.getCommand()))
						handler.handleException(new Exception("Prover response type [" + d.getType() + "] did not match command type [" + command.getCommand() + "]."));
				doDeploy(((DeployResponse)r).getFiles());
			}
		}	
	}

	public void doDeploy(Collection<DeploymentFile> files) {
		start(files.size());
		for (DeploymentFile f : files)
		{
			try
			{
				if (f.deploy())
					success(f.getFilePath());
				else 
					skipped(f.getFilePath());
			} catch (Exception ex) {
				failure(f.getFilePath(), ex);
			}
		}
		finish();	
	}

}
