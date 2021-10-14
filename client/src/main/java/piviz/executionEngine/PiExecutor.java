/**
 * Created by Anja Bog. Do not edit this line.
 */
/**
 The MIT License

 Copyright (c) 2006 Anja Bog

 Permission is hereby granted, free of charge, to any person
 obtaining a copy of this software and associated documentation files
 (the "Software"), to deal in the Software without restriction, 
 including without limitation the rights to use, copy, modify, merge, 
 publish, distribute, sublicense, and/or sell copies of the Software, 
 and to permit persons to whom the Software is furnished to do so, 
 subject to the following conditions:

 The above copyright notice and this permission notice shall be included 
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
 IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package piviz.executionEngine;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;

import piviz.exceptions.PiExecutionException;
import piviz.exceptions.PiParserError;
import piviz.exceptions.RestrictionTableException;
import piviz.helperClasses.EventValues;
import piviz.helperClasses.NameGenerator;
import piviz.helperClasses.PidGenerator;
import piviz.helperClasses.RestrictionTable;
import piviz.helperClasses.RestrictionTableEntry;


/**
 * Executor of the pi-process definitions. Execution consists of two steps, the
 * first is to find the steps that are executable next to give the user some
 * feedback or/and a choice. The second step is to execute the selected step.
 * Thereby the executor will only trigger the execution of a node and provide
 * the necessary information for this node to execute itself. For example if a
 * receive node will execute, it needs the information it receives.
 * 
 * @author Anja
 * 
 */
public class PiExecutor implements PiExecutorInterface {

	// / For sending changed events to those who are interested.
	protected transient EventListenerList listenerList = new EventListenerList();

	/**
	 * Contains the pool each agent belongs to (if given). poolTable:
	 * <key:agentName><value:poolName>
	 */
	private Hashtable poolTable;

	/**
	 * In case agents are added to the system, this avoids name collisions. This
	 * list was built by the parser initially and is given to it again in the
	 * parseAdditionalAgents() method.
	 */
	private HashSet agentNames;

	/**
	 * In case pools are added to the system, this avoids name collisions. This
	 * list was built by the parser initially and is given to it again in the
	 * parseAdditionalAgents() method.
	 */
	private HashSet poolNames;

	/** Root node of the tree with the agent definitions. */
	private SimpleNode root;

	// / Root node of the tree which will be executed.
	private SimpleNode exec;

	// / File which holds the pi-process definitions.
	private File piDefFile;

	// / Table which holds the restricted names of processes and groups of
	// processes.
	private RestrictionTable resTable;
	
	private int executionsPerformed = 0;

	/**
	 * ArrayList which holds ArrayLists containing all currently executable tau
	 * nodes sorted by channel.
	 */
	private ArrayList currentlyExecTauNodes;

	/**
	 * ArrayList which holds ArrayLists of all nodes currently blocked, sorted
	 * by channel.
	 */
	private ArrayList currentlyBlockedNodes;

	/**
	 * ArrayList which holds ArrayLists of nodes. One nodesArrayList contains
	 * all nodes that are sharing the same channel.
	 */
	private ArrayList currentlyExecNodes;

	/**
	 * In case no agents have been selected for execution, the agents have to be
	 * selected by the user. To give him a choice, all agents in the system are
	 * included in this list.
	 */
	private ArrayList currentlyDefinedAgents = new ArrayList();

	/**
	 * Message, that can be shown by the GUI.
	 */
	private String message = "No message.";

  private String previousAction = "Initial state.";

	/**
	 * Constructor with file for the execution.
	 * 
	 * @param _file
	 */
	public PiExecutor(File _file) {
		root = null;
		exec = null;
		this.piDefFile = _file;
		resTable = new RestrictionTable();
		currentlyExecTauNodes = new ArrayList();
		currentlyExecNodes = new ArrayList();
		currentlyBlockedNodes = new ArrayList();
	}
	
	private Object readResolve() {
	  listenerList = new EventListenerList();
	  return this;
	}

	/**
	 * Return current message.
	 * 
	 * @return
	 */
	public String getMessage() {
		return message;
	}
	
	public String getPreviousAction() {
    return previousAction;
  }

	public ArrayList getCurrentlyDefinedAgents() {
		return currentlyDefinedAgents;
	}
	
	public int getExecutionsPerformed()
	{
	  return executionsPerformed;
	}

	/**
	 * Parse the selected file and build the execution tree.
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws PiExecutionException
	 *             Thrown if execution tree is empty or some agent could not be
	 *             found.
	 */
	public boolean startExecution() throws FileNotFoundException,
			PiExecutionException, RestrictionTableException, PiParserError,
			Exception {

		// Parse the file.
		parseFile();
		// Reset the counters for creating the unique id's
		PidGenerator.reset();
		NameGenerator.reset();
		// Build the tree for the execution (find the agents marked for
		// execution).
		buildExecutionTree();

		return setExecutableSteps();
	}

