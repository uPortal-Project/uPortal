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
import org.w3c.dom.Document;

import org.jasig.portal.security.IPerson;
/**
 * Interface through which portal talks to the user preferences database
 *
 * @author Peter Kharchenko
 * @version $Revision$
 */
public interface IUserPreferencesStore {


    // user profiles
    /** Obtain user profile
     *
     * @param person User
     * @param userAgent User-Agent header string
     * @return user profile
     */
    public UserProfile getUserProfile(IPerson person, String userAgent);
    /** update user profile
     *
     * @param person User
     * @param profile profile update
     */
    public void updateUserProfile(IPerson person,UserProfile profile);
    /** remove user profile from the database
     *
     * @param person User
     * @param profileId profile id
     */
    public void deleteUserProfile(IPerson person,int profileId);
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
    public UserProfile addUserProfile(IPerson person,UserProfile profile);
    /**      *
     * @param userId
     * @param profileId
     */
    public UserProfile getUserProfileById(IPerson person,int profileId);
    /** retreive a list of user profiles
     *
     * @param person User
     * @return hashtable mapping user profile ids (Integer objects) to the profile objects
     */
    public Hashtable getUserProfileList(IPerson person);

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
     * @return a hasbtable mapping system profile ids (Integer objects) to the profile objects
     */
    public Hashtable getSystemProfileList();

    /** establish a browser - user profile mapping
     *
     * @param person User
     * @param userAgent User-Agent header string
     * @param profileId profile id to which given user agent will be mapped
     */
    public void setUserBrowserMapping(IPerson person,String userAgent,int profileId);
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
     * @param person User
     * @param userAgent User-Agent header string
     * @return profile id
     */
    public int getUserBrowserMapping(IPerson person,String userAgent);
    /** Determine which system profile given browser is mapped to
     *
     * @param userAgent User-Agent header string
     * @return system profile id
     */
    public int getSystemBrowserMapping(String userAgent);


    /** Retreive the entire UserPreferences object
     *
     * @param person User
     * @param profile profile
     * @return user preferences
     */
    public UserPreferences getUserPreferences(IPerson person, UserProfile profile);

    /** save user preferences
     *
     * @param person User
     * @param up user preferences object
     */
    public void putUserPreferences(IPerson person, UserPreferences up);

    /** Obtain structure stylesheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param stylesheetName structure stylesheet name
     * @return structure stylesheet user preferences. null is returned only if userId, profileId or stylesheet with an appropriate name do not exist. If all of the parameters are valid, but the user does not have any user preference settings associated with this stylesheet, return contains stylesheet preference object filled in with the defaults defined in stylesheet description.
     */
    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(IPerson person,int profileId,int stylesheetId);

    /** Obtain theme styelsheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param stylesheetName theme stylesheet name
     * @return theme stylesheet user preferences. null is returned only if userId, profileId or stylesheet with an appropriate name do not exist. If all of the parameters are valid, but the user does not have any user preference settings associated with this stylesheet, return contains stylesheet preference object filled in with the defaults defined in stylesheet description.
     */
    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(IPerson person,int profileId,int stylesheetId);

    /** Save structure stylesheet user pferences
     *
     * @param person User
     * @param profileId profile id
     * @param fsup structure stylesheet user preferences
     */
    public void setStructureStylesheetUserPreferences(IPerson person,int profileId, StructureStylesheetUserPreferences fsup);

    /** Save theme stylesheet user preferences
     *
     * @param person User
     * @param profileId profile id
     * @param ssup structure stylesheet user preferneces
     */
    public void setThemeStylesheetUserPreferences(IPerson person,int profileId, ThemeStylesheetUserPreferences ssup);

}