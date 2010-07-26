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

import javax.servlet.http.HttpSession;

import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.security.IPerson;

/**
 * Interface to user preferences management class.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 */
public interface IUserPreferencesManager {

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

    public void finishedSession(HttpSession session);
}



