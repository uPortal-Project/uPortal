/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import java.util.Iterator;
import java.util.List;
import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * A naive implementation of the command manager that
 * appends commands to a list without any reduction procedures.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 */
public class SimpleLayoutCommandManager implements ILayoutCommandManager {
    List commands;
    ILayoutManager layoutManager;
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#executeLayoutCommands(org.jasig.portal.layout.al.ILayoutManager)
     */
    public void executeLayoutCommands() {
        for (Iterator iter = commands.iterator(); iter.hasNext();) {
            ILayoutCommand command = (ILayoutCommand) iter.next();
            command.execute(this.layoutManager);
        }
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordAddCommand(org.jasig.portal.layout.node.IUserLayoutNodeDescription, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
     */
    public void recordAttachCommand(INodeId nodeId, INodeId parentId, INodeId nextId) {
        commands.add(new AttachCommand(nodeId,parentId,nextId));
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordMoveCommand(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
     */
    public void recordMoveCommand(INodeId nodeId, INodeId parentId, INodeId nextId) {
        commands.add(new MoveCommand(nodeId,parentId,nextId));
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordDeleteCommand(org.jasig.portal.layout.node.INodeId)
     */
    public void recordDeleteCommand(INodeId nodeId) {
        commands.add(new DeleteCommand(nodeId));
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#clearCommands()
     */
    public void clearCommands() {
        commands.clear();
    }
    /**
     * @return Returns the layoutManager.
     */
    public ILayoutManager getLayoutManager() {
        return layoutManager;
    }
    /**
     * @param layoutManager The layoutManager to set.
     */
    public void setLayoutManager(ILayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }
}
