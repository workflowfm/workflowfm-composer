package com.workflowfm.composer.workspace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.edit.group.UndoableSessionEditGroup;
import com.workflowfm.composer.edit.group.UpdateIntermediateEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.prover.command.Compose1Command;
import com.workflowfm.composer.prover.response.ComposeResponse;
import com.workflowfm.composer.prover.response.ExceptionResponse;
import com.workflowfm.composer.prover.response.ProverResponse;
import com.workflowfm.composer.utils.CompletionListener;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.Workspace;
import com.workflowfm.composer.workspace.edit.CompositionEdit;

public class WorkspaceComposeAction extends WorkspaceAction implements CompletionListener {
	
	private static final long serialVersionUID = 3947196297704789903L;
	private Compose1Command command;
	private boolean visible;
	
	private boolean succeeded = false;
	
	public WorkspaceComposeAction(Workspace workspace, ExceptionHandler exceptionHandler, ComposeAction action, boolean visible) throws NotFoundException {
		super(action.getDescription(), workspace, exceptionHandler, "silk_icons/arrow_join.png", KeyEvent.VK_GREATER);
		this.command = new Compose1Command(workspace, action, exceptionHandler);
		this.visible = visible;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (command != null) {
			if (command.getLhs().handleInvalid(getExceptionHandler())) {
				notifyCompletion();
				return;
			}
			if (command.getRhs().handleInvalid(getExceptionHandler())) {
				notifyCompletion();
				return;
			}
			
			command.addCompletionListener(this);
			getWorkspace().getSession().getProver().execute(command);
		}
	}

	@Override
	public void completed() {
		if (command == null) {
			throw new RuntimeException("Null command completed");
		}
		if (!command.succeeded()) {
			Log.e("Command failed: " + command.debugString());
			succeeded = false;
			notifyCompletion();
			return;
		}
			
		for (ProverResponse r : command.getResponses()) {
			if (r.isException()) {
				getExceptionHandler().handleException((ExceptionResponse)r);
				succeeded = false;
				break;
			}
			if (r instanceof ComposeResponse) {
				ComposeResponse resp = (ComposeResponse)r;
				if (!resp.getProcess().isIntermediate()) continue;
				CompositionEdit composeEdit = getWorkspace().handleComposeResponse((ComposeResponse)r, getExceptionHandler(), visible);
				new UpdateIntermediateEdit(resp.getProcess().getName(), getWorkspace(), getExceptionHandler(), new UndoableSessionEditGroup(composeEdit), true).apply(visible);
				succeeded = true;
			}
		}
		notifyCompletion();
	}
	
	public boolean succeeded() {
		return this.succeeded;
	}
}
