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

/**
 * An implementation of IPortletPreferencesStore which uses a RDBM data source to
 * persist the data.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net </a>
 * @version $Revision $
 */
public class RDBMPortletPreferencesStore implements IPortletPreferencesStore {
    private static final String READ_ONLY_TRUE = "Y";
    private static final String READ_ONLY_FALSE = "N";

    /**
     * 
     * @see org.jasig.portal.IPortletPreferencesStore#setDefinitionPreferences(int, org.apache.pluto.om.common.PreferenceSet)
     */
    public void setDefinitionPreferences(final int chanId, final PreferenceSet prefs) throws Exception {
        final Connection con = RDBMServices.getConnection();
        
        final String deleteCurrentPrefs = 
            "DELETE FROM UP_PORTLET_DEFINITION_PREFS " +
            "WHERE CHAN_ID=?";
        
        final String insertPref = 
            "INSERT INTO UP_PORTLET_DEFINITION_PREFS " +
            "(CHAN_ID, PORTLET_PREF_NAME, PORTLET_PREF_VALUE, PORTLET_PREF_READONLY) " +
            "VALUES (?, ?, ?, ?)";
        
        try {
            // Set autocommit false for the connection
            RDBMServices.setAutoCommit(con, false);
        
            PreparedStatement deleteCurrentPrefsPstmt = null;
            PreparedStatement insertPrefPstmt = null;
            
            try {
                deleteCurrentPrefsPstmt = con.prepareStatement(deleteCurrentPrefs);
                insertPrefPstmt = con.prepareStatement(insertPref);
                
                deleteCurrentPrefsPstmt.setInt(1, chanId);
                deleteCurrentPrefsPstmt.execute();
                
                for (Iterator prefItr = prefs.iterator(); prefItr.hasNext();) {
                    final Preference pref = (Preference)prefItr.next();
                    
                    insertPrefPstmt.setInt(1, chanId);
                    insertPrefPstmt.setString(2, pref.getName());
                    
                    if (pref.isReadOnly()) {
                        insertPrefPstmt.setString(4, READ_ONLY_TRUE);
                    }
                    else {
                        insertPrefPstmt.setString(4, READ_ONLY_FALSE);
                    }
                    
                    for (final Iterator valueItr = pref.getValues(); prefItr.hasNext();) {
                        String value = (String)valueItr.next();
                        
                        insertPrefPstmt.setString(3, value);
                        insertPrefPstmt.executeUpdate();
                    }
                }
                
                RDBMServices.commit(con);
            }
            catch (Exception e) {
                // Roll back the transaction
                RDBMServices.rollback(con);
                throw e;
            }
            finally {
                try { deleteCurrentPrefsPstmt.close(); } catch (Exception e) { }
                try { insertPrefPstmt.close(); } catch (Exception e) { }
            }
        }
        finally {
            RDBMServices.releaseConnection(con);
        }
    }
    
    /**
     * 
     * @see org.jasig.portal.IPortletPreferencesStore#getDefinitionPreferences(int)
     */
    public PreferenceSet getDefinitionPreferences(final int chanId) throws Exception {
        final PreferenceSetImpl prefs = new PreferenceSetImpl();
        final Connection con = RDBMServices.getConnection();
        
        final String selectCurrentPrefs = 
            "SELECT CHAN_ID, PORTLET_PREF_NAME, PORTLET_PREF_VALUE, PORTLET_PREF_READONLY " +
            "FROM UP_PORTLET_DEFINITION_PREFS " +
            "WHERE CHAN_ID=?";
        
        try {
            PreparedStatement selectCurrentPrefsPstmt = null;
            
            try {
                selectCurrentPrefsPstmt = con.prepareStatement(selectCurrentPrefs);
                
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
     * 
     * @see org.jasig.portal.IPortletPreferencesStore#setEntityPreferences(int, int, int, org.apache.pluto.om.common.PreferenceSet)
     */
    public void setEntityPreferences(final int userId, final int layoutId, final int structId, final PreferenceSet prefs) throws Exception {
        final Connection con = RDBMServices.getConnection();
        
        final String deleteCurrentPrefs = 
            "DELETE FROM UP_PORTLET_ENTITY_PREFS " +
            "WHERE USER_ID=? AND LAYOUT_ID=? AND STRUCT_ID=?";
        
        final String insertPref = 
            "INSERT INTO UP_PORTLET_ENTITY_PREFS " +
            "(USER_ID, LAYOUT_ID, STRUCT_ID, PORTLET_PREF_NAME, PORTLET_PREF_VALUE) " +
            "VALUES (?, ?, ?, ?, ?)";
        
        try {
            // Set autocommit false for the connection
            RDBMServices.setAutoCommit(con, false);
        
            PreparedStatement deleteCurrentPrefsPstmt = null;
            PreparedStatement insertPrefPstmt = null;
            
            try {
                deleteCurrentPrefsPstmt = con.prepareStatement(deleteCurrentPrefs);
                insertPrefPstmt = con.prepareStatement(insertPref);
                
                deleteCurrentPrefsPstmt.setInt(1, userId);
                deleteCurrentPrefsPstmt.setInt(2, layoutId);
                deleteCurrentPrefsPstmt.setInt(3, structId);
                deleteCurrentPrefsPstmt.execute();
                
                for (final Iterator prefItr = prefs.iterator(); prefItr.hasNext();) {
                    final Preference pref = (Preference)prefItr.next();
                    
                    insertPrefPstmt.setInt(1, userId);
                    insertPrefPstmt.setInt(2, layoutId);
                    insertPrefPstmt.setInt(3, structId);
                    insertPrefPstmt.setString(4, pref.getName());
                    
                    for (final Iterator valueItr = pref.getValues(); prefItr.hasNext();) {
                        final String value = (String)valueItr.next();
                        
                        insertPrefPstmt.setString(5, value);
                        insertPrefPstmt.executeUpdate();
                    }
                }
                
                RDBMServices.commit(con);
            }
            catch (Exception e) {
                // Roll back the transaction
                RDBMServices.rollback(con);
                throw e;
            }
            finally {
                try { deleteCurrentPrefsPstmt.close(); } catch (Exception e) { }
                try { insertPrefPstmt.close(); } catch (Exception e) { }
            }
        }
        finally {
            RDBMServices.releaseConnection(con);
        }
    }

    public PreferenceSet getEntityPreferences(final int userId, final int layoutId, final int structId) throws Exception {
        final PreferenceSetImpl prefs = new PreferenceSetImpl();
        final Connection con = RDBMServices.getConnection();

        final String selectCurrentPrefs = 
            "SELECT USER_ID, LAYOUT_ID, STRUCT_ID, PORTLET_PREF_NAME, PORTLET_PREF_VALUE " +
            "FROM UP_PORTLET_ENTITY_PREFS " +
            "WHERE USER_ID=? AND LAYOUT_ID=? AND STRUCT_ID=?";
        
        try {
            PreparedStatement selectCurrentPrefsPstmt = null;
            
            try {
                selectCurrentPrefsPstmt = con.prepareStatement(selectCurrentPrefs);
                
                selectCurrentPrefsPstmt.setInt(1, userId);
                selectCurrentPrefsPstmt.setInt(1, layoutId);
                selectCurrentPrefsPstmt.setInt(1, structId);
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
}