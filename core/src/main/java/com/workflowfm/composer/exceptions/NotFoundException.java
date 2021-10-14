package com.workflowfm.composer.exceptions;

public class NotFoundException extends Exception
{
	private static final long serialVersionUID = -1880859383615233283L;
	private String type;
	private String name;
	private String container;
	
	private String message;
	
	public NotFoundException(String type, String name, String container) {
		super("Failed to find " + type + " '" + name + "' in " + container + ".");
		this.message = "Failed to find " + type + " '" + name + "' in " + container + ".";
		this.type = type;
		this.name = name;
		this.container = container;
	}
	
	public NotFoundException(String type, String name) {
		super("Failed to find " + type + " '" + name + "'.");
		this.message = "Failed to find " + type + " '" + name + "'.";
		this.type = type;
		this.name = name;
		this.container = "";
	}
	
	public NotFoundException(String name) {
		super("Failed to find " + name + ".");
		this.message = "Failed to find " + name + ".";
		this.type = "";
		this.name = name;
		this.container = "";
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getContainer() {
		return container;
	}
	
	public String getMessage() {
		return message;
	}
}