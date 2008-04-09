/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.PortalException;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * An interface for abstracting operations performed on the user layout.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.1
 */
public interface IUserLayoutManager {
    /**
     * Gets a user layout (with appropriate markings).
     *
     * @return the user layout
     * @exception PortalException if an error occurs
     */
    public IUserLayout getUserLayout() throws PortalException;

    /**
     * Sets a user layout (with appropriate markings).
     *
     * @param userLayout the user layout
     * @exception PortalException if an error occurs
     */
    public void setUserLayout(IUserLayout userLayout) throws PortalException;

    /**
     * Output user layout (with appropriate markings) into
     * a <code>ContentHandler</code>
     *
     * @param ch a <code>ContentHandler</code> value
     * @exception PortalException if an error occurs
     */
    public void getUserLayout(ContentHandler ch) throws PortalException;

    /**
     * Output subtree of a user layout (with appropriate markings) defined by a particular node into
     * a <code>ContentHandler</code>
     *
     * @param nodeId a <code>String</code> a node determining a user layout subtree.
     * @param ch a <code>ContentHandler</code> value
     * @exception PortalException if an error occurs
     */
    public void getUserLayout(String nodeId, ContentHandler ch) throws PortalException;

    /**
     * Set a user layout store implementation.
     *
     * @param ls an <code>IUserLayoutStore</code> value
     */
    public void setLayoutStore(IUserLayoutStore ls);

    /**
     * Calls {@link #loadUserLayout(boolean)} passing false.
     */
    public void loadUserLayout() throws PortalException;
    
    /**
     * Signal manager to load a user layout.
     * 
     * @param reload If true the reload will be forced, purging any cached data.
     * @throws PortalException PortalException if an error occurs
     */
    public void loadUserLayout(boolean reload) throws PortalException;

    /**
     * Signal manager to persist user layout to a database
     *
     * @exception PortalException if an error occurs
     */
    public void saveUserLayout() throws PortalException;


    /**
     * Obtain a description of a node (channel or a folder) in a given user layout.
     *
     * @param nodeId a <code>String</code> channel subscribe id or folder id.
     * @return an <code>UserLayoutNodeDescription</code> value
     * @exception PortalException if an error occurs
     */
    public IUserLayoutNodeDescription getNode(String nodeId) throws PortalException;

