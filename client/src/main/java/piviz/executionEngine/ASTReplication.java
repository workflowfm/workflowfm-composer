/* Generated By:JJTree: Do not edit this line. ASTReplication.java */

package piviz.executionEngine;


import java.util.Hashtable;

import piviz.exceptions.PiExecutionException;
import piviz.exceptions.RestrictionTableException;
import piviz.helperClasses.PidGenerator;
import piviz.helperClasses.RestrictionTable;


/**
 * Node representing the replication pi-construct.
 * 
 * @author Anja
 * 
 */
public class ASTReplication extends SimpleNode {

	public ASTReplication(int id) {
		super(id);
	}

	public ASTReplication(PiParser p, int id) {
		super(p, id);
	}

	/**
	 * Make a copy of this subtree. This includes creating a new replication
	 * node. A replication node has exactly one child node or none (if inactive
	 * afterwards).
	 * 
	 * @return Pointer to the root of the new subtree.
	 */
	public SimpleNode copySubtree(String postFix) throws PiExecutionException {

		ASTReplication cNode = new ASTReplication(
				PiParserTreeConstants.JJTREPLICATION);
		cNode.setProcessName(this.getProcessName() + postFix);
		if (children != null) {
			if (children.length > 1)
				throw new PiExecutionException(
						toString()
								+ ": copySubtree(): Wrong number of child nodes encountered: "
								+ children.length);
			if (children.length == 1) {
				SimpleNode n = (SimpleNode) children[0];
				if (n != null) {
					cNode.jjtAddChild(n.copySubtree(postFix), 0);
				}
			}
		}
		return cNode;
	}

	/**
	 * Replication nodes are not executed during the first step. The
	 * blockExecution parameter is set to true, which means that restrictions
	 * will not be executed either if they are descendents of a replication.
	 */
	public void getAction(boolean blockExecution, SimpleNode defTreeRoot,
			Hashtable resultVector, SimpleNode parent, RestrictionTable resTable)
			throws PiExecutionException, RestrictionTableException {
		// set the parent of this node
		this.jjtSetParent(parent);

		blockExecution = true;

		if (children.length > 1)
			throw new PiExecutionException(
					toString()
							+ ": getAction(): Wrong number of child nodes encountered: "
							+ children.length);

		// go on looking for an executable action - a replication node may only
		// have one child.
		else if (children.length == 1) {
			SimpleNode n = (SimpleNode) this.jjtGetChild(0);
			if (n != null)
				n.getAction(blockExecution, defTreeRoot, resultVector, this,
						resTable);

			// Check if the child has been replaced and redo the getAction()
			// step.
			while (n != this.jjtGetChild(0)) {
				n = (SimpleNode) this.jjtGetChild(0);
				if (n != null)
					n.getAction(blockExecution, defTreeRoot, resultVector,
							this, resTable);
			}
		}

		// remove this node if it does not have any children
		else if (children.length == 0)
			parent.removeChild(this);

	}

	/**
	 * Execution spawns off a new process.
	 */
	public void executeNode(SimpleNode node, RestrictionTable resTable)
			throws RestrictionTableException, PiExecutionException {

		// First go all the way up and execute the ancestors of this node.
		super.executeNode(node, resTable);

		// Now the parent should be the root node
		if (!this.parent.toString().equals("Root"))
			throw new PiExecutionException(toString()
					+ " executeNode(): Parent was expected to be Root.");

		// copy this subtree
		// give it an own process ID
		String newPID = PidGenerator.generateNewPidFromOldOne(this.getPid());

		ASTReplication cNode = (ASTReplication) this.copySubtree(PidGenerator.getIdFromName(newPID));
		cNode.setPid(newPID);
		// add the new processes id to all non temporal entries of the other
		// processes id
		resTable.addProcessBToScopeOfProcessA(this.getPid(), newPID);
		// add it to parent
		parent.jjtAddChild(cNode, parent.jjtGetNumChildren());

		// Add sub tree to exec tree
		if (this.jjtGetNumChildren() == 1) {
			SimpleNode n = (SimpleNode) this.children[0];
			if (n != null) {
				this.parent.jjtAddChild(n, ((SimpleNode) this.parent)
						.getPositionOfChild(this));
				// n.setProcessName(n.getProcessName()+"'");
				/*
				 * n.setProcessName(PidGenerator.generateNewPidFromOldOne(n
				 * .getProcessName()));
				 */
			}

		} else if (this.jjtGetNumChildren() > 1)
			throw new PiExecutionException(toString()
					+ " executeNode: Too many child nodes encountered.");

		// remove this tree
		((SimpleNode) this.parent).removeChild(this);

	}
	
	/**
	 * Get the process definition of this node and its subnodes and return it.
	 * 
	 * @return
	 */
	public String getProcessDefinitions() {
		String str = "! ";
		if (this.jjtGetNumChildren() > 0) {
			SimpleNode n = (SimpleNode) this.jjtGetChild(0);
			if (n != null) {
				str = str + n.getProcessDefinitions();
			}
		}
		return str;
	}
}
