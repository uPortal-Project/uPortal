/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.al.common.node.INodeId;
import org.jasig.portal.layout.al.common.node.ILayoutNode;
import org.jasig.portal.layout.al.common.node.INodeDescription;
import org.jasig.portal.layout.restrictions.IRestrictionManager;

/**
 * User Modifiable Fragment Layout Manager interface.
 * @author Michael Ivanov <a href="mailto:">mvi@immagic.com</a>
 * @version $Revision$
 */
public class LayoutManagerImpl implements  ILayoutManager {
    private ILayoutCommandManager layoutCommandManager;
    private IRestrictionManager restrictionManager;
    
	
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutManager#attachNode(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
     */
    public boolean attachNode(INodeId nodeId, INodeId parentId, INodeId nextId) {
        // TODO Auto-generated method stub
        return false;
    }
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#addNode(org.jasig.portal.layout.node.INodeDescription, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
	 */
	public ILayoutNode addNode(INodeDescription nodeDesc, INodeId parentId, INodeId nextId) {
	    
		return null;
	}
	
	public ILayoutNode addNodeSubtree(ILayoutNode[] nodeDesc, INodeId parentId, INodeId nextId) {
	    return null;
	}
	
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#canDeleteNode(org.jasig.portal.layout.node.INodeId)
	 */
	public boolean canDeleteNode(INodeId nodeId) throws PortalException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#canMoveNode(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
	 */
	public boolean canMoveNode(INodeId nodeId, INodeId parentId,
			INodeId nextSiblingId) throws PortalException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#canUpdateNode(org.jasig.portal.layout.node.INodeDescription)
	 */
	public boolean canUpdateNode(INodeDescription node)
			throws PortalException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#deleteNode(org.jasig.portal.layout.node.INodeId)
	 */
	public boolean deleteNode(INodeId nodeId) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#getUserLayout()
	 */
	public IUserLayout getUserLayout() throws PortalException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#moveNode(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
	 */
	public boolean moveNode(INodeId nodeId, INodeId parentId, INodeId nextId) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#setUserLayout(org.jasig.portal.layout.IUserLayout)
	 */
	public void setUserLayout(IUserLayout userLayout) throws PortalException {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#updateNode(org.jasig.portal.layout.node.INodeDescription)
	 */
	public INodeDescription updateNode(
			INodeDescription nodeDesc) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
	/*private ILayoutNode addNode ( INodeDescription nodeDesc, IFragmentNodeId parentId, INodeId nextId ) {
		
	}
	private ILayoutNode addNode ( INodeDescription nodeDesc, ILayoutNodeId parentId, INodeId nextId ) {
		
	}
	
	private boolean moveNode ( ILayoutNodeId nodeId, ILayoutNodeId parentId, INodeId nextId ) {
		
	}
	private boolean moveNode ( ILayoutNodeId nodeId, IFragmentNodeId parentId, INodeId nextId ) {
		
	}
	private boolean moveNode ( IFragmentNodeId nodeId, ILayoutNodeId parentId, INodeId nextId ) {
		
	}
	private boolean moveNode ( IFragmentNodeId nodeId, IFragmentNodeId parentId, INodeId nextId ) {
		
	}
	
	private boolean deleteNode ( IFragmentNodeId nodeId ) {
		
	}*/
	
    /**
     * @return Returns the layoutCommandManager.
     */
    public ILayoutCommandManager getLayoutCommandManager() {
        return layoutCommandManager;
    }
    /**
     * Secify layout command manager to be used
     * @param layoutCommandManager The layoutCommandManager to set.
     */
    public void setLayoutCommandManager(ILayoutCommandManager layoutCommandManager) {
        this.layoutCommandManager = layoutCommandManager;
    }
    /**
     * @return Returns the restrictionManager.
     */
    public IRestrictionManager getRestrictionManager() {
        return restrictionManager;
    }
    /**
     * @param restrictionManager The restrictionManager to set.
     */
    public void setRestrictionManager(IRestrictionManager restrictionManager) {
        this.restrictionManager = restrictionManager;
    }
}
