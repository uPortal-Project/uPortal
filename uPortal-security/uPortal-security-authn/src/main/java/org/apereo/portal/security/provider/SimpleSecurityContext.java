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
package org.apereo.portal.security.provider;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.persondir.ILocalAccountDao;
import org.apereo.portal.persondir.ILocalAccountPerson;
import org.apereo.portal.security.IPortalPasswordService;
import org.apereo.portal.security.ISecurityContext;
import org.apereo.portal.security.PortalSecurityException;
import org.apereo.portal.spring.locator.LocalAccountDaoLocator;
import org.apereo.portal.spring.locator.PortalPasswordServiceLocator;

/**
 * This is an implementation of a SecurityContext that checks a user's credentials against an MD5
 * hashed password entry.
 */
public class SimpleSecurityContext extends ChainingSecurityContext implements ISecurityContext {

    private static final Log log = LogFactory.getLog(SimpleSecurityContext.class);

    private final int SIMPLESECURITYAUTHTYPE = 0xFF02;

    /* package-private */ SimpleSecurityContext() {}

    @Override
    public int getAuthType() {
        return this.SIMPLESECURITYAUTHTYPE;
    }

    /**
     * Authenticate user.
     *
     * @exception PortalSecurityException
     */
    @Override
    public synchronized void authenticate() throws PortalSecurityException {
        this.isauth = false;
        if (this.myPrincipal.UID != null && this.myOpaqueCredentials.credentialstring != null) {

            try {

                ILocalAccountDao accountStore = LocalAccountDaoLocator.getLocalAccountDao();
                IPortalPasswordService passwordService =
                        PortalPasswordServiceLocator.getPortalPasswordService();

                // retrieve the account from the local user store
                ILocalAccountPerson account = accountStore.getPerson(this.myPrincipal.UID);

                if (account != null) {

                    // get the account password as an ASCII string
                    String loginPassword =
                            new String(this.myOpaqueCredentials.credentialstring, UTF_8);

                    // if the password provided at login matches the hashed
                    // account password, authenticate the user
                    if (passwordService.validatePassword(loginPassword, account.getPassword())) {

                        // set the full name for this user
                        String fullName = (String) account.getAttributeValue("displayName");
                        this.myPrincipal.FullName = fullName;
                        if (log.isInfoEnabled())
                            log.info("User " + this.myPrincipal.UID + " is authenticated");
                        this.isauth = true;
                    } else {
                        log.info("Password Invalid");
                    }

                } else {
                    if (log.isInfoEnabled()) log.info("No such user: " + this.myPrincipal.UID);
                }
            } catch (Exception e) {
                log.error("Error authenticating user", e);
                throw new RuntimeException("Error authenticating user", e);
            }
        }
        // If the principal and/or credential are missing, the context authentication
        // simply fails. It should not be construed that this is an error. It happens for guest
        // access.
        else {
            log.info("Principal or OpaqueCredentials not initialized prior to authenticate");
        }
        // Ok...we are now ready to authenticate all of our subcontexts.
        super.authenticate();
        return;
    }
}
