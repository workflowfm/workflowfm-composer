package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Set;

import com.workflowfm.composer.edit.AddProcessEdit;
import com.workflowfm.composer.edit.CompositionBuilder;
import com.workflowfm.composer.edit.group.UndoableSessionEditGroup;
import com.workflowfm.composer.edit.group.UpdateProcessEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.prover.command.ComposeCommand;
import com.workflowfm.composer.prover.command.CreateProcessCommand;
import com.workflowfm.composer.prover.response.CreateProcessResponse;
import com.workflowfm.composer.prover.response.ExceptionResponse;
import com.workflowfm.composer.prover.response.ProverResponse;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.utils.CompletionListener;

// Generates 1 undoable edit that is not added to the manager.
public class VerifyProcessAction extends CompositionSessionAction implements CompletionListener {

	private static final long serialVersionUID = 2791724865388950719L;

	private CProcess process;

	private ComposeCommand command;
	private CompositionBuilder builder;

	private CreateProcessCommand createCommand;

	private boolean atomic = false;
	private boolean succeeded = false;
	
	public VerifyProcessAction(CProcess process, CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Verify Process", session, exceptionHandler, "silk_icons/shield.png", KeyEvent.VK_V, KeyEvent.VK_V, ActionEvent.ALT_MASK);
		this.process = process;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (process.isChecked() && process.isValid()) {
			notifyCompletion();
			return;
		}

		if (process.isIntermediate()) {
			// TODO This requires access to workspace so probably need different action!
			atomic = false; 
			notifyCompletion();
			return;
		}
		else if (process.isComposite()) {
			atomic = false;

			try {
				Set<CProcess> components = getSession().getComponents(process);
				for (CProcess c : components) {
					if (c.handleInvalid(getExceptionHandler())) return;
				}

				command = new ComposeCommand(process.getName(), process.getActions(), components, getExceptionHandler());
				builder = new CompositionBuilder(command.getName(), getSession(), getExceptionHandler(), false);
				builder.addAsListener(command);
				builder.addCompletionListener(this);
				getProver().execute(command);
			} catch (NotFoundException ex) {
				process.setInvalid();
				getExceptionHandler().handleException(ex);
			}
		}
		else {
			atomic = true;
			createCommand = new CreateProcessCommand(process, getExceptionHandler());
			createCommand.addCompletionListener(this);
			getProver().execute(createCommand);
		}
	}

	@Override
	public void completed() {
		if (!atomic) {
			if (!builder.succeeded())
				process.setInvalid();
			else 
				succeeded = true;
		}
		else { // TODO move to something similar to the Composition builder?
			for (ProverResponse r : createCommand.getResponses()) {
				if (r.isException()) {
					process.setInvalid();
					getExceptionHandler().handleException((ExceptionResponse)r);
					notifyCompletion();
					return;
				}
				if (r instanceof CreateProcessResponse) {
					CProcess result = ((CreateProcessResponse)r).getProcess();
					AddProcessEdit addEdit = new AddProcessEdit(process.getName(), result, getSession(), getExceptionHandler(), false);
					new UpdateProcessEdit(result.getName(), getSession(), getExceptionHandler(), new UndoableSessionEditGroup(addEdit), true).apply(false);
					succeeded = true;
				}
			}
		}
		notifyCompletion();
	}	

	public boolean succeeded() {
		return this.succeeded;
	}
}