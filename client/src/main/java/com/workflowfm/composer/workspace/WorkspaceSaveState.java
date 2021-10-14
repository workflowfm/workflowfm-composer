package com.workflowfm.composer.workspace;

import java.util.Collection;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;

public class WorkspaceSaveState {

	private String name;
	private int stepCounter;
	private Collection<CProcess> processes;
	
	public WorkspaceSaveState(Workspace workspace) {
		this.name = workspace.getName();
		this.stepCounter = workspace.getStepCounter();
		this.processes = workspace.getCompositions();
	}

	public void loadInSession(CompositionSession session) {
		for (CProcess process : processes)
			process.unCheck();
		Workspace workspace = new Workspace(name,session,stepCounter,processes);
		session.addWorkspace(workspace);
	}
}
