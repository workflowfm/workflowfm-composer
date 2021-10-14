package com.workflowfm.composer.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.workflowfm.composer.edit.RemoveWorkspaceEdit;
import com.workflowfm.composer.exceptions.ComponentExceptionHandler;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.session.CompositionSessionChangeListener;
import com.workflowfm.composer.session.actions.RedoAction;
import com.workflowfm.composer.session.actions.SaveAction;
import com.workflowfm.composer.session.actions.UndoAction;
import com.workflowfm.composer.utils.CloseableTabPanel;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.Workspace;

public class CompositionSessionUI implements CompositionSessionChangeListener, WindowManager {
	private CompositionSession session;

	private SessionToolbar toolbar;
	private ProcessListPanel processPanel;

	private JSplitPane processesSplitPane;
	private JSplitPane editorSplitPane;

	private JTabbedPane tabbedPane;

	private JPanel panel;
	private ExceptionHandler exceptionHandler;

	private Map<String, Window> windowMap = new HashMap<String, Window>();

	private CopyOnWriteArrayList<WindowManagerChangeListener> windowChangeListeners = new CopyOnWriteArrayList<WindowManagerChangeListener>();

	private UndoAction undoAction;
	private RedoAction redoAction;
	
	private SaveAction saveAction;

	public CompositionSessionUI(CompositionSession session) {
		this.session = session;
	}

	public CompositionSession getSession() {
		return this.session;
	}

	public JPanel getPanel() {
		return this.panel;
	}

	protected JTabbedPane getTabbedPane() {
		return this.tabbedPane;
	}

	//	public JComponent getGraphComponent() {
	//		return this.workspace.getGraph().getGraphEngine().getGraphComponent();
	//	}

	public void setup()
	{
		this.session.addChangeListener(this);

		this.panel = new JPanel(new BorderLayout()); //new BoxLayout(this, BoxLayout.Y_AXIS)
		this.exceptionHandler = new ComponentExceptionHandler(panel);
		//panel.setBackground(Color.WHITE);

		this.processPanel = new ProcessListPanel(session, this);
		this.processPanel.setup();

		this.undoAction = new UndoAction(session, exceptionHandler);
		this.redoAction = new RedoAction(session, exceptionHandler);
		
		this.saveAction = new SaveAction(panel, session, exceptionHandler);

		setupTabbedPane();

		setupProcessSplitPane();
		setupEditorSplitPane();
		//JSplitPane editorAndConsoleSplitPane;

		this.panel = new JPanel(new BorderLayout());
		this.panel.add(editorSplitPane,BorderLayout.CENTER);

		this.toolbar = new SessionToolbar(session,this);
		this.toolbar.setup();

		this.panel.add(toolbar.getToolbar(),BorderLayout.PAGE_START);
		session.updateUndoRedoStatus();

		setupKeys();
	}

