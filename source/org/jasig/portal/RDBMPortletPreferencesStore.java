/**
 * Copyright ? 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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
package org.jasig.portal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.pluto.om.common.Preference;
import org.apache.pluto.om.common.PreferenceSet;
import org.jasig.portal.container.om.common.PreferenceSetImpl;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.CounterStoreFactory;
import org.jasig.portal.utils.ICounterStore;

/**
 * An implementation of IPortletPreferencesStore which uses a RDBM data source to
 * persist the data.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net </a>
 * @version $Revision$ 
 */
public class RDBMPortletPreferencesStore implements IPortletPreferencesStore {
    private static final String READ_ONLY_TRUE = "Y";
    private static final String READ_ONLY_FALSE = "N";
    private static final String UP_PORTLET_PREFERENCE_VALUE = "UP_PORTLET_PREFERENCE_VALUE";

    /**
     * @see org.jasig.portal.IPortletPreferencesStore#setDefinitionPreferences(int, org.apache.pluto.om.common.PreferenceSet)
     */
    public void setDefinitionPreferences(final int chanId, final PreferenceSet prefs) throws Exception {
        final Connection con = RDBMServices.getConnection();
 
        final String selectPrefIds = 
            "SELECT PREF_ID " +
            "FROM UP_PORTLET_DEFINITION_PREFS " +
            "WHERE CHAN_ID=?";        
        
        final String deletePrefValues = 
            "DELETE FROM UP_PORTLET_PREF_VALUES " +
            "WHERE PREF_ID=?";
        
        final String deletePrefNames = 
            "DELETE FROM UP_PORTLET_DEFINITION_PREFS " +
            "WHERE CHAN_ID=?";
        
        final String insertPrefName = 
            "INSERT INTO UP_PORTLET_DEFINITION_PREFS " +
            "(CHAN_ID, PORTLET_PREF_NAME, PORTLET_PREF_READONLY, PREF_ID) " +
            "VALUES (?, ?, ?, ?)";
        
        final String insertPrefValue = 
            "INSERT INTO UP_PORTLET_PREF_VALUES " +
            "(PREF_ID, PORTLET_PREF_VALUE) " +
            "VALUES (?, ?)";
        
        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setDefinitionPreferences(chanId=" + chanId + ")");

        try {
            // Set autocommit false for the connection
            if (RDBMServices.supportsTransactions)
                RDBMServices.setAutoCommit(con, false);
            
            PreparedStatement selectPrefIdsPstmt = null;
            PreparedStatement deletePrefValuesPstmt = null;
            PreparedStatement deletePrefNamesPstmt = null;
            PreparedStatement insertPrefNamePstmt = null;
            PreparedStatement insertPrefValuePstmt = null;

            try {
                final LinkedList prefIds = new LinkedList();

                selectPrefIdsPstmt = con.prepareStatement(selectPrefIds);
                deletePrefValuesPstmt = con.prepareStatement(deletePrefValues);
                deletePrefNamesPstmt = con.prepareStatement(deletePrefNames);
                insertPrefNamePstmt = con.prepareStatement(insertPrefName);
                insertPrefValuePstmt = con.prepareStatement(insertPrefValue);
                
                //Get a list of all the preference names for this portlet
                LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setDefinitionPreferences(): " + selectPrefIds);
                selectPrefIdsPstmt.setInt(1, chanId);
                ResultSet rs = selectPrefIdsPstmt.executeQuery();

                //Go through and remove all the values. Catalog the removed pref_id's so they can be re-used so
                //the counter doesn't have to get hammered as much
                try {

                    while (rs.next()) {
                        int prefId = rs.getInt("PREF_ID");
                        prefIds.add(new Integer(prefId));
                        
                        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setDefinitionPreferences(PREF_ID=" + prefId + "): " + deletePrefValues);
                        deletePrefValuesPstmt.setInt(1, prefId);
                        deletePrefValuesPstmt.executeUpdate();
                    }
                }
                finally {
                    try { rs.close(); } catch (Exception e) { };
                }
                
                //Delete all the preference names for this portlet
                LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setDefinitionPreferences(): " + deletePrefNames);
                deletePrefNamesPstmt.setInt(1, chanId);
                deletePrefNamesPstmt.executeUpdate();
                
                //Loop through the prefs, inserting each name then the values
                for (Iterator prefItr = prefs.iterator(); prefItr.hasNext();) {
                    
                    final Preference pref = (Preference)prefItr.next();
                    int prefId = -1;
                    
                    insertPrefNamePstmt.setInt(1, chanId);
                    insertPrefNamePstmt.setString(2, pref.getName());
                    
                    if (pref.isReadOnly()) {
                        insertPrefNamePstmt.setString(3, READ_ONLY_TRUE);
                    }
                    else {
                        insertPrefNamePstmt.setString(3, READ_ONLY_FALSE);
                    }
                    
                    //Use the list of removed ids to re-use IDs before generating new ones
                    if (prefIds.size() > 0) {
                        prefId = ((Integer)prefIds.removeLast()).intValue();
                    }
                    else {
                        final ICounterStore counterStore = CounterStoreFactory.getCounterStoreImpl();
                        
                        try {
                            prefId = counterStore.getIncrementIntegerId(UP_PORTLET_PREFERENCE_VALUE);
                        }
                        catch (Exception e) {
                            counterStore.createCounter(UP_PORTLET_PREFERENCE_VALUE);
                            prefId = counterStore.getIncrementIntegerId(UP_PORTLET_PREFERENCE_VALUE);
                        }
                    }
                    
                    insertPrefNamePstmt.setInt(4, prefId);
                    
                    //Insert the name row
                    LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setDefinitionPreferences(): " + insertPrefName);
                    insertPrefNamePstmt.executeUpdate();
                    
                    //For each value a row will be inserted in the values table
                    for (final Iterator valueItr = pref.getValues(); valueItr.hasNext();) {
                        String value = (String)valueItr.next();
                        
                        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setDefinitionPreferences(): " + insertPrefValue);
                        insertPrefValuePstmt.setInt(1, prefId);
                        insertPrefValuePstmt.setString(2, value);
                        insertPrefValuePstmt.executeUpdate();
                    }
                }
                
                if (RDBMServices.supportsTransactions)
                    RDBMServices.commit(con);
            }
            catch (Exception e) {
                // Roll back the transaction
                if (RDBMServices.supportsTransactions)
                    RDBMServices.rollback(con);
                throw e;
            }
            finally {
                try { selectPrefIdsPstmt.close(); } catch (Exception e) { }
                try { deletePrefValuesPstmt.close(); } catch (Exception e) { }
                try { deletePrefNamesPstmt.close(); } catch (Exception e) { }
                try { insertPrefNamePstmt.close(); } catch (Exception e) { }
                try { insertPrefValuePstmt.close(); } catch (Exception e) { }
            }
        }
        finally {
            RDBMServices.releaseConnection(con);
        }
    }
    
