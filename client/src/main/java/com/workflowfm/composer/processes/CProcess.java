package com.workflowfm.composer.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.utils.CustomGson;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.utils.UIUtils;
import com.workflowfm.composer.utils.Utils;

/**
 * Stores data that represents a service. This includes the service name and its
 * inputs and outputs.
 */
public class CProcess implements Serializable {
	
	public static final String nameOfMerge(String name) { return "&" + name; }
	
	private static final long serialVersionUID = 2306963717780096159L;
	
	private static final ImageIcon atomicIcon = UIUtils.getIcon("silk_icons/brick.png");
	private static final ImageIcon atomicWarnIcon = UIUtils.getIcon("silk_icons/brick_error.png");
	private static final ImageIcon atomicErrorIcon = UIUtils.getIcon("silk_icons/brick_delete.png");
	private static final ImageIcon compositeIcon = UIUtils.getIcon("silk_icons/bricks.png");
	private static final ImageIcon compositeWarnIcon = UIUtils.getIcon("silk_icons/bricks_error.png");
	private static final ImageIcon compositeErrorIcon = UIUtils.getIcon("silk_icons/bricks_delete.png");
	private static final ImageIcon compositionIcon = UIUtils.getIcon("silk_icons/cog.png");
	private static final ImageIcon compositionWarnIcon = UIUtils.getIcon("silk_icons/cog_error.png");
	private static final ImageIcon compositionErrorIcon = UIUtils.getIcon("silk_icons/cog_delete.png");
	
	private String name;
	private Vector<ProcessPort> inputs;
	private ProcessPort output;
	private String proc;
	private	Vector<ComposeAction> actions;
	private ComposeProvenance prov;

	private boolean copier;
	private boolean intermediate;
	
	private boolean checked;
	private boolean valid;
	

	/** For composite processes, this variable is used to store the (expanded) graph of the composition. */
	private transient ProcessGraph atomicGraph;
	private transient ProcessGraph compositeGraph;

	// Used by JSON
	@SuppressWarnings("unused")
	private CProcess () {
		this.inputs = new Vector<ProcessPort>();
		this.actions = new Vector<ComposeAction>();
		this.checked = true;
		this.valid = true;
	}
	
	public CProcess(String name, Vector<ProcessPort> inputs,
			ProcessPort output, boolean intermediate, boolean copier,
			boolean checked, boolean valid, Vector<ComposeAction> actions) {
		this.name = name;
		this.inputs = inputs;
		this.output = output;
		this.proc = "";
		this.copier = copier;
		this.intermediate = intermediate;
		this.checked = checked;
		this.valid = valid;
		this.actions = actions;
		this.prov = output == null?null:ComposeProvenance.forTerm(name, output.getCllTerm());
	}
	
	public CProcess(String name, Vector<ProcessPort> inputs,
			ProcessPort output, boolean intermediate, boolean copier,
			boolean checked, boolean valid) {
		this(name,inputs,output,intermediate,copier,checked,valid,new Vector<ComposeAction>());
	}
	
	public CProcess(String name, Vector<ProcessPort> inputs,
			ProcessPort output, boolean intermediate, boolean copier) {
		this(name,inputs,output,intermediate,copier,false,false);
	}

	public CProcess(String name, Vector<ProcessPort> inputs,
			ProcessPort output) {
		this(name,inputs,output,false,false);
	}
	
