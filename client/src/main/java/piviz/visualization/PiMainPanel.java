/**
 * Created by Anja Bog. Do not edit this line.
 */
/**
 The MIT License

 Copyright (c) 2006 Anja Bog

 Permission is hereby granted, free of charge, to any person
 obtaining a copy of this software and associated documentation files
 (the "Software"), to deal in the Software without restriction, 
 including without limitation the rights to use, copy, modify, merge, 
 publish, distribute, sublicense, and/or sell copies of the Software, 
 and to permit persons to whom the Software is furnished to do so, 
 subject to the following conditions:

 The above copyright notice and this permission notice shall be included 
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
 IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package piviz.visualization;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.workflowfm.composer.utils.UIUtils;
import piviz.exceptions.PiExecutionException;
import piviz.exceptions.PiParserError;
import piviz.exceptions.RestrictionTableException;
import piviz.exceptions.VisualizationException;
import piviz.executionEngine.ASTReceive;
import piviz.executionEngine.ASTSend;
import piviz.executionEngine.ASTTau;
import piviz.executionEngine.PiExecutorInterface;
import piviz.helperClasses.EventValues;

import com.thoughtworks.xstream.XStream;
import com.workflowfm.composer.ui.UIAction;

/**
 * Main Panel of the Application
 * 
 * @author Anja
 */
public class PiMainPanel extends JPanel implements ActionListener, ListSelectionListener, GUIInterface
{

  // / For keeping in mind the users chosen execution path to dot
  private static Properties props = new Properties();

  // / Handle to visualization component
  private VisualizerInterface visualizer;

  private DefaultListModel scopeListModel;

  private JList scopeList;

  private JButton execStepButton;

  private JMenuItem dotExecTreeButton, dotDefTreeButton;

  private JMenuItem redoLast, importAgents, selectExecAgents;

  private JCheckBoxMenuItem scaleToFit, showExecDiag;

  private PiExecutorInterface executor;

  // / For the controller to get the information about the selected
  // communication
  private ASTTau currentlySelectedTauNode;

  // / For the controller to get the information about the selected
  // communication
  private ASTSend currentlySelectedSendNode;

  // / For the controller to get the information about the selected
  // communication
  private ASTReceive currentlySelectedReceiveNode;

  private File piFile;

  private JComponent visualizationPane;

  private JPanel piContent;

  private JTextArea originalPiProcess, currentPiProcess, editablePiProcess;

  private JLabel statusBar;

  private UIAction undoButton;

  private UIAction redoButton;

  private UIAction resetButton;

  // / For sending changed events to those who are interested.
  protected EventListenerList listenerList = new EventListenerList();

  private Stack<PiExecutorInterface> undoStack = new Stack<PiExecutorInterface>();
  private Stack<PiExecutorInterface> redoStack = new Stack<PiExecutorInterface>();

  private JPanel scopeNamesPane;
  
  private GraphListener graphListener;

  /**
   * Set the executor for this pi execution. Needed to get data for
   * visualization.
   */
  public void setExecutor(PiExecutorInterface _executor)
  {
    executor = _executor;
    executor.addActionListener(this);
  }

  /**
   * Return this GUI's executor
   * 
   * @return
   */
  public PiExecutorInterface getExecuter()
  {
    return this.executor;
  }

  /**
   * Get the currently selected process definition file.
   */
  public File getFile()
  {
    return this.piFile;
  }

  public PiMainPanel(File file)
  {

    super();

    this.piFile = null;
    this.executor = null;
    this.currentlySelectedReceiveNode = null;
    this.currentlySelectedSendNode = null;
    this.currentlySelectedTauNode = null;

    setLayout(new BorderLayout(10, 10));
    createWindowContent(this);
    //this.setJMenuBar(createMenu());
    createMenu();

    //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    //this.setLocation(50, 50);
    //this.setSize(dim.width - 100, dim.height - 100);
    //this.setVisible(true);

    // set up the properties file for dot execution path if not existent
    try
    {
      props.load(new FileInputStream("PiVizTool.properties"));
    } catch (Exception e)
    {
      // Create new properties file
      System.out.println("Creating new properties file...");
      String pathToDot = "dot";
      props.setProperty("pathToDot", pathToDot);
      saveProperties();

    }

    if (file != null)
      doOpenFile(file);
  }

