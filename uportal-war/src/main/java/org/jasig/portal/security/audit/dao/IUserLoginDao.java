/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.security.audit.dao;

import org.jasig.portal.security.audit.IUserLogin;
import org.joda.time.ReadableInstant;

/**
 * Data Access Object for writing and reading user logins.
 *
 * This interface does not specify what implementations do with this login information,
 * where and how much it is stored, etc.  In particular this interface is designed such that it
 * can be implemented in a lightweight way to store only the most recent login for each user
 * or in a more complete way to record every login by every user.
 *
 * @since uPortal 4.2
 */
public interface IUserLoginDao {

    /**
     * Create a data store entry representing a login by the user at that time.
     *
     * @param username non-null username of user who logged in
     * @param momentOfLogin non-null moment that login happened
     * @return the IUserLogin object representing that moment
     * @throws RuntimeException on failure to persist such an IUserLogin.
     */
    public IUserLogin createUserLogin(String username, ReadableInstant momentOfLogin);

    /**
     * Get the most recent time a given user logged in, or null if there is no recorded login for
     * that user.
     *
     * @param username non-null username
     * @return most recent login, or null if none
     * @throws RuntimeException if username is null
     */
    public IUserLogin readMostRecentUserLogin(String username);
}
