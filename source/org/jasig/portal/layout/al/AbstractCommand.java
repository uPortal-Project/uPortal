/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * @author Michael Ivanov: mvi at immagic.com
 */
public abstract class AbstractCommand implements ILayoutCommand {
    
	private INodeId nodeId;
    
    public AbstractCommand() {};
    
    /**
     * Create a new attach command
     * @param nodeId
     * @param parentId
     * @param nextNodeId
     */
    public AbstractCommand(INodeId nodeId) {
        this.nodeId = nodeId;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommand#execute(org.jasig.portal.layout.al.ILayoutManager)
     */
    public abstract boolean execute(ILayoutManager manager);
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