  /**
   * save properties
   * 
   * 
   */
  private void saveProperties()
  {
    try
    {
      props.save(new FileOutputStream("PiVizTool.properties"), "PiVizTool Properties");
    } catch (FileNotFoundException e1)
    {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }

  /**
   * show Preferences Dialog
   * 
   * 
   */
  private void showPreferences()
  {
    String old = props.getProperty("pathToDot");
    String newPath = JOptionPane.showInputDialog(this, "Enter dot execution path:\n" + "Example:   C:\\Programme\\GraphvizDot\\Graphviz\\bin\\dot\n", old);
    if (newPath == null)
      newPath = old;
    props.setProperty("pathToDot", newPath);
    saveProperties();

  }

  private void addExecAgents()
  {
    try
    {
      if (executor != null)
      {
        ArrayList agents = executor.getAllDefinedAgents();
        selectExecutableAgents(agents);
        if (agents.size() > 0)
        {
          executor.addExecAgents(agents);
          visualize();
        }
      }
    } catch (PiExecutionException e)
    {
      // TODO Auto-generated catch block
      showErrorNoChanges(e.getMessage() + "\nAborting Addition.");
      e.printStackTrace();
    } catch (RestrictionTableException e)
    {
      // TODO Auto-generated catch block
      showErrorNoChanges(e.getMessage() + "\nAborting Addition.");
      e.printStackTrace();
    } catch (FileNotFoundException e)
    {
      // TODO Auto-generated catch block
      showErrorNoChanges(e.getMessage() + "\nAborting Addition.");
      e.printStackTrace();
    }
  }

  /**
   * Creates the menu and its items.
   * 
   * @return completely created menu
   */
  private JMenuBar createMenu()
  {
    // File Menu Items
    JMenuItem openFile = new JMenuItem("Open File...");
    openFile.setActionCommand("OpenFile");
    openFile.setMnemonic('O');
    openFile.addActionListener(this);
    openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));

