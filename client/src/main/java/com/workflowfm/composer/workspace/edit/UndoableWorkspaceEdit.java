package com.workflowfm.composer.workspace.edit;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.workflowfm.composer.edit.graph.UndoableGraphEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.workspace.Workspace;

public abstract class UndoableWorkspaceEdit extends UndoableGraphEdit { 

	private static final long serialVersionUID = -1572341428552519323L;
	private Workspace workspace;

	public UndoableWorkspaceEdit(String description, Workspace workspace, ExceptionHandler handler, boolean visible) {
		super(description,workspace.getSession(),handler,workspace.getGraph(),visible);
		this.workspace = workspace;
	}
	
	public Workspace getWorkspace() { 
		return this.workspace; 
	}

	protected abstract void doExec();
	protected abstract void undoWorkspace();
    protected abstract void redoWorkspace();
	
	@Override
	public boolean canUndo() { 
		return super.canUndo() && done;
	}
	  
    @Override
	protected void doSession() {
    	doExec();
    	super.doSession();
    }
    
	@Override
	protected void undoSession() { 
		super.undoSession();
		undoWorkspace();
	}

	@Override
	protected void redoSession() { 
		redoWorkspace();
		super.redoSession();
	}
}
