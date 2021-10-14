package piviz.helperClasses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.TreeMap;

import piviz.exceptions.RestrictionTableException;


/**
 * The restriction table holds the names which are restricted to a process or to
 * a set of processes. [TODO: Think about temporal. Can a channel be an entry
 * for one process in a temporal entry as well as a non temporal entry at the
 * same time? - I don't think so. - The current implementation does not take
 * this into account at the moment.]
 * 
 * @author Anja
 * 
 */
public class RestrictionTable {
	private ArrayList entries;

	public RestrictionTable() {
		entries = new ArrayList();
	}

	/**
	 * Add a new entry. This method has to take care that multiple occurences of
	 * a set of pids does not occur. One set can occur exaclty twice, the first
	 * as a temporal and the second as a non temporal entry.
	 * 
	 * @param pids
	 *            Set of process identifiers.
	 * @param names
	 *            Set of names restricted to the processes specified in pids.
	 * @throws RestrictionTableException
	 */
	public void addEntry(ArrayList pids, ArrayList names, boolean temporalEntry)
			throws RestrictionTableException {
		try {
			/*
			 * Check if the incoming set of pids already exists and if this is
			 * the case just add the new names.
			 */

			RestrictionTableEntry entry = findEntryForSetOfPIDs(pids,
					temporalEntry);
			if (entry != null) {
				entry.addNames(names);

			} else {
				RestrictionTableEntry newEntry = new RestrictionTableEntry(
						pids, names, temporalEntry);
				entries.add(newEntry);
			}

		} catch (RestrictionTableException e) {
			// TODO Auto-generated catch block
			throw new RestrictionTableException(
					"RestrictionTable: addEntry(): " + e.getMessage());
		}

	}

	/**
	 * Finds the matching entry in the restriction table for this set of pids.
	 * The temporal Value of the entries have to match, too.
	 * 
	 * @param pids
	 * @param temporalEntry
	 * @return
	 */
	private RestrictionTableEntry findEntryForSetOfPIDs(ArrayList pids,
			boolean temporalEntry) {
		for (int i = 0; i < entries.size(); i++) {
			RestrictionTableEntry entry = (RestrictionTableEntry) entries
					.get(i);
			if (entry != null) {
				if (entry.getPids().containsAll(pids)
						&& (entry.isTemporalEntry() == temporalEntry)) {
					/*
					 * Since this is an exact match the entry also should not
					 * contain more pids. To check this copy the two lists and
					 * make an intersection
					 */
					ArrayList entPidCopy = new ArrayList(entry.getPids());
					entPidCopy.removeAll(pids);
					if (entPidCopy.size() == 0)
						return (RestrictionTableEntry) entries.get(i);
				}
			}
		}
		return null;
	}

	public ArrayList findEntriesForPid(String pid) {
		ArrayList foundEntries = new ArrayList();
		for (int i = 0; i < this.entries.size(); i++) {
			if (((RestrictionTableEntry) this.entries.get(i)).getPids()
					.contains(pid))
				foundEntries.add(this.entries.get(i));
		}
		return foundEntries;
	}

	/**
	 * The entries containing oldPid are updated, which means that oldPid is
	 * replaced by the set of newPids. This is useful in case of resolving
	 * compositions where parallel processes are assigned new pids, but it is
	 * vitally important that they keep the same scope as the old pid.
	 * 
	 * @param oldPid
	 * @param newPids
	 * @throws RestrictionTableException
	 */
	public void replacePid(String oldPid, ArrayList newPids)
			throws RestrictionTableException {
		try {
			// Get entries containing the old pid.
			ArrayList foundEntries = findEntriesForPid(oldPid);

			// Replace the old pid in those entries.
			for (int i = 0; i < foundEntries.size(); i++) {
				RestrictionTableEntry e = (RestrictionTableEntry) foundEntries
						.get(i);
				// add new ones
				e.addPids(newPids);
				// remove old one
				e.removePid(oldPid);
			}

		} catch (RestrictionTableException e) {
			// TODO Auto-generated catch block
			throw new RestrictionTableException(
					"RestrictionTable: updatePid(): " + e.getMessage());
		}

	}