	/**
	 * Add new agents to the currently running system.
	 * 
	 * @param definitions
	 *            Holds the definitions of the new agent(s) and pool(s)
	 * @return True, if there are steps for execution.
	 * @throws Exception
	 * @throws PiParserError
	 * @throws Exception
	 * @throws PiParserError
	 * @throws Exception
	 */
	public void importAgents(Reader definitions) throws PiParserError,
			Exception {
		// Parse the file.
		SimpleNode defRoot = null;
		defRoot = parseAdditionalAgents(definitions);

		/*
		 * put the new definitions in the definition tree, name collisions with
		 * prefiously defined agents and pool scope problems should have been
		 * handled by the parser.
		 */
		// put the ones selected for execution in the execution tree
		if (defRoot != null) {
			boolean foundExec = false;
			for (int i = 0; i < defRoot.jjtGetNumChildren(); i++) {
				ASTAgentDefinition n = (ASTAgentDefinition) defRoot
						.jjtGetChild(i);
				if (n != null) {
					root.jjtAddChild(n, root.jjtGetNumChildren());

					// Selected for execution?
					if (n.isExecuteAgent()) {
						foundExec = true;
						addDefinedAgentNodeForExecution(n);
					}
				}
			}
			if (!foundExec) {
				message = "None of the newly imported agents has been selected\n"
						+ "for execution. Use the menu\n"
						+ "   Execution -> Select agents for execution...\n"
						+ "to add agents to the running system.";
				// Notifiy the GUI to show an information message
				ActionEvent e = new ActionEvent(this, EventValues.SHOW_MESSAGE,
						"showMessage");
				fireActionPerformed(e);
			}
			setExecutableSteps();
		}
	}

	/**
	 * Separate n and sibling into different lists, this is for example needed,
	 * if the two nodes are under the same summation node and shoud not
	 * communicate with each other.
	 * 
	 * @param n
	 * @param sibling
	 * @param nodeList
	 * @param sortedNodesVector
	 */
	private void splitList(SimpleNode n, SimpleNode sibling,
			ArrayList nodeList, ArrayList sortedNodesVector) {
		nodeList.remove(sibling);
		ArrayList newList = new ArrayList();
		newList.add(sibling);
		/*
		 * Add all other nodes of nodeList to this new list (except for the
		 * sibling node n).
		 */
		for (int l = 0; l < nodeList.size(); l++) {
			SimpleNode addNode = (SimpleNode) nodeList.get(l);
			if (addNode != null && addNode != n)
				newList.add(addNode);
		}
		/*
		 * Add the list to sortedNodesVector
		 */
		if (newList.size() > 1)
			sortedNodesVector.add(newList);
	}

	/**
	 * Go through the execution tree and dump the process definitions into the
	 * string, which is going to be returned.
	 * 
	 * @return
	 */
	public String getCurrentProcessDefinitions() {
		String defs = "";
		if (exec != null) {
			// go through execs children and dump
			for (int i = 0; i < exec.jjtGetNumChildren(); i++) {
				SimpleNode n = (SimpleNode) exec.jjtGetChild(i);
				if (n != null) {
					if (!defs.equals(""))
						defs = defs + "\n |   ";
					defs = defs + n.getProcessDefinitions();
				}
			}
		}
		return defs;
	}

	/**
	 * Choose next step to be executed and call for its execution. Afterwards
	 * find the next steps possible.
	 * 
	 * @return False if no more execution is available.
	 * @throws PiExecutionException
	 */
	public boolean autoExecute() throws PiExecutionException,
			RestrictionTableException {
		/*
		 * If there is a tau node available, execute it (but only if it is not
		 * under a replication node) because this would end us in an endless
		 * loop
		 */
		for (int i = 0; i < currentlyExecTauNodes.size(); i++) {
			// if (this.currentlyExecTauNodes.size() > 0) {
			ASTTau tauNode = (ASTTau) this.currentlyExecTauNodes.get(i);
			// check for replication parent if it has one go to next tau node
			ASTReplication repNode = tauNode.getTopMostReplicationNode(null);
			if (repNode == null)
				return execute(null, null, tauNode);
		}
		// If there is no tau node, there have to be some other nodes -> execute
		// one of these
		if (this.currentlyExecNodes.size() > 0) {
			// get a send and a receive node
			ASTSend sendNode = null;
			ASTReceive receiveNode = null;
			ArrayList nodeList = (ArrayList) this.currentlyExecNodes.get(0);
			for (int i = 0; i < nodeList.size(); i++) {
				SimpleNode n = (SimpleNode) nodeList.get(i);
				if (n != null) {
					if (n.toString().equals("Send")) {
						if (sendNode == null)
							sendNode = (ASTSend) n;
					} else if (n.toString().equals("Receive")) {
						if (receiveNode == null)
							receiveNode = (ASTReceive) n;
					}
				}
				// found a send node and a receive node -> stop searching
				if (sendNode != null && receiveNode != null)
					break;
			}
			// execute them
			// only call the receive node, it will take care of calling the
			// corresponding send node.
			// if (receiveNode != null){
			// receiveNode.executeNode(sendNode, receiveNode, resTable, false);
			// }
			return execute(sendNode, receiveNode, null);

		}
		return setExecutableSteps();
	}

