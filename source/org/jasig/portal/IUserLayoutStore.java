/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

 package org.jasig.portal;

/**
 * Interface by which portal talks to the database
 * @author George Lindholm
 * @version $Revision$
 */

import org.w3c.dom.*;
import java.sql.Connection;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import org.w3c.dom.Document;
import org.jasig.portal.security.IPerson;
import org.apache.xerces.dom.DocumentImpl;

public interface IUserLayoutStore {
  /* UserLayout  */
  public Document getUserLayout(IPerson Person,int profileId) throws Exception;
  public void setUserLayout(IPerson Person,int profileId,Document layoutXML, boolean channelsAdded) throws Exception;

    // user profiles
    /** Obtain user profile
     *
     * @param person User
     * @param userAgent User-Agent header string
     * @return user profile
     */
    public UserProfile getUserProfile(IPerson person, String userAgent) throws Exception;
    /** update user profile
     *
     * @param person User
     * @param profile profile update
     */
    public void updateUserProfile(IPerson person,UserProfile profile) throws Exception;
    /** remove user profile from the database
     *
     * @param person User
     * @param profileId profile id
     */
    public void deleteUserProfile(IPerson person,int profileId) throws Exception;
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
    public UserProfile addUserProfile(IPerson person,UserProfile profile) throws Exception;
    /**      *
     * @param userId
     * @param profileId
     */
    public UserProfile getUserProfileById(IPerson person,int profileId) throws Exception;
    /** retreive a list of user profiles
     *
     * @param person User
     * @return hashtable mapping user profile ids (Integer objects) to the profile objects
     */
    public Hashtable getUserProfileList(IPerson person) throws Exception;

    // syste profiles
    /** retreive a system profile
     *
     * @param userAgent User-Agent header string
     * @return profile object
     */
    public UserProfile getSystemProfile(String userAgent) throws Exception;
    /** update system profile
     *
     * @param profile profile object
     */
    public void updateSystemProfile(UserProfile profile) throws Exception;
    /** remove system profile from the database
     *
     * @param profileId profile id
     */
    public void deleteSystemProfile(int profileId) throws Exception;
    /** add a new system profile to the database. During this process, a new profile id will be assigned to the profile.
     *
     * @param profile profile object (profile id within will be overwritten)
     * @return profile with an newly assigned id
     */
    public UserProfile addSystemProfile(UserProfile profile) throws Exception;
    /**      *
     * @param profileId
     */
    public UserProfile getSystemProfileById(int profileId) throws Exception;
    /** obtain a list of system profiles
     *
     * @return a hasbtable mapping system profile ids (Integer objects) to the profile objects
     */
    public Hashtable getSystemProfileList() throws Exception;

    /** establish a browser - user profile mapping
     *
     * @param person User
     * @param userAgent User-Agent header string
     * @param profileId profile id to which given user agent will be mapped
     */
    public void setUserBrowserMapping(IPerson person,String userAgent,int profileId) throws Exception;
    /** establish system profile browser mapping
     *
     * @param userAgent User-Agent header string
     * @param systemProfileId profile id of a profile to which given
     *     user-agent will be mapped
     */
    public void setSystemBrowserMapping(String userAgent,int systemProfileId) throws Exception;

    // returns profileId
    /** Determine which profile a given browser mapped to
     *
     * @param person User
     * @param userAgent User-Agent header string
     * @return profile id
     */
    public int getUserBrowserMapping(IPerson person,String userAgent) throws Exception;
    /** Determine which system profile given browser is mapped to
     *
     * @param userAgent User-Agent header string
     * @return system profile id
     */
    public int getSystemBrowserMapping(String userAgent) throws Exception;


    /** Retreive the entire UserPreferences object
     *
     * @param person User
     * @param profile profile
     * @return user preferences
     */
    public UserPreferences getUserPreferences(IPerson person, UserProfile profile) throws Exception;

    /** save user preferences
     *
     * @param person User
     * @param up user preferences object
     */
    public void putUserPreferences(IPerson person, UserPreferences up) throws Exception;

    /** Obtain structure stylesheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param stylesheetName structure stylesheet name
     * @return structure stylesheet user preferences. null is returned only if userId, profileId or stylesheet with an appropriate name do not exist. If all of the parameters are valid, but the user does not have any user preference settings associated with this stylesheet, return contains stylesheet preference object filled in with the defaults defined in stylesheet description.
     */
    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(IPerson person,int profileId,int stylesheetId) throws Exception;

    /** Obtain theme stylesheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param stylesheetName theme stylesheet name
     * @return theme stylesheet user preferences. null is returned only if userId, profileId or stylesheet with an appropriate name do not exist. If all of the parameters are valid, but the user does not have any user preference settings associated with this stylesheet, return contains stylesheet preference object filled in with the defaults defined in stylesheet description.
     */
    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(IPerson person,int profileId,int stylesheetId) throws Exception;

    /** Save structure stylesheet user pferences
     *
     * @param person User
     * @param profileId profile id
     * @param fsup structure stylesheet user preferences
     */
    public void setStructureStylesheetUserPreferences(IPerson person,int profileId, StructureStylesheetUserPreferences fsup) throws Exception;

    /** Save theme stylesheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param ssup structure stylesheet user preferneces
     */
    public void setThemeStylesheetUserPreferences(IPerson person,int profileId, ThemeStylesheetUserPreferences ssup) throws Exception;


  /* ChannelRegistry */
  public String getNextStructChannelId(IPerson person) throws Exception;
  public String getNextStructFolderId(IPerson person) throws Exception;

  /**
   *  CoreStylesheetDescription
   */
  // functions that allow one to browse available core stylesheets in various ways
  /** Obtain a listing of structure stylesheets from the database
   *
   * @param mimeType
   * @return Returns a hashtable mapping structure stylesheet names to a
   *     word-description (a simple String) of that stylesheet
   */
  public Hashtable getStructureStylesheetList (String mimeType) throws Exception;

  /** Obtains a list of theme stylesheets available for a particular structure stylesheet
   *
   * @param structureStylesheetName name of the structure stylehsset
   * @return Returns a hashtable mapping theme stylesheet names to an array (String[])
   * containing five string elements:
   * <ol>
   *  <li>stylesheet description text</li>
   *  <li>mime type specified by the stylesheet</li>
   *  <li>device type code specified by the stylesheet</li>
   *  <li>sample image uri specified</li>
   *  <li>sample image icon uri specified</li>
   * </ol>
   * stylesheet description and the mime type
   */
  public Hashtable getThemeStylesheetList (int structureStylesheetId) throws Exception;



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



  /**
   *  ReferenceAuthentication
   *
   */
  public String[] getUserAccountInformation(String username) throws Exception;

  /* ReferenceDirectoryInfo
  Normally directory information would come from a real directory server using
  for example, LDAP.  The reference inplementation uses the database for
  directory information.
  */

  public String[] getUserDirectoryInformation(String username) throws Exception;

  /* Counters */
  public int getIncrementIntegerId(String tableName) throws Exception;
  public void createCounter(String tableName) throws Exception;
  public void setCounter(String tableName, int value) throws Exception;


}

