/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.layout.al.common;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.INode;
import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * An interface defining a layout subtree with basic node operations.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision$
 */
public interface ILayoutSubtree extends ILayoutCommands {
    /**
     * Returns an id of the root folder.
     *
     * @return an id of the root node
     */
    public INodeId getRootNodeId();
    

    /**
     * Obtain a description of a node (channel or a folder) in a given user layout.
     *
     * @param nodeId a node id.
     * @return an <code>ILayoutNode</code> value
     * @exception PortalException if an error occurs
     */
    public INode getNode(INodeId nodeId) throws PortalException;

}