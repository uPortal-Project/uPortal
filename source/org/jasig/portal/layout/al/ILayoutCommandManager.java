/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * An interface describing layout command manager.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 */
public interface ILayoutCommandManager {
    
    /**
     * Specify the layout manager
     * @param manager - manager for which the commands are issued
     */
    void setLayoutManager(ILayoutManager manager);
    
    /**
     * Execute a sequence of the recorded commands.
     * @param manager - the manager on which to execute the commands
     */
    void executeLayoutCommands();
    
    /**
     * Record attach node command
     * @param nodeId id of the node being attached
     * @param parentId parent
     * @param nextId next node id
     */
    void recordAttachCommand(INodeId nodeId, INodeId parentId, INodeId nextId);
    /**
     * Record move node command
     * @param nodeId id of the node being moved
     * @param parentId parent node id
     * @param nextId next node id
     */
    void recordMoveCommand(INodeId nodeId, INodeId parentId, INodeId nextId);
    /**
     * Record delete node command
     * @param nodeId node id
     */
    void recordDeleteCommand(INodeId nodeId);
    
    /**
     * Clear all user modification commands
     */
    void clearCommands();
}
