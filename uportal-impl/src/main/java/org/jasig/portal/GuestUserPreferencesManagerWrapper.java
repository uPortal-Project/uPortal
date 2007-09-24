/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;

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

    /* This function processes request parameters related to
     * setting Structure/Theme stylesheet parameters and attributes.
     * (uP_sparam, uP_tparam, uP_sfattr, uP_scattr uP_tcattr)
     * It also processes layout root requests (uP_root)
     * @param req current <code>HttpServletRequest</code>
     */
     public void processUserPreferencesParameters (HttpServletRequest req) {
         this.gulm.processUserPreferencesParameters(req);
    }

    /**
     * Returns current person object
     * @return current <code>IPerson</code>
     */
    public IPerson getPerson () {
        return this.gulm.getPerson();
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

    public void finishedSession(HttpSessionBindingEvent bindingEvent) {
        this.gulm.finishedSession(bindingEvent,this.sessionId);
    }

}



