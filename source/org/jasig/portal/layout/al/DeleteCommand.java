/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * Delete command implementation
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 */
public class DeleteCommand implements ILayoutCommand {
    INodeId nodeId;
    
    public DeleteCommand() {};
    /**
     * Construct new delete command
     * @param nodeId
     */
    public DeleteCommand(INodeId nodeId) {
        this.nodeId = nodeId;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommand#execute(org.jasig.portal.layout.al.ILayoutManager)
     */
    public boolean execute(ILayoutManager manager) {
        return manager.deleteNode(nodeId);
    }
    /**
     * @return Returns the nodeId.
     */
    public INodeId getNodeId() {
        return nodeId;
    }
    /**
     * @param nodeId The nodeId to set.
     */
    public void setNodeId(INodeId nodeId) {
        this.nodeId = nodeId;
    }
}