	public CProcess(CProcess other) {
		this.name = other.getName();
		this.inputs = new Vector<ProcessPort>(other.getInputs().size());
		for (ProcessPort input : other.getInputs()) {
			this.inputs.add(new ProcessPort(input));
		}
		this.output = new ProcessPort(other.getOutput());
		this.proc = other.getProc();
		this.copier = other.isCopier();
		this.intermediate = other.isIntermediate();
		this.checked = other.isChecked();
		this.valid = other.isValid();
		this.actions = new Vector<ComposeAction>(other.getActions().size());
		for (ComposeAction action : other.getActions()) {
			this.actions.add(new ComposeAction(action));
		}
		this.prov = new ComposeProvenance(other.getProvenance());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!this.name.equals(name)) {
			unCheck();
			this.name = name;
		}
	}

	public Vector<ProcessPort> getInputs() {
		return inputs;
	}

	public Collection<CllTerm> getInputCll() {
		Vector<CllTerm> c = new Vector<CllTerm>(inputs.size());

		for (ProcessPort tm : inputs)
			c.add(tm.getCllTerm());

		return c;
	}

	public Collection<String> getInputResources(Prover prover) {
		Vector<String> c = new Vector<String>(inputs.size());

		for (ProcessPort tm : inputs)
			c.add(prover.cllResourceString(tm.getCllTerm()));

		return c;
	}
	
	public ProcessPort getInput(String channel) throws NotFoundException {
		for (ProcessPort input : this.inputs) 
		{
			if (input.getChannel().equals(channel)) return input;
		}
		throw new NotFoundException("input channel", channel, this.name);
	}

	public int getInputIndex(String channel) throws NotFoundException {
		for (ListIterator<ProcessPort> it = this.inputs.listIterator(); it.hasNext();) 
		{
			int index = it.nextIndex();
			ProcessPort input = it.next();
			if (input.getChannel().equals(channel)) return index;
		}
		throw new NotFoundException("input channel", channel, this.name);
	}
	
	public void setInputs(Vector<ProcessPort> inputs) {
		if (!this.inputs.equals(inputs)) {
			this.inputs = inputs;
			unCheck();
		}
	}

	public void addInput(ProcessPort input) {
		this.inputs.add(input);
		unCheck();
	}
	
	public void setInput(String channel, ProcessPort input) throws NotFoundException {
		for(int i=0; i < inputs.size(); i++)
		{
			if (inputs.elementAt(i).getChannel().equals(channel)) {
				inputs.set(i, input);
				unCheck();
				return;
			}
		}
		throw new NotFoundException("input channel", channel, this.name);
	}
	
	public void removeInput(String channel) throws NotFoundException {
		for(int i=0; i < inputs.size(); i++)
		{
			if (inputs.elementAt(i).getChannel().equals(channel)) {
				inputs.remove(i);
				unCheck();
				return;
			}
		}
		throw new NotFoundException("input channel", channel, this.name);
	}
	
//	public Collection<String> getInputNamesAndChannels() {
//		Vector<String> h = new Vector<String>(inputs.size());
//
//		for (int i = 0; i < inputs.length; ++i)
//			h[i] = "" + inputs[i];
//
//		return h;
//	}

	public ProcessPort getOutput() {
		return output;
	}

	public CllTerm getOutputCll() {
		return output.getCllTerm();
	}
	
	public void setOutput(ProcessPort output) {
		if (!this.output.equals(output)) {
			this.output = output;
			unCheck();
		}
	}
	
	public boolean renameComponent(String previousName, String newName) {
		boolean changed = false;
		for (ComposeAction act : actions) {
			if (act.renameProcess(previousName, newName))
				changed = true;
		}
		if (changed) unCheck();
		return changed;
	}
	
//	public String getOutputResource(Prover prover) {
//		return prover.resourceOfJson(output.getCllTerm().getJson());
//	}
	
	public String getLabel() {
		if (isCopier()) {
			return "\u2297";
		} else {
			return name;
		}
	}
	
	public String getProc() {
		return this.proc;
	}
	
	public void setProc(String proc) {
		if (!this.proc.equals(proc)) {
			this.proc = proc;
			//unCheck(); // TODO Workspace.getCompositeProcess needs this unchanged, but it shouldn't!
		}
	}
	
	public Vector<ComposeAction> getActions() {
		return actions;
	}
	
	public Set<String> getRootDependencies() {
		return ComposeAction.getRootDependencies(actions);
	}
	
	public Set<String> getAllDependencies() {
		return ComposeAction.getAllDependencies(actions);
	}
	
	public String toString() {
		String s = "Name = " + name + " Input = ";

		String delim = "";
		for (ProcessPort i : getInputs()) {
			s += delim + i.getCllTerm();
			delim = ", ";
		}

		if (getInputs().size() == 0)
			s += "? ";

		s += ", Output = " + (output == null ? "?" : output.getCllTerm());

		return s;
	}

	public boolean isAtomic() {
		return actions.size() == 0;
	}

	public boolean isComposite() {
		return !isAtomic();
	}

	public boolean isCopier() {
		return copier;
	}

	public boolean isIntermediate() {
		return intermediate;
	}
	
	public boolean isChecked() {
		return this.checked;
	}
	
	public void unCheck() {
		this.checked = false;
		this.valid = false;
	}
	
	public boolean isValid() {
		return this.valid;
	}
	
	public void setInvalid() {
		this.checked = true;
		this.valid = false;
	}
	
	public boolean hasChannel(String channelName) {
		if (output.getChannel().equals(channelName))
			return true;

		for (ProcessPort m : inputs) {
			if (m.getChannel().equals(channelName))
				return true;
		}
		return false;
	}
	
	public boolean hasInputChannel(String channelName) {
		for (ProcessPort m : inputs) {
			if (m.getChannel().equals(channelName))
				return true;
		}
		return false;
	}

	public String[] getChannels() {
		ArrayList<String> c = new ArrayList<String>();

		for (ProcessPort m : inputs) {
			c.add(m.getChannel());
		}
		c.add(output.getChannel());

		return Utils.objectArrayToStringArray(c.toArray());
	}

	public ImageIcon getIcon() {
		if (isAtomic()) {
			if (checked) {
				if (valid) return atomicIcon;
				else return atomicErrorIcon;
			} else return atomicWarnIcon;
		} else {
			if (intermediate) {
				if (checked) {
					if (valid) return compositionIcon;
					else return compositionErrorIcon;
				} else return compositionWarnIcon;
			} else {
				if (checked) {
					if (valid) return compositeIcon;
					else return compositeErrorIcon;
				} else return compositeWarnIcon;
			}
		}
	}

	public ProcessGraph getAtomicGraph()
	{
		if (atomicGraph == null) {
			ProcessGraph graph = new ProcessGraph();
			Log.d("Creating atomic graph.");
			graph.createProcessGraph(this);
			Log.d("Atomic graph done.");
			this.atomicGraph = graph;
			return graph;
		}
		else return atomicGraph;
	}

