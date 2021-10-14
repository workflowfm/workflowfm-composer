package com.workflowfm.composer.edit;

import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.workflowfm.composer.edit.group.UndoableSessionEditGroup;
import com.workflowfm.composer.edit.group.UpdateProcessEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.prover.command.ComposeCommand;
import com.workflowfm.composer.prover.response.ComposeResponse;
import com.workflowfm.composer.prover.response.ExceptionResponse;
import com.workflowfm.composer.prover.response.ProverResponse;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.utils.Completable;
import com.workflowfm.composer.utils.CompletionListener;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.Workspace;
import com.workflowfm.composer.workspace.edit.StoreCompositionEdit;

public class CompositionBuilder implements Completable {

	private String name;
	private CompositionSession session;
	private ExceptionHandler handler;
	private boolean visible;
	
	private boolean succeeded = false;
	
	protected transient CopyOnWriteArrayList<CompletionListener> completionListeners = new CopyOnWriteArrayList<CompletionListener>();
	@Override
	public void addCompletionListener(CompletionListener a) { completionListeners.add(a); }
	@Override
	public void removeCompletionListener(CompletionListener a) { completionListeners.remove(a); }
	protected void notifyCompletion() { for (CompletionListener a : completionListeners) a.completed(); }
	
	public CompositionBuilder(String name, CompositionSession session, ExceptionHandler handler, boolean visible) {
		this.name = name;
		this.session = session;
		this.handler = handler;
		this.visible = visible;
	}

	public void addAsListener(ComposeCommand command) {
		CompletionListener listener = new CompletionListener() {
			CompositionBuilder builder = CompositionBuilder.this;
			@Override
			public void completed() {
				if (command == null) {
					throw new RuntimeException("Null command completed");
				}
				if (!command.succeeded()) {
					Log.e("Command failed: " + command.debugString());
					succeeded = false;
					builder.notifyCompletion();
					return;
				}
				builder.build(command.getResponses());
			}
		};
		command.addCompletionListener(listener);
	}

	
	public void build(Vector<ProverResponse> responses) {
		Workspace temp = new Workspace("Workspace " + name, session);
		UndoableSessionEditGroup composeGroup = new UndoableSessionEditGroup("Compose " + name, session, handler);
		
		boolean exceptionOccured = false;
		for (ProverResponse r : responses) {
			if (r.isException()) {
				handler.handleException((ExceptionResponse)r);
				exceptionOccured = true;
				break;
			}
			if (r instanceof ComposeResponse) {
				ComposeResponse cr = (ComposeResponse)r;
				composeGroup.append(temp.handleComposeResponse(cr,handler,false));
			}
		}	
		
		if (!exceptionOccured) {
			composeGroup.append(new StoreCompositionEdit(name, temp, handler, visible && !session.processExists(name)));
			succeeded = true;
		} else {
			succeeded = false;
		}
		
		UndoableSessionEditGroup editGroup = new UpdateProcessEdit(name, session, handler, composeGroup, succeeded);
		Log.d("Generated edit group with " + editGroup.getEdits().size() + " edits");
		for (UndoableSessionEdit edit : editGroup.getEdits()) {
			Log.d(edit.getPresentationName());
		}
		editGroup.apply(visible);
		
		notifyCompletion();
	}
	
	public boolean succeeded() {
		return this.succeeded;
	}
}
