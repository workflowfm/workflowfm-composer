package com.workflowfm.composer.processes;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.processes.ui.ProcessVertex;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.Workspace;

public class ComposeProvenance implements Serializable {
	private static final long serialVersionUID = -5750214718839260436L;

	public static final String BUFFER_SOURCE = "#";
	public static final String IDENTIFIER_SEPARATOR = ":";
	
	private String type;
	private String name = "";
	private Vector<ComposeProvenance> args;
	
	public ComposeProvenance(String type, String name, Vector<ComposeProvenance> args) {
		this.type = type;
		this.name = name;
		this.args = args;
	}

	public ComposeProvenance(String source) {
		this("source",source,new Vector<ComposeProvenance>());
	}

	public ComposeProvenance(ComposeProvenance other) {
		this.type = other.getType();
		this.name = other.getSource();
		this.args = new Vector<ComposeProvenance>(other.getArgs().size());
		for (ComposeProvenance arg : other.getArgs()) {
			this.args.add(new ComposeProvenance(arg));
		}
	}

	static public ComposeProvenance forTerm(String source, CllTerm term) {
		if (term.isAtomic()) return new ComposeProvenance(source);
		else {
			Vector<ComposeProvenance> args = new Vector<ComposeProvenance>();
			for (CllTerm subt : term.getArgs())
				args.add(forTerm(source,subt));
			return new ComposeProvenance(term.getType(), "", args);
		}
	}
	
	// Constructor used by GSON so that args is not null.
	@SuppressWarnings("unused")
	private ComposeProvenance() {
		this("","",new Vector<ComposeProvenance>());
	}

	public void clarifyOutputProvenance(Workspace workspace) {
		if (name.length() > 0 && !name.equals(ComposeProvenance.BUFFER_SOURCE)) {
			String[] parts = name.split(ComposeProvenance.IDENTIFIER_SEPARATOR);
			if (parts.length == 2 && workspace.processExists(parts[0])) {
				try {
					if (workspace.getProcess(parts[0]).isAtomic()) {
						Log.d("Resolved source [" + name + "] to: " + parts[0]);
						name = parts[0];
						return;
					}
					for (CProcess p : workspace.getComponents(workspace.getActionsForProcess(workspace.getComposition(parts[0])))) {
						if (p.hasInputChannel(parts[1]))
						{
							Log.d("Resolved source [" + name + "] to: " + p.getName());
							name = p.getName();
							return;
						}
					}					
				} catch (NotFoundException e) { }
				Log.d("Failed to resolve source [" + name + "]");
			}
		}
		
		for (ComposeProvenance prov : args)
			prov.clarifyOutputProvenance(workspace);
	}
	
	public boolean isLeaf() {
		return args.size() == 0;
	}

	public Optional<String> getSingleSource() {
		Set<String> sources = getSources();
		if (sources.size() == 1)
			return Optional.of(sources.iterator().next());
		else
			return Optional.empty();
	}

	public boolean isBuffered() {
		Set<String> sources = getSources();
		return (sources.size() == 1 && sources.contains(BUFFER_SOURCE));
	}

	public boolean hasNoBuffers() {
		Set<String> sources = getSources();
		return (!sources.contains(BUFFER_SOURCE));
	}
	
	
	/** Returns all the sources that occur in the CLL term. */
	public Set<String> getSources() {
		HashSet<String> res = new HashSet<String>();
		if (isLeaf()) res.add(name);
		else {
			for (ComposeProvenance c : args) {
				res.addAll(c.getSources());
			}
		}
		return res;
	}

	public String getType() { return type; }

	public String getSource() { return name; }

	//public void setName(String name) { this.name = name; }

	public Vector<ComposeProvenance> getArgs() { return args; }

	@Override 
	public boolean equals(Object o) {
		if (!(o instanceof ComposeProvenance)) return false;
		ComposeProvenance other = (ComposeProvenance) o;
		return (other.getType().equalsIgnoreCase(type)
				&& other.getSource().equalsIgnoreCase(name)
				&& args.equals(other.getArgs()));
	}

	private ComposeProvenance merge(ComposeProvenance c, String type) {
		Vector<ComposeProvenance> args = new Vector<ComposeProvenance>();
		if (this.type.equals(type)) {
			args = this.args;
		} else {
			args.add(this);
		}
		if (c.getType().equals(type)) {
			args.addAll(c.getArgs());
		} else {
			args.add(c);
		}
		return new ComposeProvenance(type,"",args);
	}

	public ComposeProvenance tensor(ComposeProvenance c) {
		return merge(c,"times");
	}

	public ComposeProvenance times(ComposeProvenance c) {
		return this.tensor(c);
	}

	public ComposeProvenance plus(ComposeProvenance c) {
		return merge(c,"plus");
	}

	public ComposeProvenance merge(ComposeProvenance c) { 
		return merge(c,type); 
	}

	public void expand(ComposeProvenance c) {
		//TODO check if atomic or unary?
		this.args.add(c);
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("(");
		boolean first = true;
		if (type.equals("times")) {
			for (ComposeProvenance arg : args) {
				if (first) {
					first = false;
					result.append(arg.toString());
				} else {
					result.append(" ** " + arg.toString());
				}
			}
			result.append(")");
			return result.toString();
		} else if (type.equals("plus")) {
			for (ComposeProvenance arg : args) {
				if (first) {
					first = false;
					result.append(arg.toString());
				} else {
					result.append(" ++ " + arg.toString());
				}
			}
			result.append(")");
			return result.toString();
		} else if (type.equals("source")) {
			result.append(name + ")");
			return result.toString();
		} else {
			for (ComposeProvenance arg : args) {
				if (first) {
					first = false;
					result.append(arg.toString());
				} else {
					result.append(" [" + type + "] " + arg.toString());
				}
			}
			result.append(")");
			return result.toString();
		}
	}
	
//	public ComposeProvenance neg() {
//		Vector<ComposeProvenance> args = new Vector<ComposeProvenance>();
//		args.add(this);
//		return new ComposeProvenance("neg","",args);
//	}

}