    /**
     * @see org.jasig.portal.IPortletPreferencesStore#getDefinitionPreferences(int)
     */
    public PreferenceSet getDefinitionPreferences(final int chanId) throws Exception {
        final PreferenceSetImpl prefs = new PreferenceSetImpl();
        final Connection con = RDBMServices.getConnection();
        
        final String selectPrefs = 
            "SELECT UPDP.PORTLET_PREF_NAME, UPDP.PORTLET_PREF_READONLY, UPPV.PORTLET_PREF_VALUE " +
            "FROM UP_PORTLET_DEFINITION_PREFS UPDP, UP_PORTLET_PREF_VALUES UPPV " +
            "WHERE UPDP.PREF_ID=UPPV.PREF_ID AND CHAN_ID=?";
        
        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::getDefinitionPreferences(chanId=" + chanId + ")");
                
        try {
            PreparedStatement selectCurrentPrefsPstmt = null;
            
            try {
                selectCurrentPrefsPstmt = con.prepareStatement(selectPrefs);
                
                LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::getDefinitionPreferences(): " + selectPrefs);
                selectCurrentPrefsPstmt.setInt(1, chanId);
                final ResultSet rs = selectCurrentPrefsPstmt.executeQuery();
                
                final Map prefsBuilder = new HashMap();
                final Map readOnlyMap = new HashMap();
                
                try {
                    while (rs.next()) {
                        final String prefName = rs.getString("PORTLET_PREF_NAME");
                        final String prefValue = rs.getString("PORTLET_PREF_VALUE");
                        
                        if (!readOnlyMap.containsKey(prefName)) {
                            if (READ_ONLY_TRUE.equals(rs.getString("PORTLET_PREF_READONLY"))) {
                                readOnlyMap.put(prefName, Boolean.TRUE);
                            }
                            else {
                                readOnlyMap.put(prefName, Boolean.FALSE);
                            }
                        }
                        
                        List prefList = (List)prefsBuilder.get(prefName);
                        
                        if (prefList == null)
                        {
                            prefList = new LinkedList();
                            prefsBuilder.put(prefName, prefList);
                        }
                        
                        prefList.add(prefValue);
                    }
                }
                finally {
                    try { rs.close(); } catch (Exception e) { }
                }
                
                for (final Iterator prefKeyItr = prefsBuilder.keySet().iterator(); prefKeyItr.hasNext();) {
                    final String prefName = (String)prefKeyItr.next();
                    final List prefValues = (List)prefsBuilder.get(prefName);
                    final boolean readOnly = Boolean.TRUE.equals(readOnlyMap.get(prefName));
                    
                    prefs.add(prefName, prefValues, readOnly);
                }
            }
            finally {
                try { selectCurrentPrefsPstmt.close(); } catch (Exception e) { }
            }
        }
        finally {
            RDBMServices.releaseConnection(con);
        }
        
        return prefs;
    }

