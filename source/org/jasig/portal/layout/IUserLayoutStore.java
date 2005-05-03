/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

 package org.jasig.portal.layout;

/**
 * Interface by which portal talks to the database
 * @author George Lindholm
 * @version $Revision$
 */

import java.util.Hashtable;

import org.jasig.portal.StructureStylesheetDescription;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;

public interface IUserLayoutStore {

    /**
     * Retreive a user layout document.
     *
     * @param Person an <code>IPerson</code> object specifying the user
     * @param profile a user profile 
     * @return a <code>Document</code> containing user layout (conforms to userLayout.dtd)
     * @exception Exception if an error occurs
     */
    public Document getUserLayout (IPerson Person, UserProfile profile) throws Exception;

    /**
     * Persist user layout document.
     *
     * @param Person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param layoutXML a <code>Document</code> containing user layout (conforming to userLayout.dtd)
     * @param channelsAdded a boolean flag specifying if new channels have been added to the current user layout (for performance optimization purposes)
     * @exception Exception if an error occurs
     */
    public void setUserLayout (IPerson Person, UserProfile  profile,Document layoutXML, boolean channelsAdded) throws Exception;

    // user profiles
    /** Obtain user profile associated with a particular browser
     *
     * @param person User
     * @param userAgent User-Agent header string
     * @return user profile or <code>null</code> if no user profiles are associated with the given user agent.
     */
    public UserProfile getUserProfile (IPerson person, String userAgent) throws Exception;

    /** update user profile
     *
     * @param person User
     * @param profile profile update
     */
    public void updateUserProfile (IPerson person,UserProfile profile) throws Exception;

    /** remove user profile from the database
     *
     * @param person User
     * @param profileId profile id
     */
    public void deleteUserProfile (IPerson person,int profileId) throws Exception;

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
    public UserProfile addUserProfile (IPerson person,UserProfile profile) throws Exception;

    /**  Obtains a user profile by profile id.    
     * @param person an <code>IPerson</code> object representing the user 
     * @param profileId profile id
     */
    public UserProfile getUserProfileById (IPerson person,int profileId) throws Exception;

    /** retreive a list of profiles associated with a user
     *
     * @param person User
     * @return a <code>Hashtable</code> mapping user profile ids (<code>Integer</code> objects) to the {@link UserProfile} objects
     */
    public Hashtable getUserProfileList (IPerson person) throws Exception;

    // syste profiles
    /** retreive a system profile associated with a given browser
     *
     * @param userAgent User-Agent header string
     * @return profile object
     */
    public UserProfile getSystemProfile (String userAgent) throws Exception;

    /** update system profile
     *
     * @param profile profile object
     */
    public void updateSystemProfile (UserProfile profile) throws Exception;

    /** remove system profile from the database
     *
     * @param profileId profile id
     */
    public void deleteSystemProfile (int profileId) throws Exception;

    /** add a new system profile to the database. During this process, a new profile id will be assigned to the profile.
     *
     * @param profile profile object (profile id within will be overwritten)
     * @return profile with an newly assigned id
     */
    public UserProfile addSystemProfile (UserProfile profile) throws Exception;

    /** Obtain a system profile
     * @param profileId system profile id
     */
    public UserProfile getSystemProfileById (int profileId) throws Exception;

    /** obtain a list of system profiles
     *
     * @return a <code>Hashtable</code> mapping system profile ids (<code>Integer</code> objects) to the {@link UserProfile} objects
     */
    public Hashtable getSystemProfileList () throws Exception;

    /** establish a browser - user profile mapping
     *
     * @param person User
     * @param userAgent User-Agent header string
     * @param profileId profile id to which given user agent will be mapped
     */
    public void setUserBrowserMapping (IPerson person,String userAgent,int profileId) throws Exception;

    /** establish system profile browser mapping
     *
     * @param userAgent User-Agent header string
     * @param systemProfileId profile id of a profile to which given
     *     user-agent will be mapped
     */
    public void setSystemBrowserMapping (String userAgent,int systemProfileId) throws Exception;


    /** Retreive the entire UserPreferences object
     *
     * @param person User
     * @param profile profile
     * @return user preferences
     */
    public UserPreferences getUserPreferences (IPerson person, UserProfile profile) throws Exception;

    /** save user preferences
     *
     * @param person User
     * @param up user preferences object
     */
    public void putUserPreferences (IPerson person, UserPreferences up) throws Exception;

    /** Obtain structure stylesheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param stylesheetId structure stylesheet id
     * @return structure stylesheet user preferences. null is returned only if userId, profileId or stylesheet with an appropriate name do not exist. If all of the parameters are valid, but the user does not have any user preference settings associated with this stylesheet, return contains stylesheet preference object filled in with the defaults defined in stylesheet description.
     */
    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences (IPerson person, int profileId, int stylesheetId) throws Exception;

    /** Obtain theme stylesheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param stylesheetId theme stylesheet id
     * @return theme stylesheet user preferences. null is returned only if userId, profileId or stylesheet with an appropriate name do not exist. If all of the parameters are valid, but the user does not have any user preference settings associated with this stylesheet, return contains stylesheet preference object filled in with the defaults defined in stylesheet description.
     */
    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences (IPerson person, int profileId, int stylesheetId) throws Exception;