	/**
	 * This method builds the executable part of the data structure, it finds
	 * the agents which are specified to be executed. If more than one agent is
	 * defined to be executed, they are executed in parallel.
	 */
	private void buildExecutionTree() throws PiExecutionException {

		System.out.print("Start building execution tree...");
		// no defintions tree?
		if (root == null)
			throw new PiExecutionException(
					"buildExecutionTree: Defintion Tree does not exist (null).");

		// find agents that are defined to be executed and set them as the exec
		// nodes children
		exec = new ASTRoot(PiParserTreeConstants.JJTROOT);
		int numChildren = root.jjtGetNumChildren();

		if (numChildren <= 0)
			throw new PiExecutionException(
					"buildExecutionTree: Agent definition tree contains no agents.");

		/*
		 * Collect all the agents from the definition tree, that are set for
		 * execution. Also collect all of the defined agents in a different
		 * list, which will be needed in case no agent has been pre selected for
		 * execution.
		 */
		ArrayList execAgents = new ArrayList();
		currentlyDefinedAgents.clear();
		for (int i = 0; i < numChildren; ++i) {
			SimpleNode n = (SimpleNode) root.jjtGetChild(i);
			if (n != null) {
				if (n.toString() == "AgentDefinition") {
					currentlyDefinedAgents.add(n);
					if (((ASTAgentDefinition) n).isExecuteAgent())
						execAgents.add(n);
				}
			}
		}

		/*
		 * no agents pre selected - tell the GUI to make a message box for agent
		 * selection and return the selected agents.
		 */
		if (execAgents.size() < 1) {
			ActionEvent e = new ActionEvent(this,
					EventValues.SELECT_EXEC_AGENTS, "selectExecAgents");
			/**
			 * The object handling the agents selection uses the
			 * currentlyDefinedAgents list to give the user the selection and
			 * modifies it, so it contains only the selected elements
			 * afterwards.
			 */
			fireActionPerformed(e);

			if (currentlyDefinedAgents.size() < 1)
				return;
			else
				execAgents.addAll(currentlyDefinedAgents);
		}

		for (int i = 0; i < execAgents.size(); i++) {
			SimpleNode n = (SimpleNode) execAgents.get(i);
			if (n != null) {
				/*
				 * // create new DefinedAgentNode ASTDefinedAgent ag = new
				 * ASTDefinedAgent( PiParserTreeConstants.JJTDEFINEDAGENT);
				 * ag.setAgentName(((ASTAgentDefinition) n).getAgentName());
				 * ag.setParameters(((ASTAgentDefinition) n).getParameters()); //
				 * ag.setProcessName(((ASTAgentDefinition) n).getAgentName());
				 * ag.setProcessName(PidGenerator
				 * .generateNewPidFromOldOne(((ASTAgentDefinition) n)
				 * .getAgentName()));
				 * 
				 * exec.jjtAddChild(ag, exec.jjtGetNumChildren()); // set the
				 * initial pid for the process under // the exec node. String
				 * pid = PidGenerator.generateNewPid(); ag.setPid(pid);
				 */
				addDefinedAgentNodeForExecution((ASTAgentDefinition) n);
			}
		}

		// valid execution tree?
		if (exec.jjtGetNumChildren() <= 0)
			throw new PiExecutionException(
					"Error: buildExecutionTree: Execution tree is empty.");
		// + "Agents to be executed need to be defined:
		// 'exec agent N(a) = ...'");
		/*
		 * for (int i = 0; i < execNumChildren; i++) { SimpleNode n =
		 * (SimpleNode) exec.jjtGetChild(i); if (n != null) { String pid =
		 * PidGenerator.generateNewPid(); n.setPid(pid); } }
		 */
		System.out.println("finished building execution tree.");

	}

	/**
	 * Collect all agent definition nodes from the definition tree.
	 * 
	 * @return List contains all agents of the system.
	 */
	public ArrayList getAllDefinedAgents() {
		ArrayList agents = new ArrayList();
		for (int i = 0; i < root.jjtGetNumChildren(); ++i) {
			SimpleNode n = (SimpleNode) root.jjtGetChild(i);
			if (n != null) {
				if (n.toString() == "AgentDefinition")
					agents.add(n);
			}
		}
		return agents;
	}

	/**
	 * Add an already defined agent to the executed system.
	 * 
	 * @param execAgents
	 * @return
	 * @throws PiExecutionException
	 * @throws RestrictionTableException
	 */
	public void addExecAgents(ArrayList execAgents)
			throws PiExecutionException, RestrictionTableException {
		for (int i = 0; i < execAgents.size(); i++) {
			SimpleNode n = (SimpleNode) execAgents.get(i);
			if (n != null) {
				addDefinedAgentNodeForExecution((ASTAgentDefinition) n);
			}
		}
		setExecutableSteps();
	}

