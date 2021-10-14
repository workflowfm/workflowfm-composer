package com.workflowfm.composer.processes;

import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.utils.validate.ValidationException;
import com.workflowfm.composer.utils.validate.Validator;

public class CProcessValidator implements Validator<CProcess> {

	private Validator<String> processValidator;
	private Validator<ProcessPort> portValidator;
	
	public CProcessValidator(Prover prover) {
		this(prover.getProcessValidator(),new ProcessPortValidator(prover));
	}
	
	public CProcessValidator(Validator<String> processNameValidator, Validator<ProcessPort> portValidator) {
		this.processValidator = processNameValidator;
		this.portValidator = portValidator;
	}

	@Override
	public void validate(CProcess field) throws ValidationException {
		processValidator.validate(field.getName());
		for (ProcessPort port : field.getInputs()) {
			portValidator.validate(port);
		}
		portValidator.validate(field.getOutput());
	}

}
