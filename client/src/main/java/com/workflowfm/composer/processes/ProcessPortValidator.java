package com.workflowfm.composer.processes;

import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.utils.validate.ValidationException;
import com.workflowfm.composer.utils.validate.Validator;

public class ProcessPortValidator implements Validator<ProcessPort> {

	private Validator<String> channelValidator;
	private Validator<CllTerm> termValidator;
	
	public ProcessPortValidator(Prover prover) {
		this(prover.getChannelValidator(),new CllTermValidator(prover));
	}
	
	public ProcessPortValidator(Validator<String> channelValidator, Validator<CllTerm> termValidator) {
		this.channelValidator = channelValidator;
		this.termValidator = termValidator;
	}

	@Override
	public void validate(ProcessPort field) throws ValidationException {
		channelValidator.validate(field.getChannel());
		termValidator.validate(field.getCllTerm());
	}

}