    /**
     * @see org.jasig.portal.IPortletPreferencesStore#setEntityPreferences(int, int, String, org.apache.pluto.om.common.PreferenceSet)
     */
    public void setEntityPreferences(final int userId, final int layoutId, final String chanDescId, final PreferenceSet prefs) throws Exception {
        final Connection con = RDBMServices.getConnection();
        
        final String selectPrefIds = 
            "SELECT PREF_ID " +
            "FROM UP_PORTLET_ENTITY_PREFS " +
            "WHERE USER_ID=? AND LAYOUT_ID=? AND CHAN_DESC_ID=?";        
        
        final String deletePrefValues = 
            "DELETE FROM UP_PORTLET_PREF_VALUES " +
            "WHERE PREF_ID=?";
        
        final String deletePrefNames = 
            "DELETE FROM UP_PORTLET_ENTITY_PREFS " +
            "WHERE USER_ID=? AND LAYOUT_ID=? AND CHAN_DESC_ID=?";
        
        final String insertPrefName = 
            "INSERT INTO UP_PORTLET_ENTITY_PREFS " +
            "(USER_ID, LAYOUT_ID, CHAN_DESC_ID, PORTLET_PREF_NAME, PREF_ID) " +
            "VALUES (?, ?, ?, ?, ?)";
        
        final String insertPrefValue = 
            "INSERT INTO UP_PORTLET_PREF_VALUES " +
            "(PREF_ID, PORTLET_PREF_VALUE) " +
            "VALUES (?, ?)";
        
        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setEntityPreferences(userId=" + userId + ", layoutId=" + layoutId + ", chanDescId=" + chanDescId + ")");

        try {
            // Set autocommit false for the connection
            if (RDBMServices.supportsTransactions)
                RDBMServices.setAutoCommit(con, false);
        
            PreparedStatement selectPrefIdsPstmt = null;
            PreparedStatement deletePrefValuesPstmt = null;
            PreparedStatement deletePrefNamesPstmt = null;
            PreparedStatement insertPrefNamePstmt = null;
            PreparedStatement insertPrefValuePstmt = null;

            try {
                final LinkedList prefIds = new LinkedList();
                    
                selectPrefIdsPstmt = con.prepareStatement(selectPrefIds);
                deletePrefValuesPstmt = con.prepareStatement(deletePrefValues);
                deletePrefNamesPstmt = con.prepareStatement(deletePrefNames);
                insertPrefNamePstmt = con.prepareStatement(insertPrefName);
                insertPrefValuePstmt = con.prepareStatement(insertPrefValue);
                
                //Get a list of all the preference names for this instance of this portlet
                LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setEntityPreferences(): " + selectPrefIds);
                selectPrefIdsPstmt.setInt(1, userId);
                selectPrefIdsPstmt.setInt(2, layoutId);
                selectPrefIdsPstmt.setString(3, chanDescId);
                ResultSet rs = selectPrefIdsPstmt.executeQuery();
                
                //Go through and remove all the values. Catalog the removed pref_id's so they can be re-used so
                //the counter doesn't have to get hammered as much
                try {
                    while (rs.next()) {
                        int prefId = rs.getInt("PREF_ID");
                        prefIds.add(new Integer(prefId));
                        
                        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setEntityPreferences(PREF_ID=" + prefId + "): " + deletePrefValues);
                        deletePrefValuesPstmt.setInt(1, prefId);
                        deletePrefValuesPstmt.executeUpdate();
                    }
                }
                finally {
                    try { rs.close(); } catch (Exception e) { };
                }
                
                //Delete all the preference names for this instance of this portlet
                LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setEntityPreferences(): " + deletePrefNames);
                deletePrefNamesPstmt.setInt(1, userId);
                deletePrefNamesPstmt.setInt(2, layoutId);
                deletePrefNamesPstmt.setString(3, chanDescId);
                deletePrefNamesPstmt.executeUpdate();
                
                //Loop through the prefs, inserting each name then the values
                for (Iterator prefItr = prefs.iterator(); prefItr.hasNext();) {
                    final Preference pref = (Preference)prefItr.next();
                    int prefId = -1;
                    
                    insertPrefNamePstmt.setInt(1, userId);
                    insertPrefNamePstmt.setInt(2, layoutId);
                    insertPrefNamePstmt.setString(3, chanDescId);
                    insertPrefNamePstmt.setString(4, pref.getName());
                    
                    //Use the list of removed ids to re-use IDs before generating new ones
                    if (prefIds.size() > 0) {
                        prefId = ((Integer)prefIds.removeLast()).intValue();
                    }
                    else {
                        final ICounterStore counterStore = CounterStoreFactory.getCounterStoreImpl();
                        
                        try {
                            prefId = counterStore.getIncrementIntegerId(UP_PORTLET_PREFERENCE_VALUE);
                        }
                        catch (Exception e) {
                            counterStore.createCounter(UP_PORTLET_PREFERENCE_VALUE);
                            prefId = counterStore.getIncrementIntegerId(UP_PORTLET_PREFERENCE_VALUE);
                        }
                    }
                    
                    insertPrefNamePstmt.setInt(5, prefId);
                    
                    //Insert the name row
                    LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setEntityPreferences(): " + insertPrefName);
                    insertPrefNamePstmt.executeUpdate();
                    
                    //For each value a row will be inserted in the values table
                    for (final Iterator valueItr = pref.getValues(); valueItr.hasNext();) {
                        String value = (String)valueItr.next();

                        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::setEntityPreferences(): " + insertPrefValue);
                        insertPrefValuePstmt.setInt(1, prefId);
                        insertPrefValuePstmt.setString(2, value);
                        insertPrefValuePstmt.executeUpdate();
                    }
                }

                if (RDBMServices.supportsTransactions)
                    RDBMServices.commit(con);
            }
            catch (Exception e) {
                // Roll back the transaction
                if (RDBMServices.supportsTransactions)
                    RDBMServices.rollback(con);
                throw e;
            }
            finally {
                try { selectPrefIdsPstmt.close(); } catch (Exception e) { }
                try { deletePrefValuesPstmt.close(); } catch (Exception e) { }
                try { deletePrefNamesPstmt.close(); } catch (Exception e) { }
                try { insertPrefNamePstmt.close(); } catch (Exception e) { }
                try { insertPrefValuePstmt.close(); } catch (Exception e) { }
            }
        }
        finally {
            RDBMServices.releaseConnection(con);
        }
    }

