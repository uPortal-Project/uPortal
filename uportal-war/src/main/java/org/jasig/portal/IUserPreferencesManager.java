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

package org.jasig.portal;

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
    public IPerson getPerson();

    /**
     * Returns {@link IUserLayoutManager} object for performing layout-related operations.
     *
     * @return an <code>IUserLayoutManager</code> value
     */
    public IUserLayoutManager getUserLayoutManager();

    /**
     * Returns current profile.
     * @return current <code>UserProfile</code>
     */
    public IUserProfile getUserProfile();

    /**
     * Returns current theme stylesheet description
     * @deprecated use {@link #getUserProfile()} and {@link UserProfile#getThemeStylesheetId()}
     */
    @Deprecated
    public long getThemeStylesheetDescriptorId();

    /**
     * Returns current structure stylesheet description
     * @deprecated use {@link #getUserProfile()} and {@link UserProfile#getStructureStylesheetId()}
     */
    @Deprecated
    public long getStructureStylesheetDescriptorId();
}
