package com.workflowfm.composer.utils.validate;

public interface Validator<E> {
	public void validate(E field) throws ValidationException;
}
