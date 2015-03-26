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

package org.jasig.portal.security.audit;

import org.jasig.portal.security.audit.dao.IUserLoginDao;
import org.joda.time.ReadableInstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Obvious, DAO-backed implementation of IUserLoginRegistry.
 * Registry layer in the Service-Registry-DAO-JPA architecture.
 * Applies the policy of not storing the login timestamp for the "guest" user.
 * @since uPortal 4.2
 */
@Component
public class UserLoginRegistry
    implements IUserLoginRegistry {

    private IUserLoginDao userLoginDao;

    @Override
    public void storeUserLogin(final String username, final ReadableInstant momentOfLogin) {

        if (!"guest".equals(username)) {
            this.userLoginDao.createUserLogin(username, momentOfLogin);
        }

    }

    @Override public IUserLogin mostRecentLoginBy(final String username) {
        return this.userLoginDao.readMostRecentUserLogin(username);
    }

    @Autowired
    public void setUserLoginDao(final IUserLoginDao userLoginDao) {
        this.userLoginDao = userLoginDao;
    }
}
