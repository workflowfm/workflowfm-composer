package piviz.helperClasses;

import java.util.ArrayList;

import piviz.exceptions.RestrictionTableException;


/**
 * One entry for the restriction table holds a set of processes and a set of
 * names that are restricted to the specified set of processes.
 * 
 * @author Anja
 * 
 */
public class RestrictionTableEntry {
	// / Set of process identifiers.
	private ArrayList pids;

	// / Set of names.
	private ArrayList names;

	// / Specifies if a table entry is valid for execution (temporalEntry =
	// false) or just for finding executable steps.
	boolean temporalEntry;

	public RestrictionTableEntry(ArrayList _pids, ArrayList _names,
			boolean _temporalEntry) {
		pids = new ArrayList();
		names = new ArrayList();
		this.setNames(_names);
		this.setPids(_pids);
		temporalEntry = _temporalEntry;
	}

	public ArrayList getNames() {
		return names;
	}

	public void setNames(ArrayList names) {
		this.names = names;
	}

	public ArrayList getPids() {
		return pids;
	}

	public void setPids(ArrayList pids) {
		this.pids = pids;
	}

	/**
	 * Add one name to the specified entry.
	 * 
	 * @param name
	 * @throws RestrictionTableException
	 */
	public void addName(String name) throws RestrictionTableException {
		if (!names.contains(name))
			this.names.add(name);
		else
			throw new RestrictionTableException(
					"RestrictionTable: addName(): Name already exists.");
	}

	/**
	 * Add a set of names to the specified entry.
	 * 
	 * @param names
	 * @throws RestrictionTableException
	 *             An already restricted name should not be added twice because
	 *             otherwise it would not be unique.
	 */
	public void addNames(ArrayList _names) throws RestrictionTableException {
		for (int i = 0; i < _names.size(); i++) {
			if (!names.contains(_names.get(i)))
				this.names.add(_names.get(i));
			else
				throw new RestrictionTableException(
						"RestrictionTable: addNames(): Name already exists.");
		}
	}
	
	/**
	 * Add a set of pids to the specified entry.
	 * 
	 * @param pids
	 * @throws RestrictionTableException
	 *             An already existing pid should not be added twice because
	 *             this is a hint that the pids are not unique.
	 */
	public void addPids(ArrayList _pids) throws RestrictionTableException {
		for (int i = 0; i < _pids.size(); i++) {
			if (!pids.contains(_pids.get(i)))
				this.pids.add(_pids.get(i));
			else
				throw new RestrictionTableException(
						"RestrictionTable: addPids(): Pid already exists.");
		}
	}

	public void addPid(String pid) {
		this.pids.add(pid);
	}

	public boolean isTemporalEntry() {
		return temporalEntry;
	}
	
	public void removePid(String pid){
		this.pids.remove(pid);
	}
	
	public void removeName(String name){
		this.names.remove(name);
	}

}
