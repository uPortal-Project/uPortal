/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import java.util.Enumeration;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.AggregatedLayout;
import org.jasig.portal.layout.al.common.IUserLayout;
import org.jasig.portal.layout.al.common.ILayoutManager;
import org.jasig.portal.layout.al.common.LayoutEventListener;
import org.jasig.portal.layout.al.common.node.ILayoutNode;
import org.jasig.portal.layout.al.common.node.INode;
import org.jasig.portal.layout.al.common.node.INodeDescription;
import org.jasig.portal.layout.al.common.node.INodeId;
import org.jasig.portal.layout.al.common.node.NodeType;
import org.jasig.portal.layout.restrictions.IRestrictionManager;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * Aggregated user layout manager implementation
 * 
 * @author Michael Ivanov <a href="mailto:">mvi@immagic.com</a>
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision$
 */
public class LayoutManagerImpl implements  ILayoutManager {
    private ILayoutCommandManager layoutCommandManager;
    private IRestrictionManager restrictionManager;
    
    // internal representation of an assembled layout
    AggregatedLayout currentLayout;
    
    // fragment manager
    IFragmentManager fragmentManager;    
	
    // assemble user layout from scratch
    public void loadUserLayout() {
        // obtain user fragment, set it to be root
        // should lost folder (i.e. loose nodes be obtained separately?)
        
        // obtain list of all pushed fragments
        
        // obtain list of operations
        
        // perform operations, recording which pushed fragments
        // has been successfully attached
        
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutManager#addNodes(org.jasig.portal.layout.al.common.node.ILayoutNode, org.jasig.portal.layout.al.common.node.INodeId, org.jasig.portal.layout.al.common.node.INodeId)
     */
    public ILayoutNode addNodes(ILayoutNode node, INodeId parentId, INodeId nextId) {
        
        // attach newly constructed copy to the active layout, checking restrictions first
        // if fragmentId of the nodes being attached is not the same as the
        // fragmentId of the attachment point, place the subtree under the "lost folder" for that fragment
        // and record appropriate "move" operation.
        
        return null;
    }
    
    /* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#deleteNode(org.jasig.portal.layout.node.INodeId)
	 */
	public boolean deleteNode(INodeId nodeId) {
		// check restrictions
	    // if central modifications
	    //	if fragment root, delete fragment ?
	    //  otherwise: perform node deletion
	    // else (local modifications)
	    //  record "delete" operation
		return false;
	}
    
	
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#moveNode(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
	 */
	public boolean moveNode(INodeId nodeId, INodeId parentId, INodeId nextId) {
		// check restrictions
	    // 
	    // edit-oriented logic:
	    // if cm(d) {
	    //   if(cm(s)) {
	    //     remove node from s, add to d fragments
	    //   } else {
	    //     perform local delete on s
	    //     add node to d
	    //   }
	    // } else {
	    //    if(cm(s)) {
	    //      remove node from s, add to user fragment
	    //      record attachment node operation on d
	    //    } else {
	    //      record move operation from s to d
	    //    }
	    // }
	    // 
	    // // simplified central mod logic:
	    // if(cm(d) && cm(s)) {
	    //   remove node from s, add to d
	    // } else {
	    //   record move operation from d to s
	    // }
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#updateNode(org.jasig.portal.layout.node.INodeDescription)
	 */
	public INodeDescription updateNode(INodeDescription nodeDesc) {
		// check restrictions
	    // if central modification {
	    //   change fragment node
	    // } else {
	    //   record local update command
	    // } 
		return null;
	}
    /**
     * Import a subtree by constructing a local copy with
     * assigned ids.
     * @param node
     * @return a local aggregated layout node copy with assigned ids
     */
    protected IALNode importNodes(ILayoutNode node) {
        // construct subtree copy, assigning layout ids.
        // if the layout node implements IALNode, make use of the available
        // restriction information, otherwise assign default restrictions (none?)
        
        return null;
    }
    
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
	 * @see org.jasig.portal.layout.al.ILayoutManager#getUserLayout()
	 */
	public IUserLayout getUserLayout() throws PortalException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.al.ILayoutManager#setUserLayout(org.jasig.portal.layout.IUserLayout)
	 */
	public void setUserLayout(IUserLayout userLayout) throws PortalException {
		// TODO Auto-generated method stub

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
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#addLayoutEventListener(org.jasig.portal.layout.al.common.LayoutEventListener)
     */
    public boolean addLayoutEventListener(LayoutEventListener l) {
        // TODO Auto-generated method stub
        return false;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#addNode(org.jasig.portal.layout.al.common.node.INode, org.jasig.portal.layout.al.common.node.INodeId, org.jasig.portal.layout.al.common.node.INodeId)
     */
    public INodeDescription addNode(INode node, INodeId parentId, INodeId nextSiblingId) throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#canAddNode(org.jasig.portal.layout.al.common.node.INode, org.jasig.portal.layout.al.common.node.INodeId, org.jasig.portal.layout.al.common.node.INodeId)
     */
    public boolean canAddNode(INode node, INodeId parentId, INodeId nextSiblingId) throws PortalException {
        // TODO Auto-generated method stub
        return false;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#canUpdateNode(org.jasig.portal.layout.al.common.node.INodeId, org.jasig.portal.layout.al.common.node.INodeDescription)
     */
    public boolean canUpdateNode(INodeId nodeId, INodeDescription nodeDescription) throws PortalException {
        // TODO Auto-generated method stub
        return false;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#createNodeDescription(org.jasig.portal.layout.al.common.node.NodeType)
     */
    public INodeDescription createNodeDescription(NodeType nodeType) throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getCacheKey()
     */
    public String getCacheKey() throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getChildIds(org.jasig.portal.layout.al.common.node.INodeId)
     */
    public Enumeration getChildIds(INodeId nodeId) throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getDepth(org.jasig.portal.layout.al.common.node.INodeId)
     */
    public int getDepth(INodeId nodeId) throws PortalException {
        // TODO Auto-generated method stub
        return 0;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getLayoutId()
     */
    public int getLayoutId() {
        // TODO Auto-generated method stub
        return 0;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getNextSiblingId(org.jasig.portal.layout.al.common.node.INodeId)
     */
    public INodeId getNextSiblingId(INodeId nodeId) throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getNode(org.jasig.portal.layout.al.common.node.INodeId)
     */
    public INodeDescription getNode(INodeId nodeId) throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getParentId(org.jasig.portal.layout.al.common.node.INodeId)
     */
    public INodeId getParentId(INodeId nodeId) throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getPreviousSiblingId(org.jasig.portal.layout.al.common.node.INodeId)
     */
    public INodeId getPreviousSiblingId(INodeId nodeId) throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getRootFolderId()
     */
    public INodeId getRootFolderId() {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getSubscribeId(java.lang.String)
     */
    public INodeId getSubscribeId(String fname) throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getUserLayout(org.xml.sax.ContentHandler)
     */
    public void getUserLayout(ContentHandler ch) throws PortalException {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getUserLayout(org.jasig.portal.layout.al.common.node.INodeId, org.xml.sax.ContentHandler)
     */
    public void getUserLayout(INodeId nodeId, ContentHandler ch) throws PortalException {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#getUserLayoutDOM()
     */
    public Document getUserLayoutDOM() throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#markAddTargets(org.jasig.portal.layout.al.common.node.INode)
     */
    public void markAddTargets(INode node) throws PortalException {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#markMoveTargets(org.jasig.portal.layout.al.common.node.INodeId)
     */
    public void markMoveTargets(INodeId nodeId) throws PortalException {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#removeLayoutEventListener(org.jasig.portal.layout.al.common.LayoutEventListener)
     */
    public boolean removeLayoutEventListener(LayoutEventListener l) {
        // TODO Auto-generated method stub
        return false;
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.common.ILayoutManager#updateNode(org.jasig.portal.layout.al.common.node.INodeId, org.jasig.portal.layout.al.common.node.INodeDescription)
     */
    public boolean updateNode(INodeId nodeId, INodeDescription nodeDescription) throws PortalException {
        // TODO Auto-generated method stub
        return false;
    }
}
