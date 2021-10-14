package com.workflowfm.composer.ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.workflowfm.composer.exceptions.ComponentExceptionHandler;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.dialogs.ConnectToReasonerDialog;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.utils.UIUtils;

/** The main window for the composer interface. */
public class Gui {
	
	private Prover prover;
	private CompositionSessionUI ui;
	
	private JFrame frame;

//	private JPanel consolePanel = new JPanel();
//	private JTextField commandLineTextField = new JTextField();
//	private JTextArea proofScriptTextArea;
//	private JPanel editorPanel = new JPanel();
//	private JLabel welcomeLabel = new JLabel("<html><center><b>Welcome to the WorkflowFM composer!</b><br><br>" + "Use the toolbar to <i>add new processes</i>.<br>"
//			+ "<br>You can then <i>construct composite processes</i> by combining existing processes.</centre></html>");

	
	public void setup()
  {
		final JFrame frame = new JFrame();
		frame.setTitle("WorkflowFM Composer");
		frame.setName("WorkflowFM");
		frame.setIconImage(UIUtils.getIcon("silk_icons/plugin.png").getImage());
		frame.setSize(ComposerProperties.frameWidth(), ComposerProperties.frameHeight());
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
      {
		    public void windowClosing(WindowEvent e)
		    {
		    	ComposerProperties.set("frameWidth", frame.getWidth());
          ComposerProperties.set("frameHeight", frame.getHeight());
          //ComposerProperties.set("processesDividerLocation", processesSplitPane.getDividerLocation());
          //ComposerProperties.set("editorDividerLocation", editorSplitPane.getDividerLocation());
          //ComposerProperties.set("consoleDividerLocation", editorAndConsoleSplitPane.getDividerLocation());
		    	prover.stop();
          System.exit(0);
		    }
      });
    this.frame = frame;

    try {
      SwingUtilities.invokeAndWait(new Runnable() { 
          public void run() {
            Log.d("Setting up Reasoner.");
        
            ConnectToReasonerDialog dialog = new ConnectToReasonerDialog(frame, new ComponentExceptionHandler(frame));
            dialog.setup();
            dialog.setVisible(true);
            
            Prover prover = dialog.getProver();

            if (prover == null) {
              frame.dispose();
            } else {
              Log.d("Setting up GUI.");

              ui = new CompositionSessionUI(new CompositionSession(prover));
              ui.setup();
              ui.getPanel().setPreferredSize(new Dimension(900, 600));

              Gui.this.prover = prover;
		
              frame.getContentPane().add(ui.getPanel());
              frame.pack();
              frame.setVisible(true);
            }
             
          }});
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 		
	}
	
