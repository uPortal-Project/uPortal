/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.container.om.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.entity.PortletEntityCtrl;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowList;
import org.apache.pluto.om.window.PortletWindowListCtrl;
import org.jasig.portal.container.om.common.ObjectIDImpl;
import org.jasig.portal.container.om.common.PreferenceSetImpl;
import org.jasig.portal.container.om.window.PortletWindowListImpl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletEntityImpl implements PortletEntity, PortletEntityCtrl, Serializable {

    private ObjectID objectId = null;
    private PreferenceSet originalPreferences = null;
    private PreferenceSet preferences = null;
    private PortletDefinition portletDefinition = null;
    private PortletApplicationEntity portletApplicationEntity = null;
    private PortletWindowList portletWindows = null;

    // PortletEntity methods
    
    public PortletEntityImpl() {
        preferences = new PreferenceSetImpl();
        portletWindows = new PortletWindowListImpl();
    }

    public ObjectID getId() {
        return objectId;
    }

    public PreferenceSet getPreferenceSet() {
        return preferences;
    }

    public PortletDefinition getPortletDefinition() {
        return portletDefinition;
    }

    public PortletApplicationEntity getPortletApplicationEntity() {
        return portletApplicationEntity;
    }

    public PortletWindowList getPortletWindowList() {
        return portletWindows;
    }

    public Description getDescription(Locale locale) {
        return portletDefinition.getDescription(locale);
    }

    // PortletEntityCtrl methods
    
    public void setId(String id) {
        this.objectId = ObjectIDImpl.createFromString(id);
    }

    public void setPortletDefinition(PortletDefinition portletDefinition) {
        this.portletDefinition = portletDefinition;
    }

    public void store() throws IOException {
        // Persist portlet entity (preferences)
        //TODO Persist the portlet entity

        // Save preferences as original preferences
        originalPreferences = new PreferenceSetImpl();
        ((PreferenceSetImpl)originalPreferences).addAll(preferences);
    }

    public void reset() throws IOException {
        ((PreferenceSetImpl)preferences).clear();
        if (originalPreferences != null) {
            ((PreferenceSetImpl)preferences).addAll(originalPreferences);
        }
    }

    // Additional methods
    
    public void setPreferences(PreferenceSet preferences) {
        this.preferences = preferences;
    }
    
    public void addPortletWindow(PortletWindow portletWindow) {
        ((PortletWindowListCtrl)portletWindows).add(portletWindow);
    }
}
