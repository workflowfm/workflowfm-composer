package com.workflowfm.composer.processes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ComposeAction { 
	private String act;
	private String larg;
	private String lsel;
	private String rarg;
	private String rsel;
	private String res;
	
	public ComposeAction(String act, String larg, String lsel, String rarg,
			String rsel, String res) {
		this.act = act;
		this.larg = larg;
		this.lsel = lsel;
		this.rarg = rarg;
		this.rsel = rsel;
		this.res = res;
	}
	
	public ComposeAction(ComposeAction other) {
		this.act = other.getAction();
		this.larg = other.getLarg();
		this.lsel = other.getLsel();
		this.rarg = other.getRarg();
		this.rsel = other.getRsel();
		this.res = other.getResult();
	}
	
	public String getAction() {
		return act;
	}

	public String getLarg() {
		return larg;
	}

	public String getLsel() {
		return lsel;
	}

	public String getRarg() {
		return rarg;
	}

	public String getRsel() {
		return rsel;
	}

	public String getResult() {
		return res;
	}
	
	public Set<String> getDependencies() {
		HashSet<String> res = new HashSet<String>();
		res.add(larg);
		res.add(rarg);
		return res;
	}
	
	public static Set<String> getAllDependencies(Collection<ComposeAction> actions) {
		HashSet<String> res = new HashSet<String>();
		for (ComposeAction act : actions) {
			res.addAll(act.getDependencies());
		}
		
		return res;
	}

	public static Set<String> getRootDependencies(Collection<ComposeAction> actions) {
		HashSet<String> res = new HashSet<String>();
		HashSet<String> inter = new HashSet<String>();
		for (ComposeAction act : actions) {
			res.addAll(act.getDependencies());
			inter.add(act.getResult());
		}
		res.removeAll(inter);
		
		return res;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ComposeAction)) return false;
		ComposeAction other = (ComposeAction)o;
		return this.act.equals(other.getAction()) &&
				this.lsel.equals(other.getLsel()) &&
				this.larg.equals(other.getLarg()) &&
				this.rsel.equals(other.getRsel()) &&
				this.rarg.equals(other.getRarg()) &&
				this.res.equals(other.getResult());
	}
	
	public String debugString() {
		return act + ": " + larg + " (" + lsel + ") + " + rarg + " (" + rsel + ") = " + res;
	}
	
	public String getDescription() {
		return debugString();
	}
	
	public boolean renameProcess (String previousName, String newName) {
		boolean changed = false;
		if (this.larg.equals(previousName)) {
			this.larg = newName;
			changed = true;
		}
		if (this.rarg.equals(previousName)) {
			this.rarg = newName;
			changed = true;
		}
		if (this.res.equals(previousName)) {
			this.res = newName;
			changed = true;
		}
		return changed;
	}
}
