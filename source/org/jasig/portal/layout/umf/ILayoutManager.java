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
public interface ILayoutManager {
	
	public ILayoutNode addNode ( IUserLayoutNodeDescription nodeDesc, INodeId parentId, INodeId nextId );
	
	public boolean moveNode ( INodeId nodeId, INodeId parentId, INodeId nextId );
	
	public boolean deleteNode ( INodeId nodeId );
	
	public IUserLayoutNodeDescription updateNode ( IUserLayoutNodeDescription nodeDesc );
	
	 /**
     * Gets a user layout (with appropriate markings).
     *
     * @return the user layout
     * @exception PortalException if an error occurs
     */
    public IUserLayout getUserLayout() throws PortalException;

    /**
     * Sets a user layout (with appropriate markings).
     *
     * @param userLayout the user layout
     * @exception PortalException if an error occurs
     */
    public void setUserLayout(IUserLayout userLayout) throws PortalException;
    
    /**
     * Test if a particular node can be moved to a given location.
     *
     * @param nodeId a <code>String</code> id of a node to be moved.
     * @param parentId a <code>String</code> id of a parent to which the node to be moved.
     * @param nextSiblingId a <code>String</code> id of a sibling prior to which the node is to be inserted (<code>null</code> to append at the end)
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    public boolean canMoveNode(INodeId nodeId, INodeId parentId, INodeId nextSiblingId) throws PortalException;

    /**
     * Tests if a particular node can be deleted.
     *
     * @param nodeId a <code>String</code> node id.
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    public boolean canDeleteNode(INodeId nodeId) throws PortalException;

    /**
     * Test if a certain node can be updated.
     *
     * @param node a <code>IUserLayoutNodeDescription</code> node id.
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    public boolean canUpdateNode(IUserLayoutNodeDescription node) throws PortalException;


}
