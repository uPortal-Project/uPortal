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
import java.util.Hashtable;


/**
 * Interface through which portal talks to the user preferences database
 *
 * @author Peter Kharchenko
 * @version $Revision$
 */
public interface IUserPreferencesDB {


    // user profiles
    /** Obtain user profile
     *
     * @param userId user id
     * @param userAgent User-Agent header string
     * @return user profile
     */
    public UserProfile getUserProfile(int userId, String userAgent);
    /** update user profile
     *
     * @param userId user id
     * @param profile profile update
     */
    public void updateUserProfile(int userId,UserProfile profile);
    /** remove user profile from the database
     *
     * @param userId user id
     * @param profileId profile id
     */
    public void deleteUserProfile(int userId,int profileId);
    /**
     * Creates a new user profile in the database.
     * In the process, new profileId is assigned to the profile
     *
     * @param userId user id
     * @param profile profile object (profile id in this object will be
     *     overwritten)
     * @return profile object with the profile id set to the newly generated
     *     id
     */
    public UserProfile addUserProfile(int userId,UserProfile profile);
    /**      *
     * @param userId
     * @param profileId
     */
    public UserProfile getUserProfileById(int userId,int profileId);
    /** retreive a list of user profiles
     *
     * @param userId user id
     * @return hashtable mapping user profile names to the profile objects
     */
    public Hashtable getUserProfileList(int userId);

    // syste profiles
    /** retreive a system profile
     *
     * @param userAgent User-Agent header string
     * @return profile object
     */
    public UserProfile getSystemProfile(String userAgent);
    /** update system profile
     *
     * @param profile profile object
     */
    public void updateSystemProfile(UserProfile profile);
    /** remove system profile from the database
     *
     * @param profileId profile id
     */
    public void deleteSystemProfile(int profileId);
    /** add a new system profile to the database. During this process, a new profile id will be assigned to the profile.
     *
     * @param profile profile object (profile id within will be overwritten)
     * @return profile with an newly assigned id
     */
    public UserProfile addSystemProfile(UserProfile profile);
    /**      *
     * @param profileId
     */
    public UserProfile getSystemProfileById(int profileId);
    /** obtain a list of system profiles
     *
     * @return a hasbtable mapping system profile names to the profile objects
     */
    public Hashtable getSystemProfileList();

    /** establish a browser - user profile mapping
     *
     * @param userId user id
     * @param userAgent User-Agent header string
     * @param profileId profile id to which given user agent will be mapped
     */
    public void setUserBrowserMapping(int userId,String userAgent,int profileId);
    /** establish system profile browser mapping
     *
     * @param userAgent User-Agent header string
     * @param systemProfileId profile id of a profile to which given
     *     user-agent will be mapped
     */
    public void setSystemBrowserMapping(String userAgent,int systemProfileId);

    // returns profileId
    /** Determine which profile a given browser mapped to
     *
     * @param userId user id
     * @param userAgent User-Agent header string
     * @return profile id
     */
    public int getUserBrowserMapping(int userId,String userAgent);
    /** Determine which system profile given browser is mapped to
     *
     * @param userAgent User-Agent header string
     * @return system profile id
     */
    public int getSystemBrowserMapping(String userAgent);


    /** Retreive the entire UserPreferences object
     *
     * @param userId user id
     * @param profile profile
     * @return user preferences
     */
    public UserPreferences getUserPreferences(int userId, UserProfile profile);

    /** save user preferences
     *
     * @param userId user id
     * @param up user preferences object
     */
    public void putUserPreferences(int userId, UserPreferences up);

    /** Obtain structure stylesheet user preferences
     *
     * @param userId user id
     * @param profileId profile id
     * @param stylesheetName structure stylesheet name
     * @return structure stylesheet user preferences. null is returned only if userId, profileId or stylesheet with an appropriate name do not exist. If all of the parameters are valid, but the user does not have any user preference settings associated with this stylesheet, return contains stylesheet preference object filled in with the defaults defined in stylesheet description.
     */
    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(int userId,int profileId,int stylesheetId);
    /** Obtain theme styelsheet user preferences
     *
     * @param userId user id
     * @param profileId profile id
     * @param stylesheetName theme stylesheet name
     * @return theme stylesheet user preferences. null is returned only if userId, profileId or stylesheet with an appropriate name do not exist. If all of the parameters are valid, but the user does not have any user preference settings associated with this stylesheet, return contains stylesheet preference object filled in with the defaults defined in stylesheet description.
     */
    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(int userId,int profileId,int stylesheetId);


    /** Save structure stylesheet user pferences
     *
     * @param userId user id
     * @param profileId profile id
     * @param fsup structure stylesheet user preferences
     */
    public void setStructureStylesheetUserPreferences(int userId,int profileId, StructureStylesheetUserPreferences fsup);
    /** Save theme stylesheet user preferences
     *
     * @param userId user id
     * @param profileId profile id
     * @param ssup structure stylesheet user preferneces
     */
    public void setThemeStylesheetUserPreferences(int userId,int profileId, ThemeStylesheetUserPreferences ssup);


}
