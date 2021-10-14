package com.workflowfm.composer.workspace;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.ProcessStore;
import com.workflowfm.composer.processes.ProcessStoreChangeListener;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.prover.response.ComposeResponse;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.edit.CompositionEdit;
import com.workflowfm.composer.workspace.edit.JoinEdit;
import com.workflowfm.composer.workspace.edit.TensorEdit;
import com.workflowfm.composer.workspace.edit.WithEdit;

public class Workspace implements ProcessStore, ProcessStoreChangeListener
{
	private CompositionSession session;
	private String name;

	private ProcessGraph graph = new ProcessGraph();

	private Map<String, CProcess> compositionMap = new HashMap<String, CProcess>();

	private int stepCounter = 1;	

	private transient CopyOnWriteArrayList<ProcessStoreChangeListener> stateChangeListeners = new CopyOnWriteArrayList<ProcessStoreChangeListener>();

	public Workspace(String name, CompositionSession session) {
		this.session = session;
		this.name = name;
	}

	public Workspace(String name, CompositionSession session, int stepCounter, Collection<CProcess> compositions) {
		this.session = session;
		this.name = name;
		this.stepCounter = stepCounter;
		for (CProcess process : compositions)
			addComposition(process);
	}

	public CProcess getComposition(String name) throws NotFoundException
	{
		CProcess p = compositionMap.get(name);

		if (p == null)
			throw new NotFoundException("composition",name,getName());

		return p;
	}

	public boolean compositionExists(String name)
	{
		return (compositionMap.get(name) != null);
	}

	public void addComposition(CProcess process) {
		if (compositionExists(process.getName())) {
			throw new RuntimeException("Composition " + process.getName() + " already exists.");
		} else {
			Log.d("Adding process: " + process.getName());
			compositionMap.put(process.getName(), process);
			notifyCompositionAdded(process);
		}
	}

	public void updateComposition(String name, CProcess process) throws NotFoundException {
		if (!processExists(name))
			throw new NotFoundException("composition",name,getName());

		if (!process.equals(getProcess(name))) {
			Log.d("Updating process: " + name + " (new: "+ process.getName() + ")");
			compositionMap.remove(name);
			compositionMap.put(process.getName(), process);
			notifyCompositionUpdated(name, process);
		} else {
			Log.d("No change when updating composition: " + name + " to " + process.getName() + " (valid:" + process.isValid() + "-checked:" + process.isChecked() + ")");
			compositionMap.put(process.getName(), process); // this may update checked/verified state
		}	
	}

	public void removeComposition(CProcess process) throws NotFoundException {
		this.removeComposition(process.getName());	
	}

	public void removeComposition(String name) throws NotFoundException {
		if (processExists(name)) {
			Log.d("Removing composition: " + name);
			CProcess process = compositionMap.get(name);
			notifyCompositionRemoved(process);
			compositionMap.remove(name);
		}
		else throw new NotFoundException("composition",name,getName());
	}

	public int compositionCount()
	{
		return compositionMap.size();
	}

	public Collection<CProcess> getCompositions() {
		return compositionMap.values();
	}

	@Override
	public CProcess getProcess(String name) throws NotFoundException
	{
		if (compositionExists(name)) return getComposition(name);
		return session.getProcess(name);
	}

	@Override
	public boolean processExists(String name)
	{
		return (compositionExists(name) || session.processExists(name));
	}

	@Override
	public void addProcess(CProcess process) {
		if (process.isIntermediate()) { // TODO check name duplicate with workspace?
			addComposition(process);
		}
		else {
			session.addProcess(process);
		}
	}

	@Override
	public void updateProcess(String name, CProcess process) throws NotFoundException {
		if (compositionExists(name)) {
			updateComposition(name, process);
		} else {
			session.updateProcess(name, process);
		}
	}

	@Override
	public void removeProcess(String name) throws NotFoundException {
		if (compositionExists(name)) removeComposition(name);
		else session.removeProcess(name);
	}

	@Override
	public void removeProcess(CProcess process) throws NotFoundException {
		this.removeProcess(process.getName());
	}

	public CompositionSession getSession() {
		return session;
	}

