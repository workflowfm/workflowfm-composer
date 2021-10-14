package com.workflowfm.composer.session;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;

import com.workflowfm.composer.edit.UndoableSessionEdit;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllValidator;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ProcessStore;
import com.workflowfm.composer.processes.ProcessStoreChangeListener;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.Workspace;

public class CompositionSession implements ProcessStore {
	private Prover prover;
	private CllValidator validator;

	private Map<String, CProcess> processMap = new HashMap<String, CProcess>();

	private Map<String, Workspace> workspaceMap = new HashMap<String, Workspace>();
	private Workspace activeWorkspace = null;
	
	private UndoManager undoManager = new UndoManager();
	
	private int workspaceCounter = 0;	
	private int processCounter = 0;
	
	private File saveFile = null;

	private CopyOnWriteArrayList<CompositionSessionChangeListener> stateChangeListeners = new CopyOnWriteArrayList<CompositionSessionChangeListener>();
	private CopyOnWriteArrayList<ProcessStoreChangeListener> storeChangeListeners = new CopyOnWriteArrayList<ProcessStoreChangeListener>();
	
	//private TreeSet<String> resources = new TreeSet<String>();

	//private SimpleOntologyManager simpleOntologyManager = new SimpleOntologyManager( OWLManager.createOWLOntologyManager() );	

	//private Vector<DeploymentLogListener> deploymentLogListeners = new Vector<DeploymentLogListener>();


	public CompositionSession(Prover prover) {
		this.prover = prover;
		this.validator = new CllValidator(prover);
	}

	@Override
	public CProcess getProcess(String name) throws NotFoundException
	{
		CProcess p = processMap.get(name);

		if (p == null)
			throw new NotFoundException("process",name,"composition session");

		return p;
	}

	@Override
	public boolean processExists(String name)
	{
		return (processMap.get(name) != null);
	}

	@Override
	public void addProcess(CProcess process) {
		if (processExists(process.getName())) {
			throw new RuntimeException("Process " + process.getName() + " already exists.");
		} else {
			Log.d("Adding process: " + process.getName());
			processMap.put(process.getName(), process);
			notifyProcessAdded(process);
		}
	}
	
	@Override
	public void updateProcess(String name, CProcess process) throws NotFoundException {
		if (!processExists(name))
			throw new NotFoundException("process",name,"composition session");
		
		if (!process.equals(getProcess(name))) {
			Log.d("Updating process: " + name + " (new: "+ process.getName() + ")");
			processMap.remove(name);
			processMap.put(process.getName(), process);
			notifyProcessUpdated(name, process);
		} else {
			Log.d("No change when updating process: " + name + " to " + process.getName() + " (valid:" + process.isValid() + "-checked:" + process.isChecked() + ")");
			processMap.put(process.getName(), process); // This may update checked/verified state.
		}	
	}
	
	@Override
	public void removeProcess(CProcess process) throws NotFoundException {
		this.removeProcess(process.getName());
	}

	@Override
	public void removeProcess(String name) throws NotFoundException {
		if (processExists(name)) {
			Log.d("Removing process: " + name);
			CProcess process = processMap.get(name);
			notifyProcessRemoved(process);
			processMap.remove(name);
		}
		else throw new NotFoundException("process",name,"composition session");
	}
	
	public int processCount()
	{
		return processMap.size();
	}
	
	public Collection<CProcess> getProcesses() {
		return processMap.values();
	}
	
	
	public Prover getProver()
	{
		return prover;
	}

	public void setProver(Prover prover)
	{
		this.prover = prover;
	}
		
	public int getWorkspaceCounter() {
		return workspaceCounter;
	}

	public void setWorkspaceCounter(int workspaceCounter) {
		this.workspaceCounter = workspaceCounter;
	}

//	public boolean channelNameExists(String name)
//	{
//		Collection<CProcess> procs = processMap.values();
//
//		for (CProcess p : procs)
//		{
//			if (p.hasChannel(name))
//				return true;
//		}
//
//		return false;
//	}

//	public SimpleOntologyManager getSimpleOntologyManager() {
//		return simpleOntologyManager;
//	}

//	public void setSimpleOntologyManager(SimpleOntologyManager simpleOntologyManager) {
//		this.simpleOntologyManager = simpleOntologyManager;
//	}

	public String getFreshProcessName()
	{
		return this.getFreshProcessName("P");
	}

	public String getFreshProcessName(String prefix)
	{
		String processName = "";
		do {
			processCounter++;
			processName = prefix + processCounter;
		} while (processExists(processName));

		return processName;
	}	
	
	// TODO is this or getComponents better?
	public Set<CProcess> getProcessComponents(String name) throws NotFoundException {
		return getProcessComponents(getProcess(name).getActions());
	}
	
	public Set<CProcess> getProcessComponents(CProcess process) throws NotFoundException {
		return getProcessComponents(process.getActions());
	}
	
