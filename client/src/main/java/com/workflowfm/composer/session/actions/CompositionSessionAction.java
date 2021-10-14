package com.workflowfm.composer.session.actions;

import java.net.URL;
import java.util.concurrent.CopyOnWriteArrayList;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.UIAction;
import com.workflowfm.composer.utils.Completable;
import com.workflowfm.composer.utils.CompletionListener;

// TODO: Only some actions use notifyCompletion!
public abstract class CompositionSessionAction extends UIAction implements Completable {

	private static final long serialVersionUID = 2775481018429027460L;
	private CompositionSession session;
	private ExceptionHandler exceptionHandler;

	protected transient CopyOnWriteArrayList<CompletionListener> completionListeners = new CopyOnWriteArrayList<CompletionListener>();
	@Override
	public void addCompletionListener(CompletionListener a) { completionListeners.add(a); }
	@Override
	public void removeCompletionListener(CompletionListener a) { completionListeners.remove(a); }
	protected void notifyCompletion() { for (CompletionListener a : completionListeners) a.completed(); }

	
	public CompositionSessionAction(String actionName, CompositionSession session,
			ExceptionHandler exceptionHandler, String iconFilename,
			int mnemonicKey) {
		super(actionName, iconFilename, mnemonicKey);
		this.session = session;
		this.exceptionHandler = exceptionHandler;
	}

	public CompositionSessionAction(String actionName, CompositionSession session,
			ExceptionHandler exceptionHandler, String iconFilename,
			int mnemonicKey, int acceleratorKey, int acceleratorKeyModifier) {
		super(actionName, iconFilename, mnemonicKey, acceleratorKey,
				acceleratorKeyModifier);
		this.session = session;
		this.exceptionHandler = exceptionHandler;
	}

	public CompositionSessionAction(String actionName, CompositionSession session,
			ExceptionHandler exceptionHandler, URL iconURL, int mnemonicKey) {
		super(actionName, iconURL, mnemonicKey);
		this.session = session;
		this.exceptionHandler = exceptionHandler;
	}

	public CompositionSessionAction(String actionName, CompositionSession session,
			ExceptionHandler exceptionHandler, URL iconURL, int mnemonicKey,
			int acceleratorKey, int acceleratorKeyModifier) {
		super(actionName, iconURL, mnemonicKey, acceleratorKey,
				acceleratorKeyModifier);
		this.session = session;
		this.exceptionHandler = exceptionHandler;
	}

	public CompositionSession getSession() {
		return this.session;
	}

	public ExceptionHandler getExceptionHandler() {
		return this.exceptionHandler;
	}
	
	public Prover getProver() {
		return this.session.getProver();
	}
}