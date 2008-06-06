/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import java.util.EventObject;

import org.jasig.portal.layout.node.IUserLayoutNodeDescription;

/**
 * A basic user layout event, involving just one node.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0
 */
public class LayoutEvent extends EventObject {
    private static final long serialVersionUID = 1L;

    protected final IUserLayoutNodeDescription parentNode;
    protected final IUserLayoutNodeDescription node;
    


    /**
     * Construct a <code>LayoutEvent</code> from a given source,
     * and a given node.
     *
     * @param source an <code>Object</code> that generated the event
     * @param node an <code>UserLayoutNodeDescription</code> of the node that was involved
     */
    public LayoutEvent(Object source, IUserLayoutNodeDescription parentNode, IUserLayoutNodeDescription node) {
        super(source);
        this.parentNode = parentNode;
        this.node = node;
    }
    
    /**
     * Obtain a description of the parent of the node involved in the event
     * 
     * @return the parentNode
     */
    public IUserLayoutNodeDescription getParentNode() {
        return parentNode;
    }

    /**
     * Obtain a description of a node involved in the event.
     *
     * @return an <code>UserLayoutNodeDescription</code> value
     */
    public IUserLayoutNodeDescription getNodeDescription() {
        return this.node;
    }
}

