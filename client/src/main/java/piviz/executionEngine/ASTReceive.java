/* Generated By:JJTree: Do not edit this line. ASTReceive.java */

package piviz.executionEngine;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import piviz.exceptions.PiExecutionException;
import piviz.exceptions.RestrictionTableException;
import piviz.helperClasses.NameGenerator;
import piviz.helperClasses.NodeFinder;
import piviz.helperClasses.PidGenerator;
import piviz.helperClasses.RestrictionTable;
import piviz.helperClasses.RestrictionTableEntry;


public class ASTReceive extends SimpleNode {
	private String channelName;

	private ArrayList parameters;

	public ASTReceive(int id) {
		super(id);
		parameters = new ArrayList();
	}

	public ASTReceive(PiParser p, int id) {
		super(p, id);
		parameters = new ArrayList();
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public ArrayList getParameters() {
		return parameters;
	}

	public void addParameter(String param) {
		parameters.add(param);
	}

	/**
	 * Dump this tree node in dot format. A dot node 'Receive' is created with
	 * the attributes channelName representing the channel over which is
	 * received and parameters representing the parameters to be received.
	 */
	public int dumpDot(String prefix, int nodeCount, FileWriter fw) {
		// System.out.println(toString(prefix)+ nodeCount + ";");
		try {
			// "parentNodeName -> nodeName_Id;"
			fw.write(toString(prefix) + "_" + nodeCount + ";\n");
			// nodeName_Id [label = "*ch: channelName, *params: [a,b,c]"];
			fw.write(toString() + "_" + nodeCount + " [label = " + "\""
					+ toString() + "\\n*PID: " + getPid() + "\\n*ch: "
					+ getChannelName() + "\\n*params: "
					+ getParameters().toString() + "\\n*Process: "
					+ getProcessName() + "\"];\n ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int newCount = nodeCount;

		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				SimpleNode n = (SimpleNode) children[i];
				if (n != null) {
					newCount = n.dumpDot(toString() + "_" + nodeCount + " -> ",
							newCount + 1, fw);
				}
			}
		}
		return newCount;
	}

	/**
	 * Make a copy of this subtree. This includes creating a new Receive node,
	 * setting the attributes channelName and parameters. A receive node may
	 * have exactly one or no child node (inaction afterwards).
	 * 
	 * @return Pointer to the root of the new subtree.
	 */
	public SimpleNode copySubtree(String postFix) throws PiExecutionException {

		ASTReceive cNode = new ASTReceive(PiParserTreeConstants.JJTRECEIVE);
		cNode.setChannelName(this.getChannelName());
		cNode.setParameters(new ArrayList(this.getParameters()));
		cNode.setProcessName(this.getProcessName() + postFix);
		if (children != null) {
			if (children.length > 1)
				throw new PiExecutionException(toString()
						+ " : copySubtree(): More than one child encountered.");
			if (children.length == 1) {
				SimpleNode n = (SimpleNode) children[0];
				if (n != null) {
					cNode.jjtAddChild(n.copySubtree(postFix), 0);
				}
			}
		}
		return cNode;
	}

	public void setParameters(ArrayList parameters) {
		this.parameters = parameters;
	}

	/**
	 * Names in the parameter ArrayList should not be overwritten because they
	 * are bound, they will be renamed to unique other names instead. This
	 * triggers another renaming cycle for the child nodes before going on with
	 * the original one, because the bound names have to be renamed first.
	 */
	public void renameNames(ArrayList oldNames, ArrayList newNames) {

		// replace the name of the channel if it has changed
		if (oldNames.contains(channelName))
			channelName = (String) newNames.get(oldNames.indexOf(channelName));

		// rename bound names
		for (int i = 0; i < parameters.size(); i++) {
			// incoming name already exists bound
			if (newNames.contains(parameters.get(i))) {
				// generate new name
				String newName = NameGenerator
						.generateUniqueName((String) parameters.get(i));
				// rename this name in the subtree
				ArrayList oldV = new ArrayList();
				oldV.add(parameters.get(i));
				ArrayList newV = new ArrayList();
				newV.add(newName);
				this.renameNames(oldV, newV);
				parameters.set(i, newName);
			}
		}

		// forward command to children
		super.renameNames(oldNames, newNames);
	}

	/**
	 * Adds the receive action node to the result ArrayList and return from this
	 * subtree.
	 */
	public void getAction(boolean blockExecution, SimpleNode defTreeRoot,
			Hashtable resultVector, SimpleNode parent, RestrictionTable resTable)
			throws PiExecutionException {
		// set parent node
		this.jjtSetParent(parent);

		// clear the endlessAgentLoopDetector, because of the pi-Action receive
		// no this
		// is no endless agent resolving loop.
		this.clearEndlessAgentLoopsDetector();

		if (resultVector.containsKey(this.getChannelName())) {
			ArrayList a = (ArrayList) resultVector.get(this.getChannelName());
			a.add(this);
		} else {
			ArrayList a = new ArrayList();
			a.add(this);
			resultVector.put(this.getChannelName(), a);
		}
	}

	/**
	 * This method is the anchor for the execution of a communication. The
	 * receive node in special takes care of handling communications between and
	 * within replicated processes. It triggers the "normal" execution of the
	 * receive node.
	 * 
	 * @param sendTauNode
	 * @param receiveNode
	 * @param resTable
	 *            Reference to the restriction table.
	 * @param interCommunication
	 *            If the user chooser communication between two instances of one
	 *            replication process this flag is set to true.
	 */
	public void executeNode(SimpleNode sendTauNode, SimpleNode receiveNode,
			RestrictionTable resTable, boolean spawn2RepProcesses)
			throws PiExecutionException, RestrictionTableException {

		if (sendTauNode == null && receiveNode == null)
			throw new PiExecutionException(
					toString()
							+ " executeNode(): One or both of the communicating nodes is null.");

		/*
		 * If both of the nodes are under the same replication node, they need
		 * special handling, depending on if this is an inter replication
		 * communication or intra replication process communication. In the
		 * first case two copies have to be created for each of the nodes and
		 * have to be executed. In the second case only one copy is needed and
		 * the execution can be handled by the replication node itself. Two
		 * summation siblings under a replication are handled the same way as
		 * inter replication communication.
		 */
		if (sendTauNode.getPid().equals(receiveNode.getPid())
				&& spawn2RepProcesses) {

			// get the topmost replication node
			ASTReplication repNode = sendTauNode
					.getTopMostReplicationNode(null);
			// there should be a replication node found, otherwise
			// matchSteps messed up and
			// we would not have two nodes with matching process ids
			if (repNode == null)
				throw new PiExecutionException(
						toString()
								+ " executeNode(): Communicating processes have the same process id, "
								+ "but do not have a replication ancestor.");

			// the other node should also be on the repNodes execution path
			// otherwise
			// something went badly wrong
			if (!repNode.isOnExecutionPath(receiveNode))
				throw new PiExecutionException(
						toString()
								+ " executeNode(): Communicating processes have the same process id,"
								+ " but are not children of the same replication node.");

			// Also the parent of this replication node should be the root
			// of the execution tree
			// otherwise having the same pids for the communicating nodes
			// would not be possible
			if (!repNode.jjtGetParent().toString().equals("Root"))
				throw new PiExecutionException(
						toString()
								+ " executeNode(): Parent of replication node is supposed to be root.");

			// now make some copies of the replication subtree, one
			// instance for the sending process
			// and one for the receiving process.
			if (repNode.jjtGetNumChildren() != 1)
				throw new PiExecutionException(toString()
						+ " executeNode(): Encountered non fitting number "
						+ "of children of the Replication node.");
			SimpleNode n = (SimpleNode) repNode.jjtGetChild(0);
			if (n == null)
				throw new PiExecutionException(toString()
						+ "executeNode(): Child node of replication is null.");

			// give it an own pid
			String newPid = PidGenerator
					.generateNewPidFromOldOne(this.getPid());

			SimpleNode sendSubtreeRoot = n.copySubtree(PidGenerator
					.getIdFromName(newPid));
			sendSubtreeRoot.setPid(newPid);
			// sendSubtreeRoot.setProcessName(sendSubtreeRoot.getProcessName()+"'");
			/*
			 * sendSubtreeRoot .setProcessName(PidGenerator
			 * .generateNewPidFromOldOne(sendSubtreeRoot .getProcessName()));
			 */
			/*
			 * add the new processes id to all non temporal entries of the other
			 * processes id
			 */
			resTable.addProcessBToScopeOfProcessA(this.getPid(), newPid);
			repNode.jjtGetParent().jjtAddChild(sendSubtreeRoot,
					repNode.jjtGetParent().jjtGetNumChildren() + 1);

			// give it an own pid
			newPid = PidGenerator.generateNewPidFromOldOne(this.getPid());
			SimpleNode receiveSubtreeRoot = n.copySubtree(PidGenerator
					.getIdFromName(newPid));
			receiveSubtreeRoot.setPid(newPid);
			// receiveSubtreeRoot.setProcessName(receiveSubtreeRoot.getProcessName()+"'");
			/*
			 * receiveSubtreeRoot.setProcessName(PidGenerator
			 * .generateNewPidFromOldOne(receiveSubtreeRoot .getProcessName()));
			 */
			/*
			 * add the new processes id to all non temporal entries of the other
			 * processes id
			 */
			resTable.addProcessBToScopeOfProcessA(this.getPid(), newPid);
			repNode.jjtGetParent().jjtAddChild(receiveSubtreeRoot,
					repNode.jjtGetParent().jjtGetNumChildren() + 1);

			/*
			 * Now we have to find the corresponding nodes of the send node and
			 * the receive node in the created trees and then trigger the
			 * execution with the new references.
			 */
			NodeFinder finder = new NodeFinder();
			SimpleNode newSendNode = finder.getCorrespondingNode(sendTauNode,
					n, sendSubtreeRoot);
			SimpleNode newReceiveNode = finder.getCorrespondingNode(
					receiveNode, n, receiveSubtreeRoot);
			if (newSendNode == null || newReceiveNode == null)

				throw new PiExecutionException(
						toString()
								+ " executeNode(): Inter replication action resetting of communication node"
								+ "pointers did not work, one or both is/are null");

			sendTauNode = newSendNode;
			receiveNode = newReceiveNode;
		}

		// First execute the send node
		sendTauNode.executeNode(sendTauNode, resTable);
		// then execute the receive node
		((ASTReceive) receiveNode).executeNode(sendTauNode, receiveNode,
				resTable);

	}

	/**
	 * The received names have to be checked so no bound names of the receiving
	 * process becomes overwritten, in case this would happen in a renaming
	 * cycle of the endangered name has to be started. The received name has to
	 * be checked for restriction, if it is restricted the scope concerning the
	 * received channels of the sending process has to be expanded meaning that
	 * the receiving process is added to this scope. Renaming has to take place.
	 * Afterwards the tree is removed from the exec tree and its subtree is
	 * added.
	 * 
	 * @param sendTauNode
	 * @param receiveNode
	 * @param resTable
	 */
	public void executeNode(SimpleNode sendTauNode, SimpleNode receiveNode,
			RestrictionTable resTable) throws PiExecutionException,
			RestrictionTableException {

		// Go all the way up and execute the ancestors of this node.
		super.executeNode(receiveNode, resTable);

		// Now the parent should be the root node
		if (!this.parent.toString().equals("Root"))
			throw new PiExecutionException(toString()
					+ " executeNode(): Parent was expected to be Root.");

		/*
		 * Check existing restrictions so they do not become overwritten. We
		 * have to take care of scope expansion here. Check every received name
		 * for a restriction and adjust its scope.
		 */

		ArrayList receivedNames = ((ASTSend) sendTauNode).getParameters();
		ArrayList placeHolders = ((ASTReceive) receiveNode).getParameters();
		if (receivedNames.size() != placeHolders.size())
			throw new PiExecutionException(toString()
					+ " executeNode(): Parameter lists have differing length.");

		SimpleNode childNode = null;
		if (this.jjtGetNumChildren() == 1) {
			childNode = (SimpleNode) this.children[0];

		} else if (this.jjtGetNumChildren() > 1)
			throw new PiExecutionException(toString()
					+ " executeNode: Too many child nodes encountered.");

		if (childNode != null) {
			// The list of names is not empty - handle those names.
			if (receivedNames.size() > 0) {

				/*
				 * See if the received names are already contained restricted in
				 * this process, if so create a new name for it and rename the
				 * name in this and all other processes it is restricted to and
				 * then use the incoming name.
				 */
				for (int i = 0; i < receivedNames.size(); i++) {
					String oldName = (String) receivedNames.get(i);
					RestrictionTableEntry entry = resTable
							.getEntryForPidAndName(this.getPid(), oldName);

					// entry exists
					/*
					 * Check if the sending process belongs to the same scope,
					 * skip if this is the case.
					 */
					if (entry != null) {
						if (!entry.getPids().contains(sendTauNode.getPid())) {
							// generate new name
							String newName = NameGenerator
									.generateUniqueName(oldName);
							// Rename the name in the whole scope, which means
							// traversing all
							// children of the exec node whose pid matches one
							// in the entry.
							ASTRoot execNode = (ASTRoot) this.parent;
							for (int j = 0; j < execNode.jjtGetNumChildren(); j++) {
								SimpleNode n = (SimpleNode) execNode
										.jjtGetChild(j);
								if (n != null) {
									if (entry.getPids().contains(
											(String) n.getPid())) {
										ArrayList newNameV = new ArrayList();
										newNameV.add(newName);
										ArrayList oldNameV = new ArrayList();
										oldNameV.add(oldName);
										n.renameNames(oldNameV, newNameV);
									}
								}
							}

							// replace the name in the restriction table entry
							entry.getNames().remove(oldName);
							entry.getNames().add(newName);
						}
					}
				}

				// Do the renaming
				childNode.renameNames(placeHolders, receivedNames);

				/*
				 * Add receiving process to the scope of the sending process
				 * concerning this channel. This has to be done after the
				 * renaming since the new entries in the restriction table would
				 * collide while checking for restrictions during renaming.
				 */

				String senderPid = sendTauNode.getPid();
				String receiverPid = receiveNode.getPid();
				for (int i = 0; i < receivedNames.size(); i++) {
					resTable.extendScopeOfName(senderPid, receiverPid,
							(String) receivedNames.get(i));
				}

			}

			// Remove this node
			// Add new sub tree to exec tree
			((SimpleNode)this.parent).removeChild(this);
			this.parent.jjtAddChild(childNode, this.parent.jjtGetNumChildren());
			
			// childNode.setProcessName(childNode.getProcessName() + "'");
		}

		// remove this tree
		((SimpleNode) this.parent).removeChild(this);

		System.out.println("Receive Node finished execution.");

	}

	/**
	 * Get the process definition of this node and its subnodes and return it.
	 * 
	 * @return
	 */
	public String getProcessDefinitions() {
		String str = channelName;
		String params = parameters.toString();
		params = params.substring(1, params.length() - 1);
		str = str + "(" + params + ")";

		if (this.jjtGetNumChildren() > 0) {
			SimpleNode n = (SimpleNode) this.jjtGetChild(0);
			if (n != null) {
				str = str + "." + n.getProcessDefinitions();
			}
		} else
			str = str + ".0";
		return str;
	}
}
