/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.alm;

import java.util.Map;
import java.util.Collection;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.IUserLayoutStore;

/**
 * IAggregatedUserLayoutStore defines the base methods working with aggregated user layout store.
 *
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to reflect that it is part of Aggregated
 * Layouts.
 *
 * @author Michael Ivanov
 * @version $Revision$
 */
public interface IAggregatedUserLayoutStore extends IUserLayoutStore {


   /**
     * Add the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>ALNode</code> object specifying the node
     * @return a <code>ALNode</code> object specifying the node with the generated node ID
     * @exception PortalException if an error occurs
     */
    public ALNode addUserLayoutNode (IPerson person, UserProfile profile, ALNode node) throws PortalException;

 
    /**
     * Returns the user layout internal representation.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @return a <code>IAggregatedLayout</code> object containing the internal representation of the user layout
     * @exception PortalException if an error occurs
     */

    public IAggregatedLayout getAggregatedLayout (IPerson person, UserProfile profile) throws PortalException;

    /**
     * Persists user layout document.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param layout a <code>IAggregatedLayout</code> containing an aggregated user layout
     * @exception PortalException if an error occurs
     */
    public void setAggregatedLayout (IPerson person, UserProfile  profile, IAggregatedLayout layout) throws PortalException;


    /** 
     * Persists the fragment
     * @param person an <code>IPerson</code> object specifying the user
     * @param layoutImpl a <code>ILayoutFragment</code> object containing a fragment
     * @param userPrefs a <code>UserPreferences</code> object containing user preferences for column width persistance
     * @exception PortalException if an error occurs
     */
    public void setFragment (IPerson person, ILayoutFragment layoutImpl, UserPreferences userPrefs ) throws PortalException;

    /**
     * Returns the layout fragment as a user layout.
     * @param person an <code>IPerson</code> object specifying the user
     * @param fragmentId a fragment ID
     * @return a <code>ILayoutFragment</code> object containing the internal representation of the user fragment
     * @exception PortalException if an error occurs
     */
    public ILayoutFragment getFragment (IPerson person, String fragmentId) throws PortalException;
    
	/**
		 * Deletes the fragment that has been loaded as a layout.
		 * @param person an <code>IPerson</code> object specifying the user
		 * @param fragmentId a fragment ID
		 * @exception PortalException if an error occurs
		 */
	public void deleteFragment (IPerson person, String fragmentId) throws PortalException;

     /**
     * Returns the fragment IDs/names which the user is an owner of
     * @param person an <code>IPerson</code> object specifying the user
     * @return a <code>Map</code> object containing the fragment IDs
     * @exception PortalException if an error occurs
     */
    public Map getFragments (IPerson person) throws PortalException;
    
	/**
	  * Returns the list of Ids of the fragments that the user can subscribe to
	  * @param person an <code>IPerson</code> object specifying the user
	  * @return <code>Collection</code> a set of the fragment IDs
	  * @exception PortalException if an error occurs
	  */
    public Collection getSubscribableFragments(IPerson person) throws PortalException;
    
	/**
		* Returns the user group keys which the fragment is published to
		* @param person an <code>IPerson</code> object specifying the user
		* @param fragmentId a <code>String</code> value
		* @return a <code>Collection</code> object containing the group keys
		* @exception PortalException if an error occurs
		*/
    public Collection getPublishGroups (IPerson person, String fragmentId ) throws PortalException;
    
	/**
		  * Persists the user groups which the fragment is published to
		  * @param groups an array of <code>IGroupMember</code> objects
		  * @param person an <code>IPerson</code> object specifying the user
		  * @param fragmentId a <code>String</code> value
		  * @exception PortalException if an error occurs
		  */
	public void setPublishGroups ( IGroupMember[] groups, IPerson person, String fragmentId ) throws PortalException;


    /**
     * Returns the next fragment ID
     *
     * @return a <code>String</code> next fragment ID
     * @exception PortalException if an error occurs
     */
    public String getNextFragmentId() throws PortalException;
    
	/**
			* Returns the priority range defined for the given user group
			* @param groupKey a <code>String</code> group key
			* @return a int array containing the min and max priority values
			* @exception PortalException if an error occurs
			*/
    public int[] getPriorityRange ( String groupKey ) throws PortalException;

}
