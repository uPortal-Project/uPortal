/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import java.util.Set;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.IUserLayout;
import org.jasig.portal.layout.al.common.node.INodeId;

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
     * @return a <code>Set</code> of <code>IFragmentId</code> fragment Ids.
     * @exception PortalException if an error occurs
     */
    public Set getFragmentIds() throws PortalException;

    /**
     * Returns an fragment Id for a given node.
     * Returns null if the node is not part of any fragments.
     *
     * @param nodeId a node id
     * @return a fragment id
     * @exception PortalException if an error occurs
     */
    public IFragmentId getFragmentId(INodeId nodeId) throws PortalException;

    /**
     * Returns an fragment root Id for a given fragment.
     *
     * @param fragmentId a fragment id
     * @return a fragment root node id
     * @exception PortalException if an error occurs
     */
    public IFragmentLocalNodeId getFragmentRootId(IFragmentId fragmentId) throws PortalException;
    
    /**
     * Returns the node by a given node id.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>ALNode</code> instance
     */
    public IALNode getLayoutNode(INodeId nodeId);
    
    /**
     * Returns the folder by a given folder ID.
     *
     * @param folderId a folder node id
     * @return a <code>ALFolder</code> instance
     */
    public IALFolder getLayoutFolder(INodeId folderId);
    
    /**
     * Gets the tree depth for a given node
     * @param nodeId a node id
     * @return a depth value
     * @exception PortalException if an error occurs
     */
    public int getDepth(INodeId nodeId) throws PortalException;
   
}
