/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.Preference;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.entity.PortletEntityCtrl;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowList;
import org.apache.pluto.om.window.PortletWindowListCtrl;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.IPortletPreferencesStore;
import org.jasig.portal.PortletPreferencesStoreFactory;
import org.jasig.portal.container.om.common.ObjectIDImpl;
import org.jasig.portal.container.om.common.PreferenceImpl;
import org.jasig.portal.container.om.common.PreferenceSetImpl;
import org.jasig.portal.container.om.window.PortletWindowListImpl;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.security.IPerson;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletEntityImpl implements PortletEntity, PortletEntityCtrl, Serializable {
    private static final Log log = LogFactory.getLog(PortletEntityImpl.class);
    private ObjectID objectId = null;
    private PreferenceSetImpl originalPreferences = null;
    private PreferenceSetImpl preferences = null;
    private PreferenceSetImpl publicationPreferences = null;
    private PortletDefinition portletDefinition = null;
    private PortletApplicationEntity portletApplicationEntity = null;
    private PortletWindowList portletWindows = null;
    private IPerson person = null;
    private IUserLayout layout = null;
    private IUserLayoutChannelDescription channelDescription = null;
    private ChannelDefinition channelDefinition = null;

    // PortletEntity methods
    
    public PortletEntityImpl() {
        preferences = new PreferenceSetImpl();
        publicationPreferences = new PreferenceSetImpl();
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
        try {
            //Remove preferences added from the definition and publishing
            for (final Iterator allPrefs = preferences.iterator(); allPrefs.hasNext(); ) {
                final Preference nextAllPref = (PreferenceImpl)allPrefs.next();
                final String nextAllPrefName = nextAllPref.getName();
                final Object[] nextAllPrefVals = IteratorUtils.toArray(nextAllPref.getValues());
                
                if (defPrefNameValuesPairExists(nextAllPrefName, nextAllPrefVals) || pubPrefNameValuesPairExists(nextAllPrefName, nextAllPrefVals)) {
                    allPrefs.remove();
                }
            }
            
            //Store the trimmed prefrences for the entity
            IPortletPreferencesStore portletPrefsStore = PortletPreferencesStoreFactory.getPortletPreferencesStoreImpl();
            int userId = person.getID();
            int layoutId = Integer.parseInt(layout.getId());
            String channelDescId = channelDescription.getId();
            portletPrefsStore.setEntityPreferences(userId, layoutId, channelDescId, preferences);

            //Re-load the all-preferences map
            this.loadPreferences();
        }
        catch (Exception e) {
            log.error("Could not store portlet entity preferences", e);
            
            if (e instanceof IOException)
                throw (IOException)e;
            else {
                IOException ioe = new IOException("Could not store portlet entity preferences: " + e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }
        }
    }

    /**
     * Calls {@link #prefNameValuesPairExists(Iterator, String, Object[])} using the portletDefinition preferences iterator.
     */
    private boolean defPrefNameValuesPairExists(String name, Object[] values) {
        return prefNameValuesPairExists(portletDefinition.getPreferenceSet(), name, values);
    }

    /**
     * Calls {@link #prefNameValuesPairExists(Iterator, String, Object[])} using the publicationPreferences iterator.
     */
    private boolean pubPrefNameValuesPairExists(String name, Object[] values) {
        return prefNameValuesPairExists(publicationPreferences, name, values);
    }

    /**
     * Iterates through Preference objects, checking if any have the same name and values
     * as the name and values arguments. Returns true if so, false if not.
     */
    private boolean prefNameValuesPairExists(PreferenceSet prefs, String name, Object[] values) {
        final Preference pref = prefs.get(name);
        if (pref != null) {
            Object[] nextPrefVals = IteratorUtils.toArray(pref.getValues());
            if (Arrays.equals(values, nextPrefVals)) {
                return true;
            }
        }
        
        return false;
    }

    public void reset() throws IOException {
        ((PreferenceSetImpl)preferences).clear();
        if (originalPreferences != null) {
            ((PreferenceSetImpl)preferences).addAll(originalPreferences);
        }
    }

    // Additional methods
    
    public void setPerson(final IPerson person) {
        this.person = person;
    }
    
    public void setUserLayout(final IUserLayout layout) {
        this.layout = layout;
    }
    
    public void setChannelDescription(final IUserLayoutChannelDescription channelDescription) {
        this.channelDescription = channelDescription;
    }
    
    public void setPreferences(PreferenceSet preferences) {
        this.preferences = (PreferenceSetImpl)preferences;
    }
    
    public ChannelDefinition getChannelDefinition() {
        return this.channelDefinition;
    }
    
    public void setChannelDefinition(ChannelDefinition channelDefinition) {
        this.channelDefinition = channelDefinition;
    }
    
    /**
     * re-loads preferences for the publish prefs and the entity prefs
     */
    public void loadPreferences() throws IOException {
        //Load publish time preferences
        try {
            IPortletPreferencesStore portletPrefsStore = PortletPreferencesStoreFactory.getPortletPreferencesStoreImpl();
            PreferenceSet storedPublicationPreferences = portletPrefsStore.getDefinitionPreferences(this.channelDefinition.getId());
            publicationPreferences.addAll(storedPublicationPreferences);
            preferences.addAll(publicationPreferences);
        }
        catch (Exception e) {
            log.error("Could not load portlet publication preferences", e);
            if (e instanceof IOException) {
                throw (IOException)e;
            }
            else {
                IOException ioe = new IOException("Could not load portlet publication preferences: " + e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }
        }
        
        //Load entity preferences
        try {
            IPortletPreferencesStore portletPrefsStore = PortletPreferencesStoreFactory.getPortletPreferencesStoreImpl();
            int userId = person.getID();
            int layoutId = Integer.parseInt(layout.getId());
            String channelDescId = channelDescription.getId();
            PreferenceSet subscriptionPreferences = portletPrefsStore.getEntityPreferences(userId, layoutId, channelDescId);
            preferences.addAll(subscriptionPreferences);

            // Save preferences as original preferences      
            originalPreferences = new PreferenceSetImpl();
            originalPreferences.addAll(preferences);
        }
        catch (Exception e) {
            log.error("Could not load portlet entity preferences.", e);

            if (e instanceof IOException)
                throw (IOException)e;
            else {
                IOException ioe = new IOException("Could not store portlet entity preferences: " + e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }
        }
    }
    
    
    public void removePreferences() throws Exception {
        try {
            IPortletPreferencesStore portletPrefsStore = PortletPreferencesStoreFactory.getPortletPreferencesStoreImpl();
            int userId = person.getID();
            int layoutId = Integer.parseInt(layout.getId());
            String channelDescId = channelDescription.getId();
            
            portletPrefsStore.deletePortletPreferencesByInstance(userId, layoutId, channelDescId);
        } catch (Exception e) {
            log.error("Could not delete portlet entity preferences", e);
            
            if (e instanceof IOException)
                throw (IOException)e;
            else {
                IOException ioe = new IOException("Could not store portlet entity preferences: " + e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }
        }
    }

    
    public void addPortletWindow(PortletWindow portletWindow) {
        ((PortletWindowListCtrl)portletWindows).add(portletWindow);
    }
    
    public void setPortletApplicationEntity(PortletApplicationEntity portletApplicationEntity) {
        this.portletApplicationEntity = portletApplicationEntity;
    }
}
