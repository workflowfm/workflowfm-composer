package com.workflowfm.composer.processes;

import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.utils.validate.ValidationException;
import com.workflowfm.composer.utils.validate.Validator;

public class CllValidator {

	private Validator<String> resourceValidator;
	private Validator<String> channelValidator;
	private Validator<String> processNameValidator;
	private Validator<CllTerm> termValidator;
	private Validator<ProcessPort> portValidator;
	private Validator<CProcess> processValidator;

	public static final int RESOURCE = 0;
	public static final int CHANNEL= 1;
	public static final int PROCESS = 2;


	public CllValidator(Prover prover) {
		this.resourceValidator = prover.getResourceValidator();
		this.channelValidator = prover.getChannelValidator();
		this.processNameValidator = prover.getProcessValidator();

		this.termValidator = new CllTermValidator(resourceValidator);
		this.portValidator = new ProcessPortValidator(channelValidator,termValidator);
		this.processValidator = new CProcessValidator(processNameValidator, portValidator);
	}

	public void validate(CllTerm field) throws ValidationException {
		termValidator.validate(field);	
	}

	public void validate(ProcessPort field) throws ValidationException {
		portValidator.validate(field);
	}

	public void validate(CProcess field) throws ValidationException {
		processValidator.validate(field);
	}

	public void validate(String field, int type) throws ValidationException {
		switch(type) {
		case RESOURCE:
			resourceValidator.validate(field);
			break;
		case CHANNEL:
			channelValidator.validate(field);
			break;
		case PROCESS:
			processNameValidator.validate(field);
			break;
		default :
			throw new RuntimeException("Invalid string validation type: " + type);
		}
	}
	

	public Validator<String> getResourceValidator() {
		return resourceValidator;
	}

	public Validator<String> getChannelValidator() {
		return channelValidator;
	}

	public Validator<String> getProcessNameValidator() {
		return processNameValidator;
	}

	public Validator<CllTerm> getTermValidator() {
		return termValidator;
	}

	public Validator<ProcessPort> getPortValidator() {
		return portValidator;
	}

	public Validator<CProcess> getProcessValidator() {
		return processValidator;
	}
}