	/**
	 * Create a defined agent node for the execution tree, from the given agent
	 * definition node.
	 */
	private void addDefinedAgentNodeForExecution(ASTAgentDefinition n) {
		ASTDefinedAgent ag = new ASTDefinedAgent(
				PiParserTreeConstants.JJTDEFINEDAGENT);
		ag.setAgentName(n.getAgentName());
		ag.setParameters(n.getParameters());
		// ag.setProcessName(((ASTAgentDefinition) n).getAgentName());
		ag.setProcessName(PidGenerator.generateNewPidFromOldOne(n
				.getAgentName()));

		exec.jjtAddChild(ag, exec.jjtGetNumChildren());
		// set the initial pid for the process under
		// the exec node.
		String pid = PidGenerator.generateNewPid();
		ag.setPid(pid);
	}

	/**
	 * CreateDot is for development it dumps the intermediate data structure to
	 * a dottable file.
	 * 
	 * @param n
	 *            RootNode of the tree to be dotted.
	 * @param file
	 *            Pointer to the file of the pi-process defintions. It is needed
	 *            for finding the directory where the new files will be created.
	 *            This will be relative to where the pi-definition file is.
	 * @throws IOException
	 */

	private void createDot(SimpleNode n, String fileName) throws IOException {
		if (n != null) {
			File dotFileDir = new File(piDefFile.getParent() + "/dotFiles");
			dotFileDir.mkdir();
			File dotFile = new File(dotFileDir.getPath() + "/"
					+ piDefFile.getName() + fileName + ".dot");

			dotFile.createNewFile();

			FileWriter fw = new FileWriter(dotFile);
			System.out.println("create dot file..." + dotFile.getPath());
			n.dumpDot("", 0, fw);

			/**
			 * dot the file
			 */
			System.out.println("generate dot graph..." + dotFile.getPath()
					+ ".png");
			// Process process =
			Runtime.getRuntime().exec(
					"dot -Tpng " + dotFile.getPath() + " -o "
							+ dotFile.getPath() + ".png");

			// paint the dot in the gui - not recommended for big files
			// because of extensive
			// memory usage.
			// System.out.println("show image...");
			// process.waitFor();
			// BufferedImage img = ImageIO.read(new File(dotFile.getPath()
			// + ".png"));
			// if (img == null) JOptionPane.showMessageDialog(this,
			// "Image selection failed.");
			// canvas.setImage(img);

		}
	}

	/**
	 * Parse the input file which holds the pi-process definitions and create an
	 * abstract syntax tree.
	 * 
	 * @throws PiExecutionException
	 * @throws FileNotFoundException
	 */
	private void parseFile() throws PiExecutionException,
			FileNotFoundException, PiParserError, Exception {

		if (piDefFile == null)
			new PiExecutionException("parseFile: Pi Definition File is null.");

		// get the input stream from the selected file
		FileInputStream fi = null;

		fi = new FileInputStream(piDefFile);

		// parse the selected file
		if (fi != null) {
			System.out.print("Start parsing...");
			long millisStart = System.currentTimeMillis();
			root = PiParser.parseFile(fi);
			poolTable = PiParser.getPoolTable();
			agentNames = PiParser.getAgentNames();
			poolNames = PiParser.getPoolNames();
			long millisEnd = System.currentTimeMillis();
			System.out.println("finished after: " + (millisEnd - millisStart)
					+ " milliseconds.");

		} else
			new PiExecutionException(
					"parseFile: FileInputStream is null, when trying to parse.");

	}

	private SimpleNode parseAdditionalAgents(Reader stream)
			throws PiParserError, Exception {
		// parse the selected file
		if (stream != null) {
			System.out.print("Start parsing of additional agents ... ");
			SimpleNode addAgentsRoot = PiParser.parseAdditionalAgents(stream,
					agentNames, poolTable, poolNames);
			poolTable = PiParser.getPoolTable();
			agentNames = PiParser.getAgentNames();
			poolNames = PiParser.getPoolNames();
			System.out.println("finished.");
			return addAgentsRoot;

		} else
			new PiExecutionException(
					"parseAdditionalAgents: InputStream is null, when trying to parse additional agents.");
		return null;
	}

	public Hashtable getPoolTable() {
		return poolTable;
	}

	/**
	 * Return a list of all processes running under the root. 
	 */
	public ArrayList getListOfRunningProcesses() {
		/*
		 * make a list of all running processes 
		 */
		ArrayList allProcesses = new ArrayList();
		if (exec != null) {
			for (int i = 0; i < exec.jjtGetNumChildren(); i++) {
				allProcesses.add(exec.jjtGetChild(i));
			}
		}
		return allProcesses;
	}

	public ArrayList getListOfExecutableTauNodes() {
		return currentlyExecTauNodes;
	}

	public ArrayList getListOfExecutableCommunications() {
		return currentlyExecNodes;
	}

	public ArrayList getListOfBlockedCommunications() {
		return currentlyBlockedNodes;
	}

