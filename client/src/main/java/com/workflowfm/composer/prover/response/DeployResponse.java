package com.workflowfm.composer.prover.response;

import java.util.Collection;

import com.workflowfm.composer.processes.deploy.DeploymentFile;

public class DeployResponse extends ProverResponse {

	public final static String PILIB_TYPE = "pilib";
	public final static String PEW_TYPE = "pew";
	public final static String PIVIZ_TYPE = "piviz";
	
	private String type;
	private Collection<DeploymentFile> deployment;
		
	public String getType() { return type; }
	
	public Collection<DeploymentFile> getFiles() { return deployment; }
	
	@Override
	public String debugString() { return "DeployResponse:" + type; }

	@Override
	public boolean isException() { return false; }
}