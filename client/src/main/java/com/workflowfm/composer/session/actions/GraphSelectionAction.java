package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.graph.ComposableCell;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ui.PortEdge;
import com.workflowfm.composer.processes.ui.PortVertex;
import com.workflowfm.composer.processes.ui.ProcessGraphSelectionListener;
import com.workflowfm.composer.processes.ui.ProcessVertex;
import com.workflowfm.composer.session.CompositionSession;

import com.workflowfm.composer.ui.GraphWindow;
import com.workflowfm.composer.ui.Window;
import com.workflowfm.composer.ui.WindowManager;
import com.workflowfm.composer.ui.WindowManagerChangeListener;

public abstract class GraphSelectionAction<T extends GraphWindow> extends CompositionSessionAction implements ProcessGraphSelectionListener, WindowManagerChangeListener {

	private static final long serialVersionUID = -2886682706979078716L;
	private WindowManager manager;
	
	protected T window = null;
	
	public GraphSelectionAction(String actionName, WindowManager manager,
			CompositionSession session, ExceptionHandler exceptionHandler,
			String iconFilename, int mnemonicKey) {
		super(actionName, session, exceptionHandler, iconFilename, mnemonicKey);
		this.manager = manager;
	}

	public GraphSelectionAction(String actionName, WindowManager manager,
			CompositionSession session, ExceptionHandler exceptionHandler,
			String iconFilename, int mnemonicKey, int acceleratorKey,
			int acceleratorKeyModifier) {
		super(actionName, session, exceptionHandler, iconFilename, mnemonicKey,
				acceleratorKey, acceleratorKeyModifier);
		this.manager = manager;
	}

	public GraphSelectionAction(String actionName, WindowManager manager,
			CompositionSession session, ExceptionHandler exceptionHandler,
			URL iconURL, int mnemonicKey) {
		super(actionName, session, exceptionHandler, iconURL, mnemonicKey);
		this.manager = manager;
	}

	public GraphSelectionAction(String actionName, WindowManager manager,
			CompositionSession session, ExceptionHandler exceptionHandler,
			URL iconURL, int mnemonicKey, int acceleratorKey,
			int acceleratorKeyModifier) {
		super(actionName, session, exceptionHandler, iconURL, mnemonicKey,
				acceleratorKey, acceleratorKeyModifier);
		this.manager = manager;
	}
	
	
	// This must ensure window is of type T 
	protected abstract boolean isValidWindow(GraphWindow window); 
	
	protected abstract boolean isValidSelection(CProcess selection);
	
	protected abstract CProcess getSelectionByName(String name) throws NotFoundException;
	
	protected abstract void actionPerformed(ActionEvent e, CProcess process, String bundle);
	
	private CProcess getProcess(String name) {
		CProcess process;
		try {
			process = getSelectionByName(name);
		} catch (NotFoundException ex) {
			process = null;
		}
		return process;
	}
	

	public void register() {
		manager.addChangeListener(this);
		registerWindow(); 
	}
	
	@SuppressWarnings("unchecked") 
	protected void registerWindow() {
		unregister();
		
		if (manager.getActiveWindow() instanceof GraphWindow && isValidWindow((GraphWindow)manager.getActiveWindow())) {
			this.window = (T)manager.getActiveWindow();
			this.window.getGraph().addSelectionListener(this);
			this.setEnabled(isValidSelection(getProcessFromSelection()));
		} else 
			this.window = null;
	}
	
	protected void unregister() {
		if (this.window != null) {
			this.window.getGraph().removeSelectionListener(this);
			this.window = null;
		}
		this.setEnabled(false);
	}
	
	public void dispose() {
		unregister();
		manager.removeChangeListener(this);
	}
	
	
	protected CProcess getProcessFromSelection() {
		if (this.window == null) {
			return null;
		}
		
		Object cell = window.getGraph().getSelectedCell();
		if (cell == null) {
			return null;
		}
		Object value = window.getGraph().getValue(cell);
		if (!(value instanceof ComposableCell)) {
			return null;
		}
		
		CProcess process;
		if (value instanceof ProcessVertex)
			process = ((ProcessVertex)value).getProcess();
		else {
			process = getProcess(((ComposableCell)value).getBundle());
		}

		return process;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		CProcess process = getProcessFromSelection();
		
		if (process != null && isValidSelection(process))
			actionPerformed(e, process, ((ComposableCell)window.getGraph().getValue(window.getGraph().getSelectedCell())).getBundle());;
	}

	
	@Override
	public void selectedUnknown(Object cell) {
		this.setEnabled(false);
	}

	@Override
	public void selected(ComposableCell cell) {
		this.setEnabled(isValidSelection(getProcess(cell.getBundle())));
	}

	@Override
	public void selected(PortEdge edge) {
		// TODO enable this once PortEdge is a ComposableCell
		//CProcess process = getWorkspace().getProcess(edge.getBundle());
		//this.setEnabled(process.isComposite() && !process.isIntermediate());
		this.setEnabled(false);
	}

	@Override
	public void selected(PortVertex port) {
		this.setEnabled(isValidSelection(getProcess(port.getBundle())));	
	}

	@Override
	public void selected(ProcessVertex proc) {
		this.setEnabled(isValidSelection(proc.getProcess()));	
	}

	@Override
	public void deselected() {
		this.setEnabled(false);		
	}
	
	@Override
	public void windowAdded(Window window) { }

	@Override
	public void windowActivated(Window window) { 
		registerWindow();
	}

	@Override
	public void windowRemoved(Window window) {	} // No need to handle this - there should be a windowActivated event following this
	
	public WindowManager getManager() {
		return this.manager;
	}

}