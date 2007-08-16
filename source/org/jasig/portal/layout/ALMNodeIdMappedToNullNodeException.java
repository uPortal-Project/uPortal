package org.jasig.portal.layout;

/**
 * Object representing the exceptional condition of a node id having mapped to a null node.
 * @author apetro
 *
 */
public class ALMNodeIdMappedToNullNodeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This constructor is for the case where there's an immediately mapping to null node id, and
	 * this node ID was encountered pursuing the siblings of some other node ID.
	 * @param offendingNodeId - node id that mapped to a null node, giving rise to this exceptional condition
	 * @param rootNodeId - node whose siblings are being pursued
	 */
	public ALMNodeIdMappedToNullNodeException(String offendingNodeId, String rootNodeId) {
	   super("Node identifier [" + offendingNodeId + "] mapped to null node; encountered pursuing siblings to [ " + rootNodeId + "]");
	}

}
