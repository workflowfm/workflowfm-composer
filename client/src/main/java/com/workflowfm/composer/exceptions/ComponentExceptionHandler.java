package com.workflowfm.composer.exceptions;

import java.awt.Component;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.prover.response.ExceptionResponse;
import com.workflowfm.composer.ui.dialogs.ExceptionDialog;
import com.workflowfm.composer.ui.dialogs.ProverExceptionResponseDialog;
import com.workflowfm.composer.utils.validate.ValidationException;

public class ComponentExceptionHandler implements ExceptionHandler {

	private Component component;

	public ComponentExceptionHandler(Component component) {
		this.component = component;
	}

	public Component getComponent() {
		return this.component;
	}

	//	@Override
	//	public void handleException(String title, String message) {
	//		ExceptionDialog dialog = new ExceptionDialog(title, message);
	//		dialog.show(component);
	//	}

	@Override
	public void handleException(String message, Throwable exception) {
		ExceptionDialog dialog = new ExceptionDialog("Error", message, exception);
		dialog.show(component);
	}	

	@Override
	public void handleException(ExceptionResponse response) {
		new ProverExceptionResponseDialog(response).show(component);
	}

	@Override
	public void handleException(UserError error) {
		ExceptionDialog dialog = new ExceptionDialog(error);
		dialog.show(component);
	}
	
	@Override
	public void handleException(Throwable exception) {
		ExceptionDialog dialog = new ExceptionDialog(exception);
		dialog.show(component);
	}

	@Override
	public void uncheckedProcess(CProcess process) {
		ExceptionDialog dialog = new ExceptionDialog("Error", "Unable to use unchecked process '" + process.getName() + "'.\nPlease verify the process first.");
		dialog.show(component);		
	}

	@Override
	public void invalidProcess(CProcess process) {
		ExceptionDialog dialog = new ExceptionDialog("Error", "Unable to use invalid process '" + process.getName() + "'.\nPlease verify the process first.");
		dialog.show(component);
	}

	@Override
	public void handleException(NotFoundException exception) {
		ExceptionDialog dialog = new ExceptionDialog("Error finding " + exception.getType(), exception.getMessage());
		dialog.show(component);
	}

	@Override
	public void handleException(ValidationException exception) {
		ExceptionDialog dialog = new ExceptionDialog("Invalid " + exception.getFieldType() + " '" + exception.getField() + "': " + exception.getValidationMessage());
		dialog.show(component);
	}
}
