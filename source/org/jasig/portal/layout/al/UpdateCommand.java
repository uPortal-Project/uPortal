/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.ILayoutManager;
import org.jasig.portal.layout.al.common.node.INodeDescription;
import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * A class representing an update command
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision$
 */
public class UpdateCommand extends AbstractCommand {
    private INodeDescription nodeDescription;
    
    public UpdateCommand() { 
        super(); 
    }
    
    /**
     * Create a new node update command
     * @param nodeId
     * @param nodeDescription
     */
    public UpdateCommand(INodeId nodeId, INodeDescription nodeDescription) {
        super(nodeId);
        this.nodeDescription=nodeDescription;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommand#execute(org.jasig.portal.layout.al.common.ILayoutManager)
     */
    public boolean execute(ILayoutManager manager) throws PortalException {
        return manager.updateNode(this.getNodeId(),nodeDescription);
    }
    /**
     * @return Returns the nodeDescription.
     */
    public INodeDescription getNodeDescription() {
        return nodeDescription;
    }
    /**
     * @param nodeDescription The nodeDescription to set.
     */
    public void setNodeDescription(INodeDescription nodeDescription) {
        this.nodeDescription = nodeDescription;
    }
}
