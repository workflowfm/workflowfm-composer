package com.workflowfm.composer.processes;

import com.workflowfm.composer.exceptions.NotFoundException;

public interface ProcessStore {
	public CProcess getProcess(String name) throws NotFoundException;
	public boolean processExists(String name);
	public void addProcess(CProcess process);
	public void updateProcess(String name, CProcess process) throws NotFoundException;
	public void removeProcess(String name) throws NotFoundException;
	public void removeProcess(CProcess process) throws NotFoundException;
	
	public void addChangeListener(ProcessStoreChangeListener listener);	
	public void removeChangeListener(ProcessStoreChangeListener listener);
}