	/**
	 * Check if a restriction on this channel for this process exists.
	 * 
	 * @param pid
	 * @param channel
	 * @return True if such a restriction exists, false otherwise.
	 */
	public boolean isRestrictedForProcess(String pid, String channel) {
		for (int i = 0; i < entries.size(); i++) {
			if (((RestrictionTableEntry) entries.get(i)).getPids()
					.contains(pid)
					&& ((RestrictionTableEntry) entries.get(i)).getNames()
							.contains(channel))
				return true;
		}
		return false;
	}

	/**
	 * Determine the scope of a channel from a given channel and a process
	 * identifier. The scope is defined by all processes a channel is restricted
	 * to.
	 * 
	 * @param pid
	 * @param channel
	 * @return ArrayList containig all pids channel is restricted to.
	 */
	public ArrayList getScopeOfChannel(String pid, String channel) {
		for (int i = 0; i < entries.size(); i++) {
			if (((RestrictionTableEntry) entries.get(i)).getPids()
					.contains(pid)
					&& ((RestrictionTableEntry) entries.get(i)).getNames()
							.contains(channel))
				return ((RestrictionTableEntry) entries.get(i)).getPids();
		}
		return null;
	}

	/**
	 * Find the entry which contains both of the parameters. Exactly one or no
	 * such entry exists. Temporal has to be false, since this is needed during
	 * real execution, which only handles permanent entries.
	 * 
	 * @param pid
	 * @param _name
	 * @return Entry containing pid and name, null if such an entry does not
	 *         exist.
	 */
	public RestrictionTableEntry getEntryForPidAndName(String pid, String _name) {
		for (int i = 0; i < this.entries.size(); i++) {
			RestrictionTableEntry current = (RestrictionTableEntry) this.entries
					.get(i);
			if (current != null) {
				if (// !current.isTemporalEntry() &&
				current.getPids().contains(pid)
						&& current.getNames().contains(_name)) {
					return current;
				}
			}
		}
		return null;
	}

	/**
	 * The pidB is added to all entries that contain pidA exept the temporal
	 * entries. This means that the scope of process B is extended to be the
	 * current scope of process A.
	 * 
	 * @param pidA
	 *            Process Id of an existing process.
	 * @param pidB
	 *            Process Id of a new process.
	 */
	public void addProcessBToScopeOfProcessA(String pidA, String pidB) {
		for (int i = 0; i < this.entries.size(); i++) {
			if (((RestrictionTableEntry) this.entries.get(i)).getPids()
					.contains(pidA)
					&& ((RestrictionTableEntry) this.entries.get(i))
							.isTemporalEntry() == false)
				((RestrictionTableEntry) this.entries.get(i)).getPids().add(
						pidB);
		}
	}

	/**
	 * Remove the names from the entry for pids. This happens if a temporal
	 * entry of a restriction node is replaced. The whole entry is deleted if no
	 * names are left in it.
	 * 
	 * @param _pids
	 * @param _names
	 * @param temporal
	 */
	public void removeEntry(ArrayList _pids, ArrayList _names, boolean temporal)
			throws RestrictionTableException {
		RestrictionTableEntry entry = findEntryForSetOfPIDs(_pids, temporal);
		// this entry should contain all the names in names
		if (entry != null) {
			if (!entry.getNames().containsAll(_names))
				throw new RestrictionTableException(
						"RestrictionTable: removeEntry(): Non valid entries in restriction table.");

			entry.getNames().removeAll(_names);
			if (entry.getNames().size() < 1)
				this.entries.remove(entry);
		}
	}

