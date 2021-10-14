package com.workflowfm.composer.edit.group;

import java.util.Vector;

import com.workflowfm.composer.edit.UndoableSessionEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.utils.Log;

public class UndoableSessionEditGroup extends UndoableSessionEdit {

	private static final long serialVersionUID = -7200281312655414773L;
	protected Vector<UndoableSessionEdit> edits;
	private boolean hadEffect = false;

	public UndoableSessionEditGroup(Vector<UndoableSessionEdit> edits, String description,
			CompositionSession session, ExceptionHandler handler) {
		super(description, session, handler);
		this.edits = edits;
	}

	public UndoableSessionEditGroup(String description,
			CompositionSession session, ExceptionHandler handler) {
		super(description, session, handler);
		this.edits = new Vector<UndoableSessionEdit>();
	}
	
	public UndoableSessionEditGroup(UndoableSessionEdit edit) {
		super(edit.getPresentationName(), edit.getSession(), edit.getExceptionHandler());
		this.edits = new Vector<UndoableSessionEdit>();
		this.edits.add(edit);
	}
	
	@Override
	public void apply(boolean addToUndoManager) {
		for (UndoableSessionEdit edit : edits) {
			Log.d("Applying edit: " + edit.getPresentationName());
			edit.apply(false);
			if (!hadEffect) hadEffect = edit.hadEffect();
		}
		if (addToUndoManager && hadEffect()) getSession().addToUndoManager(this);
	}
	
	@Override
	public void undo() {
		for (int i = edits.size() - 1; i >= 0; i--) {
			Log.d("Partial undo: " + edits.elementAt(i).getPresentationName());
			edits.elementAt(i).undo();
		}
	}
	
	@Override
	public boolean canUndo() {
		for (UndoableSessionEdit edit : edits) {
			if (!edit.canUndo()) return false;
		}
		return true;
	}

	
	@Override
	public void redo() {
		for (UndoableSessionEdit edit : edits) {
			Log.d("Partial redo: " + edit.getPresentationName());
			edit.redo();
		}
	}
	
	@Override
	public boolean canRedo() {
		for (UndoableSessionEdit edit : edits) {
			if (!edit.canRedo()) return false;
		}
		return true;
	}


	@Override
	public boolean hadEffect() { return hadEffect; }
	
	@Override
	protected void doSession() { }

	@Override
	protected void undoSession() { }

	@Override
	protected void redoSession() { }
	
	public Vector<UndoableSessionEdit> getEdits() {
		return edits;
	}

	public void append(UndoableSessionEdit edit) {
		if (done) {
			Log.e("[UndoableSessionEditGroup] Unable to add edit to a group that has already been applied.");
		} else {
			this.edits.add(edit);
		}
	}
	
	public void append(UndoableSessionEditGroup group) {
		append(group.getEdits());
	}
	
	public void append(Vector<UndoableSessionEdit> edits) {
		if (done) {
			Log.e("[UndoableSessionEditGroup] Unable to add edits to a group that has already been applied.");
		} else {
			this.edits.addAll(edits);
		}
	}
}
