/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.ILayoutManager;
import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * Move command representation
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @author Michael Ivanov: mvi at immagic.com
 */
public class MoveCommand extends AbstractCommand {
	
    private INodeId parentId;
    private INodeId nextNodeId;  
    
    public MoveCommand() {};
    
    /**
     * Construct move command using all arguments
     * @param nodeId
     * @param parentId
     * @param nextNodeId
     */
    public MoveCommand(INodeId nodeId, INodeId parentId, INodeId nextNodeId) {
        super(nodeId);
        this.parentId = parentId;
        this.nextNodeId = nextNodeId;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommand#execute(org.jasig.portal.layout.al.ILayoutManager)
     */
    public boolean execute(ILayoutManager manager) throws PortalException {
        return manager.moveNode(getNodeId(),parentId,nextNodeId);
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
