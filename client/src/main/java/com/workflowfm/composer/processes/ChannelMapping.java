package com.workflowfm.composer.processes;

public class ChannelMapping {
	private String from;
	private String to;
	
	public ChannelMapping() { }

	public ChannelMapping(String from, String to) {
		this.from = from;
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}
	
	@Override
	public String toString() {
		return from + " -> " + to;
	}

}
