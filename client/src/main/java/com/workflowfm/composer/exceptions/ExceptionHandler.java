package com.workflowfm.composer.exceptions;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.prover.response.ExceptionResponse;
import com.workflowfm.composer.utils.validate.ValidationException;

public interface ExceptionHandler {
	public void handleException(String message, Throwable exception);
	public void handleException(UserError error);
	public void handleException(Throwable exception);
	public void handleException(NotFoundException exception);
	public void handleException(ValidationException exception);
	public void handleException(ExceptionResponse response);
	public void uncheckedProcess(CProcess process);
	public void invalidProcess(CProcess process);
}
