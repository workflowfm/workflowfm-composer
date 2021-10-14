package com.workflowfm.composer.ui;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.workflowfm.composer.exceptions.ComponentExceptionHandler;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.prover.ProverStateListener;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.session.actions.AddWorkspaceAction;
import com.workflowfm.composer.session.actions.CreateProcessAction;
import com.workflowfm.composer.session.actions.ExportActiveImageAction;
import com.workflowfm.composer.session.actions.LayoutActiveGraphAction;
import com.workflowfm.composer.session.actions.LoadAction;
import com.workflowfm.composer.session.actions.LoadSelectionCompositionsAction;
import com.workflowfm.composer.session.actions.NewAction;
import com.workflowfm.composer.session.actions.RedoAction;
import com.workflowfm.composer.session.actions.RemoveUnusedFromActiveGraphAction;
import com.workflowfm.composer.session.actions.SaveAction;
import com.workflowfm.composer.session.actions.SaveAsAction;
import com.workflowfm.composer.session.actions.StoreSelectionProcessAction;
import com.workflowfm.composer.session.actions.UndoAction;
import com.workflowfm.composer.session.actions.VerifyActiveIntermediateProcessesAction;
import com.workflowfm.composer.session.actions.VerifyAllProcessesAction;
import com.workflowfm.composer.utils.Log;

public class SessionToolbar implements WindowManagerChangeListener, ProverStateListener {

	private CompositionSession session;
	private WindowManager manager;
	private ExceptionHandler exceptionHandler;
	
	private JToolBar toolbar;
	
	private LayoutActiveGraphAction layoutAction;
	private ExportActiveImageAction exportAction;
	private RemoveUnusedFromActiveGraphAction removeAction;
	
	private LoadSelectionCompositionsAction loadCompositionsAction;
	private StoreSelectionProcessAction storeAction;
	
	private UndoAction undoAction;
	private RedoAction redoAction;
	private SaveAction saveAction;
	
	private VerifyActiveIntermediateProcessesAction verifyIntermediatesAction;
	
	private JProgressBar progressBar;

	public SessionToolbar(CompositionSession session) {
		this(session,null);
	}
	
	public SessionToolbar(CompositionSession session, WindowManager manager) {
		this.session = session;
		this.manager = manager;
	}
	
	public JToolBar getToolbar() {
		return this.toolbar;
	}
	
	public void setup() {
		manager.addChangeListener(this);
		toolbar = new JToolBar("Session Toolbar");
		exceptionHandler = new ComponentExceptionHandler(toolbar);
		
		layoutAction = new LayoutActiveGraphAction(manager,session,exceptionHandler);
		exportAction = new ExportActiveImageAction(manager, session, exceptionHandler);
		removeAction = new RemoveUnusedFromActiveGraphAction(manager, session, exceptionHandler);
		verifyIntermediatesAction = new VerifyActiveIntermediateProcessesAction(session, exceptionHandler);
		updateGraphActions(manager.getActiveWindow());
		
		loadCompositionsAction = new LoadSelectionCompositionsAction(manager, session, exceptionHandler);
		loadCompositionsAction.register();
		storeAction = new StoreSelectionProcessAction(manager, session, exceptionHandler);
		storeAction.register();

		undoAction = new UndoAction(session, exceptionHandler);
		session.addChangeListener(undoAction);
		redoAction = new RedoAction(session, exceptionHandler);
		session.addChangeListener(redoAction);
		saveAction = new SaveAction(getToolbar(), session, exceptionHandler);
		session.addChangeListener(saveAction);
		
		progressBar = new JProgressBar();
		progressBar.setEnabled(false);
		progressBar.setStringPainted(true);
		progressBar.setString("Done");
		Dimension barSize = new Dimension();
		barSize.setSize(100.0, progressBar.getPreferredSize().getHeight()); //progressBar.getPreferredSize().getWidth() / 2.0
		progressBar.setPreferredSize(barSize);
		progressBar.setMaximumSize(barSize);
		session.getProver().addStateListener(this);
		
		setupToolbar();
		setupKeys();
	}
	
	private void setupToolbar()
	{
		toolbar.add(new NewAction(session, exceptionHandler));
		toolbar.add(new LoadAction(getToolbar(), session, exceptionHandler));
		toolbar.add(saveAction);
		toolbar.add(new SaveAsAction(getToolbar(), session, exceptionHandler));
		toolbar.add(new JToolBar.Separator());
		
		toolbar.add(new AddWorkspaceAction(session, exceptionHandler));
		toolbar.add(new JToolBar.Separator());
		
		toolbar.add(new CreateProcessAction(manager, session, exceptionHandler));
		toolbar.add(new JToolBar.Separator());
	
		toolbar.add(loadCompositionsAction);
		toolbar.add(storeAction);
		toolbar.add(new JToolBar.Separator());

		toolbar.add(undoAction);
		toolbar.add(redoAction);
		toolbar.add(new JToolBar.Separator());
		
		toolbar.add(new VerifyAllProcessesAction(session, exceptionHandler));
		toolbar.add(verifyIntermediatesAction);
		toolbar.add(new JToolBar.Separator());
		
		toolbar.add(layoutAction);
		toolbar.add(exportAction);
		toolbar.add(removeAction);
		
		toolbar.add(Box.createHorizontalGlue());
		
		toolbar.add(new JLabel("Prover: "));
		toolbar.add(progressBar);
	}
	
	private void setupKeys() {
		toolbar.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK),"undoKey");
		toolbar.getActionMap().put("undoKey", undoAction);
		toolbar.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK),"redoKey");
		toolbar.getActionMap().put("redoKey", redoAction);
		toolbar.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK),"saveKey");
		toolbar.getActionMap().put("saveKey", saveAction);
	}
	
	private void updateGraphActions(Window window) {
		if (window instanceof GraphWindow) {
			layoutAction.setEnabled(true);
			exportAction.setEnabled(true);
			removeAction.setEnabled(true);
		} else {
			layoutAction.setEnabled(false);
			exportAction.setEnabled(false);
			removeAction.setEnabled(false);
		}
		if (window instanceof WorkspaceUI) {
			verifyIntermediatesAction.setEnabled(true);
		} else {
			verifyIntermediatesAction.setEnabled(false);
		}
	}

	@Override
	public void windowActivated(Window window) { 
		updateGraphActions(window); 
	}
	
	@Override
	public void windowAdded(Window window) { }

	@Override
	public void windowRemoved(Window window) { }

	@Override
	public void logUpdated(Prover prover) { }

	@Override
	public void executionStarted(Prover prover) { 
		Log.d("Execution started");
		progressBar.setEnabled(true);
		progressBar.setIndeterminate(true);
		progressBar.setString("Working...");
	}

	@Override
	public void executionStopped(Prover prover) {
		Log.d("Execution stopped");
		progressBar.setEnabled(false);
		progressBar.setIndeterminate(false);
		progressBar.setString("Done");
	}


}
