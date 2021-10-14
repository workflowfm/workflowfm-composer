package com.workflowfm.composer.edit.graph;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.session.CompositionSession;

public class AddProcessGraphEdit extends AddNProcessGraphsEdit {

	private static final long serialVersionUID = -1071284884597514517L;
		
	public AddProcessGraphEdit(CProcess process, CompositionSession session,
			ExceptionHandler handler, ProcessGraph graph) {
		super(process,1,session,handler,graph);
	}
}