	public Set<CProcess> getProcessComponents(Collection<ComposeAction> actions) throws NotFoundException {
		HashSet<CProcess> components = new HashSet<>();
		
		// TODO Do we care about root dependencies being intermediates? Can this ever happen?
		for (String dep : ComposeAction.getAllDependencies(actions)) {
			if (processExists(dep))
				components.add(getProcess(dep));
		}
		return components;
	}
	
	public Set<CProcess> getAllDependencies(String name) throws NotFoundException {
		return getAllDependencies(getProcess(name));
	}

	public Set<CProcess> getAllDependencies(CProcess process) throws NotFoundException {
		HashSet<CProcess> res = new HashSet<CProcess>();

		for (CProcess dep : getProcessComponents(process)) {
			res.add(dep);
			res.addAll(getAllDependencies(dep));
		}
		return res;
	}
	
	// Dependencies may repeat in this one!
	public Vector<CProcess> getProcessWithOrderedDependencies(String name) throws NotFoundException {
		return getProcessWithOrderedDependencies(getProcess(name));
	}
	
	public Vector<CProcess> getProcessWithOrderedDependencies(CProcess process) throws NotFoundException {
		Vector<CProcess> res = new Vector<CProcess>();
		
		if (process.isAtomic()) {
			res.add(process);
			return res;
		}
		
		for (CProcess dep : getProcessComponents(process)) {
			res.addAll(getProcessWithOrderedDependencies(dep));
		}
		res.add(process);
		return res;
	}
	
	
	public Set<CProcess> getComponents(String name) throws NotFoundException {
		return getComponents(getProcess(name).getActions());
	}
	
	public Set<CProcess> getComponents(CProcess process) throws NotFoundException {
		return getComponents(process.getActions());
	}
	
	public Set<CProcess> getComponents(Collection<ComposeAction> actions) throws NotFoundException {
		HashSet<CProcess> components = new HashSet<>();
		
		for (String dep : ComposeAction.getRootDependencies(actions)) {
			components.add(getProcess(dep));
		}
		return components;
	}
	
	public Set<CProcess> getChildren(String name) {
		HashSet<CProcess> children = new HashSet<>();
		for (CProcess pr : this.processMap.values()) {
			if (pr.isChildOf(name)) 
				children.add(pr);
		}
		return children;
	}
	
	public Set<CProcess> getAncestors(String name) {
		HashSet<CProcess> ancestors = new HashSet<>();
		for (CProcess pr : getChildren(name)) {
			ancestors.add(pr);
			ancestors.addAll(getAncestors(pr));
		}
		return ancestors;
	}
	
	public Set<CProcess> getAncestors(CProcess process) {
		return getAncestors(process);
	}

	public UndoManager getUndoManager() {
		return this.undoManager;
	}
	
	public void addToUndoManager(UndoableSessionEdit edit) {
		Log.d("Adding to undo manager: " + edit.getPresentationName() + " (" + edit.canUndo() + ")");
		undoManager.undoableEditHappened(new UndoableEditEvent(this, edit));
		updateUndoRedoStatus();
	}
	
	public void updateUndoRedoStatus()
	{
		for (CompositionSessionChangeListener listener : this.stateChangeListeners) {
			listener.undoRedoUpdate();
		}
	}
	

	public Workspace getWorkspace(String name) throws NotFoundException
	{
		Workspace w = workspaceMap.get(name);

		if (w == null)
			throw new NotFoundException("workspace",name,"composition session");

		return w;
	}

	public boolean workspaceExists(String name)
	{
		return (workspaceMap.get(name) != null);
	}
	
	private String getNewWorkspaceName() {
		workspaceCounter++;
		return "Workspace " + workspaceCounter;
	}

	public Workspace createWorkspace() {
		String name = getNewWorkspaceName();
		Workspace w = new Workspace(name, this);
		addWorkspace(w);
		return w;
	}	
	
	public void addWorkspace(Workspace workspace) {
		Log.d("Adding workspace: " + workspace.getName());
		workspaceMap.put(workspace.getName(), workspace);
		this.addChangeListener(workspace);
		notifyWorkspaceAdded(workspace);
		setActiveWorkspace(workspace.getName());
	}

	public void removeWorkspace(String name) {
		if (workspaceExists(name)) {
			removeWorkspace(workspaceMap.get(name));
		}
	}
	
	public void removeWorkspace(Workspace workspace) {
		if (workspaceExists(workspace.getName())) {
			Log.d("Removing workspace: " + workspace.getName());
			workspaceMap.remove(workspace.getName());
			this.removeChangeListener(workspace);
			notifyWorkspaceRemoved(workspace);
		}
	}
	
	public Workspace getActiveWorkspace() {
		return this.activeWorkspace;
	}
	
