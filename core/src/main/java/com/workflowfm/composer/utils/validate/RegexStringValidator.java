package com.workflowfm.composer.utils.validate;

import java.util.regex.Pattern;

public class RegexStringValidator implements Validator<String>  {

	private String fieldType = "String";
	private Pattern regex;
	private String message;
	
	public RegexStringValidator(String fieldType, String regex, String message) {
		this.fieldType = fieldType;
		this.regex = Pattern.compile(regex);
		this.message = message;
	}

	@Override
	public void validate(String field) throws ValidationException {
		if (!regex.matcher(field.trim()).matches()) throw new ValidationException(fieldType, field, message);
	}

}
