package com.workflowfm.composer.ui.dialogs;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.utils.Log;

public class ExceptionDialog {
	
	private String title;
	private String message;
	private Throwable exception;

	public ExceptionDialog(String title, String message, Throwable exception) {
		this.title = title;
		this.message = message;
		this.exception = exception;
	}
	
	public ExceptionDialog(String title, String message) {
		this(title,message,null);
	}

	public ExceptionDialog(String message) {
		this("Error",message,null);
	}
	
	public ExceptionDialog(UserError error) {
		this("Error",error.getLocalizedMessage(),error.getCause());
	}

	public ExceptionDialog(Throwable exception) {
		this("Error","An error has occured!",exception);
	}
	
	public void show(Component component)
	{
		String text;

		Log.d("Showing ExceptionDialog '" + title + "': " + message + (exception==null?"":" [" + exception.getLocalizedMessage() + "]"));

		if (exception != null) {
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			exception.printStackTrace(printWriter);
			text = message + "\n\n" + "Error: " + "\n" + writer;
			exception.printStackTrace();
		}
		else 
			text = message;

		JTextArea textArea = new JTextArea(20, 60);
		textArea.setText(text);
		textArea.setEditable(false);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		// Set selection so text area viewport will be moved to the top
		textArea.select(0, 0);

		JOptionPane.showMessageDialog(component, new JScrollPane(textArea), title, JOptionPane.ERROR_MESSAGE);
	}
}
