/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */


package org.jasig.portal.layout.al;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.ILayoutManagerCommands;
import org.jasig.portal.layout.al.common.node.ILayoutNode;
import org.jasig.portal.layout.al.common.node.INode;
import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * IAggregatedLayoutManagerCommands defines an AL-specific extension to the regular layout manager commands
 *
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision$
 */
public interface IAggregatedLayoutManagerCommands extends ILayoutManagerCommands {
    
    
    /**
     * Attach a fragment to a specified location in the layout.
     * 
     * @param fragmentId id of the fragment to be attached
     * @param parentId layout id of the node to which the fragment should be attached,
     * @param nextId layout id of the next sibling in the layout structure
     * @return a fully qualified layout node corresponding to the root of attached fragment
     * @throws PortalException
     */
    public ILayoutNode addFragment(IFragmentId fragmentId, INodeId parentId, INodeId nextId) throws PortalException;
    
    
    /**
     * Import and attach a new node structure to the layout.
     * Note: the layout will attempt to import the specified node structure
     * into the fragment of the specified parent node. 
     * To attach a fragment, use <code>addLayoutFragment()</code> method
     *
     * @param node an <code>INode</code> structure be added.
     * @param parentId an id of a folder to which the new node structure should be added.
     * @param nextSiblingId an id of a sibling node prior to which the new node should be inserted.
     * @return an <code>ILayoutNode</code> value with a newly determined Id.
     * @exception PortalException if an error occurs
     */
    public ILayoutNode addNode(INode node, INodeId parentId, INodeId nextSiblingId) throws PortalException;
}
