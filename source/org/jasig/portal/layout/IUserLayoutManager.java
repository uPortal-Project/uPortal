package org.jasig.portal.layout;

import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.xml.sax.ContentHandler;


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
     * @exception PortalException if an error occurs
     */
    public void moveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException;

    /**
     * Delete a node (folder or a channel) from a user layout.
     *
     * @param nodeId a <code>String</code> id (channel subscribe id or folder id)
     * @exception PortalException if an error occurs
     */
    public void deleteNode(String nodeId) throws PortalException;

    /**
     * Update a given node.
     *
     * @param node an <code>UserLayoutNodeDescription</code> value with a valid id.
     * @exception PortalException if an error occurs
     */
    public void updateNode(UserLayoutNodeDescription node) throws PortalException;


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


}
