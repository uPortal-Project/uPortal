/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import java.util.Set;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.ILayout;
import org.jasig.portal.layout.al.common.node.INode;

/**
 * An aggregated-layout specific extension of the user layout interface
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.1
 */
public interface IAggregatedLayout extends ILayout {

    // the tag names constants
    public static final String LAYOUT = "layout";
    public static final String FRAGMENT = "fragment";
    public static final String FOLDER = "folder";
    public static final String CHANNEL = "channel";
    public static final String PARAMETER = "parameter";
    public static final String RESTRICTION = "restriction";
    // The names for marking nodes
    public static final String ADD_TARGET = "add_target";
    public static final String MOVE_TARGET = "move_target";


    /**
     * Returns a list of fragment Ids existing in the layout.
     *
     * @return a <code>Set</code> of <code>IFragmentId</code> fragment Ids.
     * @exception PortalException if an error occurs
     */
    public Set getFragmentIds() throws PortalException;
    
    /**
     * Import a subtree by constructing a local copy with
     * assigned ids.
     * @param node
     * @return a local aggregated layout node copy with assigned ids
     */
    public IALNode importNodes(INode node) throws PortalException;
    
}
