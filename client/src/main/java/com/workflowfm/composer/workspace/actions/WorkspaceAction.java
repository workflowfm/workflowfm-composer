package com.workflowfm.composer.workspace.actions;

import java.net.URL;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.actions.CompositionSessionAction;
import com.workflowfm.composer.workspace.Workspace;

public abstract class WorkspaceAction extends CompositionSessionAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 646288431192546345L;
	private Workspace workspace;

	public WorkspaceAction(String actionName, Workspace workspace,
			ExceptionHandler exceptionHandler, String iconFilename,
			int mnemonicKey) {
		super(actionName, workspace.getSession(), exceptionHandler, iconFilename, mnemonicKey);
		this.workspace = workspace;
	}

	public WorkspaceAction(String actionName, Workspace workspace,
			ExceptionHandler exceptionHandler, String iconFilename,
			int mnemonicKey, int acceleratorKey, int acceleratorKeyModifier) {
		super(actionName, workspace.getSession(), exceptionHandler, iconFilename, mnemonicKey, acceleratorKey,
				acceleratorKeyModifier);
		this.workspace = workspace;
	}

	public WorkspaceAction(String actionName, Workspace workspace,
			ExceptionHandler exceptionHandler, URL iconURL, int mnemonicKey) {
		super(actionName, workspace.getSession(), exceptionHandler, iconURL, mnemonicKey);
		this.workspace = workspace;
	}

	public WorkspaceAction(String actionName, Workspace workspace,
			ExceptionHandler exceptionHandler, URL iconURL, int mnemonicKey,
			int acceleratorKey, int acceleratorKeyModifier) {
		super(actionName, workspace.getSession(), exceptionHandler, iconURL, mnemonicKey, acceleratorKey,
				acceleratorKeyModifier);
		this.workspace = workspace;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}
}
