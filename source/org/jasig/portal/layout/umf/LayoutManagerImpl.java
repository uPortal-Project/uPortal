/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.umf;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.node.INodeId;
import org.jasig.portal.layout.node.ILayoutNode;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;

/**
 * User Modifiable Fragment Layout Manager interface.
 * @author Michael Ivanov <a href="mailto:">mvi@immagic.com</a>
 * @version $Revision$
 */
public class LayoutManagerImpl implements  ILayoutManager {
	
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.umf.ILayoutManager#addNode(org.jasig.portal.layout.node.IUserLayoutNodeDescription, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
	 */
	public ILayoutNode addNode(IUserLayoutNodeDescription nodeDesc,
			INodeId parentId, INodeId nextId) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.umf.ILayoutManager#canDeleteNode(org.jasig.portal.layout.node.INodeId)
	 */
	public boolean canDeleteNode(INodeId nodeId) throws PortalException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.umf.ILayoutManager#canMoveNode(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
	 */
	public boolean canMoveNode(INodeId nodeId, INodeId parentId,
			INodeId nextSiblingId) throws PortalException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.umf.ILayoutManager#canUpdateNode(org.jasig.portal.layout.node.IUserLayoutNodeDescription)
	 */
	public boolean canUpdateNode(IUserLayoutNodeDescription node)
			throws PortalException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.umf.ILayoutManager#deleteNode(org.jasig.portal.layout.node.INodeId)
	 */
	public boolean deleteNode(INodeId nodeId) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.umf.ILayoutManager#getUserLayout()
	 */
	public IUserLayout getUserLayout() throws PortalException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.umf.ILayoutManager#moveNode(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
	 */
	public boolean moveNode(INodeId nodeId, INodeId parentId, INodeId nextId) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.umf.ILayoutManager#setUserLayout(org.jasig.portal.layout.IUserLayout)
	 */
	public void setUserLayout(IUserLayout userLayout) throws PortalException {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.layout.umf.ILayoutManager#updateNode(org.jasig.portal.layout.node.IUserLayoutNodeDescription)
	 */
	public IUserLayoutNodeDescription updateNode(
			IUserLayoutNodeDescription nodeDesc) {
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
	
	/*private ILayoutNode addNode ( IUserLayoutNodeDescription nodeDesc, IFragmentNodeId parentId, INodeId nextId ) {
		
	}
	private ILayoutNode addNode ( IUserLayoutNodeDescription nodeDesc, ILayoutNodeId parentId, INodeId nextId ) {
		
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
	
	
	
}
