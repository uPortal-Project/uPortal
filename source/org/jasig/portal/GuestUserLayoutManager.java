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

import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.jndi.JNDIManager;
import org.jasig.portal.utils.BooleanLock;
import org.jasig.portal.services.LogService;
import  java.sql.*;
import  org.w3c.dom.*;

import  javax.servlet.*;
import  javax.servlet.jsp.*;
import  javax.servlet.http.*;
import  java.io.*;
import  java.util.*;
import  java.text.*;


/**
 * Multithreaded version of {@link UserLayoutManager}.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 * @see UserLayoutManager
 */
public class GuestUserLayoutManager extends UserLayoutManager  {
    private class MState {
        private ThemeStylesheetDescription tsd;
        private StructureStylesheetDescription ssd;
        private boolean unmapped_user_agent;
        private UserPreferences complete_up;
        private Document uLayoutXML;
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
    IUserPreferencesStore updb;
    ICoreStylesheetDescriptionStore csddb;


    /**
     * Initializing constructor.
     *  @param the servlet request object
     *  @param person object
     */
    public GuestUserLayoutManager (IPerson person) {
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
        updb = UserPreferencesStoreFactory.getUserPreferencesStoreImpl();
        csddb = CoreStylesheetDescriptionStoreFactory.getCoreStylesheetDescriptionStoreImpl();
        layout_write_lock.setValue(true);
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
     */
    public void registerSession(HttpServletRequest req) {
        MState newState=new MState();
        try {
            // load user preferences
            // determine user profile
            String userAgent = req.getHeader("User-Agent");
            UserProfile upl;
            // see if the profile was cached
            if((upl=(UserProfile)cached_profiles.get(userAgent))==null) {
                synchronized(cached_profiles) {
                    upl= updb.getUserProfile(m_person, userAgent);
                    if (upl == null) {
                        upl = updb.getSystemProfile(userAgent);
                    }
                    if(upl!=null) {
                        cached_profiles.put(userAgent,upl);
                    }
                }
            }

            if (upl != null) {
                // see if the user layout xml has been cached
                if(upl.isSystemProfile()) {
                    newState.uLayoutXML=(Document)sp_layouts.get(new Integer(upl.getProfileId()));
                } else {
                    newState.uLayoutXML=(Document)up_layouts.get(new Integer(upl.getProfileId()));
                }
                if(newState.uLayoutXML==null) {
                    // read uLayoutXML
                    newState.uLayoutXML = UserLayoutStoreFactory.getUserLayoutStoreImpl().getUserLayout(m_person, upl.getProfileId());
                    if(newState.uLayoutXML!=null) {
                        if(upl.isSystemProfile()) {
                            sp_layouts.put(new Integer(upl.getProfileId()),newState.uLayoutXML);
                        } else {
                            up_layouts.put(new Integer(upl.getProfileId()),newState.uLayoutXML);
                        }
                    }
                }

                if (newState.uLayoutXML == null) {
                    LogService.instance().log(LogService.ERROR, "UserLayoutManager::UserLayoutManager() : unable to retreive userLayout for user=\"" +
                               m_person.getID() + "\", profile=\"" + upl.getProfileName() + "\".");
                }

                // see if the user preferences for this profile are cached
                UserPreferences cleanUP;
                if(upl.isSystemProfile()) {
                    cleanUP=(UserPreferences)sp_cleanUPs.get(new Integer(upl.getProfileId()));
                } else {
                    cleanUP=(UserPreferences)up_cleanUPs.get(new Integer(upl.getProfileId()));
                }
                if(cleanUP==null) {
                    cleanUP=updb.getUserPreferences(m_person, upl);
                    if(cleanUP!=null) {
                        if(upl.isSystemProfile()) {
                            sp_cleanUPs.put(new Integer(upl.getProfileId()),cleanUP);
                        } else {
                            up_cleanUPs.put(new Integer(upl.getProfileId()),cleanUP);
                        }
                    }
                }

                if(cleanUP!=null) {
                    newState.complete_up=new UserPreferences(cleanUP);
                } else {
                    LogService.instance().log(LogService.ERROR,"GuestUserLayoutManager::registerSession() : unable to find UP for a profile \""+upl.getProfileName()+"\"");
                }

                // Initialize the JNDI context for this user
                //JNDIManager.initializeUserContext(newState.uLayoutXML, req.getSession(), m_person);
            } else {
                // there is no user-defined mapping for this particular browser.
                // user should be redirected to a browser-registration page.
                newState.unmapped_user_agent = true;
                LogService.instance().log(LogService.DEBUG, "GuestUserLayoutManager::registerSession() : unable to find a profile for user \"" + m_person.getID()
                           + "\" and userAgent=\"" + userAgent + "\".");
            }
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }

        stateTable.put(req.getSession(false).getId(),newState);
    }


    /* This function processes request parameters related to
     * setting Structure/Theme stylesheet parameters and attributes.
     * (uP_sparam, uP_tparam, uP_sfattr, uP_scattr uP_tcattr)
     * It also processes layout root requests (uP_root)
     */
    public void processUserPreferencesParameters (HttpServletRequest req) {
        MState state=(MState)stateTable.get(req.getSession(false).getId());
        if(state==null) {
            LogService.instance().log(LogService.ERROR,"GuestUserLayoutManager::processUserPreferencesParameters() : trying to envoke a method on a non-registered sessionId=\""+req.getSession(false).getId()+"\".");
            return;
        }
        // layout root setting
        String root;
        if ((root = req.getParameter("uP_root")) != null) {
            // If a channel specifies "me" as its root, set the root
            // to the channel's instance Id
            if (root.equals("me")) {
                String chanInstanceId = null;
                String servletPath = req.getServletPath();
                String searchFor = "/channel/";
                int chanIdBegIndex = servletPath.indexOf(searchFor) + searchFor.length();
                if (chanIdBegIndex != -1) {
                    int chanIdEndIndex = servletPath.indexOf("/", chanIdBegIndex);
                    root = servletPath.substring(chanIdBegIndex, chanIdEndIndex);
                }
            }
            state.complete_up.getStructureStylesheetUserPreferences().putParameterValue("userLayoutRoot", root);
        }
        // other params
        String[] sparams = req.getParameterValues("uP_sparam");
        if (sparams != null) {
            for (int i = 0; i < sparams.length; i++) {
                String pValue = req.getParameter(sparams[i]);
                state.complete_up.getStructureStylesheetUserPreferences().putParameterValue(sparams[i], pValue);
                LogService.instance().log(LogService.DEBUG, "GuestUserLayoutManager::processUserPreferencesParameters() : setting sparam \"" + sparams[i]
                           + "\"=\"" + pValue + "\".");
            }
        }
        String[] tparams = req.getParameterValues("uP_tparam");
        if (tparams != null) {
            for (int i = 0; i < tparams.length; i++) {
                String pValue = req.getParameter(tparams[i]);
                state.complete_up.getThemeStylesheetUserPreferences().putParameterValue(tparams[i], pValue);
                LogService.instance().log(LogService.DEBUG, "GuestUserLayoutManager::processUserPreferencesParameters() : setting tparam \"" + tparams[i]
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
                        LogService.instance().log(LogService.DEBUG, "GuestUserLayoutManager::processUserPreferencesParameters() : setting sfattr \"" + aName
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
                        LogService.instance().log(LogService.DEBUG, "GuestUserLayoutManager::processUserPreferencesParameters() : setting scattr \"" + aName
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
                        LogService.instance().log(LogService.DEBUG, "GuestUserLayoutManager::processUserPreferencesParameters() : setting tcattr \"" + aName
                                   + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
                    }
                }
            }
        }
    }

    /**
     * Returns a global channel Id given a channel instance Id
     * @param channelInstanceId
     * @return Channel's global Id
     */
    public String getChannelGlobalId (String channelInstanceId, String sessionId) {
        // Get the channel node from the user's layout
        Node channelNode = getUserLayoutNode(channelInstanceId,sessionId);
        if (channelNode == null) {
            return  (null);
        }
        // Get the global channel Id from the channel node
        Node channelIdNode = channelNode.getAttributes().getNamedItem("chanID");
        if (channelIdNode == null) {
            return  (null);
        }
        // Return the channel's global Id
        return  (channelIdNode.getNodeValue());
    }

    public boolean isUserAgentUnmapped (String sessionId) {
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            LogService.instance().log(LogService.ERROR,"GuestUserLayoutManager::userAgentUnmapped() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
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
            LogService.instance().log(LogService.ERROR,"GuestUserLayoutManager::getUserPreferences() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return null;
        }
        return  state.complete_up;
    }

    public UserPreferences getUserPreferences () {
        throw new UnsupportedOperationException();
    }

    /*
     * Resets both user layout and user preferences.
     * Note that if any of the two are "null", old values will be used.
     */
    public void setNewUserLayoutAndUserPreferences (Document newLayout, UserPreferences newPreferences,String sessionId) throws PortalException {
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            LogService.instance().log(LogService.ERROR,"GuestUserLayoutManager::setCurrentUserPreferences() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return;
        }
        if (newPreferences != null) {
            state.complete_up=newPreferences;
            updb.putUserPreferences(m_person, newPreferences);
        }
        if (newLayout != null) {
            synchronized(layout_write_lock) {
                state.uLayoutXML = newLayout;
                // one lock for all - not very efficient, but ok for the Guest layout - it should rarely change
                layout_write_lock.setValue(true);
                try {
                    UserLayoutStoreFactory.getUserLayoutStoreImpl().setUserLayout(m_person, state.complete_up.getProfile().getProfileId(),newLayout);
                } catch (Exception e) {
                    LogService.instance().log(LogService.ERROR, e);
                    throw  new GeneralRenderingException(e.getMessage());
                }
            }
        }
    }

    public void setNewUserLayoutAndUserPreferences (Document newLayout, UserPreferences newPreferences) throws PortalException {
        throw new UnsupportedOperationException();
    }


    /**
     * Get a copy of user layout.
     * @return a copy of user layout
     */
    public Document getUserLayoutCopy (String sessionId) {
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            LogService.instance().log(LogService.ERROR,"GuestUserLayoutManager::getUserLayoutCopy() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return null;
        }
        return  UtilitiesBean.cloneDocument((org.apache.xerces.dom.DocumentImpl)state.uLayoutXML);
    }

    public Document getUserLayoutCopy () {
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
     * @return
     */
    public UserProfile getCurrentProfile (String sessionId) {
        return  this.getUserPreferences(sessionId).getProfile();
    }

    public UserProfile getCurrentProfile () {
        throw new UnsupportedOperationException();
    }

    public ThemeStylesheetDescription getThemeStylesheetDescription (String sessionId) {
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            LogService.instance().log(LogService.ERROR,"GuestUserLayoutManager::getThemeStylesheetDescription() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return null;
        }
        if (state.tsd == null) {
            int sid=state.complete_up.getProfile().getThemeStylesheetId();
            state.tsd=(ThemeStylesheetDescription)ts_descripts.get(new Integer(sid));
            if(state.tsd==null) {
                state.tsd = csddb.getThemeStylesheetDescription(sid);
                ts_descripts.put(new Integer(sid),state.tsd);
            }
        }
        return  state.tsd;
    }

    public ThemeStylesheetDescription getThemeStylesheetDescription () {
        throw new UnsupportedOperationException();
    }

    public StructureStylesheetDescription getStructureStylesheetDescription (String sessionId) {
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            LogService.instance().log(LogService.ERROR,"GuestUserLayoutManager::getThemeStylesheetDescription() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return null;
        }
        if (state.ssd == null) {
            int sid=state.complete_up.getProfile().getStructureStylesheetId();
            state.ssd=(StructureStylesheetDescription)ss_descripts.get(new Integer(sid));
            if(state.ssd==null) {
                state.ssd = csddb.getStructureStylesheetDescription(sid);
                ss_descripts.put(new Integer(sid),state.ssd);
            }
        }
        return  state.ssd;
    }

    public StructureStylesheetDescription getStructureStylesheetDescription () {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a node from a user layout
     * @param elementId node id
     * @return a node
     */
    public Node getUserLayoutNode (String elementId,String sessionId) {
        return  getUserLayout(sessionId).getElementById(elementId);
    }

    public Node getUserLayoutNode (String elementId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the root of the user layout
     * @return root node of the user layout
     */
    public Document getUserLayout (String sessionId) {
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            LogService.instance().log(LogService.ERROR,"GuestUserLayoutManager::getUserLayout() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return null;
        }
        return  state.uLayoutXML;
    }

    public Document getUserLayout () {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns user layout write lock
     *
     * @return an <code>Object</code> lock
     */
    public BooleanLock getUserLayoutWriteLock() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns user layout write lock
     *
     * @return an <code>Object</code> lock
     */
    public BooleanLock getUserLayoutWriteLock(String sessionId) {
        return super.getUserLayoutWriteLock();
    }

    /**
     * helper function that allows to determine the name of a channel or
     *  folder in the current user layout given their Id.
     * @param nodeId
     * @return
     */
    public String getNodeName (String nodeId,String sessionId) {
        Element node = getUserLayout(sessionId).getElementById(nodeId);
        if (node != null) {
            return  node.getAttribute("name");
        } else {
            return  null;
        }
    }

    public String getNodeName (String nodeId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a specified channel
     * @param channelId channel id
     */
    public boolean removeChannel (String channelId,String sessionId) throws PortalException {
        MState state=(MState)stateTable.get(sessionId);
        if(state==null) {
            LogService.instance().log(LogService.ERROR,"GuestUserLayoutManager::removeChannel() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return false;
        }
        Element channel = state.uLayoutXML.getElementById(channelId);
        if (channel != null) {
            boolean rval=true;
            synchronized(layout_write_lock) {
                if(!this.deleteNode(channel)) {
                    // unable to remove channel due to unremovable/immutable restrictionsn
                    LogService.instance().log(LogService.INFO,"GuestUserLayoutManager::removeChannlel() : unable to remove a channel \""+channelId+"\"");
                    rval=false;
                } else {
                    layout_write_lock.setValue(true);
                    // channel has been removed from the userLayoutXML .. persist the layout ?
                    // NOTE: this shouldn't be done every time a channel is removed. A separate portal event should initiate save
                    // (or, alternatively, an incremental update should be done on the UserLayoutStore())
                    try {
                        /*
                          The following patch has been kindly contributed by Neil Blake <nd_blake@NICKEL.LAURENTIAN.CA>.
                        */
                        UserLayoutStoreFactory.getUserLayoutStoreImpl().setUserLayout(m_person, state.complete_up.getProfile().getProfileId(), state.uLayoutXML);
                        /* end of patch */
                    } catch (Exception e) {
                        LogService.instance().log(LogService.ERROR,"GuestUserLayoutManager::removeChannle() : database operation resulted in an exception "+e);
                        throw new GeneralRenderingException("Unable to save layout changes.");
                    }
                }
            }
            return rval;
        } else {
            LogService.instance().log(LogService.ERROR, "GuestUserLayoutManager::removeChannel() : unable to find a channel with Id=" + channelId);
            return false;
        }
    }

    public boolean removeChannel (String channelId) throws PortalException {
        throw new UnsupportedOperationException();
    }
}



