package com.workflowfm.composer.processes.ui;

import java.io.Serializable;

import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllTermPath;

/**
 * Represents a port edge on a service graph. This stores the message name, the
 * channel name and the CLL term that the edge was generated from.
 */
public class PortEdge implements Serializable{

	//TODO make this implement ComposableCell so that edges also know which bundle they belong to!
	
	private static final long serialVersionUID = -5150804823924078305L;

	private boolean buffer;
	private boolean optional;
	private boolean muted = false;

	/**
	 * TODO update Used to store the CLL term that this message was generated from e.g. so
	 * you would know that a message edge with the name B was created from the
	 * term (A ++ B ++ C). WARNING: This is not the case anymore!!!
	 */
	private CllTerm term;
	
	/**
	 * Used to store the path in the CLL term representing the location of the 
	 * particular edge.
	 */
	private CllTerm root;
	private CllTermPath path;


//	public PortEdge(String message, String channel, boolean buffer,
//			boolean optional) {
//		this(message,channel,buffer,optional,null,new CllTermPath());
//	}
	
	public PortEdge(CllTerm term, CllTerm root, CllTermPath path,
			boolean buffer, boolean optional) {
		super();
		this.term = term;
		this.root = root;
		this.path = path;
		this.buffer = buffer;
		this.optional = optional;
	}

	public PortEdge(PortEdge o, boolean buffer) {
		super();
		this.term = o.getTerm();
		this.root = o.getRootTerm();
		this.path = o.getTermPath().clone();
		this.buffer = buffer;
		this.optional = o.isOptional();
	}
	
	public PortEdge(PortEdge o) {
		super();
		this.term = o.getTerm();
		this.root = o.getRootTerm();
		this.path = o.getTermPath().clone();
		this.buffer = o.isBuffer();
		this.optional = o.isOptional();
	}

//	public PortEdge(String message) {
//		this(message, null);
//	}
	
	public CllTerm getTerm() {
		return term;
	}
	
	public CllTerm getRootTerm() {
		return this.root;
	}

	public CllTermPath getTermPath() {
		return path;
	}
	
	public boolean isBuffer() {
		return buffer;
	}

	public boolean isOptional() {
		return optional;
	}

//	public boolean messageEquals(String message) {
//		return Utils.stringsEqual(this.message, message);
//	}

//	public String getMessage() {
//		return message;
//	}
	
	public boolean isMuted() {
		return muted;
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}	
	
	public String toString() {
		return isMuted()?"":(term.isAtomic()?term.getName():"");
	}

	// TODO maybe via prover?
//	public String toInformativeString() {
//		return "Message: " + message + ", Channel: " + channel
//				+ ", Generated from: " + termGeneratedFrom;
//	}
	
	// TODO maybe we only need to compare channel and term?? Where is this used?
	@Override 
	public boolean equals(Object o) {
		if (!(o instanceof PortEdge)) return false;
		PortEdge other = (PortEdge) o;
		return (other.getTerm().equals(this.term)
				&& other.getRootTerm().equals(this.root)
				&& other.getTermPath().equals(this.path)
				&& other.isBuffer() == buffer
				&& other.isOptional() == optional);
	}

}
