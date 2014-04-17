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

package org.jasig.portal.security.provider.cas.clearpass;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;
import org.jasig.portal.security.provider.cas.CasAssertionSecurityContext;
import org.springframework.util.Assert;

/**
 * @author Scott Battaglia
 */
public class PasswordCachingCasAssertionSecurityContext extends CasAssertionSecurityContext {

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
    private static final int MAX_PASSWORD_RETRIEVAL_RETRY_ATTEMPTS = 5;
    private static final long PASSWORD_RETRIEVAL_DELAY = 300;
    private static final String PROPERTY_PASSWORD_RETRIEVAL_DELAY =
            "org.jasig.portal.security.provider.cas.clearpass.retryDelay";
    private static final String PROPERTY_PASSWORD_RETRIEVAL_MAX_RETRY_ATTEMPTS =
            "org.jasig.portal.security.provider.cas.clearpass.maxRetryAttempts";
    private static final long serialVersionUID = -3816036745827152340L;

    private final String clearPassUrl;

    // Must use synchronized for access to insure the thread attempting to return credentials
    // sees updates made by the thread pool thread (or must be made volatile).
    private byte[] cachedCredentials;

    private Assertion assertion;
    private String proxyTicket;

    // Count of number of times we've attempted to retrieve user's password
    private int passwordRetrievalCount;
    // Delay in ms before attempting to automatically retrieve user's password again
    private long passwordRetrievalDelay;
    // Maximum number of times to try to automatically retrieve the user's password
    private int passwordRetrievalMaxRetryAttempts;

    protected PasswordCachingCasAssertionSecurityContext(final String clearPassUrl) {
        super();
        Assert.notNull(clearPassUrl, "clearPassUrl cannot be null.");
        this.clearPassUrl = clearPassUrl;
        passwordRetrievalDelay = PropertiesManager.getPropertyAsLong
                (PROPERTY_PASSWORD_RETRIEVAL_DELAY, PASSWORD_RETRIEVAL_DELAY);
        passwordRetrievalMaxRetryAttempts = PropertiesManager.getPropertyAsInt
                (PROPERTY_PASSWORD_RETRIEVAL_MAX_RETRY_ATTEMPTS, MAX_PASSWORD_RETRIEVAL_RETRY_ATTEMPTS);
    }

    @Override
    protected final void postAuthenticate(final Assertion assertion) {

        this.assertion = assertion;
        retrievePasswordFromCasServer();
    }

    @Override
    public final IOpaqueCredentials getOpaqueCredentials() {
        byte[] password = retrievePasswordFromCasServer();
        if (password == null) {
            return super.getOpaqueCredentials();
        }

        final NotSoOpaqueCredentials credentials = new CacheOpaqueCredentials();
        credentials.setCredentials(password);
        return credentials;
    }

