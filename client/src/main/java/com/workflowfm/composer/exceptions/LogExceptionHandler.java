package com.workflowfm.composer.exceptions;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.prover.response.ExceptionResponse;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.utils.validate.ValidationException;

public class LogExceptionHandler implements ExceptionHandler {

	public LogExceptionHandler() { } 
	
	@Override
	public void handleException(String message,	Throwable exception) {
		Log.e(message);
		exception.printStackTrace();
	}

	@Override
	public void handleException(UserError error) {
		Log.e(error.getLocalizedMessage());
		error.printStackTrace();
	}
	
	@Override
	public void handleException(Throwable exception) {
		Log.e(exception.getLocalizedMessage());
		exception.printStackTrace();
	}

	@Override
	public void handleException(ExceptionResponse response) {
		Log.e(response.errorMessage() + ": " + response.getContent());
	}

	@Override
	public void handleException(NotFoundException exception) {
		Log.e(exception.getMessage());
	}
	
	@Override
	public void uncheckedProcess(CProcess process) {
		Log.e("Tried to use unchecked process:" + process.getName());	
	}

	@Override
	public void invalidProcess(CProcess process) {
		Log.e("Tried to use invalid process:" + process.getName());	
	}

	@Override
	public void handleException(ValidationException exception) {
		Log.e(exception.getLocalizedMessage());		
	}
}
