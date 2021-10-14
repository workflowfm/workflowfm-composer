package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.utils.CompletionListener;
import com.workflowfm.composer.utils.Log;


// Generates multiple undoable edits (that are not added to the manager).
public class VerifyMultipleProcessesAction extends CompositionSessionAction implements CompletionListener {

	private static final long serialVersionUID = 3171751708984645755L;

	private Collection<CProcess> processes = null;
	private HashSet<String> done = new HashSet<String>();
	
	private VerifyProcessAction action;
	private ActionEvent e;
	private boolean running = false;

	public VerifyMultipleProcessesAction(String actionName, CompositionSession session,
			ExceptionHandler exceptionHandler, String iconFilename,
			int mnemonicKey, int acceleratorKey, int acceleratorKeyModifier) {
		super(actionName, session, exceptionHandler, iconFilename, mnemonicKey,
				acceleratorKey, acceleratorKeyModifier);
	}

	public VerifyMultipleProcessesAction(String actionName, CompositionSession session,
			ExceptionHandler exceptionHandler, String iconFilename,
			int mnemonicKey) {
		super(actionName, session, exceptionHandler, iconFilename, mnemonicKey);
	}

	public VerifyMultipleProcessesAction(String actionName, CompositionSession session,
			ExceptionHandler exceptionHandler, URL iconURL, int mnemonicKey,
			int acceleratorKey, int acceleratorKeyModifier) {
		super(actionName, session, exceptionHandler, iconURL, mnemonicKey,
				acceleratorKey, acceleratorKeyModifier);
	}

	public VerifyMultipleProcessesAction(String actionName, CompositionSession session,
			ExceptionHandler exceptionHandler, URL iconURL, int mnemonicKey) {
		super(actionName, session, exceptionHandler, iconURL, mnemonicKey);
	}

	public Collection<CProcess> getProcesses() {
		return processes;
	}

	protected void setProcesses(Collection<CProcess> processes) {
		this.processes = processes;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (running) {
			Log.d("Action is still running...");
			return;
		}
		if (processes == null) return;
		this.e = e;
		done = new HashSet<String>();
		Log.d("Verifying processes: ");
		for (CProcess pr : processes) 
			Log.d(pr.getName());
		Log.d("---");
		running = true;
		completed();
	}
	
	@Override
	public void completed() {
		if (this.action != null && !this.action.succeeded()) {
			Log.d("Verification failed.");
			running = false;
			return;
		}
		
		if ((this.action == null || this.action.succeeded()) && processes.size() > 0) {
			for (Iterator<CProcess> it = processes.iterator() ; it.hasNext() ;) {
				CProcess pr = it.next();
				if (done.contains(pr.getName()) || (pr.isChecked() && pr.isValid())) {
					it.remove();
					continue;
				} else {
					Log.d("Initiating VerifyProcessAction for: " + pr.getName());
					this.action = new VerifyProcessAction(pr, getSession(), getExceptionHandler());
					this.action.addCompletionListener(this);
					it.remove();
					this.done.add(pr.getName());
					this.action.actionPerformed(e);
					break;
				}
			}
		}
		
		if (processes.size() == 0) {
			Log.d("Verification complete.");
			running = false;
		}
	}

}	
