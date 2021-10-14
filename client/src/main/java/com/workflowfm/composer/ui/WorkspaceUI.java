package com.workflowfm.composer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import com.workflowfm.composer.exceptions.ComponentExceptionHandler;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.graph.ComposableCell;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ui.PopupProcessGraph;
import com.workflowfm.composer.processes.ui.PortVertex;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.processes.ui.ProcessVertex;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.session.actions.LoadSelectionCompositionsAction;
import com.workflowfm.composer.session.actions.PiVizSelectionAction;
import com.workflowfm.composer.session.actions.RedoAction;
import com.workflowfm.composer.session.actions.SaveAction;
import com.workflowfm.composer.session.actions.ShowSelectionProcessGraphAction;
import com.workflowfm.composer.session.actions.StoreSelectionProcessAction;
import com.workflowfm.composer.session.actions.UndoAction;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.utils.UIUtils;
import com.workflowfm.composer.workspace.Workspace;
import com.workflowfm.composer.workspace.actions.RemoveBundleGraphAction;
import com.workflowfm.composer.workspace.actions.WorkspaceComposeAction;

public class WorkspaceUI implements GraphWindow { //, WorkspaceChangeListener
	public static final ImageIcon WORKSPACEUI_ICON = UIUtils.getIcon("silk_icons/application.png");

	private static final Color hoverHighlightColor = Color.decode(ComposerProperties.hoverHighlightColour());
	private static final Color selectHighlightColor = Color.decode(ComposerProperties.selectHighlightColour());
	
	private Workspace workspace;
	private WindowManager manager;
	private ExceptionHandler exceptionHandler;
	
	private JPanel panel;

	private MouseAdapter mouseAdapter;

	private CompositionListPanel compositionListPanel;
	
	private ShowSelectionProcessGraphAction showSelectionProcessGraphAction;
	private RemoveBundleGraphAction removeGraphAction;
	private StoreSelectionProcessAction storeAction;
	private LoadSelectionCompositionsAction loadCompositionsAction;
	private PiVizSelectionAction piVizAction;
	
	public WorkspaceUI(Workspace workspace, WindowManager manager) {
		this.manager = manager;
		this.workspace = workspace;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}

	@Override
	public JPanel getPanel() {
		return this.panel;
	}
	
	public CompositionListPanel getCompositionListPanel() {
		return this.compositionListPanel;
	}
	
	@Override
	public ProcessGraph getGraph() {
		return workspace.getGraph();
	}
	
	@Override
	public Icon getIcon() {
		return WorkspaceUI.WORKSPACEUI_ICON;
	}
	
	public Object getSelectedCell() {
		return this.workspace.getGraph().getSelectedCell();
	}
	
	@Override
	public String getName() {
		return workspace.getName();
	}

	@Override
	public void show()
	{
		this.panel = new JPanel(new BorderLayout()); //new BoxLayout(this, BoxLayout.Y_AXIS)
		this.exceptionHandler = new ComponentExceptionHandler(panel);
		// TODO Override getName() instead so it automatically updates if we rename the workspace!
		this.panel.setName(workspace.getName());

		setupActions();

		setupMouse();
		getGraph().getGraphEngine().getControlComponent().addMouseListener(mouseAdapter);
		getGraph().getGraphEngine().getControlComponent().addMouseMotionListener(mouseAdapter);

		setupKeys();
		
		getGraph().getGraphEngine().getGraphComponent().setBackground(Color.WHITE);
		new PopupProcessGraph(getGraph()).addListeners();
		
		panel.add(getGraph().getGraphEngine().getGraphComponent(),BorderLayout.CENTER);
		panel.setBackground(Color.WHITE);
		
		compositionListPanel = new CompositionListPanel(workspace,manager);
		compositionListPanel.setup();
		
		manager.addWindow(this);
	}
	
	private void setupActions() {
		this.showSelectionProcessGraphAction = new ShowSelectionProcessGraphAction(manager, workspace.getSession(), exceptionHandler);
		this.showSelectionProcessGraphAction.register();
		this.removeGraphAction = new RemoveBundleGraphAction(workspace,exceptionHandler);
		//this.storeAction = new StoreProcessAction(manager, workspace, this);
		this.storeAction = new StoreSelectionProcessAction(manager, workspace.getSession(), exceptionHandler);
		this.storeAction.register();
		this.loadCompositionsAction = new LoadSelectionCompositionsAction(manager, workspace.getSession(), exceptionHandler);
		this.loadCompositionsAction.register();
		this.piVizAction = new PiVizSelectionAction(manager, workspace.getSession(), exceptionHandler);
		this.piVizAction.register();
	}