    /**
     * Attempt to use the PGTIOU returned in the service ticket validation to obtain a proxy ticket to call CAS
     * to get the user's password.  The Proxy Granting Ticket IOU (PGTIOU) is present in the assertion.
     * CAS delivers the Proxy Granting Ticket (PGT) to one of the uPortal servers. Since the PGT may not be
     * delivered to this uPortal node, if password retrieval fails attempt again a few times after waiting a
     * bit to give the PGT a chance to get replicated to this uPortal node via distributed cache.
     *
     * <p>This method may potentially be called by multiple threads simultaneously so insure only one simultaneous
     * attempt is made to contact the CAS server to get the password.
     *
     * <p>NOTE: This method will update <code>cachedCredentials</code> so the caller does not need to.
     *
     * @return Bytes representing the user's password.  Null if not available.
     */
    protected synchronized byte[] retrievePasswordFromCasServer() {
        // If we do not have the password, attempt to retrieve it.
        if (cachedCredentials == null) {
            // Get the Proxy Granting Ticket (PGTIOU) from the assertion and use it to get a proxy ticket
            // to obtain the user's password.
            if (proxyTicket == null) {
                log.debug("Attempting to get CAS ClearPass Proxy Ticket for user {} using PGTIOU in assertion",
                        assertion.getPrincipal().getName());
                proxyTicket = assertion.getPrincipal().getProxyTicketFor(this.clearPassUrl);
            }

            // If we were able to get the proxy ticket (it was delivered to this server or replicated to this server),
            // invoke CAS to retrieve the password.
            if (proxyTicket != null) {
                log.debug("Attempting to get password for user {} using Proxy Ticket {}",
                        assertion.getPrincipal().getName(), proxyTicket);

                String password = retrievePasswordUsingProxyTicket(proxyTicket);

                if (password != null) {
                    log.debug("Password retrieved from ClearPass.");
                    this.cachedCredentials = password.getBytes();
                }
            }

            // If we still don't have the credentials, determine if we should try again later.  The uPortal
            // login process takes long enough we typically will have time for the replication to occur before
            // we need the credentials.
            if (cachedCredentials == null) {
                // If the proxy ticket is not available because it was sent to another server and hasn't replicated
                // to this server yet, or if the password retrieval from CAS failed for some reason we'll
                // try again a bit later if we haven't exceeded the max retrieval attempts setting.
                String message = proxyTicket == null ? "proxy ticket" : "password";
                if (++passwordRetrievalCount > passwordRetrievalMaxRetryAttempts) {
                    // NOTE:  Each time a portlet requiring a password renders it will attempt to obtain the
                    // password so this error message may appear many times with counts greatly exceeding
                    // passwordRetrievalMaxRetryAttempts since they are not automatic retries but valid attempts
                    // to get the password.
                    log.error("Unable to obtain {} for {} from CAS ClearPass in {} attempts. Check your"
                            + " uPortal and CAS configuration.  See uPortal manual section on Caching and"
                            + " Replaying credentials", message, assertion.getPrincipal().getName(),
                            passwordRetrievalCount);
                } else {
                    log.info("Unable to obtain {} for {} from CAS ClearPass. Will try again {} more times {}",
                            message, assertion.getPrincipal().getName(),
                            passwordRetrievalMaxRetryAttempts - passwordRetrievalCount + 1,
                            proxyTicket == null ? " for PGT to replicate to this server through distributed cache"
                                : "");
                    executor.schedule(new ObtainPasswordWorker(this), passwordRetrievalDelay, TimeUnit.MILLISECONDS);
                }
            }
        }
        return cachedCredentials;
    }

    private synchronized byte[] getCachedCredentials() {
        return cachedCredentials;
    }

    protected final String retrievePasswordUsingProxyTicket(final String proxyTicket) {
        try  {
            final String url = this.clearPassUrl + (this.clearPassUrl.contains("?") ? "&" : "?") + "ticket=" + proxyTicket;
            final String response = retrieveResponseFromServer(url, "UTF-8");
            final String password = XmlUtils.getTextForElement(response, "credentials");


            if (CommonUtils.isNotBlank(password)) {
                return password;
            }

            // Response won't have password so OK to log it.
            log.error("Unable to Retrieve Password using url {}.  If you see a [403] HTTP response code returned from"
                    +" CommonUtils then it most likely means the proxy configuration on the CAS server is not"
                    + " correct.\n\nFull Response from ClearPass was [{}]", url, response);
            return null;
        } catch (Exception e) {
            /*
             * uPortal failed to obtain the user's password from the ClearPass
             * feature.  This issue commonly occurs in local dev environments
             * because CAS will not share the user's credential over a non-SSL
             * connection.  We need to log this event, but should not fail the
             * whole AuthN.
             */
            if (log.isWarnEnabled()) {
                log.warn("Unable to retrieve the credential from the ClearPass " +
                        "service for proxy ticket {}", proxyTicket, e);
            }
            return null;
        }
    }

    /**
     * Exists purely for testing purposes.
     */
    protected String retrieveResponseFromServer(final String url, final String encoding) {
        return CommonUtils.getResponseFromServer(url, "UTF-8");
    }


    /**
	 * Copied from {@link org.jasig.portal.security.provider.CacheSecurityContext}
	 */
	private class CacheOpaqueCredentials extends ChainingOpaqueCredentials implements NotSoOpaqueCredentials {

		private static final long serialVersionUID = 1l;

		public String getCredentials() {
		  return this.credentialstring != null ? new String(this.credentialstring) : null;
		}
	}

    private class ObtainPasswordWorker implements Runnable {
        PasswordCachingCasAssertionSecurityContext context;

        ObtainPasswordWorker(PasswordCachingCasAssertionSecurityContext context) {
            this.context = context;
        }

        /**
         * Attempt to retrieve the password from the CAS Server if it has not already been obtained.
         */
        @Override
        public void run() {
            retrievePasswordFromCasServer();
        }
    }
}
