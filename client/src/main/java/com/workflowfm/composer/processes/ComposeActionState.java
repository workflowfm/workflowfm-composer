package com.workflowfm.composer.processes;

import java.util.Collection;
import java.util.ListIterator;
import java.util.Vector;

import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.utils.Utils;

public class ComposeActionState {
	private String label = "";
	private int ctr = 0;
	private Collection<MergedInput> merged = new Vector<MergedInput>();
	private Collection<InputProvenance> iprov = new Vector<InputProvenance>();
	private Collection<OutputProvenance> prov = new Vector<OutputProvenance>();
	
	public ComposeActionState() { }

	public ComposeActionState (String label) {
		this.label = label.replace(' ', '_');
	}

	public ComposeActionState (String label, Collection<CProcess> processes) {
		this.label = label.replace(' ', '_');
		for (CProcess proc : processes) {
			if (proc.getProvenance() != null)
				prov.add(new OutputProvenance(proc.getName(),proc.getProvenance()));
		}
	}

	public ComposeActionState (String label, CProcess lproc, CProcess rproc) {
		this.label = label.replace(' ', '_');
		if (lproc.getProvenance() != null)
			prov.add(new OutputProvenance(lproc.getName(),lproc.getProvenance()));
		if (rproc.getProvenance() != null)
			prov.add(new OutputProvenance(rproc.getName(),rproc.getProvenance()));
	}	

	public ComposeActionState(String label, CProcess lproc, CProcess rproc, Vector<MergedInput> merged) {
		this(label,lproc,rproc);
		this.merged = merged;
	}
	
	// For testing!
	public ComposeActionState(String label, CProcess lproc, CProcess rproc, Vector<MergedInput> merged, CllTerm term, ComposeProvenance prov) {
		this(label,lproc,rproc,merged);
		this.iprov.add(new InputProvenance(term,prov));
	}
	
	public class OutputProvenance {
		private String name;
		private ComposeProvenance prov;
		
		public OutputProvenance() { }
		public OutputProvenance(String name, ComposeProvenance prov) {
			this.name = name;
			this.prov = prov;
		}
		public String getName() { return name; }
		public ComposeProvenance getProvenance() { return prov; }
	}

	public class InputProvenance {
		private CllTerm term;
		private ComposeProvenance prov;
		
		public InputProvenance() { }
		public InputProvenance(CllTerm term, ComposeProvenance prov) {
			super();
			this.term = term;
			this.prov = prov;
		}
		public CllTerm getTerm() { return term; }
		public ComposeProvenance getProvenance() { return prov; }
	}

	public int getCtr() {
		return ctr;
	}
	
	public Collection<MergedInput> getMerged() {
		return merged;
	}
	
	public ComposeProvenance getOutputProvenance(String name) throws NotFoundException {
		for (OutputProvenance op : prov) {
			if (op.getName().equals(name)) return op.getProvenance();
		}
		throw new NotFoundException("output provenance for", name, "action state");
	}
	
	public ComposeProvenance getInputProvenance(CllTerm term) throws NotFoundException {
		for (InputProvenance ip : iprov) {
			if (ip.getTerm().equals(term)) return ip.getProvenance();
		}
		throw new NotFoundException("input provenance for", term.toString(), "action state"); // TODO term.toString ?!?
	}	
	
	// TODO update with provs
	public String debugString() { return "AState[Lbl:" + label + "|Ctr:" + ctr + "|Merge:" + Utils.stringOf(merged, ",") + "]"; } 
}
