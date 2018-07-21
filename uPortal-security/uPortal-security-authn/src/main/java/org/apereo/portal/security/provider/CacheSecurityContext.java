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
import org.apereo.portal.security.IOpaqueCredentials;
import org.apereo.portal.security.IParentAwareSecurityContext;
import org.apereo.portal.security.ISecurityContext;
import org.apereo.portal.security.IStringEncryptionService;
import org.apereo.portal.security.PortalSecurityException;
import org.apereo.portal.spring.locator.PasswordEncryptionServiceLocator;

/**
 * This is an implementation of a SecurityContext that performs absolutely NO validation of the
 * Principal but merely caches the claimed password. We implement this to provide the illusion of
 * single-signon but it comes with significant risk. A channel is able to retrieve the originally
 * validated password of passphrase to perform just-in-time validation but the means of validation
 * is now COMPLETELY in the hands of the channel. If the channel utilizes a weak
 * authenticity-checking mechanism and the password is the same as the one that portal users regard
 * as secure, then unbeknown to the user, their "secure" password is being placed in jeopardy.
 * PLEASE use this SecurityContext implementation sparingly and with your eyes open!
 * CacheSecurityContext can be chained together with another context such that both are required.
 * This allows an authentication provider such as SimpleLdapSecurityContext to be used to verify the
 * password and CacheSecurityContext to allow channels access to the password. Example of
 * security.properties settings to accomplish this:
 *
 * <p>root=org.apereo.portal.security.provider.SimpleSecurityContextFactory
 * root.cache=org.apereo.portal.security.provider.CacheSecurityContextFactory
 * principalToken.root=userName credentialToken.root=password
 *
 * <p>To ensure that both contexts are exercised the portal property
 * org.apereo.portal.security.provider.ChainingSecurityContext.stopWhenAuthenticated must be set to
 * false (by default it is set to true).
 */
class CacheSecurityContext extends ChainingSecurityContext
        implements ISecurityContext, IParentAwareSecurityContext {

    private static final long serialVersionUID = 1L;
    private static final int CACHESECURITYAUTHTYPE = 0xFF03;
    private static final Log LOG = LogFactory.getLog(CacheSecurityContext.class);

    private ISecurityContext parentContext;
    private byte[] cachedcredentials;

    /* package-private */ CacheSecurityContext() {}

    @Override
    public int getAuthType() {
        return CACHESECURITYAUTHTYPE;
    }

    @Override
    public synchronized void authenticate() throws PortalSecurityException {
        String msg =
                "Contexts that implement IParentAwareSecurityContext must "
                        + "authenticate through authenticate(ISecurityContext)";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public void authenticate(ISecurityContext parent) throws PortalSecurityException {

        // Save the parent for future use
        parentContext = parent;

        // First verify the parent context authenticated successfully
        if (!parentContext.isAuthenticated()) {
            return;
        }

        // Great;  now cache the claimed password, if provided
        if (this.myOpaqueCredentials.credentialstring != null) {

            // Encrypt our credentials using the spring-configured password
            // encryption service
            IStringEncryptionService encryptionService =
                    PasswordEncryptionServiceLocator.getPasswordEncryptionService();
            String encryptedPassword =
                    encryptionService.encrypt(
                            new String(this.myOpaqueCredentials.credentialstring, UTF_8));
            byte[] encryptedPasswordBytes = encryptedPassword.getBytes(UTF_8);

            // Save our encrypted credentials so the parent's authenticate()
            // method doesn't blow them away.
            this.cachedcredentials = new byte[encryptedPasswordBytes.length];
            System.arraycopy(
                    encryptedPasswordBytes,
                    0,
                    this.cachedcredentials,
                    0,
                    encryptedPasswordBytes.length);

            LOG.info("Credentials successfully cached");
        }
    }

    /**
     * We need to override this method in order to return a class that implements the
     * NotSoOpaqueCredentials interface.
     */
    @Override
    public IOpaqueCredentials getOpaqueCredentials() {
        if (parentContext != null && parentContext.isAuthenticated()) {
            NotSoOpaqueCredentials oc = new CacheOpaqueCredentials();
            oc.setCredentials(this.cachedcredentials);
            return oc;
        } else return null;
    }

    /**
     * This is a new implementation of an OpaqueCredentials class that implements the less-opaque
     * NotSoOpaqueCredentials.
     */
    private class CacheOpaqueCredentials extends ChainingSecurityContext.ChainingOpaqueCredentials
            implements NotSoOpaqueCredentials {

        private static final long serialVersionUID = 1L;

        @Override
        public String getCredentials() {
            if (this.credentialstring != null) return new String(this.credentialstring, UTF_8);
            else return null;
        }
    }
}
