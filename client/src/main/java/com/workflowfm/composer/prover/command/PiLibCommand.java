package com.workflowfm.composer.prover.command;

import java.io.File;
import java.util.Collection;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.prover.response.DeployResponse;
import com.workflowfm.composer.session.CompositionSession;

public class PiLibCommand extends ProverCommand {
	private CProcess process;
	private Collection<CProcess> components;
	private String separator = File.separator;
	private String path;
	private String pkg;
	private String project;
	private boolean main;
	private boolean java;

	public PiLibCommand(CompositionSession session, String name, String path,
			String pkg, String project, boolean main, 
			ExceptionHandler handler) throws NotFoundException {
		this(session.getProcess(name), session.getAllDependencies(name), path,
				pkg, project, main, false, handler);
	}
	
	public PiLibCommand(CompositionSession session, String name, String path,
			String pkg, String project, boolean main, boolean stateful, 
			ExceptionHandler handler) throws NotFoundException {
		this(session.getProcess(name), session.getAllDependencies(name), path,
				pkg, project, main, stateful, handler);
	}

	public PiLibCommand(CompositionSession session, CProcess process,
			String path, String pkg, String project, boolean main,
			boolean stateful, ExceptionHandler handler) throws NotFoundException {
		this(process, session.getAllDependencies(process), path, pkg, project,
				main, stateful, handler);
	}

	public PiLibCommand(CProcess process, Collection<CProcess> components,
			String path, String pkg, String project, boolean main,
			boolean stateful, ExceptionHandler handler) {
		super(stateful?DeployResponse.PEW_TYPE:DeployResponse.PILIB_TYPE, handler);
		this.process = process;
		this.components = components;
		this.path = path;
		this.pkg = pkg;
		this.project = project;
		this.main = main;
		this.java = false;
		
		if (path.length() < separator.length()) this.path += separator;
		else if (!path.substring(path.length()-separator.length()).equals(separator)) this.path += separator;
	}
	
	@Override
	public String debugString() { 
		StringBuffer deps = new StringBuffer();
		for (CProcess dep : components) {
			deps.append(dep.getName());
			deps.append(";");
		}
		return "PiLibCommand[" + process.getName() + " (" + deps + ")]"; 
	}
}