    /**
     * @see org.jasig.portal.IPortletPreferencesStore#getEntityPreferences(int, int, java.lang.String)
     */
    public PreferenceSet getEntityPreferences(final int userId, final int layoutId, final String chanDescId) throws Exception {
        final PreferenceSetImpl prefs = new PreferenceSetImpl();
        final Connection con = RDBMServices.getConnection();

        final String selectPrefs = 
            "SELECT UPEP.PORTLET_PREF_NAME, UPPV.PORTLET_PREF_VALUE " +
            "FROM UP_PORTLET_ENTITY_PREFS UPEP, UP_PORTLET_PREF_VALUES UPPV " +
            "WHERE UPEP.PREF_ID=UPPV.PREF_ID AND UPEP.USER_ID=? AND UPEP.LAYOUT_ID=? AND UPEP.CHAN_DESC_ID=?";

        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::getEntityPreferences(userId=" + userId + ", layoutId=" + layoutId + ", chanDescId=" + chanDescId + ")");

        try {
            PreparedStatement selectCurrentPrefsPstmt = null;
            
            try {
                selectCurrentPrefsPstmt = con.prepareStatement(selectPrefs);
                
                LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::getEntityPreferences(): " + selectPrefs);
                
                selectCurrentPrefsPstmt.setInt(1, userId);
                selectCurrentPrefsPstmt.setInt(2, layoutId);
                selectCurrentPrefsPstmt.setString(3, chanDescId);
                ResultSet rs = selectCurrentPrefsPstmt.executeQuery();
                
                final Map prefsBuilder = new HashMap();
                
                try {
                    while (rs.next()) {
                        final String prefName = rs.getString("PORTLET_PREF_NAME");
                        final String prefValue = rs.getString("PORTLET_PREF_VALUE");
                                                
                        List prefList = (List)prefsBuilder.get(prefName);
                        
                        if (prefList == null)
                        {
                            prefList = new LinkedList();
                            prefsBuilder.put(prefName, prefList);
                        }
                        
                        prefList.add(prefValue);
                    }
                }
                finally {
                    try { rs.close(); } catch (Exception e) { }
                }
                
                for (final Iterator prefKeyItr = prefsBuilder.keySet().iterator(); prefKeyItr.hasNext();) {
                    final String prefName = (String)prefKeyItr.next();
                    final List prefValues = (List)prefsBuilder.get(prefName);
                    
                    prefs.add(prefName, prefValues);
                }
            }
            finally {
                try { selectCurrentPrefsPstmt.close(); } catch (Exception e) { }
            }
        }
        finally {
            RDBMServices.releaseConnection(con);
        }
        
        return prefs;
    }
    