	/**
	 * The entry in the restriction table which holds a restriction on the name
	 * _name for the process with the id exixtingPid is sought out and if such
	 * an entry exists newPid will be added to it. Only non temporal entries
	 * will be taken into account. If no entry is found nothing will happen.
	 * 
	 * @param existingPid
	 *            Id of the process whose scope is extended.
	 * @param newPid
	 *            Id of the process that is added to the scope of the name.
	 * @param _name
	 *            Name concerning which the scope is extended.
	 */
	public void extendScopeOfName(String existingPid, String newPid,
			String _name) {
		// find entry
		RestrictionTableEntry current = getEntryForPidAndName(existingPid,
				_name);
		if (current != null) {
			/*
			 * Such an entry exists, now copy the list of pids, remove the name
			 * from this list and make a new entry containing all the copied
			 * pids + the new pid and the name.
			 */
			ArrayList newPidList = new ArrayList(current.getPids());
			newPidList.add(newPid);
			ArrayList newNameList = new ArrayList();
			newNameList.add(_name);
			/*
			 * Before creating a new entry check if this set of pids already
			 * exists and if so just add the name there.
			 */
			RestrictionTableEntry newEntry;
			if ((newEntry = findEntryForSetOfPIDs(newPidList, false)) != null)
				newEntry.getNames().add(_name);
			else {
				newEntry = new RestrictionTableEntry(newPidList, newNameList,
						false);
				this.entries.add(newEntry);
			}
			current.getNames().remove(_name);
			if (current.getNames().size() == 0)
				this.entries.remove(current);

		}
		// for (int i = 0; i < this.entries.size(); i++) {
		// RestrictionTableEntry current = (RestrictionTableEntry) this.entries
		// .get(i);
		// if (current != null) {
		// if (!current.isTemporalEntry()
		// && current.getPids().contains(existingPid)
		// && current.getNames().contains(_name)) {
		// current.getPids().add(newPid);
		// // exactly one such an entry can exist.
		// return;
		// }
		// }
		// }
	}

	/**
	 * Clears the restriction table from all entries which have the temporal
	 * flag set to true.
	 * 
	 */
	public void removeTemporalEntries() {
		for (int i = 0; i < this.entries.size(); i++) {
			RestrictionTableEntry current = (RestrictionTableEntry) this.entries
					.get(i);
			if (current != null) {
				if (current.isTemporalEntry()) {
					this.entries.remove(current);
					i--;
				}
				// also remove this entry if it contains no names or no pids
				else if (current.getNames().size() == 0 || current.getPids().size() == 0){
					this.entries.remove(current);
					i--;
				}
			}
		}
	}

	/**
	 * Create a sorted map of the channels and their scopes. <key: channel><value:
	 * ArrayList pids>
	 * 
	 * @return
	 */

	public TreeMap getScopes() {
		Hashtable keyTracker = new Hashtable();
		TreeMap treeMap = new TreeMap();

		for (int i = 0; i < entries.size(); i++) {
			RestrictionTableEntry entry = (RestrictionTableEntry) entries
					.get(i);
			if (entry != null) {
				ArrayList names = entry.getNames();
				ListIterator iter = names.listIterator();
				while (iter.hasNext()) {
					String channel = (String) iter.next();
					if (!(channel == null || channel == "")) {
						Integer keyIdentifier = new Integer("1");
						if (keyTracker.containsKey(channel)) {
							keyIdentifier = new Integer(((Integer) keyTracker
									.get(channel)).intValue() + 1);
							keyTracker.put(channel, keyIdentifier);
						} else
							keyTracker.put(channel, keyIdentifier);

						if (keyIdentifier.intValue() == 1) {
							treeMap
									.put(channel,
											new ArrayList(entry.getPids()));
						} else {
							treeMap.put(channel + " ("
									+ keyIdentifier.toString() + ")",
									new ArrayList(entry.getPids()));
						}
					}
				}
			}

		}

		return treeMap;
	}

	/**
	 * Cleans the restriction table by removing all entries of pids not
	 * contained in existingPids.
	 * 
	 * @param existingPids
	 *            Contains the process identifiers of all currently running
	 *            processes.
	 */
	public void clean(HashSet existingPids) {

		for (int i = 0; i < entries.size(); i++) {
			RestrictionTableEntry e = (RestrictionTableEntry) entries.get(i);
			if (e != null) {
				ArrayList pids = e.getPids();
				pids.retainAll(existingPids);

				if (pids.size() < 1){
					entries.remove(e);
					i--;
				}
			}
		}
	}

}
