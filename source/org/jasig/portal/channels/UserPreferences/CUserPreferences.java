/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.channels.UserPreferences;

import org.jasig.portal.*;
import org.jasig.portal.security.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.w3c.dom.*;
import org.apache.xalan.xslt.*;
import org.xml.sax.DocumentHandler;
import java.io.*;
import java.util.*;
/** <p>Manages User Layout, user preferences and profiles </p>
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CUserPreferences implements IPrivilegedChannel
{

    UserLayoutManager ulm;

    ChannelRuntimeData runtimeData = null;
    StylesheetSet set = null;

    private static final String fs = File.separator;
    private static final String portalBaseDir = GenericPortalBean.getPortalBaseDir ();
    String stylesheetDir = portalBaseDir + fs + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CUserPreferences";

    private UserPreferences up=null;
    private Document userLayoutXML = null;

    private int mode;
    public static final int MANAGE_PREFERENCES = 1;
    public static final int MANAGE_PROFILES = 2;

    IPrivilegedChannel internalState=null;
    IPrivilegedChannel managePreferences=null;
    IPrivilegedChannel manageProfiles=null;
    protected IUserPreferencesDB updb;
    private PortalControlStructures pcs;

    public CUserPreferences ()
    {
        this.runtimeData = new ChannelRuntimeData ();
        this.set = new StylesheetSet (stylesheetDir + fs + "CUserPreferences.ssl");
        this.set.setMediaProps (portalBaseDir + fs + "properties" + fs + "media.properties");
        // initial state should be manage preferences
        manageProfiles=new ManageProfilesState(this);
        managePreferences=new GPreferencesState(this);
        internalState=managePreferences;
    }

    protected UserLayoutManager getUserLayoutManager() { return ulm; }
    protected UserPreferences getCurrentUserPreferences() { return up; }
    protected ChannelRuntimeData getRuntimeData() { return runtimeData; }
    protected StylesheetSet getStylesheetSet() { return set; }

    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        if(ulm==null)
            ulm=pcs.getUserLayoutManager();

        if(up==null)
            up=ulm.getUserPreferencesCopy();
        // instantiate the browse state here
        this.pcs=pcs;
        internalState.setPortalControlStructures(pcs);

    }


    /** Returns static channel properties to the portal
     * @return handle to subscription properties
     */
    public ChannelSubscriptionProperties getSubscriptionProperties ()
    {
        ChannelSubscriptionProperties csb = new ChannelSubscriptionProperties ();

        // Properties which are not specifically set here will assume default
        // values as determined by ChannelSubscriptionProperties
        csb.setName ("User Preferences");
        return csb;
    }

    /** Returns channel runtime properties
     * @return handle to runtime properties
     */
    public ChannelRuntimeProperties getRuntimeProperties ()
    {
        // Channel will always render, so the default values are ok
        return new ChannelRuntimeProperties ();
    }

    /** Processes layout-level events coming from the portal
     * @param ev a portal layout event
     */
    public void receiveEvent (LayoutEvent ev)
    {
        // no events for this channel
        internalState.receiveEvent(ev);
    }

    /** Receive static channel data from the portal
     * @param sd static channel data
     */
    public void setStaticData (ChannelStaticData sd) throws PortalException
    {
        internalState.setStaticData(sd);
    }

    /** CUserPreferences listens for an HttpRequestParameter "userPreferencesAction"
     * and based on its value changes state between profile management and layout/stylesheet
     * preferences.
     * @param rd handle to channel runtime data
     */
    public void setRuntimeData (ChannelRuntimeData rd) throws PortalException
    {
        this.runtimeData = rd;
        String action = runtimeData.getParameter ("userPreferencesAction");
        if(action!=null) {
            String profileName=runtimeData.getParameter("profileName");
            boolean systemProfile=false;
            if(profileName!=null) {
                String profileType=runtimeData.getParameter("profileType");
                if(profileType!=null && profileType.equals("system")) systemProfile=true;
            }

            if(action.equals("manageProfiles")) {
                if(profileName!=null) {
                    //if(!profile.equals(currentProfileName)) {
                    // need a new manage preferences state

                    //			}
                } else {
                    // reset to the manage profiles state
                    manageProfiles.setRuntimeData(rd);
                    this.internalState=manageProfiles;
                }
            } else if(action.equals("managePreferences")) {
                UserProfile profile=null;
                if(profileName!=null) {
                    // find the profile mapping
                    updb=new UserPreferencesDBImpl();
                    if(systemProfile)
                        profile=updb.getSystemProfileByName(profileName);
                    else
                        profile=updb.getUserProfileById(ulm.getPerson().getID(),profileName);
                } else {
                    profile=up.getProfile();
                }
                managePreferences.setRuntimeData(rd);
                this.internalState=managePreferences;

            }
        }
        if(internalState!=null)
            internalState.setRuntimeData(rd);

    }

    /** Output channel content to the portal
     * @param out a sax document handler
   */
    public void renderXML (DocumentHandler out)  throws PortalException
    {
        internalState.renderXML(out);
    }


    private void prepareSaveChanges () {
        // write code to persist the userLayoutXML to the session
        // and the database (remember, as the user interacts with this
        // channel, changes are only made to a copy of the userLayoutXML
        // until this method is called)

        ulm.setNewUserLayoutAndUserPreferences(userLayoutXML,up);
    }
}