	private void setupMouse() {
		this.mouseAdapter =  new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				getGraph().getGraphEngine().deHighlightByColor(selectHighlightColor);
				try {
					Object previousCell = getSelectedCell();
					Object selectedCell = getGraph().getGraphEngine().getCellAt(e.getX(), e.getY());

					// Debug selection
					if (selectedCell != null) {
						Object val = getGraph().getValue(selectedCell);
						Log.d("cell clicked = " + val + 
								(val instanceof ComposableCell?" - bundle = " + ((ComposableCell)val).getBundle():"") +
								(val instanceof ProcessVertex?" - bottom = " + ((ProcessVertex)getGraph().getValue(getGraph().getBottomMostProcess(selectedCell))).toString() +
										" - isBottom = " + ((ProcessVertex)val).isBottom() + " - terminator = " + ((ProcessVertex)val).isTerminator():"") + 
								(val instanceof PortVertex?" - port = " + getWorkspace().getProver().portResourceString(((PortVertex)val).getPort()):""));
						
						// If an edge was clicked, substitute with the PortVertex instead. 
						// If there is not PortVertex then there are no available actions.
						if (getGraph().getGraphEngine().isEdge(selectedCell))
						{
							selectedCell = getGraph().getPortCellOfEdge(selectedCell);
							if (selectedCell == null) return;
						}
						
						getGraph().getGraphEngine().deHighlightCell(selectedCell);
						getGraph().getGraphEngine().highlightCell(selectedCell, selectHighlightColor);
						getGraph().highlightCorrespondingPorts(selectedCell, selectHighlightColor, true);
					}
					
					workspace.getGraph().setSelectedCell(selectedCell);
					
					if (e.getButton() == 3 || e.isPopupTrigger() || (e.getButton() == 1 && e.isControlDown()))
					{
						rightClick(e, previousCell, selectedCell);
						return;
					}
					else
					{
						//clearMarkers();
						//if (selectedCell != null)
						//	getGraph().highlightCorrespondingPorts(selectedCell,Color.blue);

						//addMarker(selectedCell2, Color.green);
					}		
				} catch (Exception ex1) {
					ex1.printStackTrace();
				}	
			}
			
