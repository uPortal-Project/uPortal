/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import org.jasig.portal.layout.node.IUserLayoutNodeDescription;

/**
 * A layout event involving old parent reference. 
 * Used to related "move" and "delete" node events.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0
 */
public class LayoutMoveEvent extends LayoutEvent {
    private static final long serialVersionUID = 1L;

    protected final IUserLayoutNodeDescription oldParentNode;

    /**
     * Construct a <code>LayoutMoveEvent</code> from a given source,
     * node, and node's old parent.
     *
     * @param source an <code>Object</code> that generated the event
     * @param node an <code>UserLayoutNodeDescription</code> of the node that was involved
     * @param oldParentNodeId a <code>String</code> value of an old parent id
     */
    public LayoutMoveEvent(Object source, IUserLayoutNodeDescription parentNode,  IUserLayoutNodeDescription node, IUserLayoutNodeDescription oldParentNode) {
        super(source, parentNode, node);
        this.oldParentNode=oldParentNode;
    }
    
    /**
     * Obtain the OLD parent node. This is the node
     * to which a given node used to be attached.
     * 
     * @return the old parent node
     */
    public IUserLayoutNodeDescription getOldParentNode() {
        return this.oldParentNode;
    }
    
    /**
     * Obtain an id of an OLD parent node.  This is the node
     * to which a given node used to be attached.
     *
     * @return a <code>String</code> node id
     */
    public String getOldParentNodeId() {
        return this.oldParentNode.getId();
    }
}

