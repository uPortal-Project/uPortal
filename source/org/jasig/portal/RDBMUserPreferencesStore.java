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
import org.w3c.dom.Document;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;

/**
 * Reference implementation of IUserPreferencesDB
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */
public class RDBMUserPreferencesStore
    implements IUserPreferencesStore {

    private class SystemUser implements IPerson {
      public void setID(int sID) {}
      public int getID() {return 0;}

      public void setFullName(String sFullName) {}
      public String getFullName() {return "uPortal System Account";}

      public Object getAttribute (String key) {return null;}
      public void setAttribute (String key, Object value) {}

      public Enumeration getAttributes () {return null;}
      public Enumeration getAttributeNames () {return null;}

      public boolean isGuest() {return(false);}

      public ISecurityContext getSecurityContext() { return(null); }
      public void setSecurityContext(ISecurityContext context) {}
    }

    private IPerson systemUser = new SystemUser(); // We should be getting this from the uPortal


  /**
   * put your documentation comment here
   * @param person
   * @param profile
   * @return
   */
  public UserPreferences getUserPreferences (IPerson person, UserProfile profile) {
    int profileId = profile.getProfileId();
    UserPreferences up = new UserPreferences(profile);
    up.setStructureStylesheetUserPreferences(getStructureStylesheetUserPreferences(person, profileId, profile.getStructureStylesheetId()));
    up.setThemeStylesheetUserPreferences(getThemeStylesheetUserPreferences(person, profileId, profile.getThemeStylesheetId()));
    return  up;
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @return
   */
  public UserPreferences getUserPreferences (IPerson person, int profileId) {
    UserPreferences up = null;
    UserProfile profile = this.getUserProfileById(person, profileId);
    if (profile != null) {
      up = getUserPreferences(person, profile);
    }
    return  (up);
  }

  /**
   * put your documentation comment here
   * @param person
   * @param userAgent
   * @return
   */
  public int getUserBrowserMapping (IPerson person, String userAgent) {
    try {
      return  UserLayoutStoreFactory.getUserLayoutStoreImpl().getUserBrowserMapping(person, userAgent);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
    return  (0);
  }

  /**
   * put your documentation comment here
   * @param person
   * @param userAgent
   * @param profileId
   */
  public void setUserBrowserMapping (IPerson person, String userAgent, int profileId) {
    try {
      UserLayoutStoreFactory.getUserLayoutStoreImpl().setUserBrowserMapping(person, userAgent, profileId);
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
    this.setUserBrowserMapping(systemUser, userAgent, profileId);
  }

  /**
   * put your documentation comment here
   * @param userAgent
   * @return
   */
  public int getSystemBrowserMapping (String userAgent) {
    return  getUserBrowserMapping(systemUser, userAgent);
  }

  /**
   * put your documentation comment here
   * @param person
   * @param userAgent
   * @return
   */
  public UserProfile getUserProfile (IPerson person, String userAgent) {
    int profileId = getUserBrowserMapping(person, userAgent);
    if (profileId == 0)
      return  null;
    return  this.getUserProfileById(person, profileId);
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
    UserProfile up = this.getUserProfileById(systemUser, profileId);
    up.setSystemProfile(true);
    return  up;
  }

  /**
   * put your documentation comment here
   * @param profileId
   * @return
   */
  public UserProfile getSystemProfileById (int profileId) {
    UserProfile up = this.getUserProfileById(systemUser, profileId);
    up.setSystemProfile(true);
    return  up;
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @return
   */
  public UserProfile getUserProfileById (IPerson person, int profileId) {
    UserProfile upl = null;
    try {
      upl = UserLayoutStoreFactory.getUserLayoutStoreImpl().getUserProfileById(person, profileId);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
    return  upl;
  }

  /**
   * put your documentation comment here
   * @param person
   * @return
   */
  public Hashtable getUserProfileList (IPerson person) {
    Hashtable pv = null;
    try {
      pv = UserLayoutStoreFactory.getUserLayoutStoreImpl().getUserProfileList(person);
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
    Hashtable pl = this.getUserProfileList(systemUser);
    for (Enumeration e = pl.elements(); e.hasMoreElements();) {
      UserProfile up = (UserProfile)e.nextElement();
      up.setSystemProfile(true);
    }
    return  pl;
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profile
   */
  public void updateUserProfile (IPerson person, UserProfile profile) {
    try {
      UserLayoutStoreFactory.getUserLayoutStoreImpl().updateUserProfile(person, profile);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param profile
   */
  public void updateSystemProfile (UserProfile profile) {
    this.updateUserProfile(systemUser, profile);
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profile
   * @return
   */
  public UserProfile addUserProfile (IPerson person, UserProfile profile) {
    try {
      profile = UserLayoutStoreFactory.getUserLayoutStoreImpl().addUserProfile(person, profile);
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
    return  addUserProfile(systemUser, profile);
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   */
  public void deleteUserProfile (IPerson person, int profileId) {
    try {
      UserLayoutStoreFactory.getUserLayoutStoreImpl().deleteUserProfile(person, profileId);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param profileId
   */
  public void deleteSystemProfile (int profileId) {
    this.deleteUserProfile(systemUser, profileId);
  }

  /**
   * put your documentation comment here
   * @param person
   * @param up
   */
  public void putUserPreferences (IPerson person, UserPreferences up) {
    // store profile
    UserProfile profile = up.getProfile();
    this.updateUserProfile(person, profile);
    this.setStructureStylesheetUserPreferences(person, profile.getProfileId(), up.getStructureStylesheetUserPreferences());
    this.setThemeStylesheetUserPreferences(person, profile.getProfileId(), up.getThemeStylesheetUserPreferences());
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @param stylesheetId
   * @return
   */
  public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences (IPerson person, int profileId, int stylesheetId) {
    try {
      return  UserLayoutStoreFactory.getUserLayoutStoreImpl().getStructureStylesheetUserPreferences(person, profileId, stylesheetId);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
    return  null;
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @param stylesheetId
   * @return
   */
  public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences (IPerson person, int profileId, int stylesheetId) {
    try {
      return  UserLayoutStoreFactory.getUserLayoutStoreImpl().getThemeStylesheetUserPreferences(person, profileId, stylesheetId);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
    return  null;
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @param fsup
   */
  public void setStructureStylesheetUserPreferences (IPerson person, int profileId, StructureStylesheetUserPreferences fsup) {
    // update the database
    try {
      UserLayoutStoreFactory.getUserLayoutStoreImpl().setStructureStylesheetUserPreferences(person, profileId, fsup);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @param ssup
   */
  public void setThemeStylesheetUserPreferences (IPerson person, int profileId, ThemeStylesheetUserPreferences ssup) {
    // update the database
    try {
      UserLayoutStoreFactory.getUserLayoutStoreImpl().setThemeStylesheetUserPreferences(person, profileId, ssup);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }
}



