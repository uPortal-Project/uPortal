/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package  org.jasig.portal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.security.IPerson;

/**
 * A class that allows <code>GuestUserPreferencesManager</code> to be presented as <code>IUserpreferencesManager</code>.
 *
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 */
public class GuestUserPreferencesManagerWrapper implements IUserPreferencesManager {
    String sessionId;
    GuestUserPreferencesManager gulm;

    public GuestUserPreferencesManagerWrapper() {
        sessionId=null;
        gulm=null;
    }

    /**
     * Creates a new <code>GuestUserpreferencesManagerWrapper</code> instance.
     *
     * @param gulm a <code>GuestUserpreferencesManager</code> value
     * @param sessionId a <code>String</code> value
     */
    public GuestUserPreferencesManagerWrapper(GuestUserPreferencesManager gulm, String sessionId) {
        this.gulm=gulm;
        this.sessionId=sessionId;
    }

    /**
     * Returns current person object
     * @return current <code>IPerson</code>
     */
    public IPerson getPerson () {
        return this.gulm.getPerson();
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IUserPreferencesManager#reloadStructureStylesheet(javax.servlet.http.HttpServletRequest)
     */
    public void reloadStructureStylesheet(HttpServletRequest req) throws Exception { 
        this.gulm.reloadStructureStylesheet(req);
    }
    
    /**
     * Determine if the user agent associated with this session has been successfuly mapped to a profile
     * @return <code>true</code> if no mapping was found
     */
    public boolean isUserAgentUnmapped() {
        return this.gulm.isUserAgentUnmapped(this.sessionId);
    }

    /*
     * Resets both user layout and user preferences.
     * Note that if any of the two are "null", old values will be used.
     */
    public void setNewUserLayoutAndUserPreferences (IUserLayoutManager newLayout, UserPreferences newPreferences) throws PortalException {
        this.gulm.setNewUserLayoutAndUserPreferences(newLayout,newPreferences,this.sessionId);
    }

    /**
     * Returns a copy of the user preferences
     * @return a copy of the <code>UserPreferences</code> object
     */
    public UserPreferences getUserPreferencesCopy () {
        return this.gulm.getUserPreferencesCopy(this.sessionId);
    }

    /**
     * Returns current profile.
     * @return current <code>UserProfile</code>
     */
    public UserProfile getCurrentProfile () {
        return this.gulm.getCurrentProfile(this.sessionId);
    }

    /**
     * Returns current theme stylesheet description
     * @return current <code>ThemeStylesheetDescription</code>
     */
    public ThemeStylesheetDescription getThemeStylesheetDescription () throws Exception {
        return this.gulm.getThemeStylesheetDescription(this.sessionId);
    }

    /**
     * Returns current structure stylesheet description
     * @return current <code>StructureStylesheetDescription</code>
     */
    public StructureStylesheetDescription getStructureStylesheetDescription () throws Exception {
        return this.gulm.getStructureStylesheetDescription(this.sessionId);
    }

    public IUserLayoutManager getUserLayoutManager() {
        return this.gulm.getUserLayoutManager(this.sessionId);
    }

    /**
     * Returns current user preferences.
     * @return current <code>UserPreferences</code>
     */
    public UserPreferences getUserPreferences() {
        return this.gulm.getUserPreferences(this.sessionId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IUserPreferencesManager#finishedSession(javax.servlet.http.HttpSession)
     */
    public void finishedSession(HttpSession session) {
        this.gulm.finishedSession(session, this.sessionId);
    }

}



