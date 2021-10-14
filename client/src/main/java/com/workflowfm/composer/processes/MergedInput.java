package com.workflowfm.composer.processes;

public class MergedInput {
	private ProcessPort term;
	private String leftChannel;
	private String rightChannel;
	
	public MergedInput() { }
	
	public MergedInput(ProcessPort term, String left, String right) {
		super();
		this.term = term;
		this.leftChannel = left;
		this.rightChannel = right;
	}

	public ProcessPort getTerm() {
		return term;
	}

	public String getLeft() {
		return leftChannel;
	}

	public String getRight() {
		return rightChannel;
	}

	@Override
	public String toString() {
		return "(" + leftChannel + "," + rightChannel + ")-> " + term.toString();
	}

}