	public static void setLookAndFeel()
	{
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	public void quit() 
	{
		//frame.processEvent(new WindowEvent(Gui.this, WindowEvent.WINDOW_CLOSING));
		frame.dispose();
	}

	public static void main(String args[])
	{
		// set the name of the application menu item
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorkflowFM");

		//System.out.println("Setting up application...");
		// take the menu bar off the jframe
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		// Enable antialiasing for Swing
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
				
		Gui gui = new Gui();
		gui.setup();
		// takeScreenshots(gui);

//		if (args.length > 0)
//		{
//			try
//			{
//				new LoadAction(gui).doLoad(new File(args[0]));
//			}
//			catch (Exception e)
//			{
//				gui.showErrorDialog("Error", "Error", e);
//			}
//		}
	}

	//TODO export graph as image
	// Used to take screenshots that featured as figures in a paper about the
	// interface
	// TODO: It would be better to allow such functionality through command-line
	// arguments
	//	public static void takeScreenshots(Gui gui)
	//	{
	//		gui.setSize(4000, 4000);
	//		String[] f = { "paper_example1", "paper_example2", "paper_example3", "paper_example4", "paper_example5", "paper_example6", "paper_example7", "paper_example8", "paper_example9", "paper_example10", "paper_example11", "paper_example12", "paper_example13", "paper_example1b", "paper_example2b", "paper_example3b", "paper_example4b", "paper_example5b", "paper_example6b", "paper_example7b", "paper_example8b", "paper_example9b", "paper_example10b" };
	//
	//		// String[] f = { "paper_example7" };
	//		// if (args.length > 0)
	//		{
	//			for (String s : f)
	//			{
	//				String proofScriptFilename = "proofs/Diagrams2012/" + s + ".ml";
	//				String exportFilename = "screenshots/" + s + ".png";
	//				try
	//				{
	//					gui.loadFile(new File(proofScriptFilename));
	//					gui.exportGraphAsImage(new File(exportFilename));
	//				}
	//				catch (Exception e)
	//				{
	//					gui.showExceptionAsErrorDialog(e);
	//					// e.printStackTrace();
	//					// System.exit(-1);
	//				}
	//			}
	//		}
	//	}


//	private AbstractAction newAction = new NewAction(this);
//	private AbstractAction saveAction = new SaveAction(this);
//	private AbstractAction saveAsAction = new SaveAsAction(this);
//	private AbstractAction loadAction = new LoadAction(this);
//	private AbstractAction exportImageAction = new ExportImageAction(this,workspace.getGraph());
//	private AbstractAction quitAction = new QuitAction(this);
//	private AbstractAction undoAction = new UndoAction(this);
//	private AbstractAction redoAction = new RedoAction(this);
//	private AbstractAction toggleConsoleAction = new ToggleConsoleAction(this);
//	private AbstractAction layoutAction = new LayoutAction(this,workspace.getGraph());
//	public AbstractAction loadOntologyAction = new LoadOntologyAction("Load OWL Ontology", 
//			this.getClass().getClassLoader().getResource("ontology.add.png"), KeyEvent.VK_W, KeyEvent.VK_W, ActionEvent.CTRL_MASK, 
//			workspace.getSimpleOntologyManager(), this);
//	public AbstractAction setActiveOntologyAction = new SetActiveOntologyAction("Set Active Ontology", 
//			this.getClass().getClassLoader().getResource("ontology.png"), KeyEvent.VK_T, KeyEvent.VK_T, ActionEvent.CTRL_MASK, 
//			workspace.getSimpleOntologyManager(), this);
//	public AbstractAction aboutAction = new AboutAction(this);
//
//	//remove unused action
//	private AbstractAction createServiceAction = new CreateServiceAction(this);
//	private AbstractAction storeServiceAction = new StoreServiceAction(this);

//	public void toggleConsole()
//	{
//		consolePanel.setVisible(!consolePanel.isVisible());
//		ComposerProperties.set("consoleVisible", consolePanel.isVisible());
//
//		// Required, otherwise the console stays hidden
//		editorAndConsoleSplitPane.setDividerLocation(ComposerProperties.consoleDividerLocation());
//	}
//
//	//TODO remove unused services action
//	//	public AbstractAction removeUnusedServicesFromWorkspaceAction = new UIAction("Clear unconnected services from workspace", "third_party/silk_icons/bin.png", KeyEvent.VK_E, KeyEvent.VK_E,
//	//			ActionEvent.CTRL_MASK) {
//	//		@Override
//	//		public void actionPerformed(ActionEvent e)
//	//		{
//	//			executeCommandInBackground(new RemoveUnusedServicesFromWorkspaceCommand());
//	//		}
//	//	};
//
//		try
//		{
//			workspace.setProver(new HolLight());
//			workspace.init(this);
//		}
//		catch (Throwable e)
//		{
//			showAlertDialog("Error", "Could not start HOL Light checkpoint. Check to see if you can launch the checkpoint from\na terminal and make sure the checkpoint script is not already running.");
//			throw new RuntimeException(e);
//		}
//
//		frame.setSize(ComposerProperties.frameWidth(), ComposerProperties.frameHeight());
//
//		public void popupHandler(MouseEvent e, CProcess service) {
//				menu.add(new InspectPiCalculusAction(Gui.this, service));
//				menu.add(new DeployServiceAction(Gui.this, service.getName()));
//
//		workspace.getGraph().getGraphEngine().getGraphComponent().setPreferredSize(new Dimension(ComposerProperties.frameWidth(), ComposerProperties.frameHeight()));
//
//		editorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, processesSplitPane, editorPanel);
//		//editorSplitPane.setOneTouchExpandable(true);
//		editorSplitPane.setDividerLocation(ComposerProperties.editorDividerLocation());
//
//		JPanel mainPanel = new JPanel();
//		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
//		mainPanel.setPreferredSize(new Dimension(ComposerProperties.frameWidth(), ComposerProperties.frameHeight()));
//		//mainPanel.add(availableServicesPanel);
//		//mainPanel.add(editorPanel);
//		mainPanel.add(editorSplitPane);
//		mainPanel.setBackground(Color.WHITE);
//
//		editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
//		editorPanel.setBackground(Color.WHITE);
//
//		editorAndConsoleSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, consolePanel);
//		editorAndConsoleSplitPane.setOneTouchExpandable(true);
//		editorAndConsoleSplitPane.setDividerLocation(ComposerProperties.consoleDividerLocation());
//		getContentPane().add(editorAndConsoleSplitPane);
//
//		welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
//		welcomeLabel.setBackground(Color.WHITE);
//		welcomeLabel.setOpaque(true);
//
//		showWelcomeScreen();
//
//		Log.d("ConsoleVisible preference: " + ComposerProperties.consoleVisible());
//		if (!ComposerProperties.consoleVisible())
//			consolePanel.setVisible(false);

//	}
//
//	public void showWelcomeScreen()
//	{
//		editorPanel.removeAll();
//		editorPanel.add(welcomeLabel);
//	}
//
//	public void setupMenubar(final Gui frame)
//	{
//		JMenuBar menuBar = new JMenuBar();
//		JMenu menu;
//
//		menu = newMenu(menuBar, "File", KeyEvent.VK_F);
//		menu.add(newAction);
//		menu.add(loadAction);
//		menu.add(saveAction);
//		menu.add(saveAsAction);
//		menu.add(exportImageAction);
//		menu.add(quitAction);
//		menu = newMenu(menuBar, "Edit", KeyEvent.VK_E);
//		menu.add(undoAction);
//		menu.add(redoAction);
//
//		menu = newMenu(menuBar, "View", KeyEvent.VK_V);
//		menu.add(toggleConsoleAction);
//		menu.add(layoutAction);
//
//		menu = newMenu(menuBar, "Ontologies", KeyEvent.VK_O);
//		menu.add(loadOntologyAction);
//		menu.add(setActiveOntologyAction);
//
//		menu = newMenu(menuBar, "Help", KeyEvent.VK_H);
//		menu.add(aboutAction);
//
//		frame.setJMenuBar(menuBar);
//	}
//
//
//	public void addLabelledToolbarButton(AbstractAction action)
//	{
//		JButton b = new JButton(action);
//		b.setText((String) action.getValue(AbstractAction.SHORT_DESCRIPTION));
//		toolbar.add(b);
//	}
//
//	public void setupProofScriptLogView(final Gui frame)
//	{
//		consolePanel.setLayout(new GridLayout(0, 2, 15, 15));
//
//		proofScriptTextArea = new JTextArea();
//		JScrollPane sp2 = new JScrollPane(proofScriptTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		JPanel panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//		JLabel label = new JLabel("Proof Script");
//		label.setIcon(new ImageIcon("third_party/silk_icons/script.png"));
//
//		panel.add(label);
//		panel.add(sp2);
//		consolePanel.add(panel);
//
//		final JTextArea holInputLogTextArea = new JTextArea();
//		sp2 = new JScrollPane(holInputLogTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//		label = new JLabel("Prover Console");
//		label.setIcon(new ImageIcon("third_party/silk_icons/book_open.png"));
//		panel.add(label);
//		panel.add(sp2);
//
//		JPanel commandLinePanel = new JPanel();
//		commandLinePanel.setLayout(new BoxLayout(commandLinePanel, BoxLayout.X_AXIS));
//		JButton commandSubmitButton = new JButton(); // TODO submitCommandAction);
//
//		commandLineTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, commandLineTextField.getPreferredSize().height));
//		commandLinePanel.add(commandLineTextField);
//
//		commandLinePanel.add(commandSubmitButton);
//		panel.add(commandLinePanel);
//		consolePanel.add(panel);
//
//		workspace.getProver().addInputLogChangeListener(new ProverStateListener() {
//			@Override
//			public void changed(HolLight prover)
//			{
//				holInputLogTextArea.setText(prover.getInputLog());
//				Utils.scrollToBottomOfTextArea(holInputLogTextArea);
//			}
//		});
//	}
//
//	public void showAlertDialog(String title, String message)
//	{
//		JOptionPane.showMessageDialog(Gui.this, message, title, 1);
//	}
//
//	private void hideWelcomeScreen()
//	{
//		editorPanel.removeAll();
//		editorPanel.add(workspace.getGraph().getGraphEngine().getGraphComponent());
//	}

}
