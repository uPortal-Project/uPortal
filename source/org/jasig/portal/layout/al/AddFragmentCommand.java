/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * A class representing add fragment command
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision$
 */
public class AddFragmentCommand implements IALCommand {
	
    private IFragmentId fragmentId;
	private INodeId parentId;
    private INodeId nextNodeId; 
    
    public AddFragmentCommand() {
      super();
    }
 
    /**
     * Create a new add command
     * @param node
     * @param parentId
     * @param nextNodeId
     */
    public AddFragmentCommand(IFragmentId fragmentId, INodeId parentId, INodeId nextNodeId) {
        this.fragmentId=fragmentId;
        this.parentId = parentId;
        this.nextNodeId = nextNodeId;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommand#execute(org.jasig.portal.layout.al.ILayout)
     */
    public boolean execute(IALCommands manager) throws PortalException {
        return (manager.addFragment(fragmentId,parentId,nextNodeId)!=null);
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
    /**
     * @return Returns the fragmentId.
     */
    public IFragmentId getFragmentId() {
        return fragmentId;
    }
    /**
     * @param fragmentId The fragmentId to set.
     */
    public void setFragmentId(IFragmentId fragmentId) {
        this.fragmentId = fragmentId;
    }
}
