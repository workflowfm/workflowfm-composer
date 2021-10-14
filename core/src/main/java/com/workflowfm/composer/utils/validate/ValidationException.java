package com.workflowfm.composer.utils.validate;

public class ValidationException extends Exception
{
	private static final long serialVersionUID = -1304217569052694589L;

	private String fieldType;
	private String field;
	private String message;
	
	public ValidationException(String fieldType, String field, String message) {
		super("Error in " + fieldType + " '" + field + "': " + message);
		this.fieldType = fieldType;
		this.field = field;
		this.message = message;
	}

	public String getFieldType() {
		return fieldType;
	}

	public String getField() {
		return field;
	}

	public String getValidationMessage() {
		return message;
	}
}