/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jasig.portal.layout.node.INodeId;

/**
 * A layout command manager that optimizies command recording
 * by reducing successive commands on the same node, and by
 * removing commands that can no longer be performed.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 */
public class ReducingLayoutCommandManager implements ILayoutCommandManager {
    List commands;
    ILayoutManager layoutManager;
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#clearCommands()
     */
    public void clearCommands() {
        commands.clear();
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#executeLayoutCommands(org.jasig.portal.layout.al.ILayoutManager)
     */
    public void executeLayoutCommands() {
        List successfulCommands=new ArrayList();
        for (Iterator iter = this.commands.iterator(); iter.hasNext();) {
            ILayoutCommand command = (ILayoutCommand) iter.next();
            if(command.execute(this.layoutManager)) {
                successfulCommands.add(command);
            }
        }
        // replace existing command list with the set of successfull commands
        this.commands=successfulCommands;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordAttachCommand(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
     */
    public void recordAttachCommand(INodeId nodeId, INodeId parentId, INodeId nextId) {
        // reduction rules: attach operation is always done on a new node, so there
        // are should not be any commands to reduce
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordDeleteCommand(org.jasig.portal.layout.node.INodeId)
     */
    public void recordDeleteCommand(INodeId nodeId) {
        // reduction rules: reduce all commands involving that node id, unless
        //   nodeId is a fragment root and there are commands involving
        //   that fragment (not node!) in history besides the attach command.
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordMoveCommand(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
     */
    public void recordMoveCommand(INodeId nodeId, INodeId parentId, INodeId nextId) {
        // reduction rules: 
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