    redoLast = new JMenuItem("Reload Last");
    redoLast.setMnemonic('R');
    redoLast.setActionCommand("RedoLastFile");
    redoLast.addActionListener(this);
    redoLast.setEnabled(false);
    redoLast.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));

    // button for printing the Execution Tree
    dotExecTreeButton = new JMenuItem("DotExecTree");
    dotExecTreeButton.setActionCommand("DotExecTree");
    dotExecTreeButton.setToolTipText("Dot the current execution tree.");
    dotExecTreeButton.addActionListener(this);
    dotExecTreeButton.setEnabled(false);

    // button for printing the Execution Tree
    dotDefTreeButton = new JMenuItem("DotDefTree");
    dotDefTreeButton.setActionCommand("DotDefTree");
    dotDefTreeButton.setToolTipText("Dot the definition tree.");
    dotDefTreeButton.addActionListener(this);
    dotDefTreeButton.setEnabled(false);

    JMenuItem quit = new JMenuItem("Quit");
    quit.setActionCommand("quit");
    quit.setMnemonic('Q');
    quit.addActionListener(this);
    quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));

    JMenuItem prefer = new JMenuItem("Set dot execution path...");
    prefer.setActionCommand("prefer");
    prefer.addActionListener(this);
    prefer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));

    // File Menu
    JMenu file = new JMenu("File");
    file.setMnemonic('F');
    file.add(openFile);
    file.add(redoLast);
    file.addSeparator();
    file.add(prefer);
    file.addSeparator();
    file.add(dotDefTreeButton);
    file.add(dotExecTreeButton);
    file.addSeparator();
    file.add(quit);

    // View Menu Items
    scaleToFit = new JCheckBoxMenuItem("Scale to Fit");
    scaleToFit.setState(true);

    final JCheckBoxMenuItem scopeNamesVisible = new JCheckBoxMenuItem("Show scoped names pane");
    scopeNamesVisible.setState(false);
    scopeNamesVisible.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        scopeNamesPane.setVisible(scopeNamesVisible.isSelected());
      }
    });

    // View Menu
    JMenu view = new JMenu("View");
    view.setMnemonic('V');
    view.add(scopeNamesVisible);
    view.add(scaleToFit);

    // Execution Menu Items
    showExecDiag = new JCheckBoxMenuItem("Ask before execution");
    showExecDiag.setState(false);

    importAgents = new JMenuItem("Import Agents...");
    importAgents.setMnemonic('I');
    importAgents.setActionCommand("importAgents");
    importAgents.addActionListener(this);
    importAgents.setEnabled(false);
    importAgents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));

    selectExecAgents = new JMenuItem("Select Agents for Execution...");
    selectExecAgents.setMnemonic('S');
    selectExecAgents.setActionCommand("selectExecAgents");
    selectExecAgents.addActionListener(this);
    selectExecAgents.setEnabled(false);
    selectExecAgents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));

    // Execution Menu
    JMenu executionMenu = new JMenu("Execution");
    executionMenu.setMnemonic('E');
    executionMenu.add(showExecDiag);
    executionMenu.addSeparator();
    executionMenu.add(importAgents);
    executionMenu.add(selectExecAgents);

    // Help Menu Items
    JMenuItem helpInfoButton = new JMenuItem("About PiVizTool");
    helpInfoButton.setMnemonic('A');
    helpInfoButton.setActionCommand("showAbout");
    helpInfoButton.addActionListener(this);

    JMenuItem helpOperationButton = new JMenuItem("Operation Tips");
    helpOperationButton.setMnemonic('O');
    helpOperationButton.setActionCommand("showOperationTips");
    helpOperationButton.addActionListener(this);

    // Help Menu
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic('H');
    helpMenu.add(helpOperationButton);
    helpMenu.add(helpInfoButton);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(file);
    menuBar.add(view);
    menuBar.add(executionMenu);
    menuBar.add(helpMenu);

    return menuBar;
  }

  public static void writeStringToFile(File file, String s) throws IOException
  {
    Writer writer = null;
    try
    {
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
      writer.write(s);
    } finally
    {
      if (writer != null)
        writer.close();
    }
  }

  /**
   * Creates the Layout Manager and the items within the layout manager.
   * 
   */
  private void createWindowContent(Container windowContent)
  {
    scopeNamesPane = new JPanel();
    BoxLayout layout = new BoxLayout(scopeNamesPane, BoxLayout.Y_AXIS);
    scopeNamesPane.setLayout(layout);
    JLabel label = new JLabel("Scoped names");
    scopeNamesPane.add(label);

    scopeListModel = new DefaultListModel();
    scopeList = new JList(scopeListModel);
    scopeList.addListSelectionListener(this);
    JScrollPane listScroller = new JScrollPane(scopeList);
    listScroller.setPreferredSize(new Dimension(100, 750));
    scopeNamesPane.add(listScroller);

    scopeNamesPane.setVisible(false);

    /**
     * Tabs for graphical representation of pi-system and string output of
     * original as well as current system.
     */
    JTabbedPane tabs = new JTabbedPane();

    /**
     * Panel for the graphical representation
     */
    piContent = new JPanel(new BorderLayout());
    tabs.addTab("Graphical Pi-Process", piContent);

    /**
     * Panel for the initial process definitions
     */
    originalPiProcess = new JTextArea();
    JScrollPane originalPiScroll = new JScrollPane(originalPiProcess);
    tabs.addTab("Original Pi-Process", originalPiScroll);
    originalPiProcess.setEditable(false);

    /**
     * Panel for the current state of the process definitions
     */
    currentPiProcess = new JTextArea();
    JScrollPane currenPiScroll = new JScrollPane(currentPiProcess);
    tabs.addTab("Current Pi-Process", currenPiScroll);
    currentPiProcess.setEditable(false);

    editablePiProcess = new JTextArea();
    editablePiProcess.setEditable(true);
    JScrollPane editablePiScroll = new JScrollPane(editablePiProcess);
    JPanel bPanel = new JPanel(new GridLayout(1, 2));
    JButton visualizeButton = new JButton("Visualise");
    visualizeButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        try
        {
          File temp = File.createTempFile("edit", ".txt");
          temp.deleteOnExit();
          writeStringToFile(temp, editablePiProcess.getText());
          doOpenFile(temp);
        } catch (IOException e1)
        {
          showError(e1.getMessage());
        }
      }
    });
    bPanel.add(visualizeButton);
    JButton saveAsButton = new JButton("Save as...");
    saveAsButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        if (fc.showSaveDialog(PiMainPanel.this) == JFileChooser.APPROVE_OPTION)
        {
          try
          {
            writeStringToFile(fc.getSelectedFile(), editablePiProcess.getText());
          } catch (IOException e1)
          {
            showError(e1.getMessage());
          }
        }
      }
    });
    bPanel.add(saveAsButton);
    JPanel ePanel = new JPanel(new BorderLayout());
    ePanel.add(editablePiScroll, BorderLayout.CENTER);
    ePanel.add(bPanel, BorderLayout.SOUTH);
    tabs.addTab("Editable Pi-Process", ePanel);

    visualizationPane = new JScrollPane();
    piContent.add(visualizationPane, BorderLayout.CENTER);

    // Create Status Bar
    statusBar = new JLabel();
    statusBar.setText(" ");

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(scopeNamesPane, BorderLayout.WEST);
    mainPanel.add(tabs, BorderLayout.CENTER);

    windowContent.add(createToolBar(), BorderLayout.PAGE_START);
    windowContent.add(mainPanel, BorderLayout.CENTER);
    windowContent.add(statusBar, BorderLayout.PAGE_END);

    updateButtonAndLabelStatuses();
  }

  /**
   * Create the toolbar.
   * 
   * @return
   */
  private JToolBar createToolBar()
  {
    JToolBar tB = new JToolBar();

    // create auto execute next step Button
    execStepButton = new JButton(UIUtils.getIcon("silk_icons/control_play_blue.png"));
    execStepButton.setActionCommand("AutoExecNextStep");
    execStepButton.setToolTipText("Auto exectute the next step (chosen by simulator).");
    execStepButton.addActionListener(this);
    execStepButton.setEnabled(false);

    undoButton = new UIAction("Undo", "silk_icons/arrow_undo.png", KeyEvent.VK_U, KeyEvent.VK_Z, ActionEvent.CTRL_MASK)
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        redoStack.push(getCloneOfExecutor());
        restoreExecutorState(undoStack.pop());
      }
    };

    redoButton = new UIAction("Redo", "silk_icons/arrow_redo.png", KeyEvent.VK_R, KeyEvent.VK_Y, ActionEvent.CTRL_MASK)
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        undoStack.push(getCloneOfExecutor());
        restoreExecutorState(redoStack.pop());
      }
    };

    resetButton = new UIAction("Reset", "silk_icons/flag_green.png", KeyEvent.VK_R)
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        redoStack.push(getCloneOfExecutor());
        while (undoStack.size() > 1)
          redoStack.push(undoStack.pop());
        restoreExecutorState(undoStack.pop());
      }
    };

    tB.add(execStepButton);
    tB.add(resetButton);
    tB.add(undoButton);
    tB.add(redoButton);

    return tB;
  }

  private void restoreExecutorState(PiExecutorInterface newExecutor)
  {
    setExecutor(newExecutor);
    createVisualizer();
    setStateAfterExecution(true, true);
  }

  private void showAbout()
  {
    JOptionPane.showMessageDialog(this, "PiVizTool v1.0 by Anja Bog", "About PiVizTool", JOptionPane.INFORMATION_MESSAGE);
  }

  private void showOperationTips()
  {
    JOptionPane.showMessageDialog(this, "The flow graph consists of oval and rectangle nodes/subgraphs. Oval\n"
        + "nodes represent processes, rectangle nodes represent pools/abstract\n" + "processes.\n"
        + "Distinguish between active and passive capabilities of processes. Shaded\n"
        + "links between processes are currently inactive/blocked and can therefore\n" + "not be selected for execution.\n" + "Process Execution:\n"
        + "  - Click on shaded oval nodes to perform a tau action.\n" + "  - Click on black links to perform a communication.\n"
        + "  - Click on rectangle pools to open or close them.\n", "Operation Tips", JOptionPane.PLAIN_MESSAGE);
  }

  private PiExecutorInterface getCloneOfExecutor()
  {
    XStream xstream = new XStream();
    String xml = xstream.toXML(executor);
    return (PiExecutorInterface) xstream.fromXML(xml);
  }

  /** ******************************************************************** */
  /**
   * ActionListener Interface
   */
  public void actionPerformed(ActionEvent e)
  {
    redoStack.clear();
    undoStack.push(getCloneOfExecutor());

    try
    {
      if (e.getSource() == executor)
      {
        int id = e.getID();
        if (id == EventValues.SELECT_EXEC_AGENTS)
          selectExecutableAgents(executor.getCurrentlyDefinedAgents());
        else if (id == EventValues.SHOW_MESSAGE)
          showMessage(executor.getMessage());
      } else
      {
        String cmd = e.getActionCommand();
        // Events handled by the controller
        if (cmd.equals("RedoLastFile"))
        {
          System.out.println("Redo last file...");
          // doOpenFile();
          fireActionPerformed(new ActionEvent(this, EventValues.START_EXECUTION, "startExecution"));
        } else if (cmd.equals("DotExecTree"))
          fireActionPerformed(new ActionEvent(this, EventValues.DOT_EXEC_TREE, "dotExecTree"));
        else if (cmd.equals("DotDefTree"))
          fireActionPerformed(new ActionEvent(this, EventValues.DOT_DEFINITION_TREE, "dotDefTree"));
        else if (cmd.equals("AutoExecNextStep"))
        {
          fireActionPerformed(new ActionEvent(this, EventValues.AUTO_EXECUTION, "autoExecute"));
        } // internal events
        else if (cmd.equals("OpenFile"))
        {
          doOpenFile(null);
        } else if (cmd.equals("quit"))
          return; //TODO what to do what to do? this.dispose();
        else if (cmd.equals("showAbout"))
          showAbout();
        else if (cmd.equals("prefer"))
          showPreferences();
        else if (cmd.equals("showOperationTips"))
          showOperationTips();
        else if (cmd.equals("updateGrappaPanel"))
          visualize();
        else if (cmd.equals("executeTauNode"))
          executeTauNode();
        else if (cmd.equals("executeCommunicationNoVisUpdate"))
          executeCommunicationNoVis();
        else if (cmd.equals("executeTauNodeNoVisUpdate"))
          executeTauNodeNoVis();
        else if (cmd.equals("executeCommunication"))
          executeCommunication();
        else if (cmd.equals("importAgents"))
          importAgents();
        else if (cmd.equals("selectExecAgents"))
          addExecAgents();
      }
    } catch (IOException e1)
    {
      // TODO Auto-generated catch block
      showError(e1.getMessage());
      e1.printStackTrace();
    }
  }

  /** ******************************************************************** */

  /**
   * Show the user a dialog for importing agents and call the appropriate
   * methods of the executor if selection looks valid.
   */
  private void importAgents()
  {
//    try
//    {
//    // (JSwingUtilities.windowForComponent(panel)
//      ImportAgentsDialog dia = new ImportAgentsDialog(this);
//      dia.setLocationRelativeTo(this);
//      Reader reader = dia.open();
//      if (reader != null)
//      {
//        if (executor != null)
//        {
//          executor.importAgents(reader);
//          visualizer.updateAfterAgentImport();
//          visualize();
//        }
//      }
//    } catch (PiParserError e)
//    {
//      // TODO Auto-generated catch block
//      showErrorNoChanges(e.getMessage() + "\nAborting import.");
//      e.printStackTrace();
//    } catch (Exception e)
//    {
//      // TODO Auto-generated catch block
//      showErrorNoChanges(e.getMessage());
//      e.printStackTrace();
//    }
  }

  /**
   * Open a file and trigger to start the execution of the pi definitions.
   * 
   * @param file
   *          the file to open or, if this is set to null, the user is asked to
   *          select a file.
   */
  public void doOpenFile(File file)
  {
    try
    {
      System.out.println("Open File...");

      if (file == null)
      {
        JFileChooser ch = new JFileChooser();
        ch.showOpenDialog(this);
        file = ch.getSelectedFile();
      }

      if (file != null)
      {
        this.piFile = file;
        ActionEvent e = new ActionEvent(this, EventValues.START_EXECUTION, "startExecution");
        fireActionPerformed(e);

        originalPiProcess.setText(getOriginalFileString());
        editablePiProcess.setText(getOriginalFileString());

      }
    } catch (IOException e1)
    {
      showError(e1.getMessage());
      e1.printStackTrace();
    } catch (VisualizationException e1)
    {
      showError(e1.getMessage());
      e1.printStackTrace();
    }

  }

  /**
   * See if execution is possible and if so start.
   * 
   */
  public void startNewExecution(boolean executionPossible)
  {
    try
    {
      if (piFile != null)
      {

        if (executor == null)
        {
          showError("MainFrame internal error: Pi execution engine is not set (null).");
          return;
        }

        createVisualizer();

        scopeListModel.clear();
        undoStack.clear();
        redoStack.clear();

        visualize();
        execStepButton.setEnabled(executionPossible);
        dotExecTreeButton.setEnabled(executionPossible);
        dotDefTreeButton.setEnabled(executionPossible);
        importAgents.setEnabled(executionPossible);
        selectExecAgents.setEnabled(executionPossible);
        redoLast.setEnabled(true);

        updateButtonAndLabelStatuses();
      }

    } catch (FileNotFoundException e)
    {
      // TODO Auto-generated catch block
      showError(e.getMessage());
      e.printStackTrace();
    } catch (PiParserError e)
    {
      // TODO Auto-generated catch block
      showError(e.getMessage());
      e.printStackTrace();
    } catch (Exception e)
    {
      // TODO Auto-generated catch block
      showError(e.getMessage());
      e.printStackTrace();
    }

  }

  private void updateButtonAndLabelStatuses()
  {
    undoButton.setEnabled(!undoStack.isEmpty());
    resetButton.setEnabled(!undoStack.isEmpty());
    redoButton.setEnabled(!redoStack.isEmpty());

    execStepButton.setEnabled(executor != null && executor.hasExecutableSteps());
    statusBar.setText(executor == null ? " " : " Step " + executor.getExecutionsPerformed() + " - " + executor.getPreviousAction());
  }

  /**
   * Visualization after execution if necessary.
   * 
   * @param moreSteps
   *          more steps are available.
   * @param updateVis
   *          true => visible interaction has taken place, update the
   *          visualization, false => no visible interaction has taken place, no
   *          update of visualization.
   */
  public void setStateAfterExecution(boolean moreSteps, boolean updateVis)
  {
    try
    {
      updateButtonAndLabelStatuses();

      if (updateVis)
        visualize();

      // TODO: update the current process definitions view if necessary
      String str = executor.getCurrentProcessDefinitions();
      currentPiProcess.setText(str);

    } catch (FileNotFoundException e)
    {
      // TODO Auto-generated catch block
      showError(e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Trigger the execution of the step within a closed pool. Either a send and a
   * receive node or a tau node is given for execution. No visualization update
   * will be triggered.
   * 
   * @param send
   *          SendNode to be executed.
   * @param receive
   *          ReceiveNode to be executed.
   * @param tau
   *          TauNode to be executed.
   */

  private int showYesNoDialog(String message)
  {
    if (showExecDiag.getState())
      return JOptionPane.showConfirmDialog(this, message, "Execution", JOptionPane.YES_NO_OPTION);
    else
      return JOptionPane.YES_OPTION;

  }

  public void showErrorNoChanges(String message)
  {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Display the error in a message box.
   * 
   * @param message
   *          Error message.
   */
  public void showError(String message)
  {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    piContent.remove(visualizationPane);
    piContent.repaint();

  }

  /**
   * Display a message box.
   * 
   * @param message
   */
  public void showMessage(String message)
  {
    JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.INFORMATION_MESSAGE);
  }

  private void updateScopeList()
  {
    Set keys = executor.getScopes().keySet();
    Iterator iter = keys.iterator();
    while (iter.hasNext())
    {
      String channel = (String) iter.next();
      if (!scopeListModel.contains(channel))
        scopeListModel.addElement(channel);
    }

    for (Enumeration e = scopeListModel.elements(); e.hasMoreElements();)
    {
      String scope = (String) e.nextElement();
      if (!keys.contains(scope))
        scopeListModel.removeElement(scope);
    }
  }

  private void visualize() throws FileNotFoundException
  {

    System.out.print("Creating visualization...");
    long startMillis = System.currentTimeMillis();

    piContent.remove(visualizationPane);

    // ArrayList scopesToShow = new ArrayList();
    Hashtable scopeTable = new Hashtable();
    // (ArrayList) executor.getScopes().get( selectedScope);
    Object[] selectedVals = scopeList.getSelectedValues();
    for (int i = 0; i < selectedVals.length; i++)
    {
      String scope = (String) selectedVals[i];
      // scopesToShow.addAll((ArrayList) executor.getScopes().get(scope));
      TreeMap scopes = (TreeMap) executor.getScopes();
      if (scopes.containsKey(scope))
        scopeTable.put(scope, (ArrayList) executor.getScopes().get(scope));
    }

    if (executor.hasExecutableSteps())
    {
      try
      {
        visualizationPane = visualizer.createVisualOutput(executor.getListOfExecutableTauNodes(), executor.getListOfExecutableCommunications(),
            executor.getListOfRunningProcesses(), executor.getListOfBlockedCommunications(), scopeTable, scaleToFit.getState());
      } catch (FileNotFoundException e)
      {
        showError(e.getMessage());
        e.printStackTrace();
      } catch (VisualizationException e)
      {
        showError(e.getMessage());
        visualizationPane = null;
        e.printStackTrace();
      }
    } else
    {
      JLabel label = new JLabel("No execution steps available.");
      label.setHorizontalAlignment(SwingConstants.CENTER);
      visualizationPane = label;
    }

    updateScopeList();

    if (visualizationPane == null)
    {
      visualizationPane = new JScrollPane();
    }
    piContent.add(visualizationPane, BorderLayout.CENTER);

    this.show();

    // update the current process definitions view if
    // necessary
    String str = executor.getCurrentProcessDefinitions();
    currentPiProcess.setText(str);

    long endMillis = System.currentTimeMillis();
    System.out.println("finished after: " + (endMillis - startMillis) + " milliseconds.");

  }

  /**
   * Trigger execution of selected tau node with visual update.
   * 
   */
  private void executeTauNode()
  {
    int choice = showYesNoDialog("Execute selected tau node?");
    if (choice == JOptionPane.NO_OPTION)
    {
      visualizer.clearSelection();
      return;
    }
    this.currentlySelectedTauNode = visualizer.getCurrentlySelectedTauNode();

    fireActionPerformed(new ActionEvent(this, EventValues.EXECUTE_TAU, "executeTau"));
  }

  /**
   * Trigger execution of tau node without visual update.
   * 
   */
  private void executeTauNodeNoVis()
  {
    this.currentlySelectedTauNode = visualizer.getCurrentlySelectedTauNode();

    fireActionPerformed(new ActionEvent(this, EventValues.EXECUTE_TAU_INVISIBLY, "executeTauInvisibly"));
  }

  /**
   * Trigger execution of communication without visual update.
   * 
   */
  private void executeCommunication()
  {

    int choice = showYesNoDialog("Execute selected communication?");
    if (choice == JOptionPane.NO_OPTION)
    {
      visualizer.clearSelection();
      return;
    }

    this.currentlySelectedReceiveNode = visualizer.getCurrentlySelectedReceiveNode();
    this.currentlySelectedSendNode = visualizer.getCurrentlySelectedSendNode();

    // fire the event
    fireActionPerformed(new ActionEvent(this, EventValues.EXECUTE_COMMUNICATION, "executeCommunication"));
  }

  /**
   * Trigger execution of communication without visual update.
   * 
   */
  private void executeCommunicationNoVis()
  {
    this.currentlySelectedReceiveNode = visualizer.getCurrentlySelectedReceiveNode();
    this.currentlySelectedSendNode = visualizer.getCurrentlySelectedSendNode();

    fireActionPerformed(new ActionEvent(this, EventValues.EXECUTE_COMMUNICATION_INVISIBLY, "executeCommunicationInvisbly"));
  }

  public void valueChanged(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting() == false)
    {
      try
      {
        visualize();
      } catch (FileNotFoundException e1)
      {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }

  /** ****************ActionListener Methoden********************************* */

  public void addActionListener(ActionListener listener)
  {
    listenerList.add(ActionListener.class, listener);
  }

  public void removeActionListener(ActionListener listener)
  {
    listenerList.remove(ActionListener.class, listener);
  }

  protected void fireActionPerformed(ActionEvent e)
  {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();

    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2)
    {
      if (listeners[i] == ActionListener.class)
      {
        ((ActionListener) listeners[i + 1]).actionPerformed(e);
      }
    }
  }

  public ASTReceive getCurrentlySelectedReceiveNode()
  {
    return currentlySelectedReceiveNode;
  }

  public ASTSend getCurrentlySelectedSendNode()
  {
    return currentlySelectedSendNode;
  }

  public ASTTau getCurrentlySelectedTauNode()
  {
    return currentlySelectedTauNode;
  }

  private String getOriginalFileString() throws IOException, VisualizationException
  {
    String contents = "";

    if (piFile == null)
      throw new VisualizationException("Original file can not be displayed, because none is chosen.");

    BufferedReader in = new BufferedReader(new FileReader(piFile));
    String str;
    while ((str = in.readLine()) != null)
    {
      contents = contents + str + "\n";
    }
    in.close();

    return contents;
  }

  /**
   * Use agents to create a dialog where the user may select the agents, that he
   * wants to have executed.
   * 
   * @param agents
   */
  private void selectExecutableAgents(ArrayList agents)
  {
//    ArrayList selected = new ArrayList();
//    SelectAgentsDialog dialog = new SelectAgentsDialog(this, agents);
//    dialog.setLocationRelativeTo(this);
//    selected = dialog.open();
//    agents.clear();
//    agents.addAll(selected);
  }

  private void createVisualizer()
  {
    visualizer = new Visualizer(executor.getPoolTable(), props);
    visualizer.addActionListener(this);
    
    if (graphListener != null)
      visualizer.setGraphListener(graphListener);
  }
  
  public void setGraphListener(GraphListener graphListener)
  {
    this.graphListener = graphListener;
    visualizer.setGraphListener(graphListener);
  }

  public void setEmbeddedPanel(JPanel embeddedPanel)
  {
    BorderLayout layout = (BorderLayout) getLayout();
    Component mainPanel = layout.getLayoutComponent(BorderLayout.CENTER);

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, embeddedPanel);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(400);

    remove(mainPanel);
    add(splitPane, BorderLayout.CENTER);
  }
}
