package com.workflowfm.composer.edit.group;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;

public class UpdateProcessEdit extends UndoableSessionEditGroup {

	private static final long serialVersionUID = 3098580046918223418L;

	public UpdateProcessEdit (String name, CompositionSession session, ExceptionHandler handler, UndoableSessionEditGroup createGroup, boolean reAddGraphs) {
		this(name,name,session,handler,createGroup,reAddGraphs);
	}
	
	public UpdateProcessEdit (String oldName, String newName, CompositionSession session, ExceptionHandler handler, UndoableSessionEditGroup createGroup, boolean reAddGraphs) {
		super(createGroup.getPresentationName(), createGroup.getSession(), createGroup.getExceptionHandler());
		RemoveAllGraphsOfProcessEdit removeEdit = null;
		
		if (session.processExists(oldName)) {
			removeEdit = new RemoveAllGraphsOfProcessEdit(oldName, session, handler);
			this.append(removeEdit);
		}
		
		this.append(createGroup);
		
		if (reAddGraphs && removeEdit != null) {
			this.append(new AddAllRemovedProcessGraphsEdit(newName,removeEdit,session));
		}
	}	
}
