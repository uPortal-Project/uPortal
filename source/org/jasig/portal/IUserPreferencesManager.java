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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;

import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.security.IPerson;

/**
 * Interface to user preferences management class.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 */
public interface IUserPreferencesManager {

    /* This function processes request parameters related to
     * setting Structure/Theme stylesheet parameters and attributes.
     * (uP_sparam, uP_tparam, uP_sfattr, uP_scattr uP_tcattr)
     * It also processes layout root requests (uP_root)
     * @param req current <code>HttpServletRequest</code>
     */
    public void processUserPreferencesParameters (HttpServletRequest req);

    /**
     * Returns current person object
     * @return current <code>IPerson</code>
     */
    public IPerson getPerson ();

    /**
     * Returns {@link IUserLayoutManager} object for performing layout-related operations.
     *
     * @return an <code>IUserLayoutManager</code> value
     */
    public IUserLayoutManager getUserLayoutManager();

    /**
     * Returns a global channel Id given a channel instance Id
     * @param channelInstanceId instance id of a channel
     * @return channel global id
     */
    //    public String getChannelPublishId (String channelInstanceId);

    /**
     * Determine if the user agent associated with this session has been successfuly mapped to a profile
     * @return <code>true</code> if no mapping was found
     */
    public boolean isUserAgentUnmapped();

    /*
     * Resets both user layout and user preferences.
     * Note that if any of the two are "null", old values will be used.
     */
    public void setNewUserLayoutAndUserPreferences (IUserLayoutManager newLayout, UserPreferences newPreferences) throws PortalException;

    /**
     * Returns a copy of the user preferences
     * @return a copy of the <code>UserPreferences</code> object
     */
    public UserPreferences getUserPreferencesCopy ();

    /**
     * Returns current profile.
     * @return current <code>UserProfile</code>
     */
    public UserProfile getCurrentProfile ();

    /**
     * Returns current theme stylesheet description
     * @return current <code>ThemeStylesheetDescription</code>
     */
    public ThemeStylesheetDescription getThemeStylesheetDescription () throws Exception;

    /**
     * Returns current structure stylesheet description
     * @return current <code>StructureStylesheetDescription</code>
     */
    public StructureStylesheetDescription getStructureStylesheetDescription () throws Exception;

    /**
     * Returns current user preferences.
     * @return current <code>UserPreferences</code>
     */
    public UserPreferences getUserPreferences();

    public void finishedSession(HttpSessionBindingEvent bindingEvent);
}