	/**
	 * Before executing the next step all the possible actions have to be found,
	 * which can be a matching send and receive nodes in parallel or a tau node.
	 * 
	 */
	private Hashtable findPossibleSteps() throws PiExecutionException,
			RestrictionTableException {
		System.out.print("Start finding possible steps...");
		long startMillis = System.currentTimeMillis();
		Hashtable resultVector = new Hashtable();

		// clear the restriction table from all temporal entries
		resTable.removeTemporalEntries();
		/*
		 * Clean restriction table which means removing entries of non existing
		 * process identifiers
		 */
		HashSet pids = new HashSet();
		// find all possible actions
		for (int i = 0; i < exec.jjtGetNumChildren(); i++) {
			SimpleNode n = (SimpleNode) exec.jjtGetChild(i);
			if (n != null) {
				n.getAction(false, root, resultVector, exec, resTable);
				pids.add(n.getPid());
				/*
				 * getAction could have removed the child node n, this has to be
				 * checked and in case of removal the index has to be adjusted,
				 * so every child is handled.
				 */
				if (exec.jjtGetChild(i) != n)
					i--;

			}
		}

		resTable.clean(pids);

		long endMillis = System.currentTimeMillis();
		System.out.println("finished after: " + (endMillis - startMillis)
				+ " milliseconds.");
		return resultVector;
	}

	/**
	 * This method finds all further send and receive actions that are in the
	 * subtrees of the nodes contained in the possibleActions array.
	 * 
	 * @param possibleActions
	 * @return Array containing all blocked actions.
	 */
	private Hashtable findBlockedSteps(Hashtable possibleActions)
			throws RestrictionTableException {
		System.out.print("Start finding blocked steps...");
		long startMillis = System.currentTimeMillis();
		Hashtable blockedActions = new Hashtable();

		Collection sortedNodeLists = possibleActions.values();
		Iterator iter = sortedNodeLists.iterator();
		while (iter.hasNext()) {
			ArrayList nodeList = (ArrayList) iter.next();
			if (nodeList != null) {
				ListIterator iter2 = nodeList.listIterator();
				while (iter2.hasNext()) {
					SimpleNode n = (SimpleNode) iter2.next();
					if (n != null) {
						n.getBlockedActions(blockedActions, resTable);
					}
				}
			}
		}

		// ListIterator iter = possibleActions.listIterator();
		// while (iter.hasNext()) {
		// SimpleNode n = (SimpleNode) iter.next();
		// if (n != null)
		// n.getBlockedActions(blockedActions, resTable);
		// }
		long endMillis = System.currentTimeMillis();
		System.out.println("finished after: " + (endMillis - startMillis)
				+ " milliseconds.");
		return blockedActions;
	}

	/**
	 * Put all nodes sharing the bound name in the same list with n and the
	 * other in another list.
	 * 
	 * @param n
	 * @param nodeList
	 * @param sortedNodesVector
	 * @return True if changes to the lists were made.
	 * @throws PiExecutionException
	 */
	private void handleBoundNames(SimpleNode n, ArrayList nodeList,
			ArrayList sortedNodesVector) throws PiExecutionException {
		String channel = "";
		if (n.toString().equals("Send")) {
			channel = ((ASTSend) n).getChannelName();
		} else if (n.toString().equals("Receive")) {
			channel = ((ASTReceive) n).getChannelName();
		}
		/* Remove all nodes sharing the binding node with n into an own list. */
		ArrayList boundNodes = new ArrayList();
		nodeList.remove(n);
		boundNodes.add(n);
		SimpleNode bindingNode = n.getBindingNode(channel);
		if (bindingNode == null)
			throw new PiExecutionException(
					"Bound name was detected, but binding node is null.");

		for (int i = 0; i < nodeList.size(); i++) {
			SimpleNode n2 = (SimpleNode) nodeList.get(i);
			if (n2 != null) {
				SimpleNode bindingNode2 = n2.getBindingNode(channel);
				if (bindingNode2 == bindingNode) {
					nodeList.remove(n2);
					boundNodes.add(n2);
				}
			}
		}

		if (boundNodes.size() > 1)
			sortedNodesVector.add(boundNodes);
	}

