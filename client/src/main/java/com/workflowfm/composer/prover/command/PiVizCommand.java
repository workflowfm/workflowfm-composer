package com.workflowfm.composer.prover.command;

import java.util.Collection;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;

import com.workflowfm.composer.session.CompositionSession;

public class PiVizCommand extends ProverCommand {
	private CProcess process;
	private Collection<CProcess> components;

	public PiVizCommand(CompositionSession session, String name, ExceptionHandler handler) throws NotFoundException {
		this(session.getProcess(name),session.getAllDependencies(name),handler);
	}
	
	public PiVizCommand(CompositionSession session, CProcess process, ExceptionHandler handler) throws NotFoundException {
		this(process,session.getAllDependencies(process),handler);
	}

	public PiVizCommand(CProcess process, Collection<CProcess> components, ExceptionHandler handler) {		
		super("piviz", handler);
		this.process = process;
		this.components = components;
	}
	
	@Override
	public String debugString() { 
		StringBuffer deps = new StringBuffer();
		for (CProcess dep : components) {
			deps.append(dep.getName());
			deps.append(";");
		}
		return "PiVizCommand[" + process.getName() + " (" + deps + ")]"; 
	}
}
