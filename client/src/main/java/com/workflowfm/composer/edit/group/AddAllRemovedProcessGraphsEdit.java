package com.workflowfm.composer.edit.group;

import com.workflowfm.composer.edit.UndoableSessionEdit;
import com.workflowfm.composer.edit.graph.AddRemovedProcessGraphEdit;
import com.workflowfm.composer.edit.graph.RemoveProcessGraphEdit;
import com.workflowfm.composer.processes.ProcessStore;

public class AddAllRemovedProcessGraphsEdit extends UndoableSessionEditGroup {

	private static final long serialVersionUID = 8172738463872130983L;

	public AddAllRemovedProcessGraphsEdit(String newName, RemoveAllGraphsOfProcessEdit removeEdit, ProcessStore store) {
		super("Add graphs of " + removeEdit.getName(), removeEdit.getSession(), removeEdit.getExceptionHandler());
	
		for (UndoableSessionEdit sEdit : removeEdit.getEdits()) {
			RemoveProcessGraphEdit rEdit = (RemoveProcessGraphEdit)sEdit; 
			edits.add(new AddRemovedProcessGraphEdit(newName,rEdit,store));
		}
	}
}
