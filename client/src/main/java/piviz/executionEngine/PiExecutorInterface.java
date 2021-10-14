package piviz.executionEngine;

import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;

import piviz.exceptions.PiExecutionException;
import piviz.exceptions.PiParserError;
import piviz.exceptions.RestrictionTableException;


/**
 * Interface to hide the specialized PiExecutor
 * 
 * @author Anja
 * 
 */
public interface PiExecutorInterface {

	/***************************************************************************
	 * Choose next step to be executed and call for its execution. Afterwards
	 * find the next steps possible.
	 **************************************************************************/
	public boolean autoExecute() throws PiExecutionException,
			RestrictionTableException;

	/**
	 * Create a dot for the current execution tree.
	 */
	public void dotCurrentExecTree() throws IOException;

	/**
	 * Create a dot for the definition tree.
	 */
	public void dotDefinitionTree() throws IOException;

	public boolean execute(ASTSend send, ASTReceive receive, ASTTau tau)
			throws PiExecutionException, RestrictionTableException;

	public ArrayList getListOfBlockedCommunications();

	public ArrayList getListOfExecutableCommunications();

	public ArrayList getListOfExecutableTauNodes();

	public ArrayList getListOfRunningProcesses();

	public Hashtable getPoolTable();

	public TreeMap getScopes();
	
	public boolean hasExecutableSteps();

	public boolean startExecution() throws FileNotFoundException,
			PiExecutionException, RestrictionTableException, PiParserError,
			Exception;

	/**
	 * Go through the execution tree and dump the process definitions into the
	 * string, which is going to be returned.
	 */
	public String getCurrentProcessDefinitions();

	// / For adding action listeners -> changed events for GUI
	public void addActionListener(ActionListener listener);

	/**
	 * Returns already existing list of defined agents in the definition tree.
	 * 
	 * @return
	 */
	public ArrayList getCurrentlyDefinedAgents();

	/**
	 * Get all agents that are defined in the definition tree. Use this list for
	 * example to give the user a choice of what already defined agents to add
	 * to the system. This method has to compose the list first, in contrast to
	 * "getCurrentlyDefinedAgents()".
	 * 
	 * @return
	 */
	public ArrayList getAllDefinedAgents();

	/**
	 * Add an already defined agent to the executed system.
	 * 
	 * @param execAgents
	 * @return
	 * @throws PiExecutionException
	 * @throws RestrictionTableException
	 */
	public void addExecAgents(ArrayList execAgents)
			throws PiExecutionException, RestrictionTableException;

	/**
	 * Add new agents to the currently running system.
	 * 
	 * @param definitions
	 *            Holds the definitions of the new agent(s) and pool(s)
	 * @return True, if there are steps for execution.
	 * @throws PiParserError
	 * @throws Exception
	 */
	public void importAgents(Reader definitions) throws PiParserError, Exception;
	
	/**
	 * Return current message.
	 * @return
	 */
	public String getMessage();
	
	public String getPreviousAction();
	
  /** Returns the number of executions performed */
  public int getExecutionsPerformed();
}
