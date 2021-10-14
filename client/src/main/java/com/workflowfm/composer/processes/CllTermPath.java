package com.workflowfm.composer.processes;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;
import java.util.regex.Pattern;

import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.exceptions.InvalidProvenancePathException;
import com.workflowfm.composer.exceptions.NotFoundException;

public class CllTermPath implements Cloneable, Serializable {
	private static final long serialVersionUID = -2234991204538948170L;

	public static final String DEFAULT_DELIMITER = ".";
	
	private LinkedList<Integer> path;
	
	public CllTermPath() {
		path = new LinkedList<Integer>();
	}
	
	private CllTermPath(LinkedList<Integer> path) {
		this.path = path;
	}
	
	public CllTermPath(String path, String delimiter) throws NumberFormatException {
		this.path = new LinkedList<Integer>();
		
		for (String s : path.split(Pattern.quote(delimiter))) {
			this.path.add(Integer.parseInt(s));
		}
	}

	public CllTermPath(String path) throws NumberFormatException {
		this(path,DEFAULT_DELIMITER);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public CllTermPath clone() {
		return new CllTermPath((LinkedList<Integer>)this.path.clone());
	}
	
	public void add(int i) {
		path.add(i);
	}
	
	public void push(int i) {
		path.addFirst(i);
	}
	
	public boolean isRoot() {
		return path.size() == 0;
	}
	
	public CllTerm follow(CllTerm parent) throws InvalidCllPathException {
		CllTerm result = parent;
		for (Integer i : path) {
			Vector<CllTerm> args = result.getArgs();
			if (args.size() < i+1) throw new InvalidCllPathException(parent,this);
			result = args.elementAt(i);
		}
		return result;
	}
	
	public CllTerm followParent(CllTerm parent) throws InvalidCllPathException, NotFoundException {
		if (isRoot()) new NotFoundException("parent of a root term");
		CllTerm result = parent;
		CllTerm parentResult = parent;
		for (Integer i : path) {
			Vector<CllTerm> args = result.getArgs();
			if (args.size() < i+1) throw new InvalidCllPathException(parent,this);
			parentResult = result;
			result = args.elementAt(i);
		}
		return parentResult;
	}
	
	public ComposeProvenance follow(ComposeProvenance parent) { // throws InvalidProvenancePathException {
		ComposeProvenance result = parent;
		for (Integer i : path) {
			Vector<ComposeProvenance> args = result.getArgs();
			if (args.size() < i+1) return result; //throw new InvalidProvenancePathException(parent,this);
			result = args.elementAt(i);
		}
		return result;
	}

	public boolean isLeaf(ComposeProvenance parent) { 
		ComposeProvenance result = parent;
		for (Integer i : path) {
			Vector<ComposeProvenance> args = result.getArgs();
			if (args.size() < i+1) return false;
			result = args.elementAt(i);
		}
		return result.isLeaf();
	}
	
//	public ComposeProvenance followParent(ComposeProvenance parent) throws InvalidProvenancePathException, NotFoundException {
//		if (isRoot()) new NotFoundException("parent of a root term");
//		ComposeProvenance result = parent;
//		ComposeProvenance parentResult = parent;
//		for (Integer i : path) {
//			Vector<ComposeProvenance> args = result.getArgs();
//			if (args.size() < i+1) throw new InvalidProvenancePathException(parent,this);
//			parentResult = result;
//			result = args.elementAt(i);
//		}
//		return parentResult;
//	}
	
	public CllTerm traverse(CllTerm parent, CllTermVisitor visitor) throws InvalidCllPathException {
		CllTerm result = parent;
		CllTermPath childpath = new CllTermPath();
		if (visitor.visit(result,childpath)) return result;
		for (Integer i : path) {
			Vector<CllTerm> args = result.getArgs();
			if (args.size() < i+1) throw new InvalidCllPathException(parent,this);
			childpath.add(i);
			result = args.elementAt(i);
			
			if (visitor.visit(result,childpath)) return result;
		}
		return result;
	}
	
	public CllTerm traverseParent(CllTerm parent, CllTermVisitor visitor) throws InvalidCllPathException, NotFoundException {
		if (isRoot()) new NotFoundException("parent of a root term"); //TODO update if we have a CllTerm.toString
		CllTerm result = parent;
		CllTerm parentResult = parent;
		CllTermPath childpath = new CllTermPath();
		for (Integer i : path) {
			Vector<CllTerm> args = result.getArgs();
			if (args.size() < i+1) throw new InvalidCllPathException(parent,this);
			parentResult = result;
			if (visitor.visit(parentResult,childpath)) return parentResult;
			childpath.add(i);
			result = args.elementAt(i);
		}
		return parentResult;
	}
	
	public Collection<Integer> getCollection() {
		return path;
	}
	
	public int lastIndex() {
		return path.getLast().intValue();
	}
	
	public String toString(String delimiter) {
		StringBuffer result = new StringBuffer();
		for (Integer i : path) {
			result.append(i);
			result.append(delimiter);
		}
		return result.toString();
	}
	
	public String toString() {
		return this.toString(DEFAULT_DELIMITER);
	}
}
