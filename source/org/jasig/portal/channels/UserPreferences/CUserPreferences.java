/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.UserPreferences;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.IUserLayoutManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ContentHandler;

/** 
 * CUserPreferences manages User Layout, user preferences and profiles.
 * 
 * @author Peter Kharchenko, pkharchenko@unicon.net
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CUserPreferences implements IPrivilegedChannel {
  private static final Log log = LogFactory.getLog(CUserPreferences.class);
  IUserPreferencesManager upm;
  ChannelRuntimeData runtimeData = null;
  ChannelStaticData staticData = null;
  StylesheetSet set = null;
  private static final String sslLocation = "/org/jasig/portal/channels/CUserPreferences/CUserPreferences.ssl";
  private UserPreferences up = null;
  public static final int MANAGE_PREFERENCES = 1;
  public static final int MANAGE_PROFILES = 2;
  IPrivilegedChannel internalState = null;
  IPrivilegedChannel managePreferences = null;
  IPrivilegedChannel manageProfiles = null;
  protected IUserLayoutStore ulsdb;
  private boolean initialized=false;
  UserProfile editedProfile=null;

  public CUserPreferences() throws PortalException {
    this.runtimeData = new ChannelRuntimeData();
    this.set = new StylesheetSet(this.getClass().getResource(sslLocation).toString());
    this.set.setMediaProps("/properties/media.properties");

    manageProfiles = new ManageProfilesState(this);
    ulsdb = UserLayoutStoreFactory.getUserLayoutStoreImpl();
  }


  protected IUserPreferencesManager getUserPreferencesManager() {
    return  upm;
  }

  protected IUserLayoutManager getUserLayoutManager() {
    return getUserPreferencesManager().getUserLayoutManager();
  }

  protected UserPreferences getCurrentUserPreferences() {
    return  up;
  }

  protected ChannelRuntimeData getRuntimeData() {
    return  runtimeData;
  }


  protected StylesheetSet getStylesheetSet() {
    return  set;
  }


  public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
    if (upm == null)
      upm = pcs.getUserPreferencesManager();
    if (up == null)
      up = upm.getUserPreferencesCopy();
    // instantiate the browse state here

    if (!initialized) {
        instantiateManagePreferencesState(up.getProfile());
        // Initial state should be manage preferences
        internalState = managePreferences;
        internalState.setStaticData(staticData);
        editedProfile=up.getProfile();
        initialized=true;
    }
    if(internalState!=null) {
        internalState.setPortalControlStructures(pcs);
    }
  }


    /**
     * Instantiates appropriate managePreferences object.
     *
     * @param profile profile for which preferences are to be edited
     */
    private void instantiateManagePreferencesState(UserProfile profile) {
        try {
            ThemeStylesheetDescription tsd = ulsdb.getThemeStylesheetDescription(profile.getThemeStylesheetId());
            if(tsd!=null) {
                String cupmClass = tsd.getCustomUserPreferencesManagerClass();
                managePreferences = (IPrivilegedChannel)Class.forName(cupmClass).newInstance();
                ((BaseState)managePreferences).setContext(this);
            } else {
                log.error("CUserPreferences::instantiateManagePreferencesState() : unable to retrieve theme stylesheet description. stylesheetId="+profile.getThemeStylesheetId());
                managePreferences = new GPreferencesState(this);
            }
        } catch (Exception e) {
            log.error("Error instantiating user profile [" + profile + "]", e);
            managePreferences = new GPreferencesState(this);
        }
    }

  /** Returns channel runtime properties
   * @return handle to runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties() {
    // Channel will always render, so the default values are ok
    return  new ChannelRuntimeProperties();
  }

  /** Processes layout-level events coming from the portal
   * @param ev a portal layout event
   */
  public void receiveEvent(PortalEvent ev) {
    // no events for this channel
    if (internalState != null) {
      internalState.receiveEvent(ev);
    } 
  }

  /** Receive static channel data from the portal
   * @param sd static channel data
   */
  public void setStaticData(ChannelStaticData sd) throws PortalException {
    this.staticData = sd;
  }

  /** CUserPreferences listens for an HttpRequestParameter "userPreferencesAction"
   * and based on its value changes state between profile management and layout/stylesheet
   * preferences.
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
    this.runtimeData = rd;
    String action = runtimeData.getParameter("userPreferencesAction");
    if (action != null) {
      Integer profileId = null;
      try {
        profileId = new Integer(runtimeData.getParameter("profileId"));
      } catch (NumberFormatException nfe) {};
      boolean systemProfile = false;
      if (profileId != null) {
        String profileType = runtimeData.getParameter("profileType");
        if (profileType != null && profileType.equals("system"))
          systemProfile = true;
      }

      if (action.equals("manageProfiles")) {
          this.internalState = manageProfiles;
      } else if (action.equals("managePreferences")) {
          if (profileId != null) {
              // find the profile mapping
            try {
              if (systemProfile) {
                  UserProfile newProfile = ulsdb.getSystemProfileById(profileId.intValue());
                  if(newProfile!=null && (!(editedProfile.isSystemProfile() && editedProfile.getProfileId()==newProfile.getProfileId()))) {
                      // new profile has been selected
                      editedProfile=newProfile;
                      instantiateManagePreferencesState(editedProfile);
                  }
              } else {
                  UserProfile newProfile = ulsdb.getUserProfileById(upm.getPerson(), profileId.intValue());
                  if(newProfile!=null && (editedProfile.isSystemProfile() || (editedProfile.getProfileId()!=newProfile.getProfileId()))) {
                      // new profile has been selected
                      editedProfile=newProfile;
                      instantiateManagePreferencesState(editedProfile);
                  }
              }
            } catch (Exception e) {
                throw new PortalException(e);
            }
          }

          if(editedProfile==null) {
              editedProfile = up.getProfile();
          }

          //        managePreferences.setRuntimeData(rd);
          this.internalState = managePreferences;
      }
    }

    if (internalState != null) {
      internalState.setRuntimeData(rd);
    }
  }

  /**
   * Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML(ContentHandler out) throws PortalException {
    internalState.renderXML(out);
  }

  protected UserPreferences getUserPreferencesFromStore(UserProfile profile) throws Exception {
      up = ulsdb.getUserPreferences(getUserPreferencesManager().getPerson(), profile);
      up.synchronizeWithUserLayoutXML(UserLayoutStoreFactory.getUserLayoutStoreImpl().getUserLayout(getUserPreferencesManager().getPerson(), getCurrentUserPreferences().getProfile()));
      return up;
  }

  protected UserProfile getEditedUserProfile() {
    return editedProfile;
  }
}



