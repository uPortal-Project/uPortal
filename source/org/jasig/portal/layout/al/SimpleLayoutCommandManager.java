/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import java.util.Iterator;
import java.util.List;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.ILayoutManager;
import org.jasig.portal.layout.al.common.node.INode;
import org.jasig.portal.layout.al.common.node.INodeDescription;
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
    public void executeLayoutCommands() throws PortalException {
        for (Iterator iter = commands.iterator(); iter.hasNext();) {
            ILayoutCommand command = (ILayoutCommand) iter.next();
            command.execute(this.layoutManager);
        }
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordAddCommand(org.jasig.portal.layout.node.IUserLayoutNodeDescription, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
     */
    public INodeDescription addNode(INode nodeId, INodeId parentId, INodeId nextId) {
        commands.add(new AddCommand(nodeId,parentId,nextId));
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordMoveCommand(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
     */
    public boolean moveNode(INodeId nodeId, INodeId parentId, INodeId nextId) {
        commands.add(new MoveCommand(nodeId,parentId,nextId));
        return true;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManagerCommands#updateNode(org.jasig.portal.layout.al.common.node.INodeId, org.jasig.portal.layout.al.common.node.INodeDescription)
     */
    public boolean updateNode(INodeId nodeId, INodeDescription nodeDescription) throws PortalException {
        commands.add(new UpdateCommand(nodeId,nodeDescription));
        return true;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordDeleteCommand(org.jasig.portal.layout.node.INodeId)
     */
    public boolean deleteNode(INodeId nodeId) {
        commands.add(new DeleteCommand(nodeId));
        return true;
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
