package com.workflowfm.composer.utils.validate;

public class NonemptyStringValidator implements Validator<String>  {

	private String fieldType = "String";
	
	public NonemptyStringValidator(String fieldType) {	
		this.fieldType = fieldType;
	}

	@Override
	public void validate(String field) throws ValidationException {
		if (field.trim().length() == 0) throw new ValidationException(fieldType, field, "Cannot be empty");
	}
}
