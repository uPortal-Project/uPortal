/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * Delete command implementation
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @author Michael Ivanov: mvi at immagic.com
 */
public class DeleteCommand extends AbstractCommand {
    
    public DeleteCommand() {
    	super();
    }
    /**
     * Construct new delete command
     * @param nodeId
     */
    public DeleteCommand(INodeId nodeId) {
        super(nodeId);
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommand#execute(org.jasig.portal.layout.al.ILayout)
     */
    public boolean execute(IALCommands manager) throws PortalException {
        return manager.deleteNode(getNodeId());
    }
    
}
