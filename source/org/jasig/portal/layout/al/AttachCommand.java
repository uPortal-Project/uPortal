/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @author Michael Ivanov: mvi at immagic.com
 */
public class AttachCommand extends AbstractCommand {

	private INodeId parentId;
    private INodeId nextNodeId; 
    
    public AttachCommand() {
      super();
    }
    
    
    /**
     * Create a new attach command
     * @param nodeId
     * @param parentId
     * @param nextNodeId
     */
    public AttachCommand(INodeId nodeId, INodeId parentId, INodeId nextNodeId) {
        super(nodeId);
        this.parentId = parentId;
        this.nextNodeId = nextNodeId;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommand#execute(org.jasig.portal.layout.al.ILayoutManager)
     */
    public boolean execute(ILayoutManager manager) {
        return manager.attachNode(getNodeId(),parentId,nextNodeId);
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
}
