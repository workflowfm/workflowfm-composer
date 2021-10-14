package com.workflowfm.composer.processes;

import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.utils.validate.ValidationException;
import com.workflowfm.composer.utils.validate.Validator;

public class CllTermValidator implements Validator<CllTerm> {

	private Validator<String> resourceValidator;
	
	public CllTermValidator(Prover prover) {
		this(prover.getResourceValidator());
	}
	
	public CllTermValidator(Validator<String> resourceValidator) {
		this.resourceValidator = resourceValidator;
	}

	@Override
	public void validate(CllTerm field) throws ValidationException {
		if (field.isAtomic()) {
			resourceValidator.validate(field.getName());
		} else {
			// TODO validate operator?
			
			for (CllTerm tm : field.getArgs())
				validate(tm);
		}
		
	}

}
