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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal;

import  java.util.*;
import  java.io.*;


/**
 * Reference implementation of IUserPreferencesDB
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */
public class ReferenceUserPreferencesStore
    implements IUserPreferencesStore {
  int systemUserId = 0;         // Set this somehow

  /**
   * put your documentation comment here
   * @param userId
   * @param profile
   * @return 
   */
  public UserPreferences getUserPreferences (int userId, UserProfile profile) {
    int profileId = profile.getProfileId();
    UserPreferences up = new UserPreferences(profile);
    up.setStructureStylesheetUserPreferences(getStructureStylesheetUserPreferences(userId, profileId, profile.getStructureStylesheetName()));
    up.setThemeStylesheetUserPreferences(getThemeStylesheetUserPreferences(userId, profileId, profile.getThemeStylesheetName()));
    return  up;
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @return 
   */
  public UserPreferences getUserPreferences (int userId, int profileId) {
    UserPreferences up = null;
    UserProfile profile = this.getUserProfileById(userId, profileId);
    if (profile != null) {
      up = getUserPreferences(userId, profile);
    }
    return  up;
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param userAgent
   * @return 
   */
  public int getUserBrowserMapping (int userId, String userAgent) {
    try {
      return  GenericPortalBean.getUserLayoutStore().getUserBrowserMapping(userId, userAgent);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
    return  0;
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param userAgent
   * @param profileId
   */
  public void setUserBrowserMapping (int userId, String userAgent, int profileId) {
    try {
      GenericPortalBean.getUserLayoutStore().setUserBrowserMapping(userId, userAgent, profileId);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param userAgent
   * @param profileId
   */
  public void setSystemBrowserMapping (String userAgent, int profileId) {
    this.setUserBrowserMapping(systemUserId, userAgent, profileId);
  }

  /**
   * put your documentation comment here
   * @param userAgent
   * @return 
   */
  public int getSystemBrowserMapping (String userAgent) {
    return  getUserBrowserMapping(systemUserId, userAgent);
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param userAgent
   * @return 
   */
  public UserProfile getUserProfile (int userId, String userAgent) {
    int profileId = getUserBrowserMapping(userId, userAgent);
    if (profileId == 0)
      return  null;
    return  this.getUserProfileById(userId, profileId);
  }

  /**
   * put your documentation comment here
   * @param userAgent
   * @return 
   */
  public UserProfile getSystemProfile (String userAgent) {
    int profileId = getSystemBrowserMapping(userAgent);
    if (profileId == 0)
      return  null;
    UserProfile up = this.getUserProfileById(systemUserId, profileId);
    up.setSystemProfile(true);
    return  up;
  }

  /**
   * put your documentation comment here
   * @param profileId
   * @return 
   */
  public UserProfile getSystemProfileById (int profileId) {
    UserProfile up = this.getUserProfileById(systemUserId, profileId);
    up.setSystemProfile(true);
    return  up;
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @return 
   */
  public UserProfile getUserProfileById (int userId, int profileId) {
    UserProfile upl = null;
    try {
      upl = GenericPortalBean.getUserLayoutStore().getUserProfileById(userId, profileId);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
    return  upl;
  }

  /**
   * put your documentation comment here
   * @param userId
   * @return 
   */
  public Hashtable getUserProfileList (int userId) {
    Hashtable pv = null;
    try {
      pv = GenericPortalBean.getUserLayoutStore().getUserProfileList(userId);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
    return  pv;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public Hashtable getSystemProfileList () {
    Hashtable pl = this.getUserProfileList(0);
    for (Enumeration e = pl.elements(); e.hasMoreElements();) {
      UserProfile up = (UserProfile)e.nextElement();
      up.setSystemProfile(true);
    }
    return  pl;
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profile
   */
  public void updateUserProfile (int userId, UserProfile profile) {
    try {
      GenericPortalBean.getUserLayoutStore().updateUserProfile(userId, profile);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param profile
   */
  public void updateSystemProfile (UserProfile profile) {
    this.updateUserProfile(0, profile);
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profile
   * @return 
   */
  public UserProfile addUserProfile (int userId, UserProfile profile) {
    try {
      profile = GenericPortalBean.getUserLayoutStore().addUserProfile(userId, profile);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
    return  profile;
  }

  /**
   * put your documentation comment here
   * @param profile
   * @return 
   */
  public UserProfile addSystemProfile (UserProfile profile) {
    return  addUserProfile(0, profile);
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   */
  public void deleteUserProfile (int userId, int profileId) {
    try {
      GenericPortalBean.getUserLayoutStore().deleteUserProfile(userId, profileId);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param profileId
   */
  public void deleteSystemProfile (int profileId) {
    this.deleteUserProfile(0, profileId);
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param up
   */
  public void putUserPreferences (int userId, UserPreferences up) {
    // store profile
    UserProfile profile = up.getProfile();
    this.updateUserProfile(userId, profile);
    this.setStructureStylesheetUserPreferences(userId, profile.getProfileId(), up.getStructureStylesheetUserPreferences());
    this.setThemeStylesheetUserPreferences(userId, profile.getProfileId(), up.getThemeStylesheetUserPreferences());
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @param stylesheetName
   * @return 
   */
  public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences (int userId, int profileId, String stylesheetName) {
    try {
      return  GenericPortalBean.getUserLayoutStore().getStructureStylesheetUserPreferences(userId, profileId, stylesheetName);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
    return  null;
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @param stylesheetName
   * @return 
   */
  public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences (int userId, int profileId, String stylesheetName) {
    try {
      return  GenericPortalBean.getUserLayoutStore().getThemeStylesheetUserPreferences(userId, profileId, stylesheetName);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
    return  null;
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @param fsup
   */
  public void setStructureStylesheetUserPreferences (int userId, int profileId, StructureStylesheetUserPreferences fsup) {
    // update the database
    try {
      GenericPortalBean.getUserLayoutStore().setStructureStylesheetUserPreferences(userId, profileId, fsup);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @param ssup
   */
  public void setThemeStylesheetUserPreferences (int userId, int profileId, ThemeStylesheetUserPreferences ssup) {
    // update the database
    try {
      GenericPortalBean.getUserLayoutStore().setThemeStylesheetUserPreferences(userId, profileId, ssup);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }
}



