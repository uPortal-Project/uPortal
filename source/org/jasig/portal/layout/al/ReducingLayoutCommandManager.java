/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.ILayoutNode;
import org.jasig.portal.layout.al.common.node.INode;
import org.jasig.portal.layout.al.common.node.INodeDescription;
import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * A layout command manager that optimizies command recording
 * by reducing successive commands on the same node, and by
 * removing commands that can no longer be performed.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 */
public class ReducingLayoutCommandManager implements IALCommandManager {
    List commands;
    IAggregatedLayout layoutManager;
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.IAggregatedLayoutCommandManager#clearCommands()
     */
    public void clearCommands() {
        commands.clear();
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.IAggregatedLayoutCommandManager#executeLayoutCommands(org.jasig.portal.layout.al.ILayout)
     */
    public void executeLayoutCommands() throws PortalException {
        // make a copy of the commands list before executing anything,
        // because commands will be propagated back to the command manager
        // during the execution process.
        List existingCommands=new ArrayList(commands);
        // clear commands stack
        commands.clear();
        // execute commands
        for (Iterator iter = existingCommands.iterator(); iter.hasNext();) {
            ILayoutCommand command = (ILayoutCommand) iter.next();
            command.execute(this.layoutManager);
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.IAggregatedLayoutCommandManager#recordAddCommand(org.jasig.portal.layout.node.IUserLayoutNodeDescription, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
     */
    public ILayoutNode addNode(INode nodeId, INodeId parentId, INodeId nextId) {
        // TODO: should throw some kind of an exception here. This method should never be called.
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.IAggregatedLayoutManagerCommands#addFragment(org.jasig.portal.layout.al.IFragmentId, org.jasig.portal.layout.al.common.node.INodeId, org.jasig.portal.layout.al.common.node.INodeId)
     */
    public ILayoutNode addFragment(IFragmentId fragmentId, INodeId parentId, INodeId nextId) throws PortalException {
        commands.add(new AddFragmentCommand(fragmentId,parentId,nextId));
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.IAggregatedLayoutCommandManager#recordMoveCommand(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
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
     * @see org.jasig.portal.layout.al.IAggregatedLayoutCommandManager#recordDeleteCommand(org.jasig.portal.layout.node.INodeId)
     */
    public boolean deleteNode(INodeId nodeId) {
        // reduction rules: reduce all commands involving that node id, unless
        //   nodeId is a fragment root and there are commands involving
        //   that fragment (not node!) in history besides the attach command.        
        commands.add(new DeleteCommand(nodeId));
        return true;
    }
    
    /**
     * @return Returns the layoutManager.
     */
    public IAggregatedLayout getLayoutManager() {
        return layoutManager;
    }
    /**
     * @param layoutManager The layoutManager to set.
     */
    public void setLayoutManager(IAggregatedLayout layoutManager) {
        this.layoutManager = layoutManager;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.IAggregatedLayoutCommandManager#notifyCentralDeleteNode(org.jasig.portal.layout.al.common.node.INodeId)
     */
    public void notifyCentralDeleteNode(INodeId nodeId) {
        // TODO Auto-generated method stub
    }
}
