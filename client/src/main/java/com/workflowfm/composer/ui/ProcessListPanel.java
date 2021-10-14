package com.workflowfm.composer.ui;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import com.workflowfm.composer.edit.graph.AddProcessGraphEdit;
import com.workflowfm.composer.exceptions.ComponentExceptionHandler;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ProcessStoreChangeListener;
import com.workflowfm.composer.processes.ui.ProcessCellRenderer;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.session.CompositionSessionChangeListener;
import com.workflowfm.composer.session.actions.CreateProcessCopyAction;
import com.workflowfm.composer.session.actions.DeleteProcessAction;
import com.workflowfm.composer.session.actions.EditProcessAction;
import com.workflowfm.composer.session.actions.LoadCompositionsAction;
import com.workflowfm.composer.session.actions.PiLibAction;
import com.workflowfm.composer.session.actions.PiVizAction;
import com.workflowfm.composer.session.actions.RedoAction;
import com.workflowfm.composer.session.actions.SaveAction;
import com.workflowfm.composer.session.actions.ShowProcessGraphAction;
import com.workflowfm.composer.session.actions.UndoAction;
import com.workflowfm.composer.session.actions.VerifyProcessAction;
import com.workflowfm.composer.session.actions.VerifyProcessAndComponentsAction;
import com.workflowfm.composer.utils.SortedComboBoxModel;
import com.workflowfm.composer.utils.UIUtils;
import com.workflowfm.composer.workspace.Workspace;
import com.workflowfm.composer.workspace.actions.AddProcessGraphAction;

public class ProcessListPanel implements CompositionSessionChangeListener, ProcessStoreChangeListener {

	private CompositionSession session;
	private WindowManager manager;

	// Note that DefaultComboBoxModel implements DefaultListModel! 
	private SortedComboBoxModel<String> processesListModel = new SortedComboBoxModel<String>(); //new DefaultComboBoxModel<String>(); 

	private JPanel panel;
	private JScrollPane listScroller;
	private ExceptionHandler exceptionHandler;

	public ProcessListPanel(CompositionSession session, WindowManager manager) {
		this.session = session;
		this.manager = manager;
	}

	public JPanel getPanel() {
		return this.panel;
	}

	public void setup() {
		this.session.addChangeListener((CompositionSessionChangeListener)this);
		this.session.addChangeListener((ProcessStoreChangeListener)this);
		setupPanel();
		setupKeys();
	}

	private void setupPanel() {
		this.panel = new JPanel();
		this.exceptionHandler = new ComponentExceptionHandler(panel);

		final JList<String> processList = new JList<String>(processesListModel);
		processList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		processList.setLayoutOrientation(JList.VERTICAL);
		processList.setVisibleRowCount(-1);
		processList.setCellRenderer(new ProcessCellRenderer(session));

		processList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				processList.setSelectedIndex(processList.locationToIndex(e.getPoint()));
				String selectedProcessName = (String) processList.getSelectedValue();

				if (selectedProcessName == null)
					return;

				try { 
					CProcess process = session.getProcess(selectedProcessName);

					if (e.getButton() == 3 || e.isPopupTrigger() || (e.getButton() == 1 && e.isControlDown()))
					{
						popup(e,process);
					}
					else if (e.getClickCount() == 2)
					{
						if (session.getActiveWorkspace() != null)
							new AddProcessGraphEdit(process, session, exceptionHandler, session.getActiveWorkspace().getGraph()).apply();
					}
				} catch (NotFoundException e1) {
					throw new RuntimeException("Clicked on a ProcessListPanel process that was not found in the Session.", e1);
				}
			}		
		});

		listScroller = new JScrollPane(processList);

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		panel.add(UIUtils.createImageLabel("Processes", "silk_icons/text_list_bullets.png"), BorderLayout.NORTH);
		panel.add(listScroller, BorderLayout.SOUTH);
	}

	private void setupKeys() {
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK),"undoKey");
		panel.getActionMap().put("undoKey", new UndoAction(session, exceptionHandler));
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK),"redoKey");
		panel.getActionMap().put("redoKey", new RedoAction(session, exceptionHandler));
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK),"saveKey");
		panel.getActionMap().put("saveKey", new SaveAction(getPanel(), session, exceptionHandler));
	}

	protected void popup(MouseEvent e, CProcess process) {
		JPopupMenu menu = new JPopupMenu();

		boolean sep1 = false;
		if (session.getActiveWorkspace() != null) {
			menu.add(new AddProcessGraphAction(session.getActiveWorkspace(), exceptionHandler, process));
			sep1 = true;
		}
		if (process.isComposite()) {
			menu.add(new ShowProcessGraphAction(process, manager, session, exceptionHandler));
			sep1 = true;
		}
		if (sep1) menu.addSeparator();
		if (process.isComposite()) {
			menu.add(new LoadCompositionsAction(session, exceptionHandler, process));
		} else {
			menu.add(new EditProcessAction(process.getName(), manager, session, exceptionHandler));
			menu.add(new CreateProcessCopyAction(process.getName(), manager, session, exceptionHandler));
		}
		menu.addSeparator();
		menu.add(new VerifyProcessAction(process, session, exceptionHandler));
		menu.add(new VerifyProcessAndComponentsAction(process, session, exceptionHandler));
		menu.addSeparator();
		menu.add(new PiVizAction(process, manager, session, exceptionHandler));
		if (process.isComposite()) menu.add(new PiLibAction(process.getName(), manager, session, exceptionHandler));
		menu.addSeparator();
		menu.add(new DeleteProcessAction(process, session, exceptionHandler));
		menu.show(panel, 
				e.getX() - listScroller.getHorizontalScrollBar().getValue(), 
				e.getY() - listScroller.getVerticalScrollBar().getValue());
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
}
