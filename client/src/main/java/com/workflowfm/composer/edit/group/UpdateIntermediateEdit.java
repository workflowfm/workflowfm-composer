package com.workflowfm.composer.edit.group;

import com.workflowfm.composer.edit.graph.AddRemovedProcessGraphEdit;
import com.workflowfm.composer.edit.graph.RemoveProcessGraphEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.workspace.Workspace;

public class UpdateIntermediateEdit extends UndoableSessionEditGroup {

	private static final long serialVersionUID = -797333367849304130L;

	public UpdateIntermediateEdit (String name, Workspace workspace, ExceptionHandler handler, UndoableSessionEditGroup createGroup, boolean reAddGraphs) {
		this(name,name,workspace,handler,createGroup,reAddGraphs);
	}
	
	public UpdateIntermediateEdit (String oldName, String newName, Workspace workspace, ExceptionHandler handler, UndoableSessionEditGroup createGroup, boolean reAddGraphs) {
		super(createGroup.getPresentationName(), createGroup.getSession(), createGroup.getExceptionHandler());
		RemoveProcessGraphEdit removeEdit = null;
		
		if (workspace.processExists(oldName)) {
			removeEdit = new RemoveProcessGraphEdit(oldName, workspace.getSession(), handler, workspace.getGraph());
			this.append(removeEdit);
		}
		
		this.append(createGroup);
		
		if (reAddGraphs && removeEdit != null) {
			this.append(new AddRemovedProcessGraphEdit(newName,removeEdit,workspace));
		}
	}	
}
