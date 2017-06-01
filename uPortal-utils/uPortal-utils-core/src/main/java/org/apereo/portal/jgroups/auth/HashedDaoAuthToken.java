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
package org.apereo.portal.jgroups.auth;

import java.io.DataInput;
import java.io.DataOutput;
import org.jgroups.Message;
import org.jgroups.auth.AuthToken;
import org.jgroups.logging.LogFactory;
import org.jgroups.util.Util;
import org.springframework.security.core.token.Sha512DigestUtils;

public class HashedDaoAuthToken extends AuthToken {
    private static volatile AuthDao authDao;

    public static void setAuthDao(AuthDao authDao) {
        if (HashedDaoAuthToken.authDao != null) {
            LogFactory.getLog(HashedDaoAuthToken.class)
                    .warn(
                            "A AuthDao was already set. "
                                    + HashedDaoAuthToken.authDao
                                    + " will be replaced with "
                                    + authDao);
        }
        HashedDaoAuthToken.authDao = authDao;
    }

    private String authValue;

    public String getAuthValue() {
        String av = authValue;
        if (av == null) {
            if (HashedDaoAuthToken.authDao == null) {
                throw new IllegalStateException(
                        "An AuthDao needs to be injected for the HashedDaoAuthToken to function");
            }

            final String authToken = authDao.getAuthToken(this.getName());

            if (authToken != null) {
                av = Sha512DigestUtils.shaHex(authToken);
                this.authValue = av;
            }
        }

        return av;
    }

    public String getName() {
        return HashedDaoAuthToken.class.getName();
    }

    @Override
    public int size() {
        if (authValue == null) {
            getAuthValue();     // load value if not set
        }
        return  authValue == null ? 0 : authValue.length();     // might still be null
    }

    public boolean authenticate(AuthToken token, Message msg) {

        if ((token != null) && (token instanceof HashedDaoAuthToken)) {
            // Found a valid Token to authenticate against
            HashedDaoAuthToken serverToken = (HashedDaoAuthToken) token;

            final String localAuthValue = this.getAuthValue();
            final String messageAuthValue = serverToken.getAuthValue();
            if ((localAuthValue != null)
                    && (messageAuthValue != null)
                    && (localAuthValue.equalsIgnoreCase(messageAuthValue))) {
                // validated
                log.debug("HashedDaoAuthToken match");
                return true;
            } else {
                return false;
            }
        }

        if (log.isWarnEnabled()) {
            log.warn("Invalid AuthToken instance - wrong type or null: " + token);
        }
        return false;
    }

    public void writeTo(DataOutput out) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("HashedDaoAuthToken writeTo()");
        }
        Util.writeObject(this.getAuthValue(), out);
    }

    public void readFrom(DataInput in) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("HashedDaoAuthToken readFrom()");
        }
        this.authValue = (String) Util.readObject(in);
    }
}
