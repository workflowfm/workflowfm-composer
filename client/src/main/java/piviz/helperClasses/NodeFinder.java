/**
 * 
 */
package piviz.helperClasses;

import piviz.executionEngine.SimpleNode;

/**
 * @author Anja
 * 
 */
public class NodeFinder {

	public NodeFinder() {
	}

	/**
	 * Find the node in the target tree which is on the same place as the
	 * sourceNode in the sourceTree.
	 * 
	 * @param node
	 * @param sourceTree
	 * @param targetTree
	 * @return Node which is on the same place in the targetTree as the
	 *         sourceNode in the sourceTree if existent, otherwise return null.
	 */
	public SimpleNode getCorrespondingNode(SimpleNode sourceNode,
			SimpleNode sourceTree, SimpleNode targetTree) {

		SimpleNode foundNode = null;

		// find the node in the targetTree
		foundNode = this.getNode(sourceNode, sourceTree, targetTree);

		// check if they have the same type.
		if (foundNode != null)
			if (foundNode.getClass() == sourceNode.getClass())
				return foundNode;
		return null;
	}

	/**
	 * Go through both trees simultaneously and once sourceNode in the
	 * sourceTree is found, the target node will be found in the target tree at
	 * the same position.
	 * 
	 * @param node
	 * @param tree
	 * @param targetTree
	 
	 *           
	 * @return Will point to the node in the target tree, which has the same
	 *            position as node in tree, if it exists.
	 */
	private SimpleNode getNode(SimpleNode node, SimpleNode tree,
			SimpleNode targetTree) {
		if (tree == node) {
			return targetTree;
		} else {
			if (tree.jjtGetNumChildren() > 0) {
				for (int i = 0; i < tree.jjtGetNumChildren(); i++) {
					SimpleNode newTree = (SimpleNode) tree.jjtGetChild(i);
					SimpleNode newTargetTree = (SimpleNode) targetTree
							.jjtGetChild(i);
					if (newTree != null && targetTree != null) {
						SimpleNode result = getNode(node, newTree,
								newTargetTree);
						if (result != null) {
							return result;
						}
					}
				}
			}
			return null;
		}
	}
}
