package com.workflowfm.composer.processes.ui.edit;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.workflowfm.composer.processes.CProcess;

public class UndoableProcessEdit extends AbstractUndoableEdit { 

	/**
	 * 
	 */
	private static final long serialVersionUID = -6306701586578716646L;
	private String description;
	private EditProcessPanel panel;
	private CProcess process;

	public UndoableProcessEdit(String description, EditProcessPanel panel, CProcess process) {
		this.description = description;
		this.panel = panel;
		this.process = process;
	}

    public void undo() throws CannotUndoException {
        super.undo();
        CProcess old = panel.getProcess();
        panel.setProcess(this.process);
        this.process = old;
        panel.refresh();        
    }

	@Override
	public boolean canUndo() {
		return process != null && super.canUndo();
	}

    public void redo() throws CannotRedoException {
        super.redo();
        CProcess old = panel.getProcess();
        panel.setProcess(this.process);
        this.process = old;
        panel.refresh();  
    }

	@Override
	public boolean canRedo() {
		return process != null && super.canRedo();
	}

	@Override
	public String getPresentationName() {
		return this.description;
	}
}