    /** Save structure stylesheet user pferences
     *
     * @param person User
     * @param profileId profile id
     * @param fsup structure stylesheet user preferences
     */
    public void setStructureStylesheetUserPreferences (IPerson person,int profileId, StructureStylesheetUserPreferences fsup) throws Exception;

    /** Save theme stylesheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param ssup structure stylesheet user preferneces
     */
    public void setThemeStylesheetUserPreferences (IPerson person,int profileId, ThemeStylesheetUserPreferences ssup) throws Exception;


  /* ChannelRegistry */
  /**
   * Generate an instance id for a channel being added to the user layout
   *
   * @param person an <code>IPerson</code> value
   * @return a <code>String</code> value
   * @exception Exception if an error occurs
   */
    public String generateNewChannelSubscribeId (IPerson person) throws Exception;
    
    /**
     * Generate a folder id for a folder being added to the user layout
     *
     * @param person an <code>IPerson</code> value
     * @return a <code>String</code> value
     * @exception Exception if an error occurs
     */
    public String generateNewFolderId (IPerson person) throws Exception;

  /**
   *  CoreStylesheetDescription
   */
  // functions that allow one to browse available core stylesheets in various ways
  /** Obtain a list of all structure stylesheet registered in the portal
   * that (given a proper theme stylesheet choice) can support a given mime type. 
   * Even though structure stylesheets themselves do not carry any mime type 
   * specification, the choice of available theme stylesheets determines if a certain
   * structure is available for a given mime type.
   *
   * @param mimeType mime type that should be supported
   * @return a <code>Hashtable</code> mapping stylesheet id (<code>Integer</code> objects) to {@link StructureStylesheetDescription} objects
   */
  public Hashtable getStructureStylesheetList (String mimeType) throws Exception;

  /** Obtains a list of theme stylesheets available for a particular structure stylesheet.
   *
   * @param structureStylesheetId id of the structure stylehsset
   * @return a <code>Hashtable</code> mapping stylesheet id (<code>Integer</code> objects) to {@link ThemeStylesheetDescription} objects
   */
  public Hashtable getThemeStylesheetList (int structureStylesheetId) throws Exception;


    /**
     * Obtain a list of strcture stylesheet descriptions registered on the system
     * @return a <code>Hashtable</code> mapping stylesheet id (<code>Integer</code> objects) to {@link StructureStylesheetDescription} objects
     * @exception Exception
     */
    public Hashtable getStructureStylesheetList () throws Exception;

    /**
     * Obtain a list of theme stylesheet descriptions registered on the system
     * @return a <code>Hashtable</code> mapping stylesheet id (<code>Integer</code> objects) to {@link ThemeStylesheetDescription} objects
     * @exception Exception
     */
    public Hashtable getThemeStylesheetList () throws Exception;


  /** Obtains a list of mime types available on the installation
   *
   * @return Returns a hasbtale mapping mime type strings to their word
   *     descriptions (simple String)
   */
  public Hashtable getMimeTypeList () throws Exception;



  // functions that allow access to the entire CoreStylesheetDescription object.
  // These functions are used when working with the stylesheet, and not for browsing purposes.
  /** Obtains a complete description of the structure stylesheet
   *
   * @param stylesheetId id of the structure stylesheet
   * @return a description of the structure stylesheet
   */
  public StructureStylesheetDescription getStructureStylesheetDescription (int stylesheetId) throws Exception;



  /** Obtains a complete description of a theme stylesheet
   *
   * @param stylesheetId id of a theme stylesheet
   * @return a description of a theme stylesheet
   */
  public ThemeStylesheetDescription getThemeStylesheetDescription (int stylesheetId) throws Exception;



  // functions that allow to manage core stylesheet description collection
  /** removes stylesheet description
   *
   * @param stylesheetId id of the stylesheet
   */
  public void removeStructureStylesheetDescription (int stylesheetId) throws Exception;



  /** Removes theme stylesheet
   *
   * @param stylesheetId id of the stylesheet
   */
  public void removeThemeStylesheetDescription (int stylesheetId) throws Exception;



  /** Registers new structure stylesheet with the portal database
   *
   * @param stylesheetDescriptionURI Location of the stylesheet description XML file
   * @param stylesheetURI Location of the actual stylesshet XML file
   * @return id assigned to the stylesheet or null if the operation failed
   */
  public Integer addStructureStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI) throws Exception;



  /**
   * Updates an existing structure stylesheet description.
   * @param stylesheetDescriptionURI Location of the stylesheet description XML file
   * @param stylesheetURI Location of the actual stylesshet XML file
   * @param stylesheetId the id of the existing stylesheet description
   * @return true if the update successful
   */
  public boolean updateStructureStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI, int stylesheetId);



  /**
   * Updates an existing theme stylesheet description.
   * @param stylesheetDescriptionURI Location of the stylesheet description XML file
   * @param stylesheetURI Location of the actual stylesshet XML file
   * @param stylesheetId the id of the existing stylesheet description
   * @return true if the update successful
   */
  public boolean updateThemeStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI, int stylesheetId) throws Exception;



  /** Registers a new theme stylesheet with the portal databases
   *
   * @param stylesheetDescriptionURI Location of the stylesheet description
   *     XML file
   * @param stylesheetURI Location of the actual stylesheet XML file
   * @return id assigned to the stylesheet or null if the operation failed
   */
  public Integer addThemeStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI) throws Exception;

}