	/**
	 * Determines which actions can be done regarding matching channels,
	 * restrictions...
	 * 
	 * @param stepVector
	 *            Contains all available steps.
	 * @param tauNodes
	 *            Return ArrayList for tau nodes. They can be executed without
	 *            further matching.
	 * @param sortedNodeVectors
	 *            Return ArrayList for all other executable steps.
	 * @throws PiExecutionException
	 */
	private void matchSteps(Hashtable stepTable, ArrayList sortedNodesVector,
			ArrayList tauNodes) throws PiExecutionException {

		System.out.print("Start matching steps...");
		long startMillis = System.currentTimeMillis();

		/*
		 * Extract the tauNodes from the stepTable and the lists of other nodes.
		 * The list of tau nodes is stored under the key "0-tau-0".
		 */
		ArrayList tauArray = null;
		if (stepTable.containsKey("0-tau-0")) {
			tauArray = (ArrayList) stepTable.get("0-tau-0");
			tauNodes.addAll(tauArray);
		}

		/*
		 * Add all other lists to the interaction vector.
		 */
		sortedNodesVector.addAll(stepTable.values());
		sortedNodesVector.remove(tauArray);
		/*
		 * Now remove all entries in the sortedNodeVector that have only one
		 * node in the list since this node will not be able to communicate or
		 * that have only one kind of node in the list (like all receiving nodes -
		 * no communication either). Also check the scoping of the channels and
		 * in case the scopes do not match seperate the nodeLists.
		 */

		for (int i = 0; i < sortedNodesVector.size(); i++) {
			ArrayList nodeList = (ArrayList) sortedNodesVector.get(i);
			int startingI = i;

			if (nodeList.size() < 2) {
				sortedNodesVector.remove(i);
				i--;
			} else {
				boolean receiveFound = false;
				boolean sendFound = false;
				RestrictionTableEntry entry = null;
				SimpleNode nodeWBoundName = null;
				for (int j = 0; j < nodeList.size(); j++) {
					// search for at least one receive node and at least one
					// send node
					SimpleNode n = (SimpleNode) nodeList.get(j);
					String pid = "";
					String channel = "";
					if (n != null) {
						pid = n.getPid();
						if (n.toString().equals("Send")) {
							sendFound = true;
							channel = ((ASTSend) n).getChannelName();
							if (n.isBoundName(channel))
								nodeWBoundName = n;
						} else if (n.toString().equals("Receive")) {
							receiveFound = true;
							channel = ((ASTReceive) n).getChannelName();
							if (n.isBoundName(channel))
								nodeWBoundName = n;
						}

					}
					// Check for some kind of restrictions on the channel of
					// this node
					if (entry == null) {
						// only replace if scope has not been set yet
						entry = resTable.getEntryForPidAndName(pid, channel);// resTable.getScopeOfChannel(pid,
						// channel);
					}

				}
				// remove if not at least one of the opposite types is available
				if (!(receiveFound && sendFound)) {
					sortedNodesVector.remove(i);
					i--;
				} else if (nodeWBoundName != null) {
					handleBoundNames(nodeWBoundName, nodeList,
							sortedNodesVector);
					i--;
				}
				// handle restrictions
				else if (entry != null) {
					/*
					 * Handle temporal entries and non temporal entries
					 * differently. Non temporal entries may include process
					 * parts that should not be in the scope because they are
					 * under another restriction which has not been resolved yet
					 * and the process parts have not received an own pid yet.
					 */
					if (!entry.isTemporalEntry()) {
						/*
						 * Move all nodes from the given scope into an own
						 * nodeList and add it to sortedNodeVectors. Redo this
						 * entry of sortedNodeVectors afterwards, because there
						 * might only be one entry left or there are still other
						 * restrictions. To avoid endless loops we have to check
						 * if all of the pids are in the same scope if this is
						 * the case, we leave this entry alone.
						 */

						// collect all pids of the nodes in this list in a
						// ArrayList
						ArrayList pidVector = new ArrayList();
						for (int j = 0; j < nodeList.size(); j++) {
							SimpleNode n = (SimpleNode) nodeList.get(j);
							pidVector.add(n.getPid());
						}

						if (!entry.getPids().containsAll(pidVector)) {
							ArrayList restrictedNodeList = new ArrayList();
							for (int j = 0; j < nodeList.size(); j++) {
								SimpleNode n = (SimpleNode) nodeList.get(j);
								if (n != null) {
									if (entry.getPids().contains(n.getPid())) {
										restrictedNodeList.add(n);
										nodeList.remove(j);
										j--;
									}
								}
							}

							/*
							 * add the newly created nodelist to the ArrayList
							 * of sorted sets
							 */

							if (restrictedNodeList.size() > 1)
								sortedNodesVector.add(restrictedNodeList);

							i--;
						}
					} else {
						/*
						 * If the found entry is a temporal entry, see if all
						 * nodes have the node restricting their as a parent. To
						 * avoid loops, if all end up in the same list, then go
						 * on.
						 */
						ArrayList resNodes = new ArrayList();
						ArrayList noResNodes = new ArrayList();
						ListIterator iter = nodeList.listIterator();
						while (iter.hasNext()) {
							SimpleNode n = (SimpleNode) iter.next();
							if (n != null) {
								String chan = "";
								if (n.toString().equals("Receive"))
									chan = ((ASTReceive) n).getChannelName();
								else
									chan = ((ASTSend) n).getChannelName();

								boolean isChild = n
										.isChildOfRestrictionNode(chan);

								if (isChild)
									resNodes.add(n);
								else
									noResNodes.add(n);

							}

						}

						if (!(resNodes.containsAll(nodeList) || noResNodes
								.containsAll(nodeList))) {
							sortedNodesVector.remove(nodeList);
							sortedNodesVector.add(resNodes);
							sortedNodesVector.add(noResNodes);
							i--;
						}
					}
				}
				/*
				 * Siblings under a summation node should not be able to
				 * communicate with each other - except they are under a
				 * composition node, which is a child of the summation node. By
				 * removing those siblings into a list of their own for each
				 * sibling, this will be avoided. To find siblings the pids will
				 * be compared, if two nodes have the same pid they are
				 * potentially siblings. Then their ancestors have to be
				 * compared and if they are a descendant of the same summation
				 * node, they will be moved to a list of their own which will
				 * have exactly the same entries as their old list except for
				 * their sibling node.
				 */

				/*
				 * Find siblings and remove one of them in its own list. This
				 * nodeList will be worked on until no siblings are left in it
				 * but it has to be rechecked again. The newly created nodeLists
				 * of the siblings that are added to the ArrayList might still
				 * contain siblings.
				 */

				for (int j = 0; j < nodeList.size(); j++) {
					SimpleNode n = (SimpleNode) nodeList.get(j);
					if (n != null) {
						// go through the rest of the list and look for a
						// sibling
						for (int k = j + 1; k < nodeList.size(); k++) {
							SimpleNode sibling = (SimpleNode) nodeList.get(k);
							if (sibling != null) {
								// pids are the same?
								if (n.getPid().equals(sibling.getPid())) {
									/*
									 * check if they are really siblings or if
									 * they are just in the same process under a
									 * not yet executed replication node - if
									 * they are siblings under a not yet
									 * executed replication node, they can still
									 * be executed, because instances of
									 * replications can talk to each other.
									 */
									/*
									 * Pay attention to the special case of the
									 * two communicators being under the same
									 * summation node, but in the same subtree
									 * under the same composition node, which is
									 * an ancestor of the mentioned summation
									 * node. Then communication may still take
									 * place.
									 */
									// if both are under replication node - skip
									if (!(n.isDescendentOfReplication() && sibling
											.isDescendentOfReplication()))

									{
										ArrayList nSum = new ArrayList();
										n.getAncestorSummationNodes(nSum);
										ArrayList siblingSum = new ArrayList();
										sibling
												.getAncestorSummationNodes(siblingSum);

										// Do they share a Summation node?
										nSum.retainAll(siblingSum);

										if (nSum.size() > 0) {
											/*
											 * Check for a composition under the
											 * lowest summation - find lowest
											 * summation (longest distance to
											 * root)
											 */
											SimpleNode lowSum = null;
											int distance = -1;
											for (int m = 0; m < nSum.size(); m++) {
												SimpleNode s = (SimpleNode) nSum
														.get(m);
												int dis = -1;
												if (s != null) {
													dis = s
															.getDistanceFromRoot();
												}
												if (dis > distance)
													lowSum = s;
											}
											ArrayList nComp = new ArrayList();
											n
													.getAncestorCompositionNodes(nComp);
											ArrayList sibComp = new ArrayList();
											sibling
													.getAncestorCompositionNodes(sibComp);
											nComp.retainAll(sibComp);

											/*
											 * See if one of the composition
											 * nodes they share is under the
											 * lowest summation, if yes - skip.
											 */
											boolean hasLowComp = false;
											for (int m = 0; m < nComp.size(); m++) {
												SimpleNode c = (SimpleNode) nComp
														.get(m);
												if (lowSum.isOnExecutionPath(c)) {
													hasLowComp = true;
													break;
												}
											}
											if (!hasLowComp) {
												/*
												 * They share a node, now remove
												 * the sibling from nodeList and
												 * create a list of its own and
												 * then add this list to
												 * nodeList
												 */
												splitList(n, sibling, nodeList,
														sortedNodesVector);
												// nodeList.remove(sibling);
												// ArrayList newList = new
												// ArrayList();
												// newList.add(sibling);
												// // Add all other nodes of
												// // nodeList
												// // to
												// // this new list (except
												// // for the sibling node n).
												// for (int l = 0; l < nodeList
												// .size(); l++) {
												// SimpleNode addNode =
												// (SimpleNode) nodeList
												// .get(l);
												// if (addNode != null
												// && addNode != n)
												// newList.add(addNode);
												// }
												// // Add the list to
												// // sortedNodesVector
												// if (newList.size() > 1)
												// sortedNodesVector
												// .add(newList);
												// Work over this entry of
												// nodeList
												// again.
												if (startingI == i)
													i--;
											}
										}

									}
									/*
									 * The following is needed for matching the
									 * steps of blocked nodes. If two blocked
									 * nodes have the same pid and, they might
									 * block each other and therefore the one
									 * further down has to be removed from the
									 * list.
									 */
									if (n.isOnExecutionPath(sibling)) {
										nodeList.remove(sibling);
										// Work over this entry of nodeList
										// again.
										if (startingI == i)
											i--;
									} else if (sibling.isOnExecutionPath(n)) {
										nodeList.remove(n);
										// Work over this entry of nodeList
										// again.
										if (startingI == i)
											i--;
									}
								}
							}
						}
					}
				}
			}
		}
		long endMillis = System.currentTimeMillis();
		System.out.println("finished after: " + (endMillis - startMillis)
				+ " milliseconds.");

	}

