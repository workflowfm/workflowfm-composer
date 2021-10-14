package com.workflowfm.composer.processes.ui;

import java.io.Serializable;
import java.util.Vector;

import com.workflowfm.composer.graph.ComposableCell;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ProcessPort;

/**
 * Represents a process vertex on a graph. Stores data about the process such
 * the user readable name of the process and the label the prover uses to
 * identify the process.
 */
public class ProcessVertex implements Serializable, ComposableCell {
	
	private static final long serialVersionUID = -5484985087972618772L;
	
	public static final int DEFAULT_VERTEX = 0;
	public static final int TERMINATOR_VERTEX = 1;
	public static final int MERGE_VERTEX = 2;
	
	private CProcess process;

	private String bundle;
	
	private boolean bottom = true;
	private int type = DEFAULT_VERTEX;

	public ProcessVertex(CProcess process, String bundle) {
		this.process = process;
		this.bundle = bundle;
	}

//	public ProcessVertex(CProcess process) {
//		this(process,process.getName());
//	}
	
	public ProcessVertex(CProcess process, String bundle, int type) {
		this(process,bundle);
		this.type = type;
	}

//	public ProcessVertex(CProcess process, int type) {
//		this(process);
//		this.type = type;
//	}

	public ProcessVertex(String name, String bundle, ProcessPort port) {
		this.process = new CProcess(CProcess.nameOfMerge(name),new Vector<ProcessPort>(),port);
		this.type = MERGE_VERTEX;
		this.bundle = bundle;
		this.bottom = false;
	}
	
	public String toString() {
		if (isTerminator())
			return ""; // "}"; // "\u2699";
		else if (isMerge())
			return "&";
		else 
			return process.getLabel();
	}

	public String getBundle() {
		return bundle;
	}

	// We do not want to set the bundle, because this will change *all* references to this vertex.
	// Instead, we clone the processvertex.
	//public void setBundle(String bundle) {
	//	this.bundle = bundle;
	//}

	public CProcess getProcess() {
		return process;
	}

	@Override
	public ProcessVertex newBundle(String newBundle) {
		ProcessVertex res = new ProcessVertex(this.process,newBundle,type);
		res.setBottom(bottom);
		return res;
	}

	
	public boolean isBottom() {
		return bottom;
	}

	public void setBottom(boolean bottom) {
		this.bottom = bottom;
	}

	public boolean isTerminator() {
		return this.type == TERMINATOR_VERTEX;
	}

	public boolean isMerge() {
		return this.type == MERGE_VERTEX;
	}	
}