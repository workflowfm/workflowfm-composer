package com.workflowfm.composer.exceptions;

import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllTermPath;

public class InvalidCllPathException extends Exception
{
	private static final long serialVersionUID = -6287266876432311405L;
	private CllTerm term;

	public InvalidCllPathException(CllTerm term, CllTermPath path)
	{
		super("Invalid CLL path used: " + path.toString());
		this.term = term;
	}
	
	public CllTerm getTerm() { return term; }
	
}