/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.layout.al.common;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.INode;
import org.jasig.portal.layout.al.common.node.INodeDescription;
import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * An interface describing node operation commands of the user layout manager.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision$
 */
public interface ILayoutManagerCommands {
    /**
     * Add a new node to a current user layout.
     *
     * @param node an <code>INodeDescription</code> value of a node to be added (Id doesn't have to be set)
     * @param parentId an id of a folder to which the new node (channel or folder) should be added.
     * @param nextSiblingId an id of a sibling node (channel or folder) prior to which the new node should be inserted.
     * @return an <code>INodeDescription</code> value with a newly determined Id.
     * @exception PortalException if an error occurs
     */
    public INodeDescription addNode(INode node, INodeId parentId, INodeId nextSiblingId) throws PortalException;
    /**
     * Move a node (channel or folder) from one location to another.
     *
     * @param nodeId a node Id.
     * @param parentId an id of a folder to which the node should be moved.
     * @param nextSiblingId an id of a sibling node (folder or channel) prior to which the node should be placed. (<code>null</code> to append at the end)
     * @return a <code>boolean</code> value noting if the operation was successful
     * @exception PortalException if an error occurs
     */
    public boolean moveNode(INodeId nodeId, INodeId parentId, INodeId nextSiblingId) throws PortalException;
    /**
     * Delete a node (folder or a channel) from a user layout.
     *
     * @param nodeId an node id
     * @return a <code>boolean</code> value noting if the operation was successful
     * @exception PortalException if an error occurs
     */
    public boolean deleteNode(INodeId nodeId) throws PortalException;
    /**
     * Update a given node.
     *
     * @param nodeId an id of a node being updated
     * @param nodeDescription an <code>INodeDescription</code>.
     * @return a <code>boolean</code> value noting if the operation was successful
     * @exception PortalException if an error occurs
     */
    public boolean updateNode(INodeId nodeId, INodeDescription nodeDescription) throws PortalException;
}