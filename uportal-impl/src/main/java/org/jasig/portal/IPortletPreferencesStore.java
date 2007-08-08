/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.apache.pluto.om.common.PreferenceSet;

/**
 * The IPortletPreferenceStore allows a portlet to atomicly persist preferences at two
 * different levels. Definition level preferences are common to all instances of a
 * channel. Entity level preferences are specific for on a per user per instance basis.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ 
 */
public interface IPortletPreferencesStore {
    /**
     * Stores the definition level preferences described by the PreferenceSet 
     * interface. The store operation is atomic, it can be assumed that if the 
     * method completes with no exception being thrown the store was successfull. 
     * If an exception is thrown it can be assumed that no changes were made to 
     * the underlying persistant store.
     * 
     * @param chanId The id of the channel to store the preferences for.
     * @param prefs The PreferenceSet which describes the data to store.
     * @throws Exception If any error occurs while storing the data.
     */
    public abstract void setDefinitionPreferences(final int chanId, final PreferenceSet prefs) throws Exception;
    
    /**
     * Gets the definition level preferences for the specified channel into an
     * implementation of the PreferenceSet interface. 
     * 
     * @param chanId The id of the channel to get the preferences for.
     * @return An implementation of the PreferenceSet interface that contains the preferences.
     * @throws Exception If any error occurs while getting the data.
     */
    public abstract PreferenceSet getDefinitionPreferences(final int chanId) throws Exception;
    
    /**
     * Stores the entity level preferences described by the PreferenceSet 
     * interface. The store operation is atomic, it can be assumed that if the 
     * method completes with no exception being thrown the store was successfull. 
     * If an exception is thrown it can be assumed that no changes were made to 
     * the underlying persistant store. The userId, layoutId and structId make up
     * the primary key for the preferences.
     * 
     * @param userId The id of the user to store the preferences for.
     * @param layoutId The layout fragment id to store the preferences for.
     * @param chanDescId The structure id to store the preferences for.
     * @param prefs The PreferenceSet which describes the data to store.
     * @throws Exception If any error occurs while storing the data.
     */
    public abstract void setEntityPreferences(final int userId, final int layoutId, final String chanDescId, final PreferenceSet prefs) throws Exception;

    /**
     * Gets the enitity level preferences for the specified channel into an
     * implementation of the PreferenceSet interface. The userId, layoutId and
     * structId make up the primary key for the preferences.
     *  
     * @param userId The id of the user to get the preferences for.
     * @param layoutId The layout fragment id to get the preferences for.
     * @param chanDescId The structure id to get the preferences for.
     * @return An implementation of the PreferenceSet interface that contains the preferences.
     * @throws Exception If any error occurs while getting the data.
     */
    public abstract PreferenceSet getEntityPreferences(final int userId, final int layoutId, final String chanDescId) throws Exception;
    
    /**
     * Removes all portlet preferences stored for the specified user.
     * 
     * @param userId The id of the user to remove the preferences for.
     * @throws Exception If any error occurs while removing the data.
     */
    public abstract void deletePortletPreferencesByUser(int userId) throws Exception;
    
    /**
     * Removes portlet preferences for the specific user and instance of a portlet.
     * 
     * @param userId The id of the user to remove the preferences for.
     * @param layoutId The layout fragment id to remove the preferences for.
     * @param chanDescId The structure id to remove the preferences for.
     * @throws Exception If any error occurs while removing the data.
     */
    public abstract void deletePortletPreferencesByInstance(final int userId, final int layoutId, final String chanDescId) throws Exception;
}
