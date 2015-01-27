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

import org.apache.commons.lang3.Validate;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Obvious implementation of IAuditService.
 * Service layer in the Service-Registry-DAO architecture.
 */
@Service
public class AuditService
    implements IAuditService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IUserLoginRegistry userLoginRegistry;

    private boolean propagateFailures = false;


    @Override
    public void recordLogin(final String username, final ReadableInstant momentOfLogin) {

        try {
            Validate.notNull(username, "Cannot record the login of a null user.");
            Validate.notNull(momentOfLogin, "Cannot record that a user logged in at a null time");

            if (momentOfLogin.toInstant().isAfterNow()) {
                throw new IllegalArgumentException("Cannot record that a login happened in the future."
                    + " Because that doesn't make any sense.");
            }

            userLoginRegistry.storeUserLogin(username, momentOfLogin);

        } catch (final Exception e) {

            logger.error("Failed to record the login by user {} that happened at {}.",
                username, momentOfLogin, e);

            if (propagateFailures) {
                throw e;
            }
        }



    }

    @Autowired
    public void setUserLoginRegistry(final IUserLoginRegistry userLoginRegistry) {
        this.userLoginRegistry = userLoginRegistry;
    }

    /**
     * Set whether the Audit Service should propogate failures.
     *
     * Defaults to false.
     *
     * At issue here is whether auditing is a nice-to-have in which case a failing audit
     * subsystem is cause for plaintive logging, or whether auditing is required such that
     * auditing failures should surface as exceptions to the systems giving rise to the auditing
     * (i.e., login).
     *
     * @param propogateAuditFailures
     */
    public void setPropogateAuditFailures(boolean propogateAuditFailures) {
        this.propagateFailures = propogateAuditFailures;
    }

    /**
     * Returns whether this AuditService is configured to propagate audit failures or not.
     * @return true if throws on failure, false if swallows exceptions.
     */
    public boolean isPropogateAuditFailures() {
        return propagateFailures;
    }
}
