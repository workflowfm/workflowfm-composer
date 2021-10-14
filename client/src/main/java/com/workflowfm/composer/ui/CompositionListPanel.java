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
import com.workflowfm.composer.session.actions.RedoAction;
import com.workflowfm.composer.session.actions.SaveAction;
import com.workflowfm.composer.session.actions.ShowProcessGraphAction;
import com.workflowfm.composer.session.actions.UndoAction;
import com.workflowfm.composer.utils.SortedComboBoxModel;
import com.workflowfm.composer.utils.UIUtils;
import com.workflowfm.composer.workspace.Workspace;
import com.workflowfm.composer.workspace.actions.AddProcessGraphAction;
import com.workflowfm.composer.workspace.actions.DeleteIntermediateProcessAction;
import com.workflowfm.composer.workspace.actions.StoreProcessAction;
import com.workflowfm.composer.workspace.actions.VerifyIntermediateProcessAction;
import com.workflowfm.composer.workspace.actions.VerifyIntermediateProcessAndComponentsAction;

public class CompositionListPanel implements ProcessStoreChangeListener {

	private Workspace workspace;
	private WindowManager manager;
	private ExceptionHandler exceptionHandler;

	// Note that DefaultComboBoxModel implements DefaultListModel! 
	private SortedComboBoxModel<String> compositionsListModel = new SortedComboBoxModel<String>(); //new DefaultComboBoxModel<String>();

	private JPanel panel;
	private JScrollPane listScroller;

	public CompositionListPanel(Workspace workspace, WindowManager manager){
		this.workspace = workspace;
		this.manager = manager;
	}

	public JPanel getPanel() {
		return this.panel;
	}

	public void setup() {
		this.workspace.addChangeListener(this); // I would build a dispose method, but the panel's life should end with the workspace
		setupPanel();
		setupKeys();
	}

	private void setupPanel() {
		this.panel = new JPanel();
		this.exceptionHandler = new ComponentExceptionHandler(panel);

		for (CProcess process : workspace.getCompositions())
			processAdded(process);

		final JList<String> processList = new JList<String>(compositionsListModel);
		processList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		processList.setLayoutOrientation(JList.VERTICAL);
		processList.setVisibleRowCount(-1);
		processList.setCellRenderer(new ProcessCellRenderer(workspace));

		processList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				processList.setSelectedIndex(processList.locationToIndex(e.getPoint()));
				String selectedProcessName = (String) processList.getSelectedValue();

				if (selectedProcessName == null)
					return;

				try {
					CProcess process = workspace.getComposition(selectedProcessName);

					if (e.getButton() == 3 || e.isPopupTrigger() || (e.getButton() == 1 && e.isControlDown()))
					{
						popup(e,process);
					}
					else if (e.getClickCount() == 2)
					{
						new AddProcessGraphEdit(process, workspace.getSession(), exceptionHandler, workspace.getGraph()).apply();
					}
				} catch (NotFoundException e1) {
					throw new RuntimeException("Clicked on a CompositionListPanel process that was not found in the Workspace.", e1);
				}
			}		
		});

		listScroller = new JScrollPane(processList);

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		panel.add(UIUtils.createImageLabel("Compositions", "silk_icons/text_list_bullets.png"), BorderLayout.NORTH);
		panel.add(listScroller, BorderLayout.SOUTH);
	}

	private void setupKeys() {
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK),"undoKey");
		panel.getActionMap().put("undoKey", new UndoAction(workspace.getSession(),exceptionHandler));
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK),"redoKey");
		panel.getActionMap().put("redoKey", new RedoAction(workspace.getSession(),exceptionHandler));
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK),"saveKey");
		panel.getActionMap().put("saveKey", new SaveAction(getPanel(), workspace.getSession(), exceptionHandler));
	}

	protected void popup(MouseEvent e, CProcess process) {
		JPopupMenu menu = new JPopupMenu();

		menu.add(new AddProcessGraphAction(workspace, exceptionHandler, process));
		menu.add(new ShowProcessGraphAction(process, manager, workspace.getSession(), exceptionHandler));
		menu.addSeparator();
		menu.add(new VerifyIntermediateProcessAction(process, workspace, exceptionHandler));
		menu.add(new VerifyIntermediateProcessAndComponentsAction(process, workspace, exceptionHandler));
		menu.addSeparator();
		if (process.isValid()) {
			menu.add(new StoreProcessAction(process, manager, workspace, exceptionHandler));
			menu.addSeparator();
		}
		//menu.addSeparator();
		//menu.add(new InspectPiCalculusAction(Gui.this, service));
		//menu.add(new DeployServiceAction(Gui.this, service.getName()));

		menu.add(new DeleteIntermediateProcessAction(process, workspace, exceptionHandler));
		menu.show(panel, 
				e.getX() - listScroller.getHorizontalScrollBar().getValue(), 
				e.getY() - listScroller.getVerticalScrollBar().getValue());
	}


	@Override
	public void processAdded(CProcess process) {
		compositionsListModel.addElement(process.getName());
	}

	@Override
	public void processUpdated(String name, CProcess process) {
		if (!name.equals(process.getName())) {
			compositionsListModel.removeElement(name);
			compositionsListModel.addElement(process.getName());
		}
		panel.repaint();
	}

	@Override
	public void processRemoved(CProcess process) {
		compositionsListModel.removeElement(process.getName());
	}

}