	/**
	 * Get all currently executable steps.
	 * 
	 * @return True if there are some steps which are executable, false if there
	 *         are none.
	 * @throws PiExecutionException
	 */
	private boolean setExecutableSteps() throws PiExecutionException,
			RestrictionTableException {

		// createDot(root, "root");
		// Find all available actions.
		Hashtable steps = findPossibleSteps();
		// filter out all steps that are not executable
		this.currentlyExecNodes.clear();
		this.currentlyExecTauNodes.clear();
		this.currentlyBlockedNodes.clear();
		matchSteps(steps, this.currentlyExecNodes, this.currentlyExecTauNodes);
		System.out.println("Executable steps ready...");

		// Find all the blocked actions and also match them.
		Hashtable blockedSteps = findBlockedSteps(steps);
		matchSteps(blockedSteps, this.currentlyBlockedNodes, new ArrayList());

		if (this.currentlyExecTauNodes.size() > 0
				|| this.currentlyExecNodes.size() > 0)
			return true;
		else
			return false;

	}
	
	public boolean hasExecutableSteps()
	{
	  return this.currentlyExecTauNodes.size() > 0 || this.currentlyExecNodes.size() > 0;
	}

	/**
	 * Create a dot for the current execution tree.
	 * 
	 * @throws IOException
	 * 
	 */
	public void dotCurrentExecTree() throws IOException {
		createDot(exec, "exec");
	}