//	public Object[] copyGraphCells()
//	{
//		return graph.getGraphEngine().cloneAllCells();
//	}
	
	public ProcessGraph getFullGraph() {
		if (this.compositeGraph == null) return getAtomicGraph();
		else return this.compositeGraph;
	}
	
	public ProcessGraph getCompositionGraph() {
		if (isIntermediate()) return getFullGraph();
		else return getAtomicGraph();
	}

	public void setCompositeGraph(ProcessGraph graph) {
		this.compositeGraph = graph;
	}
	
	// TODO 
	public String debugString() { return name; } 
	
	static public CProcess fromJson(String json) throws UserError, JsonSyntaxException {
		JsonObject object = new JsonParser().parse(json).getAsJsonObject();

		if (object.has("error")) {
			String error = object.get("error").getAsString();
			throw new UserError(error);
		}

		String classPath = "com.workflowfm.composer.processes.CProcess";
		CProcess process = null;
		try {
			process = (CProcess) CustomGson.getGson().fromJson(object,
					(Class<?>) Class.forName(classPath));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return process;
	}	

	public boolean isChildOf (String name) {
		return ComposeAction.getRootDependencies(actions).contains(name);
	}
	
	public boolean isChildOf (CProcess process) {
		return ComposeAction.getRootDependencies(actions).contains(process.getName());
	}
	
	public boolean isParentOf (CProcess process) {
		return ComposeAction.getRootDependencies(process.getActions()).contains(name);
	}
	
	public boolean handleInvalid (ExceptionHandler handler) {
		if (!isChecked()) {
			handler.uncheckedProcess(this);
			return true;
		}
		if (!isValid()) {
			handler.invalidProcess(this);
			return true;
		}
		return false;
	}
	
	public ComposeProvenance getProvenance() {
		if (this.prov == null) {
			if (isIntermediate()) Log.w("No provenance information stored for process: " + name);
			this.prov = ComposeProvenance.forTerm(name, getOutputCll());
		}
		return prov;
	}

	public void setProvenance(ComposeProvenance provenance) {
		this.prov = provenance;
	}

	public boolean update(ChannelMapping chanmap) {
//		if (!isAtomic()) {
//			Log.w("Tried to update channels of non-atomic process [" + name + "]: " + chanmap);
//			return false;
//		}
		boolean result = false;
		if (output != null) result = output.update(chanmap);
		if (inputs != null)
			for (ProcessPort input : inputs) {
				boolean r = input.update(chanmap);
				result = result || r;
			}
		if (result) atomicGraph = null;
		return result;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof CProcess)) return false;
		CProcess other = (CProcess) o;
		
		return other.getName().equals(name) &&
				other.getInputs().equals(inputs) &&
				other.getOutput().equals(output) &&
				other.getProc().equals(proc) &&
				other.getActions().equals(actions) &&
				other.isCopier() == copier &&
				other.isIntermediate() == intermediate;
	}
}