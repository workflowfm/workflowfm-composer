package com.workflowfm.composer.processes;

public interface ProcessStoreChangeListener {
	public void processAdded(CProcess process);
	public void processUpdated(String previousName, CProcess process);
	public void processRemoved(CProcess process);
}
