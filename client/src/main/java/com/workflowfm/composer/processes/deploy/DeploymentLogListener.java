package com.workflowfm.composer.processes.deploy;

public interface DeploymentLogListener {
	public void success(String file);
	public void skipped(String file);
	public void failure(String file, Exception exception);
	
	public void start(int numberOfFiles);
	public void finish();
}
