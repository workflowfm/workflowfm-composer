package com.workflowfm.composer.graph;

public abstract class CellVisitor<T> {
	
	protected T result;
	
	public CellVisitor(T initialValue) {
		this.result = initialValue;
	}
	
	public T getResult() { return result; }
	
	abstract public boolean visit(Object vertex, Object edge);
}
