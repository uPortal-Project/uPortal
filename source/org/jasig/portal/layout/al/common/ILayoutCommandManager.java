/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al.common;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.INodeId;


/**
 * An interface describing layout command manager.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @author Michael Ivanov: mvi at immagic.com
 */
public interface ILayoutCommandManager extends ILayoutManagerCommands {
    
    /**
     * Specify the layout manager
     * @param manager - manager for which the commands are issued
     */
    public void setLayoutManager(ILayout manager);
    
    /**
     * Execute a sequence of the recorded commands.
     * @param manager - the manager on which to execute the commands
     */
    public void executeLayoutCommands() throws PortalException;
    
    /**
     * Clear all user modification commands
     */
    public void clearCommands();
    
    /**
     * Notify command manager that a particular node has been centrally
     * deleted.
     * 
     * @param nodeId layout id of the node that has been centrally deleted
     */
    public void notifyCentralDeleteNode(INodeId nodeId);
}
