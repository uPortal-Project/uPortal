/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.ILayout;
import org.jasig.portal.layout.al.common.node.INode;
import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * A class representing add command
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision$
 */
public class AddCommand implements ILayoutCommand {
    private INode node;
	private INodeId parentId;
    private INodeId nextNodeId; 
    
    public AddCommand() {
      super();
    }
 
    /**
     * Create a new add command
     * @param node
     * @param parentId
     * @param nextNodeId
     */
    public AddCommand(INode node, INodeId parentId, INodeId nextNodeId) {
        this.node=node;
        this.parentId = parentId;
        this.nextNodeId = nextNodeId;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommand#execute(org.jasig.portal.layout.al.ILayout)
     */
    public boolean execute(ILayout manager) throws PortalException {
        return (manager.addNode(node,parentId,nextNodeId)!=null) ;
    }
    
    /**
     * @return Returns the nextNodeId.
     */
    public INodeId getNextNodeId() {
        return nextNodeId;
    }
    
    /**
     * @param nextNodeId The nextNodeId to set.
     */
    public void setNextNodeId(INodeId nextNodeId) {
        this.nextNodeId = nextNodeId;
    }
    
    /**
     * @return Returns the parentId.
     */
    public INodeId getParentId() {
        return parentId;
    }
    /**
     * @param parentId The parentId to set.
     */
    public void setParentId(INodeId parentId) {
        this.parentId = parentId;
    }
    /**
     * @return Returns the node.
     */
    public INode getNode() {
        return node;
    }
    /**
     * @param node The node to set.
     */
    public void setNode(INode node) {
        this.node = node;
    }
}
