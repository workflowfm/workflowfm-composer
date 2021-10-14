package com.workflowfm.composer.workspace.actions;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.utils.CompletionListener;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.Workspace;


// Generates multiple undoable edits (that are not added to the manager).
public class VerifyMultipleIntermediateProcessesAction extends WorkspaceAction implements CompletionListener {

	private static final long serialVersionUID = -5820091900662192740L;

	private Collection<CProcess> processes = null;
	private HashSet<String> done = new HashSet<String>();
	
	private VerifyIntermediateProcessAction action;
	private ActionEvent e;
	private boolean running = false;

	public VerifyMultipleIntermediateProcessesAction(String actionName, Workspace workspace,
			ExceptionHandler exceptionHandler, String iconFilename,
			int mnemonicKey, int acceleratorKey, int acceleratorKeyModifier) {
		super(actionName, workspace, exceptionHandler, iconFilename, mnemonicKey,
				acceleratorKey, acceleratorKeyModifier);
	}

	public VerifyMultipleIntermediateProcessesAction(String actionName, Workspace workspace,
			ExceptionHandler exceptionHandler, String iconFilename,
			int mnemonicKey) {
		super(actionName, workspace, exceptionHandler, iconFilename, mnemonicKey);
	}

	public VerifyMultipleIntermediateProcessesAction(String actionName, Workspace workspace,
			ExceptionHandler exceptionHandler, URL iconURL, int mnemonicKey,
			int acceleratorKey, int acceleratorKeyModifier) {
		super(actionName, workspace, exceptionHandler, iconURL, mnemonicKey,
				acceleratorKey, acceleratorKeyModifier);
	}

	public VerifyMultipleIntermediateProcessesAction(String actionName, Workspace workspace,
			ExceptionHandler exceptionHandler, URL iconURL, int mnemonicKey) {
		super(actionName, workspace, exceptionHandler, iconURL, mnemonicKey);
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
		Log.d("Verifying intermediate processes: ");
		for (CProcess pr : processes) 
			Log.d(pr.getName());
		Log.d("---");
		running = true;
		completed();
	}
	
	@Override
	public void completed() {
		if (this.action != null && !this.action.succeeded()) {
			Log.d("Verification of intermediates failed.");
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
					Log.d("Initiating VerifyIntermediateProcessAction for: " + pr.getName());
					this.action = new VerifyIntermediateProcessAction(pr, getWorkspace(), getExceptionHandler());
					this.action.addCompletionListener(this);
					it.remove();
					this.done.add(pr.getName());
					this.action.actionPerformed(e);
					break;
				}
			}
		}
		
		if (processes.size() == 0) {
			Log.d("Verification of intermediates complete.");
			running = false;
		}
	}

}	
