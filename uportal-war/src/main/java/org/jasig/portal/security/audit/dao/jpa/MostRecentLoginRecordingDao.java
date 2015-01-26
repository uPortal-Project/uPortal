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

package org.jasig.portal.security.audit.dao.jpa;

import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.security.audit.IUserLogin;
import org.jasig.portal.security.audit.dao.IUserLoginDao;
import org.joda.time.ReadableInstant;
import org.springframework.stereotype.Repository;

/**
 * IUserLoginDao implementation that records only the most recent user login.
 *
 * This has the very nice property that the recorded logins is at most one row per user,
 * and so fancy aggregation or truncation of an infinite feed of data is not required.
 *
 * @since uPortal 4.2
 */
@Repository
public class MostRecentLoginRecordingDao
    extends BasePortalJpaDao
    implements IUserLoginDao {

    /*
     * Since this implementation only stores the *most recent* login,
     * create is interpreted as create-or-replace the entry keyed by the username.
     */
    @Override
    @PortalTransactional
    public IUserLogin createUserLogin(final String username, final ReadableInstant momentOfLogin) {

        final IUserLogin storedMostRecentUserLogin =
            readMostRecentUserLogin(username);

        if (null != storedMostRecentUserLogin) {

            // remove the old most recent login so it can be replaced with the new one
            // which has the same natural ID key (username).
            getEntityManager().remove(storedMostRecentUserLogin);
            getEntityManager().flush();
        }

        // this delete-then-insert strategy is naive and ridiculous.
        // TODO: refactor to a create-or-update strategy.

        final MostRecentUserLogin currentUserLogin =
            new MostRecentUserLogin(username, momentOfLogin);

        getEntityManager().persist(currentUserLogin);
        getEntityManager().flush();

        return currentUserLogin;
    }

    @Override
    @PortalTransactional
    public IUserLogin readMostRecentUserLogin(final String username) {

        final NaturalIdQuery<MostRecentUserLogin> queryByUsername =
            createNaturalIdQuery(MostRecentUserLogin.class);
        queryByUsername.using(MostRecentUserLogin_.username, username);

        return queryByUsername.load();
    }
}
