package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.workflowfm.composer.edit.CreateWorkspaceEdit;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.prover.command.ComposeCommand;
import com.workflowfm.composer.prover.command.ProverCommand;
import com.workflowfm.composer.prover.response.ComposeResponse;
import com.workflowfm.composer.prover.response.ExceptionResponse;
import com.workflowfm.composer.prover.response.ProverResponse;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.utils.CompletionListener;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.Workspace;

public class LoadCompositionsAction extends CompositionSessionAction implements CompletionListener {

	private static final long serialVersionUID = 7025613898024072065L;
	private ProverCommand command;
	private Workspace workspace;

	private CProcess process;

	public LoadCompositionsAction(CompositionSession session, ExceptionHandler exceptionHandler, CProcess process) {
		super("Load Compositions of " + process.getName(), session, exceptionHandler, "silk_icons/table_go.png", KeyEvent.VK_L);
		this.process = process;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		CreateWorkspaceEdit cwedit = new CreateWorkspaceEdit(getSession(),getExceptionHandler());
		cwedit.apply();
		workspace = cwedit.getWorkspace();

		HashMap<String, String> freshMap = new HashMap<String, String>();
		Vector<ComposeAction> actions = new Vector<ComposeAction>();
		Set<String> names = new HashSet<String>();

		for (ComposeAction action : process.getActions()) {
			System.out.println(action.debugString());
			String larg;
			String rarg;
			String result;

			if (freshMap.containsKey(action.getLarg()))
				larg = freshMap.get(action.getLarg());
			else
				larg = action.getLarg();
			names.add(larg);

			if (freshMap.containsKey(action.getRarg()))
				rarg = freshMap.get(action.getRarg());
			else
				rarg = action.getRarg();
			names.add(rarg);

			if (workspace.processExists(action.getResult())) {
				result = workspace.getFreshCompositionName("Step", names);
				freshMap.put(action.getResult(), result);
			} else
				result = action.getResult();
			names.add(result);

			ComposeAction newact = new ComposeAction(action.getAction(), larg, action.getLsel(), rarg, action.getRsel(), result);
			System.out.println(newact.debugString());
			actions.add(newact);
		}

		String result;
		if (freshMap.containsKey(process.getName()))
			result = freshMap.get(process.getName());
		else
			result = process.getName();
		
		try {
			command = new ComposeCommand(workspace, result, actions, getExceptionHandler());
			command.addCompletionListener(this);
			getProver().execute(command); 
		} catch (NotFoundException ex) {
			getExceptionHandler().handleException(ex);
		}
	}

	@Override
	public void completed() {
		if (command == null) {
			throw new RuntimeException("Null command completed");
		}
		if (!command.succeeded()) {
			Log.e("Command failed: " + command.debugString());
			return;
		}			
		for (ProverResponse r : command.getResponses()) {
			if (r.isException()) {
				getExceptionHandler().handleException((ExceptionResponse)r);
				break;
			}
			if (r instanceof ComposeResponse) {
				workspace.handleComposeResponse((ComposeResponse)r,getExceptionHandler(),false).apply();
			}
		}
	}
}
