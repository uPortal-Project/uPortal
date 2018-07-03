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
package org.apereo.portal.security.provider.cas.clearpass;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.apereo.portal.security.IOpaqueCredentials;
import org.apereo.portal.security.provider.NotSoOpaqueCredentials;
import org.apereo.portal.security.provider.cas.CasAssertionSecurityContext;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.util.Assert;

public class PasswordCachingCasAssertionSecurityContext extends CasAssertionSecurityContext {

    private static final long serialVersionUID = -3816036745827152340L;

    private final String clearPassUrl;

    private byte[] cachedCredentials;

    protected PasswordCachingCasAssertionSecurityContext(final String clearPassUrl) {
        Assert.notNull(clearPassUrl, "clearPassUrl cannot be null.");
        this.clearPassUrl = clearPassUrl;
    }

    @Override
    protected final void postAuthenticate(final Assertion assertion) {
        retrievePasswordFromCasServer(assertion);
        super.postAuthenticate(assertion);
    }

    @Override
    public final IOpaqueCredentials getOpaqueCredentials() {
        log.debug("Invoking getOpacheCredentials()");
        if (this.cachedCredentials == null) {
            log.debug("Have no credentials, invoking superclass");
            return super.getOpaqueCredentials();
        }

        final NotSoOpaqueCredentials credentials = new CacheOpaqueCredentials();
        credentials.setCredentials(this.cachedCredentials);
        log.debug("Returning credentials");
        return credentials;
    }

    /**
     * Attempt to use the PGTIOU returned in the service ticket validation to obtain a proxy ticket
     * to call CAS to get the user's password. The Proxy Granting Ticket IOU (PGTIOU) from the
     * service ticket validation response has already been matched to the Proxy Granting Ticket
     * (PGT) that CAS delivers to one of the uPortal servers and the PGT should be present in the
     * assertion. If password retrieval fails, log messages to help the user identify the issue. It
     * may be that the uPortal servers are in a cluster and the configuration is not correct so this
     * uPortal server does not have the PGT.
     *
     * <p>NOTE: This method will update <code>cachedCredentials</code> so the caller does not need
     * to.
     *
     * @param assertion CAS Assertion
     * @return Bytes representing the user's password. Null if not available. cachedCredentials is
     *     also updated
     */
    protected byte[] retrievePasswordFromCasServer(Assertion assertion) {
        // Get the Proxy Granting Ticket from the assertion and use it to obtain the user's
        // password.
        log.debug(
                "Attempting to get CAS ClearPass Proxy Ticket for user {} using PGTIOU in assertion",
                assertion.getPrincipal().getName());
        String proxyTicket = assertion.getPrincipal().getProxyTicketFor(this.clearPassUrl);

        // If we were able to get the proxy ticket (it was delivered to this server or replicated to
        // this server),
        // invoke CAS to retrieve the password.
        if (proxyTicket != null) {
            log.debug(
                    "Attempting to get password for user {} using Proxy Ticket {}",
                    assertion.getPrincipal().getName(),
                    proxyTicket);

            String password = retrievePasswordUsingProxyTicket(proxyTicket);

            if (password != null) {
                log.debug("Password length {} retrieved from ClearPass.", password.length());
                this.cachedCredentials = password.getBytes(UTF_8);
            }
        }

        if (cachedCredentials == null) {
            // If the proxy ticket is not available because it was sent to another server and hasn't
            // replicated to this
            // server yet, or if the password retrieval from CAS failed for some reason we'll log a
            // message.
            log.error(
                    "Unable to obtain {} for {} from CAS ClearPass. Check your"
                            + " uPortal and CAS configuration.  See uPortal manual section on Caching and"
                            + " Replaying credentials",
                    proxyTicket == null ? "proxy ticket" : "password",
                    assertion.getPrincipal().getName());
        }
        return cachedCredentials;
    }

    protected final String retrievePasswordUsingProxyTicket(final String proxyTicket) {
        try {
            final String url =
                    this.clearPassUrl
                            + (this.clearPassUrl.contains("?") ? "&" : "?")
                            + "ticket="
                            + proxyTicket;
            final String response = retrieveResponseFromServer(url, "UTF-8");
            final String password = XmlUtils.getTextForElement(response, "credentials");

            if (CommonUtils.isNotBlank(password)) {
                return password;
            }

            // Response won't have password so OK to log it.
            log.error(
                    "Unable to Retrieve Password using url {}.  If you see a [403] HTTP response code returned from"
                            + " CommonUtils then it most likely means the proxy configuration on the CAS server is not"
                            + " correct.\n\nFull Response from ClearPass was [{}]",
                    url,
                    response);
            return null;
        } catch (Exception e) {
            /*
             * uPortal failed to obtain the user's password from the ClearPass feature.  This issue commonly occurs
             * in local dev environments because CAS will not share the user's credential over a non-SSL
             * connection.  We need to log this event, but should not fail the whole Authentication.
             */
            if (log.isWarnEnabled()) {
                log.warn(
                        "Unable to retrieve the credential from the ClearPass "
                                + "service for proxy ticket {}",
                        proxyTicket,
                        e);
            }
            return null;
        }
    }

    /** Exists purely for testing purposes. */
    protected String retrieveResponseFromServer(final String url, final String encoding) {
        return CommonUtils.getResponseFromServer(url, "UTF-8");
    }

    /** Copied from {@link org.apereo.portal.security.provider.CacheSecurityContext} */
    private class CacheOpaqueCredentials extends ChainingOpaqueCredentials
            implements NotSoOpaqueCredentials {

        private static final long serialVersionUID = 1l;

        @Override
        public String getCredentials() {
            log.debug("credentialString is {}", credentialstring != null ? "non-null" : "null");
            return this.credentialstring != null ? new String(this.credentialstring, UTF_8) : null;
        }
    }
}