    /**
     * @see org.jasig.portal.IPortletPreferencesStore#deletePortletPreferencesByUser(int)
     */
    public void deletePortletPreferencesByUser(int userId) throws Exception {
        final Connection con = RDBMServices.getConnection();
        
        final String selectPrefIds = 
            "SELECT PREF_ID " +
            "FROM UP_PORTLET_ENTITY_PREFS " +
            "WHERE USER_ID=?";
        
        final String deletePrefNames = 
            "DELETE FROM UP_PORTLET_ENTITY_PREFS " +
            "WHERE USER_ID=?";
        
        final String deletePrefValues = 
            "DELETE FROM UP_PORTLET_PREF_VALUES " +
            "WHERE PREF_ID=?";
        
        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::deletePortletPreferencesByUser(userId=" + userId + ")");

        try {
            // Set autocommit false for the connection
            if (RDBMServices.supportsTransactions)
                RDBMServices.setAutoCommit(con, false);

            PreparedStatement selectPrefIdsPstmt = null;
            PreparedStatement deletePrefNamesPstmt = null;
            PreparedStatement deletePrefValuesPstmt = null;

            try {
                selectPrefIdsPstmt = con.prepareStatement(selectPrefIds);
                deletePrefNamesPstmt = con.prepareStatement(deletePrefNames);
                deletePrefValuesPstmt = con.prepareStatement(deletePrefValues);

                LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::deletePortletPreferencesByUser(): " + selectPrefIds);
                selectPrefIdsPstmt.setInt(1, userId);
                ResultSet rs = selectPrefIdsPstmt.executeQuery();
                
                try {
                    while (rs.next()) {
                        int prefId = rs.getInt("PREF_ID");
                        
                        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::deletePortletPreferencesByUser(): " + deletePrefValues);
                        deletePrefValuesPstmt.setInt(1, prefId);
                        deletePrefValuesPstmt.executeUpdate();
                    }
                }
                finally {
                    try { rs.close(); } catch (Exception e) { }
                }
                
                deletePrefNamesPstmt.setInt(1, userId);
                deletePrefNamesPstmt.executeUpdate();

                if (RDBMServices.supportsTransactions)
                    RDBMServices.commit(con);
            }
            catch (Exception e) {
                // Roll back the transaction
                if (RDBMServices.supportsTransactions)
                    RDBMServices.rollback(con);
                throw e;
            }
            finally {
                try { selectPrefIdsPstmt.close(); } catch (Exception e) { }
                try { deletePrefNamesPstmt.close(); } catch (Exception e) { }
                try { deletePrefValuesPstmt.close(); } catch (Exception e) { }
            }
        }
        finally {
            RDBMServices.releaseConnection(con);
        }
    }
    
