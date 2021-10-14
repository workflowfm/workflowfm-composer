package com.workflowfm.composer.processes.ui.edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllValidator;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.ui.PortEdge;
import com.workflowfm.composer.processes.ui.PortVertex;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.processes.ui.ProcessVertex;
import com.workflowfm.composer.processes.ui.edit.actions.AddBranchAction;
import com.workflowfm.composer.processes.ui.edit.actions.DeleteBranchAction;
import com.workflowfm.composer.processes.ui.edit.actions.ExpandParentBranchAction;
import com.workflowfm.composer.processes.ui.edit.actions.RedoProcessEditAction;
import com.workflowfm.composer.processes.ui.edit.actions.RenameAtomAction;
import com.workflowfm.composer.processes.ui.edit.actions.RenameProcessAction;
import com.workflowfm.composer.processes.ui.edit.actions.UndoProcessEditAction;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.utils.Log;

public class EditProcessPanel
{
	private ProcessGraph graph;
	private CProcess process;
	private CProcess originalProcess;
	private Prover prover;
	private int ctr = 0;

	private CllValidator validator;
	private ExceptionHandler handler;
	
	private Object selectedCell = null;

	private UndoManager undoManager = new UndoManager();
	private UndoProcessEditAction undoAction = new UndoProcessEditAction(this);
	private RedoProcessEditAction redoAction = new RedoProcessEditAction(this);
	
	private DeleteBranchAction deleteBranchAction;
	private RenameProcessAction renameProcessAction;
	private RenameAtomAction renameAtomAction;
	private ExpandParentBranchAction expandParentBranchAction;
	private AddBranchAction addBranchAction;
	
	private JPanel panel;
	private JToolBar toolbar;

	private MouseAdapter mouseAdapter;

	public EditProcessPanel(Prover prover, ExceptionHandler handler) {
		this(prover,null,new CllValidator(prover),handler);
	}
	
	public EditProcessPanel(Prover prover, CProcess process, ExceptionHandler handler) {
		this(prover,process,new CllValidator(prover),handler);
	}
	
	public EditProcessPanel(Prover prover, CllValidator validator, ExceptionHandler handler) {
		this(prover,null,validator,handler);
	}
	
	public EditProcessPanel(Prover prover, CProcess process, CllValidator validator, ExceptionHandler handler) {
		this.prover = prover;
		if (process == null) {
			this.process = null;
		} else {
			this.process = new CProcess(process);
		}
		this.validator = validator;
		this.handler = handler;
	}

	
	public String getFreshChannel() {
		String channel = "c" + ctr;
		ctr++;
		return channel;
	}

	public CProcess getProcess() {
		return this.process;
	}

	public void setProcess(CProcess process) {
		this.process = process;
	}

	public CProcess getOriginalProcess() {
		return this.originalProcess;
	}
	
	public Prover getProver() {
		return this.prover;
	}

	public JPanel getPanel() {
		return this.panel;
	}
	
	public ProcessGraph getGraph() {
		return this.graph;
	}
	
	public Object getSelectedCell() {
		return this.selectedCell;
	}
	
	public JToolBar getToolbar() {
		return this.toolbar;
	}

	public void setup()
	{
		if (this.process == null) {
			ctr = 1;
			ProcessPort input = new ProcessPort("c0", new CllTerm("X"));
			ProcessPort output = new ProcessPort("cout", new CllTerm("X"));
			Vector<ProcessPort> inputs = new Vector<ProcessPort>();
			inputs.add(input);
			this.process = new CProcess("P",inputs,output);
		}
		
		this.originalProcess = new CProcess(process);

		this.panel = new JPanel(new BorderLayout()); //new BoxLayout(this, BoxLayout.Y_AXIS)
		//panel.setBackground(Color.WHITE);

		setupActions();
		
		setupToolbar();
		updateUndoRedoButtonStatus();
		panel.add(toolbar,BorderLayout.PAGE_START);

		graph = new ProcessGraph();
		graph.getGraphEngine().getGraphComponent().setPreferredSize(new Dimension(700, 300));

		setupMouse();
		graph.getGraphEngine().getControlComponent().addMouseListener(mouseAdapter);

		setupKeys();
		
		panel.add(graph.getGraphEngine().getGraphComponent(),BorderLayout.CENTER);
		refresh();
	}
	
	private void setupActions() {
		this.deleteBranchAction = new DeleteBranchAction(this);
		this.renameProcessAction = new RenameProcessAction(this);
		this.renameAtomAction = new RenameAtomAction(this);
		this.expandParentBranchAction = new ExpandParentBranchAction(this);
		this.addBranchAction = new AddBranchAction(this);		
	}