			@Override
			public void mouseMoved(MouseEvent e)
			{
				getGraph().getGraphEngine().deHighlightByColor(hoverHighlightColor);

				Object selectedCell = getGraph().getGraphEngine().getCellAt(e.getX(), e.getY());
				if (selectedCell != null)
					getGraph().highlightCorrespondingPorts(selectedCell, hoverHighlightColor, false);
			}
		};
	}

	private void setupKeys() {
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),"removeBranch");
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),"removeBranch");
		panel.getActionMap().put("removeBranch", removeGraphAction);
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK),"undoKey");
		panel.getActionMap().put("undoKey", new UndoAction(workspace.getSession(),exceptionHandler));
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK),"redoKey");
		panel.getActionMap().put("redoKey", new RedoAction(workspace.getSession(),exceptionHandler));
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK),"saveKey");
		panel.getActionMap().put("saveKey", new SaveAction(getPanel(), workspace.getSession(), exceptionHandler));
	}
	
	private void rightClick(MouseEvent e, Object previousCell, Object selectedCell) {
		if (selectedCell == null) return;

		Object selectedValue = getGraph().getValue(selectedCell);
		if (!(selectedValue instanceof ComposableCell)) {
			Log.d("Non-composable component selected.");
			return;
		}
		String selectedBundle = ((ComposableCell)selectedValue).getBundle();

		if (previousCell == null || previousCell == selectedCell) {
			popup(e);
		} else {
			
			Object previousValue = getGraph().getValue(previousCell);
			if (!(previousValue instanceof ComposableCell)) {
				Log.d("Non-composable component selected.");
				return;
			}
			String previousBundle = ((ComposableCell)previousValue).getBundle();
			
			Log.d("node1 value = " + previousValue + ", bundle = " + previousBundle);
			Log.d("node2 value = " + selectedValue + ", bundle = " + selectedBundle);
			
			ComposeAction action = null;
			
			if (previousValue instanceof ProcessVertex && selectedValue instanceof ProcessVertex) 
			{
				action = new ComposeAction("TENSOR", previousBundle, "", selectedBundle, "", workspace.getFreshCompositionName());	
			}
			
			else if (previousValue instanceof PortVertex && selectedValue instanceof PortVertex) 
			{
				PortVertex previousVertex = (PortVertex)previousValue;
				PortVertex selectedVertex = (PortVertex)selectedValue;

				if (!previousVertex.isClickable() || !selectedVertex.isClickable())
					return;
				
				if (previousVertex.isOutput() && selectedVertex.isInput() && previousVertex.getTerm().equals(selectedVertex.getTerm())) 
				{
					try {
						action = new ComposeAction("JOIN", 
								previousBundle, workspace.getProver().cllPath(previousVertex.getEdge()), 
								selectedBundle, workspace.getProver().cllResourceString(selectedVertex.getRootTerm().neg()), 
								workspace.getFreshCompositionName());
					} catch (InvalidCllPathException e1) {
						throw new RuntimeException("Invalid CLL path: " + previousVertex.getEdge().getTermPath().toString(), e1);
					}
				}
				else if (previousVertex.isInput() && selectedVertex.isOutput() && previousVertex.getTerm().equals(selectedVertex.getTerm())) 
				{
					try {
						action = new ComposeAction("JOIN", 
								selectedBundle, workspace.getProver().cllPath(selectedVertex.getEdge()), 
								previousBundle, workspace.getProver().cllResourceString(previousVertex.getRootTerm().neg()), 
								workspace.getFreshCompositionName());
					} catch (InvalidCllPathException e1) {
						throw new RuntimeException("Invalid CLL path: " + previousVertex.getEdge().getTermPath().toString(), e1);
					}
				}
				
				else if (previousVertex.isInput() && selectedVertex.isInput()) 
				{
					action = new ComposeAction("WITH", 
							previousBundle, workspace.getProver().cllResourceString(previousVertex.getRootTerm().neg()), 
							selectedBundle, workspace.getProver().cllResourceString(selectedVertex.getRootTerm().neg()), 
							workspace.getFreshCompositionName());
				}
			}
			
			if (action != null) {
				ActionEvent ae = new ActionEvent(e.getSource(), e.getID(), e.paramString(), e.getWhen(), e.getModifiers());
				getGraph().getGraphEngine().deHighlightAll();
				getGraph().setSelectedCell(null);
				try {
					new WorkspaceComposeAction(workspace, exceptionHandler, action, true).actionPerformed(ae);
				} catch (NotFoundException e1) {
					exceptionHandler.handleException(e1);
				}
			}			
		}
	}

	private void popup(MouseEvent e) {
		JPopupMenu menu = new JPopupMenu();

		menu.add(showSelectionProcessGraphAction);
		menu.add(removeGraphAction);
		menu.addSeparator();
		menu.add(piVizAction);
		menu.add(loadCompositionsAction);
		menu.addSeparator();
		menu.add(storeAction);
		//if (service.isComposite() && service instanceof CompositeProcess) { // double check just to be sure
		//	menu.add(new ShowCompositionGraphAction(Gui.this, (CompositeProcess)service));
		//	menu.add(new LoadServiceCompositionsAction(Gui.this, (CompositeProcess)service));
		//}
		//menu.addSeparator();
		//menu.add(new InspectPiCalculusAction(Gui.this, service));
		//menu.add(new DeployServiceAction(Gui.this, service.getName()));
		//menu.addSeparator();
		//menu.add(new DeleteProcessAction(Gui.this, service));
		menu.show(panel, 
				e.getX() - getGraph().getGraphEngine().getHorizontalScrollBar().getValue(), 
				e.getY() - getGraph().getGraphEngine().getVerticalScrollBar().getValue());
	}
	
	@Override
	public void dispose() {
		showSelectionProcessGraphAction.dispose();
		storeAction.dispose();
		loadCompositionsAction.dispose();
		piVizAction.dispose();
		manager.removeWindow(this);
	}
	
	
//	@Override
//	public void compositionAdded(CProcess process) {
//		getGraph().insertGraph(process.getCompositionGraph());
//	}
//
//	@Override
//	public void compositionUpdated(String name, CProcess process) {
//		// TODO Uncomment when compositionRemoved is done.
//		// compositionRemoved(process);
//		// compositionAdded(process);
//	}
//
//	@Override
//	public void compositionRemoved(CProcess process) {
//		// TODO Remove bundles with that composition?		
//	}

	@Override
	public WindowManager getManager() {
		return this.manager;
	}

}
