package org.jasig.portal.layout;

import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.xml.sax.ContentHandler;
import java.util.List;

//import  org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;

/**
 * An interface for abstracting operations performed on the user layout.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public interface IUserLayoutManager {
    /**
     * Output user layout (with appropriate markings) into
     * a <code>ContentHandler</code>
     *
     * @param ch a <code>ContentHandler</code> value
     * @exception PortalException if an error occurs
     */
    public void getUserLayout(ContentHandler ch) throws PortalException ;

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
     * Signal manager to load a user layout from a database
     *
     * @exception PortalException if an error occurs
     */
    public void loadUserLayout() throws PortalException;

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
    public UserLayoutNodeDescription getNode(String nodeId) throws PortalException;

    /**
     * Add a new node to a current user layout.
     *
     * @param node an <code>UserLayoutNodeDescription</code> value of a node to be added (Id doesn't have to be set)
     * @param parentId a <code>String</code> id of a folder to which the new node (channel or folder) should be added.
     * @param nextSiblingId a <code>String</code> an id of a sibling node (channel or folder) prior to which the new node should be inserted.
     * @return an <code>UserLayoutNodeDescription</code> value with a newly determined Id.
     * @exception PortalException if an error occurs
     */
    public UserLayoutNodeDescription addNode(UserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException;


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
    public boolean updateNode(UserLayoutNodeDescription node) throws PortalException;


    /**
     * Test if a particular node can be added at a given location.
     *
     * @param node an <code>UserLayoutNodeDescription</code> value describing the node to be added.
     * @param parentId a <code>String</code> id of a parent to which the node to be added.
     * @param nextSiblingId a <code>String</code> id of a sibling prior to which the node to be inserted. (<code>null</code> to append at the end)
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    public boolean canAddNode(UserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException;

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
     * @param nodeId a <code>String</code> node id.
     * @return a <code>boolean</code> value
     * @exception PortalException if an error occurs
     */
    public boolean canUpdateNode(String nodeId) throws PortalException;

    /**
     * Ask manager to output markings at the locations where a given node can be added.
     * The marks will appear next time {@link getUserLayout} method is called.
     *
     * @param node an <code>UserLayoutNodeDescription</code> value or <code>null</code> to stop outputting add markings.
     */
    public void markAddTargets(UserLayoutNodeDescription node);


    /**
     * Ask manager to output markings at the locations where a given node can be moved.
     * The marks will appear next time {@link getUserLayout} method is called.
     *
     * @param nodeId a <code>String</code> value or <code>null</code> to stop outputting move markings.
     * @exception PortalException if an error occurs
     */
    public void markMoveTargets(String nodeId) throws PortalException;

    /**
     * Returns an Id of a parent user layout node.
     * The user layout root node always has ID="root"
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
    public String getParentId(String nodeId) throws PortalException;

    /**
     * Determine a list of child node Ids for a given node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>List</code> of <code>String</code> child node Ids.
     * @exception PortalException if an error occurs
     */
    public List getChildIds(String nodeId) throws PortalException;

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


    //  This method should be removed whenever it becomes possible
    public Document getUserLayoutDOM() throws PortalException;

    /**
     * Returns a layout Id associated with this manager/
     *
     * @return an <code>int</code> layout Id value;
     */
    public int getLayoutId();

}
