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
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;

import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.jndi.JNDIManager;
import org.jasig.portal.layout.IUserLayoutChannelDescription;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.UserLayoutManagerFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.PropsMatcher;

/**
 * Multithreaded version of {@link UserPreferencesManager}.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 * @see UserPreferencesManager
 */
public class GuestUserPreferencesManager extends UserPreferencesManager  {
    
    private static final Log log = LogFactory.getLog(GuestUserPreferencesManager.class);
    
    private class MState {
        private ThemeStylesheetDescription tsd;
        private StructureStylesheetDescription ssd;
        private boolean unmapped_user_agent;
        private UserPreferences complete_up;
        private IUserLayoutManager ulm;
        public MState() {
            tsd=null; ssd=null; complete_up=null;
            unmapped_user_agent=false;
        }
    }

    Map stateTable;

    // tables keeping user layouts and clean user preferences for various profiles
    Hashtable sp_layouts;
    Hashtable up_layouts;

    Hashtable sp_cleanUPs;
    Hashtable up_cleanUPs;
    Hashtable ts_descripts;
    Hashtable ss_descripts;
    Hashtable cached_profiles;

    IPerson m_person;
    LocaleManager localeManager;

    final static boolean SAVE_PROFILE_GUESSES=PropertiesManager.getPropertyAsBoolean("org.jasig.portal.GuestUserPreferencesManager.save_profile_guesses");

    /**
     * Initializing constructor.
     * @param person object
     */
    public GuestUserPreferencesManager (IPerson person) {
        super(person);
        stateTable=Collections.synchronizedMap(new HashMap());
        up_cleanUPs=new Hashtable();
        sp_cleanUPs=new Hashtable();
        sp_layouts=new Hashtable();
        up_layouts=new Hashtable();
        cached_profiles=new Hashtable();
        ts_descripts=new Hashtable();
        ss_descripts=new Hashtable();
        m_person = person;
        ulsdb = UserLayoutStoreFactory.getUserLayoutStoreImpl();
    }


    /**
     * Unbinds a registered session.
     * @param sessionId a <code>String</code> value
     */
    public void unbindSession(String sessionId) {
        stateTable.remove(sessionId);
    }

