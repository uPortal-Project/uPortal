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


package  org.jasig.portal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;

import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.jndi.JNDIManager;
import org.jasig.portal.layout.IUserLayoutChannelDescription;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.layout.UserLayoutManagerFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.PropsMatcher;


/**
 * UserPreferencesManager is responsible for keeping: user id, user layout, user preferences
 * and stylesheet descriptions.
 * For method descriptions please see {@link IUserPreferencesManager}.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 */
public class UserPreferencesManager implements IUserPreferencesManager {

    // user agent mapper for guessing the profile
    static PropsMatcher uaMatcher;

    private IUserLayoutManager ulm;

    private UserPreferences complete_up;
    // caching of stylesheet descriptions is recommended
    // if they'll take up too much space, we can take them
    // out, but cache stylesheet URIs, mime type and serializer name.
    // Those are used in every rendering cycle.
    private ThemeStylesheetDescription tsd;
    private StructureStylesheetDescription ssd;
    private boolean unmapped_user_agent = false;
    IPerson m_person;
    IUserLayoutStore ulsdb = null;

    private static final boolean saveUserPreferencesAtLogout = PropertiesManager.getPropertyAsBoolean(UserPreferencesManager.class.getName() + ".save_UserPreferences_at_logout");


    /**
     * Constructor does the following
     *  1. Read layout.properties
     *  2. read userLayout from the database
     *  @param req the servlet request object
     *  @param person the person object
     */
    public UserPreferencesManager (HttpServletRequest req, IPerson person) throws PortalException {
        ulm=null;
        try {
            m_person = person;
            // load user preferences
            // Should obtain implementation in a different way!!
            ulsdb = UserLayoutStoreFactory.getUserLayoutStoreImpl();
            // determine user profile
            String userAgent = req.getHeader("User-Agent");
            if(userAgent==null || userAgent.equals("")) {
                userAgent=MediaManager.NULL_USER_AGENT;
            }
            UserProfile upl = ulsdb.getUserProfile(m_person, userAgent);
            if (upl == null) {
                upl = ulsdb.getSystemProfile(userAgent);
            }
            if(upl==null) {
                // try guessing the profile through pattern matching

                if(uaMatcher==null) {
                    // init user agent matcher
                    URL url = null;
                    try {
                        url = this.getClass().getResource("/properties/browser.mappings");
                        if (url != null) {
                          InputStream in = url.openStream();
                          try {
                            uaMatcher = new PropsMatcher(in);
                          } finally {
                            in.close();
                          }
                        }
                    } catch (IOException ioe) {
                        LogService.log(LogService.ERROR, "UserPreferencesManager::UserPreferencesManager() : Exception occurred while loading browser mapping file: " + url + ". " + ioe);
                    }
                }

                if(uaMatcher!=null) {
                    // try matching
                    String profileId=uaMatcher.match(userAgent);
                    if(profileId!=null) {
                        // user agent has been matched

                        upl=ulsdb.getSystemProfileById(Integer.parseInt(profileId));
                    }
                }

            }

            if (upl != null) {
                ulm=UserLayoutManagerFactory.getUserLayoutManager(m_person,upl);

                try {
                    complete_up=ulsdb.getUserPreferences(m_person, upl);
                } catch (Exception e) {
                    LogService.log(LogService.ERROR, "UserPreferencesManager(): caught an exception trying to retreive user preferences for user=\"" + m_person.getID() + "\", profile=\"" + upl.getProfileName() + "\".", e);
                    complete_up=new UserPreferences(upl);
                }

                try {
                    // Initialize the JNDI context for this user
                    JNDIManager.initializeSessionContext(req.getSession(),Integer.toString(m_person.getID()),Integer.toString(upl.getLayoutId()),ulm.getUserLayoutDOM());
                } catch(PortalException ipe) {
                  LogService.log(LogService.ERROR, "UserPreferencesManager(): Could not properly initialize user context", ipe);
                }
            } else {
                // there is no user-defined mapping for this particular browser.
                // user should be redirected to a browser-registration page.
                unmapped_user_agent = true;
                LogService.log(LogService.DEBUG, "UserPreferencesManager::UserPreferencesManager() : unable to find a profile for user \"" + m_person.getID()+"\" and userAgent=\""+ userAgent + "\".");
            }
        } catch (PortalException pe) {
            throw pe;
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
    }

    /**
     * Constructor does the following
     *  1. Read layout.properties
     *  2. read userLayout from the database
     *  @param req the servlet request object
     *  @param person the person object
     *  @param localeManager the locale manager
     */
    public UserPreferencesManager (HttpServletRequest req, IPerson person, LocaleManager localeManager) throws PortalException {
        ulm=null;
        try {
            m_person = person;
            // load user preferences
            // Should obtain implementation in a different way!!
            ulsdb = UserLayoutStoreFactory.getUserLayoutStoreImpl();
            // determine user profile
            String userAgent = req.getHeader("User-Agent");
            UserProfile upl = ulsdb.getUserProfile(m_person, userAgent);
            if (upl == null) {
                upl = ulsdb.getSystemProfile(userAgent);
            }
            if(upl==null) {
                // try guessing the profile through pattern matching

                if(uaMatcher==null) {
                    // init user agent matcher
                    URL url = null;
                    try {
                        url = this.getClass().getResource("/properties/browser.mappings");
                        if (url != null) {
                          InputStream in = url.openStream();
                          try {
                            uaMatcher = new PropsMatcher(in);
                          } finally {
                            in.close();
                          }
                        }
                    } catch (IOException ioe) {
                        LogService.log(LogService.ERROR, "UserPreferencesManager::UserPreferencesManager() : Exception occurred while loading browser mapping file: " + url + ". " + ioe);
                    }
                }

                if(uaMatcher!=null) {
                    // try matching
                    String profileId=uaMatcher.match(userAgent);
                    if(profileId!=null) {
                        // user agent has been matched

                        upl=ulsdb.getSystemProfileById(Integer.parseInt(profileId));
                    }
                }

            }

            if (upl != null) {
                if (LocaleManager.isLocaleAware()) {
                    upl.setLocaleManager(localeManager);
                }
                ulm=UserLayoutManagerFactory.getUserLayoutManager(m_person,upl);

                try {
                    complete_up=ulsdb.getUserPreferences(m_person, upl);
                } catch (Exception e) {
                    LogService.log(LogService.ERROR, "UserPreferencesManager(): caught an exception trying to retreive user preferences for user=\"" + m_person.getID() + "\", profile=\"" + upl.getProfileName() + "\".", e);
                    complete_up=new UserPreferences(upl);
                }

                try {
                    // Initialize the JNDI context for this user
                    JNDIManager.initializeSessionContext(req.getSession(),Integer.toString(m_person.getID()),Integer.toString(upl.getLayoutId()),ulm.getUserLayoutDOM());
                } catch(PortalException ipe) {
                  LogService.log(LogService.ERROR, "UserPreferencesManager(): Could not properly initialize user context", ipe);
                }
            } else {
                // there is no user-defined mapping for this particular browser.
                // user should be redirected to a browser-registration page.
                unmapped_user_agent = true;
                LogService.log(LogService.DEBUG, "UserPreferencesManager::UserPreferencesManager() : unable to find a profile for user \"" + m_person.getID()+"\" and userAgent=\""+ userAgent + "\".");
            }
        } catch (PortalException pe) {
            throw pe;
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
    }

    /**
     * A simpler constructor, that only initialises the person object.
     * Needed for ancestors.
     * @param person an <code>IPerson</code> object.
     */
    public UserPreferencesManager(IPerson person) {
        m_person=person;
    }

    /* This function processes request parameters related to
     * setting Structure/Theme stylesheet parameters and attributes.
     * (uP_sparam, uP_tparam, uP_sfattr, uP_scattr
     * uP_tcattr)
     * It also processes layout root requests (uP_root) and
     * (up_fname)
     * @param req current <code>HttpServletRequest</code>
     */
    public void processUserPreferencesParameters(HttpServletRequest req) {
        // save processing
        String saveWhat=req.getParameter("uP_save");
        if(saveWhat!=null) {
            try {
                if(saveWhat.equals("preferences")) {
                    ulsdb.putUserPreferences(m_person, complete_up);
                } else if(saveWhat.equals("layout")) {
                    ulm.saveUserLayout();
                } else if(saveWhat.equals("all")) {
                    ulsdb.putUserPreferences(m_person, complete_up);
                    ulm.saveUserLayout();
                  }
                LogService.log(LogService.DEBUG, "UserPreferencesManager::processUserPreferencesParameters() : persisted "+saveWhat+" changes.");

            } catch (Exception e) {
                LogService.log(LogService.ERROR, "UserPreferencesManager::processUserPreferencesParameters() : unable to persist "+saveWhat+" changes. "+e);
            }
        }

        // layout root setting
        String root;
        if ((root = req.getParameter("uP_root")) != null) {
            // If a channel specifies "me" as its root, set the root
            // to the channel's subscribe Id
            if (root.equals("me")) {
                // get uPFile spec and search for "channel" clause
                UPFileSpec upfs=new UPFileSpec(req);
                root=upfs.getTargetNodeId();
            }
            if(root!=null) {
                complete_up.getStructureStylesheetUserPreferences().putParameterValue("userLayoutRoot", root);
            } else {
                LogService.log(LogService.ERROR, "UserPreferencesManager::processUserPreferencesParameters() : unable to extract channel ID. servletPath=\""+req.getServletPath()+"\".");
            }
        }

        // fname and root are mutually exclusive and
        // should not be used in the same request,
        // as an fname is treated as the root target.
        String fname = req.getParameter( Constants.FNAME_PARAM );
        if (fname != null) {
            // get a subscribe id for the fname
            String subId = null;
            try {
             subId = ulm.getSubscribeId(fname);
            } catch ( PortalException pe ) {
               LogService.log(LogService.ERROR, "UserPreferencesManager::processUserPreferencesParameters(): Unable to get subscribe ID for fname="+fname);
              }
            if ( ulm instanceof TransientUserLayoutManagerWrapper ){
                // get wrapper implementation for focusing
                TransientUserLayoutManagerWrapper iulm =
                    (TransientUserLayoutManagerWrapper) ulm;
                // .. and now set it as the focused id
                iulm.setFocusedId(subId);
            }

            complete_up.getStructureStylesheetUserPreferences().putParameterValue("userLayoutRoot",
                                                                                  subId);
            LogService.log(LogService.DEBUG,
                           "UserPreferencesManager::processUserPreferencesParameters() : " +
                           "setting sfname \" userLayoutRoot" + "\"=\"" + subId + "\".");
        }

        // Request to change the locale
        String localesString = req.getParameter(Constants.LOCALES_PARAM);
        if (localesString != null) {
            LocaleManager localeManager = complete_up.getProfile().getLocaleManager();
            localeManager.setSessionLocales(LocaleManager.parseLocales(localesString));
        }

        // other params
        String[] sparams = req.getParameterValues("uP_sparam");
        if (sparams != null) {
            for (int i = 0; i < sparams.length; i++) {
                String pValue = req.getParameter(sparams[i]);
                complete_up.getStructureStylesheetUserPreferences().putParameterValue(sparams[i], pValue);
                LogService.log(LogService.DEBUG, "UserPreferencesManager::processUserPreferencesParameters() : setting sparam \"" + sparams[i] + "\"=\"" + pValue + "\".");
            }
        }
        String[] tparams = req.getParameterValues("uP_tparam");
        if (tparams != null) {
            for (int i = 0; i < tparams.length; i++) {
                String pValue = req.getParameter(tparams[i]);
                complete_up.getThemeStylesheetUserPreferences().putParameterValue(tparams[i], pValue);
                LogService.log(LogService.DEBUG, "UserPreferencesManager::processUserPreferencesParameters() : setting tparam \"" + tparams[i]+ "\"=\"" + pValue + "\".");
            }
        }
        // attribute processing
        // structure transformation
        String[] sfattrs = req.getParameterValues("uP_sfattr");
        if (sfattrs != null) {
            for (int i = 0; i < sfattrs.length; i++) {
                String aName = sfattrs[i];
                String[] aNode = req.getParameterValues(aName + "_folderId");
                if (aNode != null && aNode.length > 0) {
                    for (int j = 0; j < aNode.length; j++) {
                        String aValue = req.getParameter(aName + "_" + aNode[j] + "_value");
                        complete_up.getStructureStylesheetUserPreferences().setFolderAttributeValue(aNode[j], aName, aValue);
                        LogService.log(LogService.DEBUG, "UserPreferencesManager::processUserPreferencesParameters() : setting sfattr \"" + aName + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
                    }
                }
            }
        }
        String[] scattrs = req.getParameterValues("uP_scattr");
        if (scattrs != null) {
            for (int i = 0; i < scattrs.length; i++) {
                String aName = scattrs[i];
                String[] aNode = req.getParameterValues(aName + "_channelId");
                if (aNode != null && aNode.length > 0) {
                    for (int j = 0; j < aNode.length; j++) {
                        String aValue = req.getParameter(aName + "_" + aNode[j] + "_value");
                        complete_up.getStructureStylesheetUserPreferences().setChannelAttributeValue(aNode[j], aName, aValue);
                        LogService.log(LogService.DEBUG, "UserPreferencesManager::processUserPreferencesParameters() : setting scattr \"" + aName + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
                    }
                }
            }
        }
        // theme stylesheet attributes
        String[] tcattrs = req.getParameterValues("uP_tcattr");
        if (tcattrs != null) {
            for (int i = 0; i < tcattrs.length; i++) {
                String aName = tcattrs[i];
                String[] aNode = req.getParameterValues(aName + "_channelId");
                if (aNode != null && aNode.length > 0) {
                    for (int j = 0; j < aNode.length; j++) {
                        String aValue = req.getParameter(aName + "_" + aNode[j] + "_value");
                        complete_up.getThemeStylesheetUserPreferences().setChannelAttributeValue(aNode[j], aName, aValue);
                        LogService.log(LogService.DEBUG, "UserPreferencesManager::processUserPreferencesParameters() : setting tcattr \"" + aName + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
                    }
                }
            }
        }
    }

    /**
     * Returns current person object
     * @return current <code>IPerson</code>
     */
    public IPerson getPerson () {
        return  (m_person);
    }

    /**
     * Returns a global channel Id given a channel instance Id
     * @param channelSubscribeId subscribe id of a channel
     * @return channel global id
     */
    protected String getChannelPublishId(String channelSubscribeId) throws PortalException {
        // Get the channel node from the user's layout
        IUserLayoutChannelDescription channel=(IUserLayoutChannelDescription) getUserLayoutManager().getNode(channelSubscribeId);
        if(channel!=null) {
            return channel.getChannelPublishId();
        } else {
            return null;
        }
    }

    public boolean isUserAgentUnmapped() {
        return  unmapped_user_agent;
    }

    /*
     * Resets both user layout and user preferences.
     * Note that if any of the two are "null", old values will be used.
     */
    public void setNewUserLayoutAndUserPreferences(IUserLayoutManager newUlm, UserPreferences newPreferences) throws PortalException {
      try {
        if (newPreferences != null) {
            // see if the profile has changed
            if(complete_up.getProfile().getProfileId()!=newPreferences.getProfile().getProfileId() || complete_up.getProfile().isSystemProfile()!=newPreferences.getProfile().isSystemProfile()) {
                // see if a layout was passed
                if(newUlm !=null && newUlm.getLayoutId()==newPreferences.getProfile().getLayoutId()) {
                    // just use a new layout
                    this.ulm=newUlm;
                } else {
                    // construct a new user layout manager, for a new profile
                    ulm=UserLayoutManagerFactory.getUserLayoutManager(m_person,newPreferences.getProfile());
                }
            }
            ulsdb.putUserPreferences(m_person, newPreferences);
            complete_up=newPreferences;

        }
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
        throw  new GeneralRenderingException(e.getMessage());
      }
    }

    public IUserLayoutManager getUserLayoutManager() {
        return ulm;
    }

    public void finishedSession(HttpSessionBindingEvent bindingEvent) {
        // persist the layout and user preferences
        try {
            if(saveUserPreferencesAtLogout) {
                ulsdb.putUserPreferences(m_person, complete_up);
                ulm.saveUserLayout();
            }
        } catch (Exception e) {
            LogService.log(LogService.ERROR,"UserPreferencesManager::finishedSession() : unable to persist layout upon session termination !", e);
        }
    }

    public UserPreferences getUserPreferencesCopy() {
        return  new UserPreferences(this.getUserPreferences());
    }


    public UserProfile getCurrentProfile() {
        return  this.getUserPreferences().getProfile();
    }

    public ThemeStylesheetDescription getThemeStylesheetDescription() throws Exception {
        if (this.tsd == null) {
           tsd = ulsdb.getThemeStylesheetDescription(this.getCurrentProfile().getThemeStylesheetId());
        }
        return  tsd;
    }

    public StructureStylesheetDescription getStructureStylesheetDescription() throws Exception {
        if (this.ssd == null) {
            ssd = ulsdb.getStructureStylesheetDescription(this.getCurrentProfile().getStructureStylesheetId());
        }
        return  ssd;
    }

    public UserPreferences getUserPreferences() {
        return complete_up;
    }
}