	public void setActiveWorkspace(String name) {
		if (workspaceExists(name)) {
			Log.d("Setting active workspace: " + name);
			Workspace workspace = null;
			try {
				workspace = getWorkspace(name);
			} catch (NotFoundException e) { } // never happens
			this.activeWorkspace = workspace;
			notifyWorkspaceActivated(workspace);
		} else {
			Log.d("Setting null active workspace.");
			this.activeWorkspace = null;
			notifyWorkspaceActivated(null);
		}
	}
	
	public Collection<Workspace> getWorkspaces() {
		return workspaceMap.values();
	}

	public void reset() {
		processMap.clear();
		workspaceMap.clear();
		activeWorkspace = null;
		setSaveFile(null);
		undoManager.discardAllEdits();
		// updateUndoRedoStatus();
		workspaceCounter = 0;
		processCounter = 0;
//		simpleOntologyManager.reset();
		for (CompositionSessionChangeListener listener : stateChangeListeners)
			listener.sessionReset();
	}
	
	public File getSaveFile() {
		return saveFile;
	}

	public void setSaveFile(File saveFile) {
		this.saveFile = saveFile;
	}
	
	//	public void addDeploymentLogListener (DeploymentLogListener d)
	//	{
	//		deploymentLogListeners.add(d);
	//	}
	//	
	//	public void removeDeploymentLogListener (DeploymentLogListener d)
	//	{
	//		deploymentLogListeners.remove(d);
	//	}
	//
	//	public void fireDeploymentStart (int numberOfFiles)
	//	{
	//		for (DeploymentLogListener l : deploymentLogListeners)
	//		{
	//			l.start(numberOfFiles);
	//		}
	//	}
	//	
	//	public void fireDeploymentSuccess (String file)
	//	{
	//		for (DeploymentLogListener l : deploymentLogListeners)
	//		{
	//			l.success(file);
	//		}
	//	}
	//
	//	public void fireDeploymentSkipped (String file)
	//	{
	//		for (DeploymentLogListener l : deploymentLogListeners)
	//		{
	//			l.skipped(file);
	//		}
	//	}
	//	
	//	public void fireDeploymentFailure (String file, Exception exception)
	//	{
	//		for (DeploymentLogListener l : deploymentLogListeners)
	//		{
	//			l.failure(file, exception);
	//		}
	//	}
	//
	//	public void fireDeploymentFinished ()
	//	{
	//		for (DeploymentLogListener l : deploymentLogListeners)
	//		{
	//			l.finish();
	//		}
	//	}
	
	public void addChangeListener(CompositionSessionChangeListener listener) {
		this.stateChangeListeners.add(listener);
	}
	
	public void removeChangeListener(CompositionSessionChangeListener listener) {
		this.stateChangeListeners.remove(listener);
	}

	public void addChangeListener(ProcessStoreChangeListener listener) {
		this.storeChangeListeners.add(listener);
	}
	
	public void removeChangeListener(ProcessStoreChangeListener listener) {
		this.storeChangeListeners.remove(listener);
	}
	
	private void notifyWorkspaceAdded(Workspace workspace) {
		for (CompositionSessionChangeListener listener : this.stateChangeListeners) {
			listener.workspaceAdded(workspace);
		}
	}
	
	private void notifyWorkspaceActivated(Workspace workspace) {
		for (CompositionSessionChangeListener listener : this.stateChangeListeners) {
			listener.workspaceActivated(workspace);
		}
	}
	
	private void notifyWorkspaceRemoved(Workspace workspace) {
		for (CompositionSessionChangeListener listener : this.stateChangeListeners) {
			listener.workspaceRemoved(workspace);
		}
	}
	
	private void notifyProcessAdded(CProcess process) {
		for (ProcessStoreChangeListener listener : this.storeChangeListeners) {
			listener.processAdded(process);
		}
	}
	
	private void notifyProcessRemoved(CProcess process) {
		for (CProcess pr : getChildren(process.getName())) {
			Log.d("Unchecking process: " + pr.getName());
			pr.unCheck();
			notifyProcessUpdated(pr.getName(), pr);
		}
		for (ProcessStoreChangeListener listener : this.storeChangeListeners) {
			listener.processRemoved(process);
		}
	}
	
	private void notifyProcessUpdated(String previousName, CProcess process) {
		for (CProcess pr : getChildren(previousName)) {
			Log.d("Unchecking process: " + pr.getName());
			pr.unCheck();
			pr.renameComponent(previousName, process.getName());
			notifyProcessUpdated(pr.getName(), pr);
		}
		for (ProcessStoreChangeListener listener : this.storeChangeListeners) {
			listener.processUpdated(previousName, process);
		}
	}
	
	public void notifySessionSaved() {
		for (CompositionSessionChangeListener listener : this.stateChangeListeners) {
			listener.sessionSaved();
		}
	}
	
	public CllValidator getValidator() {
		return this.validator;
	}
}