	private void setupTabbedPane() {
		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
				int index = sourceTabbedPane.getSelectedIndex();
				if (index != -1) {
					Log.d("Tab changed to: " + sourceTabbedPane.getTitleAt(index));
				} else {
					Log.d("Selected null tab.");	
				}

				Window activeWindow = getActiveWindow();
				if (activeWindow instanceof WorkspaceUI) {
					session.setActiveWorkspace(activeWindow.getName());
				} else {
					session.setActiveWorkspace(null);
				}

				// Notify listeners
				for (WindowManagerChangeListener listener : windowChangeListeners) {
					listener.windowActivated(getActiveWindow());
				}
			}
		};
		this.tabbedPane.addChangeListener(changeListener);
	}

	private void setupEditorSplitPane() {
		this.editorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, processesSplitPane, tabbedPane);
		this.editorSplitPane.setDividerLocation(ComposerProperties.editorDividerLocation());
	}

	private void setupProcessSplitPane() {
		this.processesSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, processPanel.getPanel(), new JPanel());
		this.processesSplitPane.setDividerLocation(ComposerProperties.processesDividerLocation());
	}

	private void setupKeys() {
		//panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),"removeBranch");
		//panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),"removeBranch");
		//panel.getActionMap().put("removeBranch", deleteBranchAction);
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK),"undoKey");
		panel.getActionMap().put("undoKey", undoAction);
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK),"redoKey");
		panel.getActionMap().put("redoKey", redoAction);
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK),"saveKey");
		panel.getActionMap().put("saveKey", saveAction);
	}	

	private CloseableTabPanel getTab(Window window) throws NotFoundException {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (tabbedPane.getComponentAt(i).getName() != null && tabbedPane.getComponentAt(i).getName().equals(window.getName())) {
				return (CloseableTabPanel)tabbedPane.getTabComponentAt(i);
			}
		}
		throw new NotFoundException("tab", window.getName());
	}

	@Override
	public void workspaceAdded(final Workspace workspace) {
		WorkspaceUI workspaceUI = new WorkspaceUI(workspace,this);
		workspaceUI.show();
		//addWindow(workspaceUI);

		try {
			final CloseableTabPanel tab = getTab(workspaceUI);
			for (ActionListener l : tab.getCloseButton().getActionListeners()) 
				tab.getCloseButton().removeActionListener(l);
			tab.getCloseButton().addActionListener(new ActionListener() {	
				@Override
				public void actionPerformed(ActionEvent e) {
					new RemoveWorkspaceEdit(workspace, session, exceptionHandler).apply();				
				}
			});
		} catch (NotFoundException e) {
			throw new RuntimeException("Failed to find tab of an added workspace!", e);
		}
	}

	@Override
	public void workspaceActivated(Workspace workspace) {
		if (workspace == null) {
			// Update the composition list panel.
			int dividerLocation = processesSplitPane.getDividerLocation();
			processesSplitPane.setRightComponent(new JPanel());
			processesSplitPane.setDividerLocation(dividerLocation);

		} else {
			Window window = windowMap.get(workspace.getName());
			if (window == null) throw new RuntimeException("Workspace UI for Workspace '" + workspace.getName() + "' not found in the current session.");
			if (!(window instanceof WorkspaceUI)) throw new RuntimeException("Window for workspace '" + workspace.getName() + "' is not of type WorkspaceUI.");

			WorkspaceUI workspaceUI = (WorkspaceUI)window;

			// Update the composition list panel.
			int dividerLocation = processesSplitPane.getDividerLocation();
			processesSplitPane.setRightComponent(workspaceUI.getCompositionListPanel().getPanel());
			processesSplitPane.setDividerLocation(dividerLocation);

			// Highlight the tab unless it is already highlighted.
			if (tabbedPane.getSelectedComponent() == null || 
					tabbedPane.getSelectedComponent().getName() == null || 
					!tabbedPane.getSelectedComponent().getName().equals(workspace.getName())) {
				for (int i = 0; i < tabbedPane.getTabCount(); i++) {
					if (tabbedPane.getComponentAt(i).getName() != null && tabbedPane.getComponentAt(i).getName().equals(workspace.getName())) {
						tabbedPane.setSelectedIndex(i);
						break;
					}
				}
			}
		}
	}

	@Override
	public void workspaceRemoved(Workspace workspace) {
		disposeWindow(workspace.getName());
	}

	@Override
	public void undoRedoUpdate() { }

	@Override
	public void sessionSaved() { }

	@Override
	public void sessionReset() {
		Collection<Window> windows = new Vector<Window>();
		windows.addAll(windowMap.values());
		for (Window window : windows)
			window.dispose();
		windowMap.clear(); //just to be sure
	}

	@Override
	public void addWindow(final Window window) {

		if (windowMap.containsKey(window.getName())) {
			Log.d("Adding window '" + window.getName() + "' when it already exists!");
		}

		// If window name already exists, don't add.
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (tabbedPane.getComponentAt(i).getName() != null && tabbedPane.getComponentAt(i).getName().equals(window.getName())) {
				Log.d("Highlighting existing tab: " + window.getName());
				tabbedPane.setSelectedIndex(i);
				return;
			}
		}

		// Add to the window map
		windowMap.put(window.getName(),window);

		// Add component
		tabbedPane.add(window.getPanel()); // This must happen after the window has been added to the map because of events that this triggers (e.g. WorkspaceActivated)

		// Add closeable tab
		final CloseableTabPanel tab = new CloseableTabPanel(tabbedPane, window.getIcon());
		for (ActionListener l : tab.getCloseButton().getActionListeners()) 
			tab.getCloseButton().removeActionListener(l);
		tab.getCloseButton().addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				window.dispose();				
			}
		});
		tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tab);

		// Notify listeners
		for (WindowManagerChangeListener listener : windowChangeListeners) {
			listener.windowAdded(window);
		}

		// Activate
		tabbedPane.setSelectedComponent(window.getPanel());	

		Log.d("Added window: " + window.getName());
	}

	@Override
	public void removeWindow(Window window) {
		if (windowExists(window.getName())) {
			Log.d("Removing window: " + window.getName());

			// Remove from the window map
			windowMap.remove(window.getName());

			// Close related tab
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				if (tabbedPane.getComponentAt(i).getName() != null && tabbedPane.getComponentAt(i).getName().equals(window.getName())) {
					tabbedPane.remove(i); // This assumes there cannot be tabs with the same name.
					break;
				}
			}

			// Notify listeners
			for (WindowManagerChangeListener listener : windowChangeListeners) {
				listener.windowRemoved(window);
			}
		} else {
			Log.e("[Session UI] Failed to find window to remove: " + window.getName());
		}
	}

	@Override
	public void removeWindow(String name) {
		if (windowExists(name)) 
			removeWindow(windowMap.get(name));
	}

	@Override
	public Window getWindow(String name) {
		return windowMap.get(name);
	}

	@Override
	public Window getActiveWindow() {
		if (tabbedPane.getSelectedComponent() == null) return null;	
		return windowMap.get(tabbedPane.getSelectedComponent().getName());
	}

	@Override
	public boolean windowExists(String name) {
		return this.windowMap.containsKey(name);
	}

	@Override
	public Collection<Window> getWindows() {
		return windowMap.values();
	}

	public void disposeWindow(String name) {
		if (windowExists(name)) 
			windowMap.get(name).dispose();
	}


	@Override
	public void addChangeListener(WindowManagerChangeListener listener) {
		windowChangeListeners.add(listener);		
	}

	@Override
	public void removeChangeListener(WindowManagerChangeListener listener) {
		windowChangeListeners.remove(listener);		
	}

	@Override
	public Collection<WindowManagerChangeListener> getChangeListeners() {
		return this.windowChangeListeners;
	}	
}