	private void setupToolbar()
	{
		toolbar = new JToolBar("Edit Process Toolbar");
		toolbar.add(renameProcessAction);
		toolbar.add(new JToolBar.Separator());
		// TODO reset action
		//toolbar.add(new JToolBar.Separator());
		toolbar.add(undoAction);
		toolbar.add(redoAction);
	}

	private void setupMouse() {
		this.mouseAdapter =  new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				try {
					ActionEvent ae = new ActionEvent(e.getSource(), e.getID(), e.paramString(), e.getWhen(), e.getModifiers());

					selectedCell = graph.getGraphEngine().getCellAt(e.getX(), e.getY());

					if (selectedCell == null) return;

					Object val = graph.getValue(selectedCell);


					if (e.getClickCount() == 2 && val instanceof ProcessVertex) { // Double click on a process
						Log.d("Double click on ProcessVertex detected.");
						renameProcessAction.actionPerformed(ae);;
					}
					else if (e.getClickCount() == 2 && val instanceof PortEdge) { // Double click on an edge
						Log.d("Double click on PortEdge detected.");
						if (((PortEdge)val).getTerm().isAtomic()) {
							renameAtomAction.actionPerformed(ae);
						}
					}
					else if (e.getClickCount() == 2 && val instanceof PortVertex) { // Double click on port vertex
						Log.d("Double click on PortVertex detected.");
						expandParentBranchAction.actionPerformed(ae);
					}
					else if ((e.getButton() == 3 || e.isPopupTrigger() || (e.getButton() == 1 && e.isControlDown())) && graph.isPortVertex(selectedCell)) {
						PortVertex port = (PortVertex)val;
						Log.d("Displaying popup menu for port vertex: [" + EditProcessPanel.this.prover.cllResourceString(port.getTerm()) + ":" + port.getChannel() + "] path: [" + port.getTermPath() + "] input: [" + port.isInput() + "].");

						JPopupMenu menu = new JPopupMenu();
						//menu.addSeparator();
						menu.add(addBranchAction);
						// add delete action to non-root elements and to root inputs except the last remaining one
						if (!port.getTermPath().isRoot() ||								
								(port.isInput() && EditProcessPanel.this.process.getInputs().size() > 1)) 
							menu.add(deleteBranchAction);

						menu.show(graph.getGraphEngine().getControlComponent(), 
								e.getX() - getGraph().getGraphEngine().getHorizontalScrollBar().getValue(), 
								e.getY() - getGraph().getGraphEngine().getVerticalScrollBar().getValue());
						return;
					}

				} catch (Exception ex1) {
					ex1.printStackTrace();
				}	
			}
		};
	}

	private void setupKeys() {
		graph.getGraphEngine().getGraphComponent().getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),"removeBranch");
		graph.getGraphEngine().getGraphComponent().getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),"removeBranch");
		graph.getGraphEngine().getGraphComponent().getActionMap().put("removeBranch", deleteBranchAction);
		graph.getGraphEngine().getGraphComponent().getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK),"undoKey");
		graph.getGraphEngine().getGraphComponent().getActionMap().put("undoKey", undoAction);
		graph.getGraphEngine().getGraphComponent().getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK),"redoKey");
		graph.getGraphEngine().getGraphComponent().getActionMap().put("redoKey", redoAction);
		graph.getGraphEngine().getGraphComponent().getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),"renameKey");
		graph.getGraphEngine().getGraphComponent().getActionMap().put("renameKey", renameProcessAction);
	}

	public void addToUndoManager(UndoableProcessEdit action) {
		undoManager.undoableEditHappened(new UndoableEditEvent(this, action));
		updateUndoRedoButtonStatus();
	}

	public UndoManager getUndoManager() {
		return this.undoManager;
	}

	public void updateUndoRedoButtonStatus()
	{
		undoAction.setDescription(undoManager.getUndoPresentationName());
        redoAction.setDescription(undoManager.getRedoPresentationName());
		undoAction.setEnabled(undoManager.canUndo());
		redoAction.setEnabled(undoManager.canRedo());
	}

	public void refresh() {
		Log.d("Refreshing Process Editor...");

		process.getOutputCll().flatten();
		for (CllTerm c : process.getInputCll()) {
			c.flatten();
		}

		graph.getGraphEngine().clear();
		graph.createProcessGraph(process,true);
		graph.layout();
	}

	public CllValidator getValidator() {
		return validator;
	}

	public ExceptionHandler getExceptionHandler() {
		return handler;
	}


}
