package com.workflowfm.composer.utils.validate;

import java.util.Collection;
import java.util.Vector;

public class ValidatorGroup<E> implements Validator<E> {

	private Vector<Validator<E>> validators;

	public ValidatorGroup() {
		validators = new Vector<Validator<E>>();
	}
	
	public ValidatorGroup(int size) {
		validators = new Vector<Validator<E>>(size);
	}
	
	public ValidatorGroup(Collection<Validator<E>> validators) {
		this(validators.size());
		this.validators.addAll(validators);
	}
	
	public ValidatorGroup(Validator<E> validator) {
		this();
		this.validators.add(validator);
	}

	@Override
	public void validate(E field) throws ValidationException {
		for (Validator<E> v : validators) {
			v.validate(field);
		}
	}

	public Vector<Validator<E>> getValidators() {
		return validators;
	}

	public void setValidators(Vector<Validator<E>> validators) {
		this.validators = validators;
	}
	
	public void setValidators(Collection<Validator<E>> validators) {
		this.validators = new Vector<Validator<E>>(validators.size());
		this.validators.addAll(validators);
	}
	
	public void add(Validator<E> validator) {
		this.validators.add(validator);
	}
}
