/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import org.jasig.portal.StructureStylesheetDescription;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.IUserLayoutStore;

/**
 * A base class for UserLayoutStore mock objects that does nothing.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0
 */

public class UserLayoutStoreMock implements IUserLayoutStore {
    /**
     * UserLayoutStoreMock constructor comment.
     */
    public UserLayoutStoreMock() {
	super();
    }
    /** Registers new structure stylesheet with the portal database
     *
     * @param stylesheetDescriptionURI Location of the stylesheet description XML file
     * @param stylesheetURI Location of the actual stylesshet XML file
     * @return id assigned to the stylesheet or null if the operation failed
     */
    public Integer addStructureStylesheetDescription(String stylesheetDescriptionURI, String stylesheetURI) throws Exception {
	return null;
    }
    /** add a new system profile to the database. During this process, a new profile id will be assigned to the profile.
     *
     * @param profile profile object (profile id within will be overwritten)
     * @return profile with an newly assigned id
     */
    public UserProfile addSystemProfile(UserProfile profile) throws Exception {
	return null;
    }
    /** Registers a new theme stylesheet with the portal databases
     *
     * @param stylesheetDescriptionURI Location of the stylesheet description
     *     XML file
     * @param stylesheetURI Location of the actual stylesheet XML file
     * @return id assigned to the stylesheet or null if the operation failed
     */
    public Integer addThemeStylesheetDescription(String stylesheetDescriptionURI, String stylesheetURI) throws Exception {
	return null;
    }
    /**
     * Creates a new user profile in the database.
     * In the process, new profileId is assigned to the profile
     *
     * @param person User
     * @param profile profile object (profile id in this object will be
     *     overwritten)
     * @return profile object with the profile id set to the newly generated
     *     id
     */
    public UserProfile addUserProfile(org.jasig.portal.security.IPerson person, UserProfile profile) throws Exception {
	return null;
    }
    /** remove system profile from the database
     *
     * @param profileId profile id
     */
    public void deleteSystemProfile(int profileId) throws Exception {}
    /** remove user profile from the database
     *
     * @param person User
     * @param profileId profile id
     */
    public void deleteUserProfile(org.jasig.portal.security.IPerson person, int profileId) throws Exception {}
    /* ChannelRegistry */
    public String generateNewChannelSubscribeId(org.jasig.portal.security.IPerson person) throws Exception {
	return null;
    }
    /**
     * Generate a folder id for a folder being added to the user layout
     *
     * @param person an <code>IPerson</code> value
     * @return a <code>String</code> value
     * @exception Exception if an error occurs
     */
    public String generateNewFolderId(org.jasig.portal.security.IPerson person) throws Exception {
	return null;
    }
    /** Obtains a list of mime types available on the installation
     *
     * @return Returns a hasbtale mapping mime type strings to their word
     *     descriptions (simple String)
     */
    public java.util.Hashtable getMimeTypeList() throws Exception {
	return null;
    }
    /** Obtains a complete description of the structure stylesheet
     *
     * @param stylesheetId id of the structure stylesheet
     * @return a description of the structure stylesheet
     */
    public StructureStylesheetDescription getStructureStylesheetDescription(int stylesheetId) throws Exception {
	return null;
    }
    /**
     * Obtain a list of strcture stylesheet descriptions registered on the system
     * @return a <code>Hashtable</code> mapping stylesheet id (<code>Integer</code> objects) to {@link StructureStylesheetDescription} objects
     * @exception Exception
     */
    public java.util.Hashtable getStructureStylesheetList() throws Exception {
	return null;
    }
    /**
     *  CoreStylesheetDescription
     */
    public java.util.Hashtable getStructureStylesheetList(String mimeType) throws Exception {
	return null;
    }
    /** Obtain structure stylesheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param stylesheetId structure stylesheet id
     * @return structure stylesheet user preferences. null is returned only if userId, profileId or stylesheet with an appropriate name do not exist. If all of the parameters are valid, but the user does not have any user preference settings associated with this stylesheet, return contains stylesheet preference object filled in with the defaults defined in stylesheet description.
     */
    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(org.jasig.portal.security.IPerson person, int profileId, int stylesheetId) throws Exception {
	return null;
    }
    /** retreive a system profile associated with a given browser
     *
     * @param userAgent User-Agent header string
     * @return profile object
     */
    public UserProfile getSystemProfile(String userAgent) throws Exception {
	return null;
    }
    /** Obtain a system profile
     * @param profileId system profile id
     */
    public UserProfile getSystemProfileById(int profileId) throws Exception {
	return null;
    }
    /** obtain a list of system profiles
     *
     * @return a <code>Hashtable</code> mapping system profile ids (<code>Integer</code> objects) to the {@link UserProfile} objects
     */
    public java.util.Hashtable getSystemProfileList() throws Exception {
	return null;
    }
    /** Obtains a complete description of a theme stylesheet
     *
     * @param stylesheetId id of a theme stylesheet
     * @return a description of a theme stylesheet
     */
    public ThemeStylesheetDescription getThemeStylesheetDescription(int stylesheetId) throws Exception {
	return null;
    }
    /**
     * Obtain a list of theme stylesheet descriptions registered on the system
     * @return a <code>Hashtable</code> mapping stylesheet id (<code>Integer</code> objects) to {@link ThemeStylesheetDescription} objects
     * @exception Exception
     */
    public java.util.Hashtable getThemeStylesheetList() throws Exception {
	return null;
    }
    /** Obtains a list of theme stylesheets available for a particular structure stylesheet.
     *
     * @param structureStylesheetId id of the structure stylehsset
     * @return a <code>Hashtable</code> mapping stylesheet id (<code>Integer</code> objects) to {@link ThemeStylesheetDescription} objects
     */
    public java.util.Hashtable getThemeStylesheetList(int structureStylesheetId) throws Exception {
	return null;
    }
    /** Obtain theme stylesheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param stylesheetId theme stylesheet id
     * @return theme stylesheet user preferences. null is returned only if userId, profileId or stylesheet with an appropriate name do not exist. If all of the parameters are valid, but the user does not have any user preference settings associated with this stylesheet, return contains stylesheet preference object filled in with the defaults defined in stylesheet description.
     */
    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(org.jasig.portal.security.IPerson person, int profileId, int stylesheetId) throws Exception {
	return null;
    }
    /**
     * Retreive a user layout document.
     *
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile 
     * @return a <code>Document</code> containing user layout (conforms to userLayout.dtd)
     * @exception Exception if an error occurs
     */
    public org.w3c.dom.Document getUserLayout(org.jasig.portal.security.IPerson person, UserProfile profile) throws Exception {
	return null;
    }
    /** Retreive the entire UserPreferences object
     *
     * @param person User
     * @param profile profile
     * @return user preferences
     */
    public UserPreferences getUserPreferences(org.jasig.portal.security.IPerson person, UserProfile profile) throws Exception {
	return null;
    }
    /** Obtain user profile associated with a particular browser
     *
     * @param person User
     * @param userAgent User-Agent header string
     * @return user profile or <code>null</code> if no user profiles are associated with the given user agent.
     */
    public UserProfile getUserProfile(org.jasig.portal.security.IPerson person, String userAgent) throws Exception {
	return null;
    }
    /**  Obtains a user profile by profile id.    
     * @param person an <code>IPerson</code> object representing the user 
     * @param profileId profile id
     */
    public UserProfile getUserProfileById(org.jasig.portal.security.IPerson person, int profileId) throws Exception {
	return null;
    }
    /** retreive a list of profiles associated with a user
     *
     * @param person User
     * @return a <code>Hashtable</code> mapping user profile ids (<code>Integer</code> objects) to the {@link UserProfile} objects
     */
    public java.util.Hashtable getUserProfileList(org.jasig.portal.security.IPerson person) throws Exception {
	return null;
    }
    /** save user preferences
     *
     * @param person User
     * @param up user preferences object
     */
    public void putUserPreferences(org.jasig.portal.security.IPerson person, UserPreferences up) throws Exception {}
    /** removes stylesheet description
     *
     * @param stylesheetId id of the stylesheet
     */
    public void removeStructureStylesheetDescription(int stylesheetId) throws Exception {}
    /** Removes theme stylesheet
     *
     * @param stylesheetId id of the stylesheet
     */
    public void removeThemeStylesheetDescription(int stylesheetId) throws Exception {}
    /** Save structure stylesheet user pferences
     *
     * @param person User
     * @param profileId profile id
     * @param fsup structure stylesheet user preferences
     */
    public void setStructureStylesheetUserPreferences(org.jasig.portal.security.IPerson person, int profileId, StructureStylesheetUserPreferences fsup) throws Exception {}
    /** establish system profile browser mapping
     *
     * @param userAgent User-Agent header string
     * @param systemProfileId profile id of a profile to which given
     *     user-agent will be mapped
     */
    public void setSystemBrowserMapping(String userAgent, int systemProfileId) throws Exception {}
    /** Save theme stylesheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param ssup structure stylesheet user preferneces
     */
    public void setThemeStylesheetUserPreferences(org.jasig.portal.security.IPerson person, int profileId, ThemeStylesheetUserPreferences ssup) throws Exception {}
    /** establish a browser - user profile mapping
     *
     * @param person User
     * @param userAgent User-Agent header string
     * @param profileId profile id to which given user agent will be mapped
     */
    public void setUserBrowserMapping(org.jasig.portal.security.IPerson person, String userAgent, int profileId) throws Exception {}
    /**
     * Persist user layout document.
     *
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param layoutXML a <code>Document</code> containing user layout (conforming to userLayout.dtd)
     * @param channelsAdded a boolean flag specifying if new channels have been added to the current user layout (for performance optimization purposes)
     * @exception Exception if an error occurs
     */
    public void setUserLayout(org.jasig.portal.security.IPerson person, UserProfile profile, org.w3c.dom.Document layoutXML, boolean channelsAdded) throws Exception {}
    /**
     * Updates an existing structure stylesheet description.
     * @param stylesheetDescriptionURI Location of the stylesheet description XML file
     * @param stylesheetURI Location of the actual stylesshet XML file
     * @param stylesheetId the id of the existing stylesheet description
     * @return true if the update successful
     */
    public boolean updateStructureStylesheetDescription(String stylesheetDescriptionURI, String stylesheetURI, int stylesheetId) {
	return false;
    }
    /** update system profile
     *
     * @param profile profile object
     */
    public void updateSystemProfile(UserProfile profile) throws Exception {}
    /**
     * Updates an existing theme stylesheet description.
     * @param stylesheetDescriptionURI Location of the stylesheet description XML file
     * @param stylesheetURI Location of the actual stylesshet XML file
     * @param stylesheetId the id of the existing stylesheet description
     * @return true if the update successful
     */
    public boolean updateThemeStylesheetDescription(String stylesheetDescriptionURI, String stylesheetURI, int stylesheetId) throws Exception {
	return false;
    }
    /** update user profile
     *
     * @param person User
     * @param profile profile update
     */
    public void updateUserProfile(org.jasig.portal.security.IPerson person, UserProfile profile) throws Exception {}
}