	public Prover getProver()
	{
		return session.getProver();
	}

	public void setProver(Prover prover)
	{
		session.setProver(prover);
	}

	public ProcessGraph getGraph() {
		return graph;
	}

	public String getName() {
		return this.name;
	}

	public int getStepCounter() {
		return this.stepCounter;
	}

	public Set<CProcess> getDirectDependencies(String name) throws NotFoundException {
		return getDirectDependencies(getProcess(name).getActions());
	}

	public Set<CProcess> getDirectDependencies(CProcess process) throws NotFoundException {
		return getDirectDependencies(process.getActions());
	}

	public Set<CProcess> getDirectDependencies(Collection<ComposeAction> actions) throws NotFoundException {
		HashSet<CProcess> components = new HashSet<>();

		for (ComposeAction act : actions)
			for (String dep : act.getDependencies())
				components.add(getProcess(dep));
		return components;
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


	public Set<CProcess> getAllIntermediateComponents(String name) throws NotFoundException {
		return getAllIntermediateComponents(getProcess(name).getActions());
	}

	public Set<CProcess> getAllIntermediateComponents(CProcess process) throws NotFoundException {
		return getAllIntermediateComponents(process.getActions());
	}

	public Set<CProcess> getAllIntermediateComponents(Collection<ComposeAction> actions) throws NotFoundException {
		HashSet<CProcess> components = new HashSet<>();

		for (String dep : ComposeAction.getAllDependencies(actions)) {
			if (compositionExists(dep)) {
				components.add(getProcess(dep));
				components.addAll(getAllIntermediateComponents(dep));
			}
		}
		return components;
	}


	public Set<CProcess> getAllDependencies(String name) throws NotFoundException {
		return this.getAllDependencies(getProcess(name).getActions());
	}

	public Set<CProcess> getAllDependencies(CProcess process) throws NotFoundException {
		return this.getAllDependencies(process.getActions());
	}

	public Set<CProcess> getAllDependencies(Collection<ComposeAction> actions) throws NotFoundException {
		HashSet<CProcess> res = new HashSet<CProcess>();

		for (String dep : ComposeAction.getAllDependencies(actions)) {
			res.add(getProcess(dep));
			res.addAll(getAllDependencies(dep));
		}
		return res;
	}
	
	public Set<CProcess> getRootDependencies(String name) throws NotFoundException {
		CProcess process = getProcess(name);
		return getAllDependencies(process);
	}

	public Set<CProcess> getRootDependencies(CProcess process) throws NotFoundException {
		HashSet<CProcess> res = new HashSet<CProcess>();

		for (String dName : process.getRootDependencies()) {
			CProcess dep = getProcess(dName);
			res.add(dep);
			res.addAll(getAllDependencies(dep));
		}
		return res;
	}

	public Set<CProcess> getChildren(String name) {
		HashSet<CProcess> children = new HashSet<>();
		for (CProcess pr : this.compositionMap.values()) {
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
		
		for (CProcess dep : getAllIntermediateComponents(process)) {
			res.addAll(getProcessWithOrderedDependencies(dep));
		}
		res.add(process);
		return res;
	}
	

	public Vector<ComposeAction> getActionsForProcess (CProcess process) throws NotFoundException {
		return getActionsForProcess(process, process.getName());
	}

	public Vector<ComposeAction> getActionsForProcess (CProcess process, String newName) throws NotFoundException {
		Vector<ComposeAction> actions = new Vector<ComposeAction>();
		for (CProcess proc : getComponents(process)) {
			if (proc.isIntermediate()) actions.addAll(getActionsForProcess(proc,proc.getName()));
		}

		Vector<ComposeAction> resactions;
		if (newName == null || newName.equals(process.getName()))
			resactions = process.getActions();
		else {
			resactions = new Vector<ComposeAction>();
			for (ComposeAction action : process.getActions()) {
				ComposeAction resaction = action;
				if (action.getResult().equals(process.getName())) {
					resaction = new ComposeAction(action.getAction(),action.getLarg(),action.getLsel(),action.getRarg(),action.getRsel(),newName);
				}
				resactions.add(resaction);
			}
		}

		actions.addAll(resactions);
		return actions;
	}

	public CProcess getCompositeProcess(String name) throws NotFoundException {
		return getCompositeProcess(getProcess(name));		
	}

	public CProcess getCompositeProcess(CProcess composition) throws NotFoundException {
		Vector<ProcessPort> inputs = new Vector<ProcessPort>(composition.getInputs().size());
		for (ProcessPort input : composition.getInputs()) {
			inputs.add(new ProcessPort(input));
		}

		ProcessPort output = new ProcessPort(composition.getOutput());

		CProcess process = new CProcess(composition.getName(), inputs, output, false, composition.isCopier(), 
				composition.isChecked(), composition.isValid(), getActionsForProcess(composition));
		process.setProc(composition.getProc());
		process.setCompositeGraph(composition.getFullGraph());
		return process;
	}


	public String getFreshCompositionName() 
	{ 
		return getFreshCompositionName("Step");
	}

	public String getFreshCompositionName(String prefix)
	{
		return getFreshCompositionName(prefix,new Vector<String>());
	}

	public String getFreshCompositionName(String prefix, Collection<String> names) {
		String name;

		do
		{
			name = "_" + prefix + stepCounter;
			stepCounter++;

		} while (processExists(name) || names.contains(name));

		return name;
	}

	public void addChangeListener(ProcessStoreChangeListener listener) {
		this.stateChangeListeners.add(listener);
	}

	public void removeChangeListener(ProcessStoreChangeListener listener) {
		this.stateChangeListeners.remove(listener);
	}


	private void notifyCompositionAdded(CProcess process) {
		for (ProcessStoreChangeListener listener : this.stateChangeListeners) {
			listener.processAdded(process);
		}
	}

	private void notifyCompositionRemoved(CProcess process) {
		for (CProcess pr : getChildren(process.getName())) {
			Log.d("Unchecking composition: " + pr.getName());
			pr.unCheck();
			notifyCompositionUpdated(pr.getName(), pr);
		}
		for (ProcessStoreChangeListener listener : this.stateChangeListeners) {
			listener.processRemoved(process);
		}
	}

	private void notifyCompositionUpdated(String previousName, CProcess process) {
		for (CProcess pr : getChildren(previousName)) {
			Log.d("Unchecking composition: " + pr.getName());
			pr.unCheck();
			notifyCompositionUpdated(pr.getName(), pr);
		}
		for (ProcessStoreChangeListener listener : this.stateChangeListeners) {
			listener.processUpdated(previousName,process);
		}
	}


	public CompositionEdit handleComposeResponse(ComposeResponse response, ExceptionHandler handler) {
		return handleComposeResponse(response,handler,true);
	}

	public CompositionEdit handleComposeResponse(ComposeResponse response, ExceptionHandler handler, boolean visible) {
		switch (response.getAction().getAction().toUpperCase()) {
		case "JOIN":
			return new JoinEdit(this, handler, response.getProcess(), response.getAction(), response.getState(), visible);
		case "WITH":
			return new WithEdit(this, handler, response.getProcess(), response.getAction(), response.getState(), visible);
		case "TENSOR":
			return new TensorEdit(this, handler, response.getProcess(), response.getAction(), response.getState(), visible);
		default:
			Log.e("[Workspace] Unknown composition action: " + response.getAction().getAction());
			return new CompositionEdit(this, handler, response.getProcess(), response.getAction(), response.getState(), visible);
		}
	}

	@Override
	public void processAdded(CProcess process) { }

	@Override
	public void processUpdated(String previousName, CProcess process) {
		for (CProcess pr : getChildren(previousName)) {
			Log.d("Unchecking composition: " + pr.getName());
			pr.unCheck();
			pr.renameComponent(previousName, process.getName());
			notifyCompositionUpdated(pr.getName(), pr);
		}
	}

	@Override
	public void processRemoved(CProcess process) {
		for (CProcess pr : getChildren(process.getName())) {
			Log.d("Unchecking composition: " + pr.getName());
			pr.unCheck();
			notifyCompositionUpdated(pr.getName(), pr);
		}
	}
}