package com.workflowfm.composer.processes.ui;

import java.io.Serializable;

import com.workflowfm.composer.graph.ComposableCell;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllTermPath;
import com.workflowfm.composer.processes.ProcessPort;

public class PortVertex implements Serializable, ComposableCell {

	private static final long serialVersionUID = -5904077822113658025L;

	// private CProcess process;

	private PortEdge portEdge;
	private String bundle;
	private CProcess process;
	private int index = -1;
	private boolean clickable;

	// public PortVertex(CProcess process, String bundle) {
	// this.process = process;
	// this.bundle = bundle;
	// }
	//
	// public PortVertex(CProcess process) {
	// this(process, process.getName());
	// }

	public PortVertex(CProcess process, int index, PortEdge portEdge, String bundle, boolean clickable) {
		this.process = process;
		this.index = index;
		this.portEdge = portEdge;
		this.bundle = bundle;
		this.clickable = clickable;
	}
	
	public PortVertex(CProcess process, PortEdge portEdge, String bundle, boolean clickable) {
		this(process,-1,portEdge,bundle,clickable);
	}

	public String getChannel() {
//		if (process.isComposite())
//			return "";
//		else 
		if (index >= 0 && index < process.getInputs().size()) 
			return process.getInputs().elementAt(index).getChannel();
		else if (index < 0 && process.getOutput() != null)
			return process.getOutput().getChannel();
		else
			return "";
	}

	public CllTerm getTerm() {
		return portEdge.getTerm();
	}

	public CllTerm getRootTerm() {
		return portEdge.getRootTerm();
	}
	
	public CllTermPath getTermPath() {
		return portEdge.getTermPath();
	}
	
	public boolean isInput() {
		return index >= 0;
	}
	
	public boolean isOutput() {
		return index < 0;
	}
	
	public boolean isOptional() {
		return portEdge.isOptional();
	}

	public String toString() {
		//return portEdge.getTermPath().toString();
		//return "" + this.isOutput();
		//return portEdge.toString();
		//return getChannel()==null?"":getChannel();
		return ""; 
	}

	public String getBundle() {
		return bundle;
	}

	public boolean isClickable() {
		return clickable;
	}

	@Override
	public PortVertex newBundle(String newBundle) {
		return new PortVertex(process,index,portEdge,newBundle,this.clickable);
	}
	
	public PortEdge getEdge() {
		return this.portEdge;
	}
	
	public ProcessPort getPort() {
		return new ProcessPort(getChannel(), getRootTerm());
	}

//	public void update(ChannelMapping chanmap) {
//		portEdge.update(chanmap);
//	}
	// public CProcess getProcess() {
	// return process;
	// }
}
