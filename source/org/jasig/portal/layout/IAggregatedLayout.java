/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import java.util.Set;
import org.jasig.portal.PortalException;

/**
 * An aggregated-layout specific extension of the user layout interface
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.1
 */
public interface IAggregatedLayout extends IUserLayout {

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
     * @return a <code>Set</code> of <code>String</code> fragment Ids.
     * @exception PortalException if an error occurs
     */
    public Set getFragmentIds() throws PortalException;

    /**
     * Returns an fragment Id for a given node.
     * Returns null if the node is not part of any fragments.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> fragment Id
     * @exception PortalException if an error occurs
     */
    public String getFragmentId(String nodeId) throws PortalException;

    /**
     * Returns an fragment root Id for a given fragment.
     *
     * @param fragmentId a <code>String</code> value
     * @return a <code>String</code> fragment root Id
     * @exception PortalException if an error occurs
     */
    public String getFragmentRootId(String fragmentId) throws PortalException;
    
    /**
     * Returns the node by a given node ID.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>ALNode</code> instance
     */
    public ALNode getLayoutNode(String nodeId);
   
}
