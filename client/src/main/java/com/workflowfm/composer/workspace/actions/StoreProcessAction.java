package com.workflowfm.composer.workspace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.ui.StoreProcessWindow;
import com.workflowfm.composer.ui.WindowManager;
import com.workflowfm.composer.workspace.Workspace;

public class StoreProcessAction extends WorkspaceAction {

	private static final long serialVersionUID = -1679281168854579829L;
	private WindowManager manager;
	
	private CProcess process;
	
	public StoreProcessAction(CProcess process, WindowManager manager, Workspace workspace, ExceptionHandler exceptionHandler) {
		super("Store Composition", workspace, exceptionHandler, "silk_icons/bricks.png", KeyEvent.VK_S, KeyEvent.VK_F, ActionEvent.ALT_MASK);
		this.manager = manager;
		this.process = process;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		StoreProcessWindow window = new StoreProcessWindow(getWorkspace(), getExceptionHandler(), manager, process);
		window.show();
	}
}
