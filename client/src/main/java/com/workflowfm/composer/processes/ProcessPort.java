package com.workflowfm.composer.processes;

import java.io.Serializable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.utils.CustomGson;

/** Used to represent a message composed of a channel name and a CLL term. */
public class ProcessPort implements Serializable {
	private static final long serialVersionUID = 7714003987519806407L;
	
	private String channel;
	private CllTerm cll;
	
	public ProcessPort(String channel, CllTerm cll) {
		super();
		this.channel = channel;
		this.cll = cll;
	}

	public ProcessPort(ProcessPort other) {
		this.cll = new CllTerm(other.getCllTerm());
		this.channel = other.getChannel();
	}
	
	public CllTerm getCllTerm()
	{
		return cll;
	}

	public String getChannel()
	{
		return channel;
	}

	public String toString()
	{
		return "" + cll + " <> " + channel;
	}
	
	public boolean update(ChannelMapping chanmap) {
		if (channel.equals(chanmap.getFrom())) {
			channel = chanmap.getTo();
			return true;
		} else {
			return false;
		}
	}

	/** Adds input message edges to a process vertex. */
	public void addAsInputToVertex(ProcessGraph graph, CProcess process,
			int inputIndex, boolean optional, boolean clickableNodes,
			Object processVertex) {
		cll.addProcessPorts(graph, process, process.getName(), inputIndex,
				optional, clickableNodes, processVertex, true);
	}

	/** Adds output message edges to a process vertex. */
	public void addAsOutputToVertex(ProcessGraph graph, CProcess process,
			boolean optional, boolean clickableNodes, Object processVertex) {
		cll.addProcessPorts(graph, process, process.getName(), -1, optional,
				clickableNodes, processVertex, true);
	}

	/**
	 * Adds edges to a graph that represents two services being joined by a
	 * message. This works by first creating the output edges on the first
	 * service then creating the input edges on the second service where the
	 * tips of these input edges are joined to the tips of the output edges.
	 */
	public void joinVertices(ProcessGraph graph, CProcess process, boolean optional, boolean buffer, Object service1,
			Object service2) {
		cll.joinVertices(graph, process, process.getName(), optional, buffer, service1, true, service2, true);
	}

	
	@Override 
	public boolean equals(Object o) {
		if (!(o instanceof ProcessPort)) return false;
		ProcessPort other = (ProcessPort) o;
		return (other.getChannel().equalsIgnoreCase(channel)
				&& other.getCllTerm().equals(cll));
	}
	
	
	static public ProcessPort fromJson(String json) throws UserError, JsonSyntaxException {
		JsonObject object = new JsonParser().parse(json).getAsJsonObject();

		if (object.has("error")) {
			String error = object.get("error").getAsString();
			throw new UserError(error);
		}

		String classPath = "com.workflowfm.composer.processes.ProcessPort";
		ProcessPort port = null;
		try {
			port = (ProcessPort) CustomGson.getGson().fromJson(object,
					(Class<?>) Class.forName(classPath));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return port;
	}
	
	public void unneg() {
		this.cll = this.cll.unneg();
	}
	
}