    /**
     * Register arrival of a new session.
     * Create and populate new state entry.
     * @param req a <code>HttpServletRequest</code> value
     * @exception PortalException if an error occurs
     */
    public void registerSession(HttpServletRequest req) throws PortalException {
        MState newState=new MState();
        try {
            // load user preferences
            // determine user profile
            String userAgent = req.getHeader("User-Agent");
            if(userAgent==null || userAgent.equals("")) { 
                userAgent=MediaManager.NULL_USER_AGENT; 
            }
            UserProfile upl;
            // see if the profile was cached
            if((upl=(UserProfile)cached_profiles.get(userAgent))==null) {
                synchronized(cached_profiles) {
                    upl= ulsdb.getUserProfile(m_person, userAgent);
                    if (upl == null) {
                        upl = ulsdb.getSystemProfile(userAgent);
                    }
                    if(upl!=null) {
                        cached_profiles.put(userAgent,upl);
                    }
                }
            }

            if(upl==null) {
                // try guessing the profile through pattern matching

                if(uaMatcher==null) {
                    // init user agent matcher
                    URL url = null;
                    try {
                        url = this.getClass().getResource("/properties/browser.mappings");
                        if (url != null) {
                            uaMatcher = new PropsMatcher(url.openStream());
                        }
                    } catch (IOException ioe) {
                        log.error( "GuestUserPreferencesManager::GuestUserPreferencesManager() : Exception occurred while loading browser mapping file: " + url + ". " + ioe);
                    }
                }

                if(uaMatcher!=null) {
                    // try matching
                    String profileId=uaMatcher.match(userAgent);
                    if(profileId!=null) {
                        // user agent has been matched
                        log.debug("GuestUserPreferencesManager::GuestUserPreferencesManager() : userAgent \"" + userAgent + "\" has matched to a profile " + profileId);
                        upl=ulsdb.getSystemProfileById(Integer.parseInt(profileId));
                        // save mapping
                        if(SAVE_PROFILE_GUESSES) {
                            ulsdb.setSystemBrowserMapping(userAgent,upl.getProfileId());
                        }
                    } else {
                        log.debug("GuestUserPreferencesManager::GuestUserPreferencesManager() : userAgent \"" + userAgent + "\" has not matched any profile.");
                    }
                }
            }

            if (upl != null) {
                // see if the user layout xml has been cached
                if(upl.isSystemProfile()) {
                    newState.ulm=(IUserLayoutManager)sp_layouts.get(new Integer(upl.getProfileId()));
                } else {
                    newState.ulm=(IUserLayoutManager)up_layouts.get(new Integer(upl.getProfileId()));
                }
                if(newState.ulm==null) {
                    try {
			upl.setLocaleManager(localeManager);
                        newState.ulm=UserLayoutManagerFactory.immutableUserLayoutManager(UserLayoutManagerFactory.getUserLayoutManager(m_person,upl));
                        if(upl.isSystemProfile()) {
                            sp_layouts.put(new Integer(upl.getProfileId()),newState.ulm);
                        } else {
                            up_layouts.put(new Integer(upl.getProfileId()),newState.ulm);
                        }
                    } catch (PortalException pe) {
                        throw pe;
                    } catch (Exception e) {
                        throw new PortalException("GuestUserPreferencesManager::registerSession() : caught an exception while trying to retreive a userLayout for user=\"" +m_person.getID()+ "\", profile=\"" + upl.getProfileName() + "\".",e);
                    }
                }

                /*
                // modify the entire profile to be unremovable and immutable
                // mark all of the folders
                NodeList folderList=newState.uLayoutXML.getElementsByTagName("folder");
                for(int i=0;i<folderList.getLength();i++) {
                    Element e=(Element)folderList.item(i);
                    e.setAttribute("immutable","true");
                    e.setAttribute("unremovable","true");                        
                }
                // mark all of the channels
                NodeList channelList=newState.uLayoutXML.getElementsByTagName("channel");
                for(int i=0;i<channelList.getLength();i++) {
                    Element e=(Element)channelList.item(i);
                    e.setAttribute("immutable","true");
                    e.setAttribute("unremovable","true");                        
                }
                */


                // see if the user preferences for this profile are cached
                UserPreferences cleanUP;
                if(upl.isSystemProfile()) {
                    cleanUP=(UserPreferences)sp_cleanUPs.get(new Integer(upl.getProfileId()));
                } else {
                    cleanUP=(UserPreferences)up_cleanUPs.get(new Integer(upl.getProfileId()));
                }
                if(cleanUP==null) {
                    try {
                        cleanUP=ulsdb.getUserPreferences(m_person, upl);
                        if(cleanUP!=null) {
                            if(upl.isSystemProfile()) {
                                sp_cleanUPs.put(new Integer(upl.getProfileId()),cleanUP);
                            } else {
                                up_cleanUPs.put(new Integer(upl.getProfileId()),cleanUP);
                            }
                        }
                    } catch (Exception e) {
                        log.error("GuestUserPreferencesManager::registerSession() : unable to find UP for a profile \""+upl.getProfileName()+"\"");
                        cleanUP=new UserPreferences(upl);
                    }
                }

                if(cleanUP!=null) {
                    newState.complete_up=new UserPreferences(cleanUP);
                } else {
                    log.error("GuestUserPreferencesManager::registerSession() : unable to find UP for a profile \""+upl.getProfileName()+"\"");
                    newState.complete_up=new UserPreferences(upl);
                }

                // Initialize the JNDI context for this user
                JNDIManager.initializeSessionContext(req.getSession(),Integer.toString(m_person.getID()),Integer.toString(upl.getLayoutId()),newState.ulm.getUserLayoutDOM());
            } else {
                // there is no user-defined mapping for this particular browser.
                // user should be redirected to a browser-registration page.
                newState.unmapped_user_agent = true;
                log.debug("GuestUserPreferencesManager::registerSession() : unable to find a profile for user \"" + m_person.getID() + "\" and userAgent=\"" + userAgent + "\".");
            }
        } catch (PortalException pe) {
            throw pe;
        } catch (Throwable t) {
            if(t instanceof Exception) {
                throw new PortalException((Exception)t);
            } else {
                throw new PortalException(t.toString());
            }
        }
        stateTable.put(req.getSession(false).getId(),newState);
    }


