package com.workflowfm.composer.edit;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ProcessStore;
import com.workflowfm.composer.session.CompositionSession;

public class DeleteProcessEdit extends UndoableSessionEdit {
	
	private static final long serialVersionUID = 3077427904654897575L;
	private CProcess process;
	private ProcessStore store;
		
	public DeleteProcessEdit(CProcess process, ProcessStore store, CompositionSession session, ExceptionHandler handler) {
		super("Delete process " + process.getName(),session,handler);
		this.process = process;
		this.store = store;
	}
	
	@Override
	protected void doSession() {
		try {
			store.removeProcess(process);
		} catch (NotFoundException e) {	}
	}

	@Override
	protected void undoSession() {
		store.addProcess(process);
	}

	@Override
	protected void redoSession() {
		doSession();
	}
}
