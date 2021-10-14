package com.workflowfm.composer.graph;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.mxgraph.util.mxUndoableEdit;
import com.workflowfm.composer.utils.Log;

public class JGraphUndoableEdit extends AbstractUndoableEdit {

	private static final long serialVersionUID = -4915507904568253515L;
	private mxUndoableEdit mxEdit;
	
	public JGraphUndoableEdit(mxUndoableEdit mxEdit) {
		this.mxEdit = mxEdit;
	}

	@Override
	public void undo() throws CannotUndoException
	{
		if (mxEdit != null) {
			Log.d("Undoing [" + mxEdit.getChanges().size() + "] changes (" + (mxEdit.isSignificant()?"significant":"-") + ").");
			mxEdit.undo();
		}
	}

	@Override
	public boolean canRedo()
	{
		return true;
	}

	@Override
	public void redo() throws CannotRedoException
	{
		if (mxEdit != null) {
			Log.d("Redoing [" + mxEdit.getChanges().size() + "] changes.");
			mxEdit.redo();
		}
	}
	
}
