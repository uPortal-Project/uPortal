/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.security.IPerson;

/**
 * UserPreferencesManager is responsible for keeping: user id, user layout, user preferences and
 * stylesheet descriptions. For method descriptions please see {@link IUserPreferencesManager}.
 */
public class UserPreferencesManager implements IUserPreferencesManager {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private final IPerson person;
    private final IUserProfile userProfile;
    private final IUserLayoutManager userLayoutManager;

    public UserPreferencesManager(
            IPerson person, IUserProfile userProfile, IUserLayoutManager userLayoutManager) {
        this.person = person;
        this.userProfile = userProfile;
        this.userLayoutManager = userLayoutManager;
    }

    /**
     * Returns current person object
     *
     * @return current <code>IPerson</code>
     */
    @Override
    public IPerson getPerson() {
        return person;
    }

    @Override
    public IUserLayoutManager getUserLayoutManager() {
        return userLayoutManager;
    }

    @Override
    public IUserProfile getUserProfile() {
        return this.userProfile;
    }

}
