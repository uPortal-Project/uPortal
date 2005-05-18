/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.apache.pluto.om.common.PreferenceSet;

/**
 * Stub implementation of IPortletPreferencesStore used in unit testing.
 * @version $Revision$ $Date$
 */
public class StubPortletPreferencesStore 
    implements IPortletPreferencesStore {

    public void setDefinitionPreferences(int chanId, PreferenceSet prefs) throws Exception {
        // do nothing
    }

    public PreferenceSet getDefinitionPreferences(int chanId) throws Exception {
        return null;
    }

    public void setEntityPreferences(int userId, int layoutId, String chanDescId, PreferenceSet prefs) throws Exception {
        // do nothing
    }

    public PreferenceSet getEntityPreferences(int userId, int layoutId, String chanDescId) throws Exception {
        return null;
    }

    public void deletePortletPreferencesByUser(int userId) throws Exception {
        // do nothing
    }

    public void deletePortletPreferencesByInstance(int userId, int layoutId, String chanDescId) throws Exception {
        // do nothing
    }

}