    /**
     * Add a new node to a current user layout.
     *
     * @param node an <code>UserLayoutNodeDescription</code> value of a node to be added (Id doesn't have to be set)
     * @param parentId a <code>String</code> id of a folder to which the new node (channel or folder) should be added.
     * @param nextSiblingId a <code>String</code> an id of a sibling node (channel or folder) prior to which the new node should be inserted.
     * @return an <code>UserLayoutNodeDescription</code> value with a newly determined Id.
     * @exception PortalException if an error occurs
     */
    public IUserLayoutNodeDescription addNode(IUserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException;


    /**
     * Move a node (channel or folder) from one location to another.
     *
     * @param nodeId a <code>String</code> value of a node Id.
     * @param parentId a <code>String</code> id of a folder to which the node should be moved.
     * @param nextSiblingId a <code>String</code> id of a sibling node (folder or channel) prior to which the node should be placed. (<code>null</code> to append at the end)
     * @return a <code>boolean</code> value noting if the operation was successful
     * @exception PortalException if an error occurs
     */
    public boolean moveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException;

    /**
     * Delete a node (folder or a channel) from a user layout.
     *
     * @param nodeId a <code>String</code> id (channel subscribe id or folder id)
     * @return a <code>boolean</code> value noting if the operation was successful
     * @exception PortalException if an error occurs
     */
    public boolean deleteNode(String nodeId) throws PortalException;

    /**
     * Update a given node.
     *
     * @param node an <code>UserLayoutNodeDescription</code> value with a valid id.
     * @return a <code>boolean</code> value noting if the operation was successful
     * @exception PortalException if an error occurs
     */
    public boolean updateNode(IUserLayoutNodeDescription node) throws PortalException;


    /**
     * Test if a particular node can be added at a given location.
     *
     * @param node an <code>UserLayoutNodeDescription</code> value describing the node to be added.
     * @param parentId a <code>String</code> id of a parent to which the node to be added.
     * @param nextSiblingId a <code>String</code> id of a sibling prior to which the node to be inserted. (<code>null</code> to append at the end)
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    public boolean canAddNode(IUserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException;

    /**
     * Test if a particular node can be moved to a given location.
     *
     * @param nodeId a <code>String</code> id of a node to be moved.
     * @param parentId a <code>String</code> id of a parent to which the node to be moved.
     * @param nextSiblingId a <code>String</code> id of a sibling prior to which the node is to be inserted (<code>null</code> to append at the end)
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    public boolean canMoveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException;

    /**
     * Tests if a particular node can be deleted.
     *
     * @param nodeId a <code>String</code> node id.
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    public boolean canDeleteNode(String nodeId) throws PortalException;

    /**
     * Test if a certain node can be updated.
     *
     * @param node a <code>IUserLayoutNodeDescription</code> node id.
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    public boolean canUpdateNode(IUserLayoutNodeDescription node) throws PortalException;

    /**
     * Ask manager to output markings at the locations where a given node can be added.
     * The marks will appear next time <code>getUserLayout</code> method is called.
     *
     * @param node an <code>UserLayoutNodeDescription</code> value or <code>null</code> to stop outputting add markings.
     * @exception PortalException if an error occurs
     */
    public void markAddTargets(IUserLayoutNodeDescription node) throws PortalException;


    /**
     * Ask manager to output markings at the locations where a given node can be moved.
     * The marks will appear next time <code>getUserLayout</code> method is called.
     *
     * @param nodeId a <code>String</code> value or <code>null</code> to stop outputting move markings.
     * @exception PortalException if an error occurs
     */
    public void markMoveTargets(String nodeId) throws PortalException;

    /**
     * Returns an Id of a parent user layout node.
     * The user layout root node always has ID= {@link IUserLayout#ROOT_NODE_NAME}
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
    public String getParentId(String nodeId) throws PortalException;

    /**
     * Returns a list of child node Ids for a given node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>List</code> of <code>String</code> child node Ids.
     * @exception PortalException if an error occurs
     */
    public Enumeration<String> getChildIds(String nodeId) throws PortalException;

    /**
     * Determine an Id of a next sibling node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> Id value of a next sibling node, or <code>null</code> if this is the last sibling.
     * @exception PortalException if an error occurs
     */
    public String getNextSiblingId(String nodeId) throws PortalException;


    /**
     * Determine an Id of a previous sibling node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> Id value of a previous sibling node, or <code>null</code> if this is the first sibling.
     * @exception PortalException if an error occurs
     */
    public String getPreviousSiblingId(String nodeId) throws PortalException;

    /**
     * Return a cache key, uniqly corresponding to the composition and the structure of the user layout.
     *
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
    public String getCacheKey() throws PortalException;


    /**
     * Register a layout event listener
     *
     * @param l a <code>LayoutEventListener</code> object
     * @return a <code>boolean</code> success status
     */
    public boolean addLayoutEventListener(LayoutEventListener l);


    /**
     * Remove a registered layout event listener.
     *
     * @param l a <code>LayoutEventListener</code> object
     * @return a <code>boolean</code> success status
     */
    public boolean removeLayoutEventListener(LayoutEventListener l);

    /**
     * @deprecated {@link #getUserLayout()} should be used instead. Direct manipulation of the DOM can cause caching problems and issues where the layout manager doesn't know about DOM changes. Read https://lists.wisc.edu/read/messages?id=2167043 for more information.
     */
    @Deprecated
    public Document getUserLayoutDOM() throws PortalException;

    /**
     * Returns a layout Id associated with this manager/
     *
     * @return an <code>int</code> layout Id value;
     */
    public int getLayoutId();

    /**
     * Returns a subscription id using the supplied functional name.
     *
     * @param fname  the functional name to lookup
     * @return a <code>String</code> subscription id
     */
    public String getSubscribeId(String fname) throws PortalException;

    /**
     * Returns an id of the root folder.
     *
     * @return a <code>String</code> value
     */
    public String getRootFolderId();
    
	/**
		 * Returns the depth of a node in the layout tree.
		 *
		 * @param nodeId a <code>String</code> value
		 * @return a depth value
		 * @exception PortalException if an error occurs
		 */
	public int getDepth(String nodeId) throws PortalException;

    /**
     * A factory method to create an empty <code>IUserLayoutNodeDescription</code> instance
     *
     * @param nodeType a node type constant from <code>IUserLayoutNodeDescription</code> interface
     * @return an <code>IUserLayoutNodeDescription</code> instance
     * @exception PortalException if the error occurs.
     */
    public IUserLayoutNodeDescription createNodeDescription( int nodeType ) throws PortalException;
    
    /**
     * Allows layout manager specific handling of user request parameter 
     * processing potentially including passing of specific parameters to the
     * structure and theme transformations via the user preferences object.
     */
    public void processLayoutParameters(IPerson person, 
            UserPreferences userPrefs,
            HttpServletRequest req) throws PortalException;
}
