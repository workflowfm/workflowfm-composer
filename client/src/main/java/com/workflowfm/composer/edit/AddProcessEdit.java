package com.workflowfm.composer.edit;

import com.workflowfm.composer.edit.graph.AddProcessGraphEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;

public class AddProcessEdit extends UndoableSessionEdit {
	
	private static final long serialVersionUID = 7874633469123072351L;
	private CProcess process;
	private CProcess previous;
	private String previousName;
	
	private AddProcessGraphEdit addGraphEdit = null;
	private boolean visible;
	
	public AddProcessEdit(CProcess process, CompositionSession session, ExceptionHandler handler, boolean visible) {
		super(descriptionString(process.getName(),process,session) + ": " + process.getName(),session,handler);
		this.process = process;
		this.visible = visible;
		this.previousName = process.getName();
	}

	public AddProcessEdit(String previousName, CProcess process, CompositionSession session, ExceptionHandler handler, boolean visible) {
		super(descriptionString(previousName,process,session) + ": " + process.getName(),session,handler);
		this.process = process;
		this.visible = visible;
		this.previousName = previousName;
	}
	
	@Override
	protected void doSession() {
		if (getSession().processExists(previousName)) {
			try {
				this.previous = getSession().getProcess(previousName);
				if (previous.isComposite()) previous.unCheck(); // components may change through verification between undos
				getSession().updateProcess(previousName,process);
			} catch (NotFoundException e) {	}
				
		} else {
			getSession().addProcess(process);
			
			if (visible && getSession().getActiveWorkspace() != null) {
				this.addGraphEdit = new AddProcessGraphEdit(process, getSession(), getExceptionHandler(), getSession().getActiveWorkspace().getGraph());
				this.addGraphEdit.apply(false);
			}
		}
	}

	@Override
	protected void undoSession() {
		if (previous != null)
			try {
				getSession().updateProcess(process.getName(), previous);
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		else
			try {
				getSession().removeProcess(process.getName());
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		
		if (addGraphEdit != null) addGraphEdit.undo();
	}

	@Override
	protected void redoSession() {
		if (previous != null)
			try {
				getSession().updateProcess(previous.getName(), process);
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		else
			getSession().addProcess(process);
		
		if (addGraphEdit != null) addGraphEdit.redo();
	}
	
	private static String descriptionString(String previousName, CProcess process, CompositionSession session) {
		if (process.isComposite())
			if (session.processExists(previousName))
				return "Recompose";
			else
				return "Compose";
		else
			if (session.processExists(previousName))
				return "Edit";
			else
				return "Create";
	}
}
