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

package org.jasig.portal;

import java.util.Enumeration;
import java.util.HashSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Object managing user preferences.
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class UserPreferences {

    protected UserProfile profile;

    protected StructureStylesheetUserPreferences fsup;
    protected ThemeStylesheetUserPreferences ssup;

    /*
     * Copy-constructor
     */
    public UserPreferences(UserPreferences up) {
        fsup=new StructureStylesheetUserPreferences(up.getStructureStylesheetUserPreferences());
        ssup=new ThemeStylesheetUserPreferences(up.getThemeStylesheetUserPreferences());
        profile=up.getProfile();
    }

    public void setProfile(UserProfile p) { profile=p; }
    public UserProfile getProfile() { return profile; }

    public UserPreferences(UserProfile p) { this.profile=p; }

    public void setStructureStylesheetUserPreferences(StructureStylesheetUserPreferences up) {
        this.fsup=up;
    }

    public void setThemeStylesheetUserPreferences(ThemeStylesheetUserPreferences up) {
        this.ssup=up;
    }

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences() {
        return this.fsup;
    }

    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences() {
        return this.ssup;
    }

    public void synchronizeWithUserLayoutXML(Document uLayoutXML) throws PortalException {
        // make a list of channels in the XML Layout
        NodeList channelNodes=uLayoutXML.getElementsByTagName("channel");
        HashSet channelSet=new HashSet();
        for(int i=0;i<channelNodes.getLength();i++) {
            Element el=(Element) channelNodes.item(i);
            if(el!=null) {
                String channelSubscribeId=el.getAttribute("ID");
                if(!fsup.hasChannel(channelSubscribeId)) {
                    fsup.addChannel(channelSubscribeId);
                    //		    log.debug("UserPrefenreces::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences were missing a channel="+channelSubscribeId);
                }
                if(!ssup.hasChannel(channelSubscribeId)) {
                    ssup.addChannel(channelSubscribeId);
                    //		    log.debug("UserPreferences::synchUserPreferencesWithLayout() : ThemeStylesheetUserPreferences were missing a channel="+channelSubscribeId);
                }
                channelSet.add(channelSubscribeId);
            }

        }

        // make a list of categories in the XML Layout
        NodeList folderNodes=uLayoutXML.getElementsByTagName("folder");
        HashSet folderSet=new HashSet();
        for(int i=0;i<folderNodes.getLength();i++) {
            Element el=(Element) folderNodes.item(i);
            if(el!=null) {
                String caID=el.getAttribute("ID");
                if(!fsup.hasFolder(caID)) {
                    fsup.addFolder(caID);
                    //		    log.debug("UserPreferences::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences were missing a folder="+caID);
                }
                folderSet.add(caID);
            }
        }

        // cross check
        for(Enumeration e=fsup.getChannels();e.hasMoreElements();) {
            String chID=(String)e.nextElement();
            if(!channelSet.contains(chID)) {
                fsup.removeChannel(chID);
                //		log.debug("UserPreferences::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences had a non-existent channel="+chID);
            }
        }

        for(Enumeration e=fsup.getFolders();e.hasMoreElements();) {
            String caID=(String)e.nextElement();
            if(!folderSet.contains(caID)) {
                fsup.removeFolder(caID);
                //		log.debug("UserPreferences::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences had a non-existent folder="+caID);
            }
        }

        for(Enumeration e=ssup.getChannels();e.hasMoreElements();) {
            String chID=(String)e.nextElement();
            if(!channelSet.contains(chID)) {
                ssup.removeChannel(chID);
                //		log.debug("UserPreferences::synchUserPreferencesWithLayout() : ThemeStylesheetUserPreferences had a non-existent channel="+chID);
            }
        }

    }

    /**
     * Generates a unique state key
     *
     * @return a <code>String</code> key
     */
    public String getCacheKey() {
        String key=null;
        if(profile.isSystemProfile()) {
            key="system "+Integer.toString(profile.getProfileId());
        } else {
            key=Integer.toString(profile.getProfileId());
        }
        key=key.concat(fsup.getCacheKey());
        key=key.concat(ssup.getCacheKey());
        return key;

    }

}
