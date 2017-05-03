/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout;

import java.util.Enumeration;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import org.apereo.portal.PortalException;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType;
import org.w3c.dom.Document;

/**
 * An interface for abstracting operations performed on the user layout.
 */
public interface IUserLayoutManager {
    /** Represents a <folder> layout element */
    public static final String FOLDER = "folder";
    /** Represents <channel> layout element */
    public static final String CHANNEL = "channel";
    /** Represents <channel-header> layout element */
    public static final String CHANNEL_HEADER = "channel-header";
    /** Represents <parameter> layout element */
    public static final String PARAMETER = "parameter";
    /** Represents an ID element attribute */
    public static final String ID_ATTR = "ID";

    public static final QName ID_ATTR_NAME = new QName(ID_ATTR);

    /**
     * Gets a user layout (with appropriate markings).
     *
     * @return the user layout
     * @exception PortalException if an error occurs
     */
    public IUserLayout getUserLayout() throws PortalException;

    /** @return An XMLEventReader for the user's layout structure */
    public XMLEventReader getUserLayoutReader();

    /** Calls {@link #loadUserLayout(boolean)} passing false. */
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

    /** @return The set of all channels the user is subscribed to */
    public Set<String> getAllSubscribedChannels();

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
     * @param node an <code>UserLayoutNodeDescription</code> value of a node to be added (Id doesn't
     *     have to be set)
     * @param parentId a <code>String</code> id of a folder to which the new node (channel or
     *     folder) should be added.
     * @param nextSiblingId a <code>String</code> an id of a sibling node (channel or folder) prior
     *     to which the new node should be inserted.
     * @return an <code>UserLayoutNodeDescription</code> value with a newly determined Id.
     * @exception PortalException if an error occurs
     */
    public IUserLayoutNodeDescription addNode(
            IUserLayoutNodeDescription node, String parentId, String nextSiblingId)
            throws PortalException;

    /**
     * Move a node (channel or folder) from one location to another.
     *
     * @param nodeId a <code>String</code> value of a node Id.
     * @param parentId a <code>String</code> id of a folder to which the node should be moved.
     * @param nextSiblingId a <code>String</code> id of a sibling node (folder or channel) prior to
     *     which the node should be placed. (<code>null</code> to append at the end)
     * @return a <code>boolean</code> value noting if the operation was successful
     * @exception PortalException if an error occurs
     */
    public boolean moveNode(String nodeId, String parentId, String nextSiblingId)
            throws PortalException;

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
     * @param nextSiblingId a <code>String</code> id of a sibling prior to which the node to be
     *     inserted. (<code>null</code> to append at the end)
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    public boolean canAddNode(
            IUserLayoutNodeDescription node, String parentId, String nextSiblingId)
            throws PortalException;

    /**
     * Test if a particular node can be moved to a given location.
     *
     * @param nodeId a <code>String</code> id of a node to be moved.
     * @param parentId a <code>String</code> id of a parent to which the node to be moved.
     * @param nextSiblingId a <code>String</code> id of a sibling prior to which the node is to be
     *     inserted (<code>null</code> to append at the end)
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    public boolean canMoveNode(String nodeId, String parentId, String nextSiblingId)
            throws PortalException;

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
     * Ask manager to output markings at the locations where a given node can be added. The marks
     * will appear next time <code>getUserLayout</code> method is called.
     *
     * @param node an <code>UserLayoutNodeDescription</code> value or <code>null</code> to stop
     *     outputting add markings.
     * @exception PortalException if an error occurs
     */
    public void markAddTargets(IUserLayoutNodeDescription node) throws PortalException;

    /**
     * Ask manager to output markings at the locations where a given node can be moved. The marks
     * will appear next time <code>getUserLayout</code> method is called.
     *
     * @param nodeId a <code>String</code> value or <code>null</code> to stop outputting move
     *     markings.
     * @exception PortalException if an error occurs
     */
    public void markMoveTargets(String nodeId) throws PortalException;

    /**
     * Returns an Id of a parent user layout node. The user layout root node always has ID= {@link
     * IUserLayout#ROOT_NODE_NAME}
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
     * @return a <code>String</code> Id value of a next sibling node, or <code>null</code> if this
     *     is the last sibling.
     * @exception PortalException if an error occurs
     */
    public String getNextSiblingId(String nodeId) throws PortalException;

    /**
     * Determine an Id of a previous sibling node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> Id value of a previous sibling node, or <code>null</code> if
     *     this is the first sibling.
     * @exception PortalException if an error occurs
     */
    public String getPreviousSiblingId(String nodeId) throws PortalException;

    /**
     * Return a cache key, uniquely corresponding to the composition and the structure of the user
     * layout.
     *
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
    public String getCacheKey() throws PortalException;

    /**
     * @deprecated {@link #getUserLayout()} should be used instead. Direct manipulation of the DOM
     *     can cause caching problems and issues where the layout manager doesn't know about DOM
     *     changes. Read https://lists.wisc.edu/read/messages?id=2167043 for more information.
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
     * @param fname the functional name to lookup
     * @return a <code>String</code> subscription id
     */
    public String getSubscribeId(String fname) throws PortalException;

    /**
     * Returns a subscription id using the supplied functional name if it exists under the specified
     * parent folder
     *
     * @param parentFolderId The id of the parent folder to look under
     * @param fname the functional name to lookup
     * @return a <code>String</code> subscription id, null if the fname does not exist under the
     *     specified folder id.
     */
    public String getSubscribeId(String parentFolderId, String fname);

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
    public IUserLayoutNodeDescription createNodeDescription(LayoutNodeType nodeType)
            throws PortalException;
}
