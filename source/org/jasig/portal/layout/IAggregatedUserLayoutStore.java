package org.jasig.portal.layout;

import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;

import java.util.Hashtable;

/**
 * <p>Title: The IAggregatedUserLayoutStore interface </p>
 * <p>Description: This interface defines the base methods working with aggregated user layout store </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic</p>
 * @author Michael Ivanov
 * @version 1.0
 */

public interface IAggregatedUserLayoutStore extends IUserLayoutStore {


   /**
     * Add the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>UserLayoutNode</code> object specifying the node
     * @return a <code>UserLayoutNode</code> object specifying the node with the generated node ID
     * @exception PortalException if an error occurs
     */
    public UserLayoutNode addUserLayoutNode (IPerson Person, UserProfile profile, UserLayoutNode node ) throws PortalException;

    /**
     * Update the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>UserLayoutNode</code> object specifying the node
     * @return a boolean result of this operation
     * @exception PortalException if an error occurs
     */
    public boolean updateUserLayoutNode (IPerson Person, UserProfile profile, UserLayoutNode node ) throws PortalException;

    /**
     * Update the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param nodeId a <code>String</code> node ID specifying the node
     * @return a boolean result of this operation
     * @exception PortalException if an error occurs
     */
    public boolean deleteUserLayoutNode (IPerson Person, UserProfile profile, String nodeId ) throws PortalException;

    /**
     * Gets the user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param nodeId a <code>String</code> node ID specifying the node
     * @return a <code>UserLayoutNode</code> object
     * @exception PortalException if an error occurs
     */

    public UserLayoutNode getUserLayoutNode (IPerson person, UserProfile profile, String nodeId ) throws PortalException;

    /**
     * Returns the user layout internal representation.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @return a <code>Hashtable</code> object containing the internal representation of the user layout
     * @exception PortalException if an error occurs
     */

    public Hashtable getAggregatedUserLayout (IPerson person, UserProfile profile) throws PortalException;


}