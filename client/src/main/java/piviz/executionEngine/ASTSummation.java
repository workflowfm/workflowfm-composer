/* Generated By:JJTree: Do not edit this line. ASTSummation.java */

package piviz.executionEngine;


import java.util.ArrayList;

import piviz.exceptions.PiExecutionException;
import piviz.exceptions.RestrictionTableException;
import piviz.helperClasses.RestrictionTable;


/**
 * Node representing the pi-construct for choices.
 * 
 * @author Anja
 * 
 */
public class ASTSummation extends SimpleNode {
	public ASTSummation(int id) {
		super(id);
	}

	public ASTSummation(PiParser p, int id) {
		super(p, id);
	}

	/**
	 * Make a copy of this subtree. This includes creating a new Summation node
	 * and calling this method for all its children.
	 * 
	 * @return Pointer to the root of the new subtree.
	 */
	public SimpleNode copySubtree(String postFix) throws PiExecutionException {

		ASTSummation cNode = new ASTSummation(
				PiParserTreeConstants.JJTSUMMATION);
		cNode.setProcessName(this.getProcessName() + postFix);
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				SimpleNode n = (SimpleNode) children[i];
				if (n != null) {
					cNode.jjtAddChild(n.copySubtree(postFix), i);
				}
			}
		}
		return cNode;
	}

	/**
	 * The summation node is replaced by one of its children on whose execution
	 * path the node to be executed lies.
	 */
	public void executeNode(SimpleNode node, RestrictionTable resTable)
			throws PiExecutionException, RestrictionTableException {

		// First go all the way up and execute the ancestors of this node.
		super.executeNode(node, resTable);

		// Now the parent should be the root node
		if (!this.parent.toString().equals("Root"))
			throw new PiExecutionException(toString()
					+ " executeNode(): Parent was expected to be Root.");

		// Remove the summation node and all the other sibling processes.
		// Find the child with the path that has been selected.
		SimpleNode execPathChild = null;
		for (int i = 0; i < this.jjtGetNumChildren(); i++) {
			SimpleNode child = (SimpleNode) this.jjtGetChild(i);
			if (child != null) {
				// Only one of the two given nodes should be on the execution
				// path.
				if (node != null) {
					if (child.isOnExecutionPath(node)) {
						execPathChild = child;
						break;
					}
				}
			}
		}
		if (execPathChild == null)
			throw new PiExecutionException(toString()
					+ " executeNode(): Node which is not on "
					+ "a execution path has been executed.");

		// remove the summation
		((SimpleNode) this.parent).removeChild(this);
		// add the execution path to parent
		this.parent.jjtAddChild(execPathChild, parent.jjtGetNumChildren());

	}

	/**
	 * Add this node to the list and go further up if possible.
	 */
	public void getAncestorSummationNodes(ArrayList sumNodes) {
		sumNodes.add(this);
		super.getAncestorSummationNodes(sumNodes);
	}

	/**
	 * Get the process definition of this node and its subnodes and return it.
	 * 
	 * @return
	 */
	public String getProcessDefinitions() {
		String str = "";
		for (int i = 0; i < this.jjtGetNumChildren(); i++) {
			SimpleNode n = (SimpleNode) this.jjtGetChild(i);
			if (n != null) {
				if (str.equals(""))
					str = str + "(";
				else
					str = str + " + ";

				str = str + n.getProcessDefinitions();
			}
		}
		str = str + ")";
		return str;
	}
}
