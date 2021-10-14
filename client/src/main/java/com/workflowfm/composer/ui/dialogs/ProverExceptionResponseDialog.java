package com.workflowfm.composer.ui.dialogs;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.workflowfm.composer.prover.response.ExceptionResponse;

public class ProverExceptionResponseDialog  {
	private ExceptionResponse response;

	public ProverExceptionResponseDialog(ExceptionResponse response) {
		this.response = response;
	}
	
	public void show(Component component)
	{
		String text = response.errorMessage() + "\n\n" + "Error: " + "\n" + response.getContent();

		JTextArea textArea = new JTextArea(20, 60);
		textArea.setText(text);
		textArea.setEditable(false);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		// Set selection so text area viewport will be moved to the top
		textArea.select(0, 0);

		JOptionPane.showMessageDialog(component, new JScrollPane(textArea), "Prover Error", JOptionPane.ERROR_MESSAGE);
	}

	
}
