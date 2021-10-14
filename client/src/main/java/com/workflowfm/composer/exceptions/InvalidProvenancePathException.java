package com.workflowfm.composer.exceptions;

import com.workflowfm.composer.processes.CllTermPath;
import com.workflowfm.composer.processes.ComposeProvenance;

public class InvalidProvenancePathException extends Exception
{
	private static final long serialVersionUID = -5211917118139352777L;
	private ComposeProvenance term;

	public InvalidProvenancePathException(ComposeProvenance term, CllTermPath path)
	{
		super("Invalid Provenance path used: " + path.toString());
		this.term = term;
	}
	
	public ComposeProvenance getProvenance() { return term; }
	
}