    /* This function processes request parameters related to
     * setting Structure/Theme stylesheet parameters and attributes.
     * (uP_sparam, uP_tparam, uP_sfattr, uP_scattr uP_tcattr)
     * It also processes layout root requests (uP_root)
     */
    public void processUserPreferencesParameters(HttpServletRequest req) {
        MState state=(MState)stateTable.get(req.getSession(false).getId());
        if(state==null) {
            log.error("GuestUserPreferencesManager::processUserPreferencesParameters() : trying to envoke a method on a non-registered sessionId=\""+req.getSession(false).getId()+"\".");
            return;
        }
        // layout root setting
        String root;
        if ((root = req.getParameter("uP_root")) != null) {
            // If a channel specifies "me" as its root, set the root
            // to the channel's instance Id
            if (root.equals("me")) {
                // get uPFile spec and search for "channel" clause
                UPFileSpec upfs=new UPFileSpec(req);
                root=upfs.getTargetNodeId();
            }
            if(root!=null) {
                state.complete_up.getStructureStylesheetUserPreferences().putParameterValue("userLayoutRoot", root);
            } else {
                log.error( "GuestUserPreferencesManager::processUserPreferencesParameters() : unable to extract channel ID. servletPath=\""+req.getServletPath()+"\".");
            }
        }
        // other params
        String[] sparams = req.getParameterValues("uP_sparam");
        if (sparams != null) {
            for (int i = 0; i < sparams.length; i++) {
                String pValue = req.getParameter(sparams[i]);
                state.complete_up.getStructureStylesheetUserPreferences().putParameterValue(sparams[i], pValue);
                log.debug("GuestUserPreferencesManager::processUserPreferencesParameters() : setting sparam \"" + sparams[i]
                           + "\"=\"" + pValue + "\".");
            }
        }
        String[] tparams = req.getParameterValues("uP_tparam");
        if (tparams != null) {
            for (int i = 0; i < tparams.length; i++) {
                String pValue = req.getParameter(tparams[i]);
                state.complete_up.getThemeStylesheetUserPreferences().putParameterValue(tparams[i], pValue);
                log.debug("GuestUserPreferencesManager::processUserPreferencesParameters() : setting tparam \"" + tparams[i]
                           + "\"=\"" + pValue + "\".");
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
                        state.complete_up.getStructureStylesheetUserPreferences().setFolderAttributeValue(aNode[j], aName, aValue);
                        log.debug("GuestUserPreferencesManager::processUserPreferencesParameters() : setting sfattr \"" + aName
                                   + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
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
                        state.complete_up.getStructureStylesheetUserPreferences().setChannelAttributeValue(aNode[j], aName, aValue);
                        log.debug("GuestUserPreferencesManager::processUserPreferencesParameters() : setting scattr \"" + aName
                                   + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
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
                        state.complete_up.getThemeStylesheetUserPreferences().setChannelAttributeValue(aNode[j], aName, aValue);
                        log.debug("GuestUserPreferencesManager::processUserPreferencesParameters() : setting tcattr \"" + aName
                                   + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
                    }
                }
            }
        }
    }

    /**
     * Returns a global channel Id given a channel instance Id
     * @return Channel's global Id
     */
    protected String getChannelGlobalId (String channelSubscribeId, String sessionId) throws PortalException {
        // Get the channel node from the user's layout
        IUserLayoutChannelDescription channel=(IUserLayoutChannelDescription) getUserLayoutManager(sessionId).getNode(channelSubscribeId);
        if(channel!=null) {
            return channel.getChannelPublishId();
        } else {
            return null;
        }
    }

    public boolean isUserAgentUnmapped (String sessionId) {
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            log.error("GuestUserPreferencesManager::userAgentUnmapped() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return false;
        }
        return  state.unmapped_user_agent;
    }

    public boolean isUserAgentUnmapped () {
        throw new UnsupportedOperationException();
    }

    public UserPreferences getUserPreferences (String sessionId) {
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            log.error("GuestUserPreferencesManager::getUserPreferences() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return null;
        }
        return  state.complete_up;
    }

    public UserPreferences getUserPreferences () {
        throw new UnsupportedOperationException();
    }

    /*
     * Guest users can not (by definition) save any preferences. Method does nothing.
     */
    public void setNewUserLayoutAndUserPreferences (IUserLayoutManager newLayout, UserPreferences newPreferences,String sessionId) throws PortalException {
        // not implemented yet
    }

    public void setNewUserLayoutAndUserPreferences (IUserLayoutManager newLayout, UserPreferences newPreferences) throws PortalException {
        throw new UnsupportedOperationException();
    }



    public UserPreferences getUserPreferencesCopy (String sessionId) {
        return  new UserPreferences(this.getUserPreferences(sessionId));
    }

    public UserPreferences getUserPreferencesCopy () {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns current profile
     * @return UserProfile
     */
    public UserProfile getCurrentProfile (String sessionId) {
        return  this.getUserPreferences(sessionId).getProfile();
    }

    public UserProfile getCurrentProfile () {
        throw new UnsupportedOperationException();
    }

    public ThemeStylesheetDescription getThemeStylesheetDescription (String sessionId) throws Exception {
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            log.error("GuestUserPreferencesManager::getThemeStylesheetDescription() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return null;
        }
        if (state.tsd == null) {
            int sid=state.complete_up.getProfile().getThemeStylesheetId();
            state.tsd=(ThemeStylesheetDescription)ts_descripts.get(new Integer(sid));
            if(state.tsd==null) {
                state.tsd = ulsdb.getThemeStylesheetDescription(sid);
                ts_descripts.put(new Integer(sid),state.tsd);
            }
        }
        return  state.tsd;
    }

    public ThemeStylesheetDescription getThemeStylesheetDescription () {
        throw new UnsupportedOperationException();
    }

    public StructureStylesheetDescription getStructureStylesheetDescription (String sessionId) throws Exception{
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            log.error("GuestUserPreferencesManager::getThemeStylesheetDescription() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return null;
        }
        if (state.ssd == null) {
            int sid=state.complete_up.getProfile().getStructureStylesheetId();
            state.ssd=(StructureStylesheetDescription)ss_descripts.get(new Integer(sid));
            if(state.ssd==null) {
                state.ssd = ulsdb.getStructureStylesheetDescription(sid);
                ss_descripts.put(new Integer(sid),state.ssd);
            }
        }
        return  state.ssd;
    }

    public StructureStylesheetDescription getStructureStylesheetDescription () {
        throw new UnsupportedOperationException();
    }

    public IUserLayoutManager getUserLayoutManager(String sessionId) {
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            log.error("GuestUserPreferencesManager::getUserLayout() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return null;
        }
        return  state.ulm;
    }

    public IUserLayoutManager getUserLayoutManager() {
        throw new UnsupportedOperationException();
    }

    public void finishedSession(HttpSessionBindingEvent bindingEvent, String sessionId) {
        // remove session state info
        stateTable.remove(sessionId);
    }

    public void finishedSession(HttpSessionBindingEvent bindingEvent) {
        throw new UnsupportedOperationException();
    }

    public void setLocaleManager(LocaleManager lm) {
	localeManager = lm;
    }

}



