package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.deploy.DeploymentFile;
import com.workflowfm.composer.prover.command.PiVizCommand;
import com.workflowfm.composer.prover.response.DeployResponse;
import com.workflowfm.composer.prover.response.ExceptionResponse;
import com.workflowfm.composer.prover.response.ProverResponse;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.PiVizWindow;
import com.workflowfm.composer.ui.WindowManager;
import com.workflowfm.composer.utils.CompletionListener;
import com.workflowfm.composer.utils.Log;

public class PiVizAction extends CompositionSessionAction implements CompletionListener {

	private static final long serialVersionUID = 1845629557850041075L;

	private CProcess process;
	private WindowManager manager;

	private PiVizCommand command;

	public PiVizAction(CProcess process, WindowManager manager, CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Inspect \u03C0-calculus", session, exceptionHandler, "silk_icons/report_picture.png", KeyEvent.VK_I, KeyEvent.VK_I, KeyEvent.ALT_DOWN_MASK);
		this.process = process;
		this.manager = manager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!process.isChecked() || !process.isValid()) return;
		try {
			command = new PiVizCommand(getSession(), process, getExceptionHandler());
			command.addCompletionListener(this);
			getProver().execute(command);
		} catch (NotFoundException e1) {
			getExceptionHandler().handleException(e1);
			return;
		}
	}

	@Override
	public void completed() {
		if (command == null) {
			throw new RuntimeException("Null command completed");
		}
		if (!command.succeeded()) {
			process.setInvalid();
			Log.e("Command failed: " + command.debugString());
			return;
		}
		for (ProverResponse r : command.getResponses()) {
			if (r.isException()) {
				process.setInvalid();
				getExceptionHandler().handleException((ExceptionResponse)r);
				return;
			}

			if (r instanceof DeployResponse) {
				DeployResponse d = (DeployResponse)r;
				if (!d.getType().equalsIgnoreCase(command.getCommand()))
					getExceptionHandler().handleException(new Exception("Prover response type [" + d.getType() + "] did not match command type [" + command.getCommand() + "]."));
				doDeploy(((DeployResponse)r).getFiles());
			}
		}
	}
	
	public void doDeploy(Collection<DeploymentFile> files) { 
		for (DeploymentFile f : files) // we normally expect only 1 file, but just keeping it general here
		{
			new PiVizWindow(process, f.getContent(), getSession(), getExceptionHandler(), manager).show();
		}
	}

}