	/**
	 * Create a dot for the definition tree.
	 * 
	 * @throws IOException
	 */
	public void dotDefinitionTree() throws IOException {
		createDot(root, "definitionTree");
	}
	
	public boolean execute(ASTSend send, ASTReceive receive, ASTTau tau)
			throws PiExecutionException, RestrictionTableException {
	  ++executionsPerformed;
	  
	  if (tau != null)
	  {
	    previousAction = "Executing tau action of process"
      + tau.getProcessNameAtRoot();
	  }
	  else
	  {
	    previousAction = "Sending from " + send.getProcessNameAtRoot() + ": ";
	    previousAction += send.getChannelName();
	    String params = send.getParameters().toString();
	    params = params.substring(1, params.length() - 1);
	    previousAction += "<" + params + ">   Sending to " + receive.getProcessNameAtRoot() +": "
	        + receive.getChannelName();
	    params = receive.getParameters().toString();
	    params = params.substring(1, params.length() - 1);
	    previousAction += "(" + params + ")";
	  }

		if (tau != null) {
			tau.executeNode(tau, null, resTable);
		} else if (send != null && receive != null) {

			/*
			 * If both of the nodes are under the same replication node, they
			 * need special handling, depending on if this is an inter
			 * replication communication or intra replication process
			 * communication. In the first case two copies have to be created
			 * for each of the nodes and have to be executed. In the second case
			 * only one copy is needed and the execution can be handled by the
			 * replication node itself. Two summation siblings under a
			 * replication are handled the same way as inter replication
			 * communication.
			 */
			/*
			 * If the pids are equal at this point they have to be under a
			 * replication node, otherwise they would have been deleted from the
			 * communication list during matchSteps()
			 */
			boolean spawn2RepProcesses = true;

			if (send.getPid().equals(receive.getPid())) {
				/*
				 * Check if they also share a summation parent 2 Replication
				 * Processes need to be spawned anyway, else the user may choose
				 * if he wants an intra or inter communication.
				 */
				ArrayList sendSummations = new ArrayList();
				send.getAncestorSummationNodes(sendSummations);
				ArrayList recSummations = new ArrayList();
				receive.getAncestorSummationNodes(recSummations);
				/*
				 * Now make an intersection and if an element is left they share
				 * a summation node and therefore spawn2RepProcesses needs to be
				 * true.
				 */
				sendSummations.retainAll(recSummations);
				if (!(sendSummations.size() > 0)) {
					// No shared summation ancestors - ask the user if he wants
					// inter or intra.
					// int choice = vis.showYesNoDialog
					String message = "You have selected a communication within a replicated process.\n"
							+ "Do you want to have two replicated instances of the replication\n"
							+ "to communicate with each other(Inter Instance Communication)?\n"
							+ "Selecting 'NO' executes the communication within one replicated instance.";
					int choice = JOptionPane.showConfirmDialog(null, message,
							"Execution", JOptionPane.YES_NO_OPTION);
					if (choice == JOptionPane.NO_OPTION)
						spawn2RepProcesses = false;

				}

			}
			receive.executeNode(send, receive, resTable, spawn2RepProcesses);

		}
		return setExecutableSteps();
	}

	public TreeMap getScopes() {
		return resTable.getScopes();
	}

	/** ****************ActionListener Methoden********************************* */

	public void addActionListener(ActionListener listener) {
		listenerList.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener) {
		listenerList.remove(ActionListener.class, listener);
	}

	protected void fireActionPerformed(ActionEvent e) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActionListener.class) {
				((ActionListener) listeners[i + 1]).actionPerformed(e);
			}
		}
	}

}
