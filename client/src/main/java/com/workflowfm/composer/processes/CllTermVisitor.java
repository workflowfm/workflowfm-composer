package com.workflowfm.composer.processes;

public abstract class CllTermVisitor {
	
	public CllTermVisitor() { }
	
	abstract public boolean visit(CllTerm term, CllTermPath path);
}
