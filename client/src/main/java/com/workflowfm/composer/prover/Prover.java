package com.workflowfm.composer.prover;

import java.util.Vector;

import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllTermPath;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.ui.PortEdge;
import com.workflowfm.composer.prover.command.ProverCommand;
import com.workflowfm.composer.prover.response.ProverResponse;
import com.workflowfm.composer.utils.validate.Validator;

public interface Prover {
	public String getLog();
	public void addToLog(String c);
	public void addStateListener(ProverStateListener p);
	public void removeStateListener(ProverStateListener p);

	public String cllResourceString(CllTerm cll);
	public String portResourceString(ProcessPort port);
	public String portResourceString(String channel, CllTerm term);
	public String cllPath(PortEdge e) throws InvalidCllPathException;
	public String cllPath(CllTerm tm, CllTermPath path) throws InvalidCllPathException;
	
	public Vector<ProverResponse> parseResponses(Vector<String> jsonOutputs) throws UserError;
	
	public void start() throws UserError;
	public void stop();
	public void restart() throws UserError;
	
	public void execute(ProverCommand command);
	
	public Validator<String> getResourceValidator();
	public Validator<String> getChannelValidator();
	public Validator<String> getProcessValidator();
}
