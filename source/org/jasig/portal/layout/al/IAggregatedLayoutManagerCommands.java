/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */


package org.jasig.portal.layout.al;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.ILayoutManagerCommands;
import org.jasig.portal.layout.al.common.node.ILayoutNode;
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
    
}
