/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

/**
 * A basic user layout event, involving just one node.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public class LayoutEvent extends java.util.EventObject {
    protected IUserLayoutNodeDescription node;


    /**
     * Construct a <code>LayoutEvent</code> from a given source,
     * and a given node.
     *
     * @param source an <code>Object</code> that generated the event
     * @param node an <code>UserLayoutNodeDescription</code> of the node that was involved
     */
    public LayoutEvent(Object source,IUserLayoutNodeDescription node) {
        super(source);
        this.node=node;
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