    /**
     * @see org.jasig.portal.IPortletPreferencesStore#deletePortletPreferencesByInstance(int, int, String)
     */
    public void deletePortletPreferencesByInstance(final int userId, final int layoutId, final String chanDescId) throws Exception {
        final Connection con = RDBMServices.getConnection();
                
        final String selectPrefIds = 
            "SELECT PREF_ID " +
            "FROM UP_PORTLET_ENTITY_PREFS " +
            "WHERE USER_ID=? AND LAYOUT_ID=? AND CHAN_DESC_ID=?";
        
        final String deletePrefNames = 
            "DELETE FROM UP_PORTLET_ENTITY_PREFS " +
            "WHERE USER_ID=? AND LAYOUT_ID=? AND CHAN_DESC_ID=?";
        
        final String deletePrefValues = 
            "DELETE FROM UP_PORTLET_PREF_VALUES " +
            "WHERE PREF_ID=?";
        
        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::deletePortletPreferencesByInstance(userId=" + userId + ", layoutId=" + layoutId + ", chanDescId=" + chanDescId + ")");

        try {
            // Set autocommit false for the connection
            if (RDBMServices.supportsTransactions)
                RDBMServices.setAutoCommit(con, false);

            PreparedStatement selectPrefIdsPstmt = null;
            PreparedStatement deletePrefNamesPstmt = null;
            PreparedStatement deletePrefValuesPstmt = null;

            try {
                selectPrefIdsPstmt = con.prepareStatement(selectPrefIds);
                deletePrefNamesPstmt = con.prepareStatement(deletePrefNames);
                deletePrefValuesPstmt = con.prepareStatement(deletePrefValues);

                LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::deletePortletPreferencesByInstance(): " + selectPrefIds);
                selectPrefIdsPstmt.setInt(1, userId);
                selectPrefIdsPstmt.setInt(2, layoutId);
                selectPrefIdsPstmt.setString(3, chanDescId);
                ResultSet rs = selectPrefIdsPstmt.executeQuery();
                
                try {
                    while (rs.next()) {
                        int prefId = rs.getInt("PREF_ID");
                        
                        LogService.log(LogService.DEBUG, "RDBMPortletPreferencesStore::deletePortletPreferencesByInstance(): " + deletePrefValues);
                        deletePrefValuesPstmt.setInt(1, prefId);
                        deletePrefValuesPstmt.executeUpdate();
                    }
                }
                finally {
                    try { rs.close(); } catch (Exception e) { }
                }
                
                deletePrefNamesPstmt.setInt(1, userId);
                deletePrefNamesPstmt.setInt(2, layoutId);
                deletePrefNamesPstmt.setString(3, chanDescId);                
                deletePrefNamesPstmt.executeUpdate();

                if (RDBMServices.supportsTransactions)
                    RDBMServices.commit(con);
            }
            catch (Exception e) {
                // Roll back the transaction
                if (RDBMServices.supportsTransactions)
                    RDBMServices.rollback(con);
                throw e;
            }
            finally {
                try { selectPrefIdsPstmt.close(); } catch (Exception e) { }
                try { deletePrefNamesPstmt.close(); } catch (Exception e) { }
                try { deletePrefValuesPstmt.close(); } catch (Exception e) { }
            }
        }
        finally {
            RDBMServices.releaseConnection(con);
        }
    }    
}