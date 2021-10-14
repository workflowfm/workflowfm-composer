package com.workflowfm.composer.edit;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;

public abstract class UndoableSessionEdit extends AbstractUndoableEdit { 

	private static final long serialVersionUID = 6942783095239733882L;
	private String description;
	private CompositionSession session;
	private ExceptionHandler handler;
	
	protected boolean done = false;
	

	public UndoableSessionEdit(String description, CompositionSession session, ExceptionHandler handler) {
		this.description = description;
		this.session = session;
		this.handler = handler;
	}
	
	public CompositionSession getSession() { 
		return this.session; 
	}
	
	public ExceptionHandler getExceptionHandler() {
		return this.handler;
	}
	
	protected abstract void doSession();
	protected abstract void undoSession();
    protected abstract void redoSession();
    
    public void apply() {
    	this.apply(true);
    }
    
	public void apply(boolean addToUndoManager) {
		try {
			doSession();
			done = true;
			if (addToUndoManager && hadEffect()) session.addToUndoManager(this);
		} catch (Exception e) {
			this.handler.handleException(e);
		}
	}
	
	public boolean hadEffect() { return true; }
    
    public void setDescription(String description) {
    	this.description = description;
    }
	
	@Override
	public boolean canUndo() { 
		return super.canUndo() && done;
	}
	
	@Override
    public void undo() throws CannotUndoException {
        super.undo();
        undoSession();
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        redoSession();
    }

	@Override
	public String getPresentationName() {
		return this.description;
	}
}
