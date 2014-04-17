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
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;
import org.jasig.portal.security.provider.cas.CasAssertionSecurityContext;
import org.springframework.util.Assert;

/**
 * @author Scott Battaglia
 */
public class PasswordCachingCasAssertionSecurityContext extends CasAssertionSecurityContext {

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
    private static final int MAX_PASSWORD_RETRIEVAL_ATTEMPTS = 5;
    private static final long PASSWORD_RETRIEVAL_DELAY = 300;
    private static final long serialVersionUID = -3816036745827152340L;

    private final String clearPassUrl;

    // Must use synchronized for access to insure the thread attempting to return credentials
    // sees updates made by the thread pool thread (or must be made volatile).
    private byte[] cachedCredentials;

    private Assertion assertion;
    private String proxyTicket;

    private int passwordRetrievalCount;

    protected PasswordCachingCasAssertionSecurityContext(final String clearPassUrl) {
        super();
        Assert.notNull(clearPassUrl, "clearPassUrl cannot be null.");
        this.clearPassUrl = clearPassUrl;
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
     * Attempt to use the PGTIOU to obtain the PGT to call CAS to get the user's password.  The Proxy Granting
     * Ticket IOU (PGTIOU) is present in the assertion.  CAS delivers the Proxy Ticket (PGT) to one of the
     * uPortal servers. Since the PGT may not be delivered to this uPortal node, if password retrieval fails
     * attempt again a few times after waiting a bit to give the PGT a chance to get replicated to this uPortal
     * node via distributed cache.
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
            // Get the Proxy Granting Ticket (PGTIOU) from the assertion and use it to get the proxy ticket (PGT)
            // that CAS delivered to one of the uPortal servers in the cluster so we can use the proxy ticket
            // to get the user's password.
            if (proxyTicket == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to get Proxy Ticket (PGT) using Proxy Granting Ticket (PGTIOU) for user "
                            + assertion.getPrincipal().getName());
                }
                proxyTicket = assertion.getPrincipal().getProxyTicketFor(this.clearPassUrl);
            }

            // If we were able to get the proxy ticket (it was delivered to this server or replicated to this server),
            // invoke CAS to retrieve the password.
            if (proxyTicket != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to get password using Proxy Ticket (PGT) " + proxyTicket + " for user "
                            + assertion.getPrincipal().getName());
                }

                String password = retrievePasswordUsingProxyTicket(proxyTicket);

                if (password != null) {
                    log.debug("Password retrieved from ClearPass.");
                    this.cachedCredentials = password.getBytes();
                } else {
                    // For some reason we were unable to get the password from CAS.  Fall through and let the
                    // background thread attempt again in case CAS had a transient issue.
                    log.debug("Unable to retrieve password from ClearPass using proxy ticket.");
                }
            }

            // If we still don't have the credentials, determine if we should try again later.  The uPortal
            // login process takes long enough we typically will have time for the replication to occur before
            // we need the credentials.
            if (cachedCredentials == null) {
                // If the proxy ticket is not available because it was sent to another server and hasn't replicated
                // to this server yet, or if the password retrieval from CAS failed for some reason we'll
                // try again a bit later if we haven't exceeded the max retrieval attempts setting.
                if (++passwordRetrievalCount >= MAX_PASSWORD_RETRIEVAL_ATTEMPTS) {
                    if (log.isDebugEnabled()) {
                        log.error("Unable to obtain proxy ticket (PGT) from proxy granting ticket (PGTIOU) for"
                                + " ClearPass service during for " + assertion.getPrincipal().getName()
                                + " in " + MAX_PASSWORD_RETRIEVAL_ATTEMPTS + " attempts. Check your uPortal and CAS"
                                + " configuration.  See uPortal manual section on Caching and Replaying credentials");
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.info("Unable to obtain proxy ticket (PGT) from proxy granting ticket (PGTIOU) for"
                                + " ClearPass service during for " + assertion.getPrincipal().getName() + ". Will try"
                                + " again " + Integer.toString(MAX_PASSWORD_RETRIEVAL_ATTEMPTS - passwordRetrievalCount)
                                + " more times to wait for it to replicate to this server through distributed cache");
                    }
                    executor.schedule(new ObtainPasswordWorker(this), PASSWORD_RETRIEVAL_DELAY, TimeUnit.MILLISECONDS);
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


            if (log.isTraceEnabled()) {
                log.trace(String.format("ClearPass Response was:\n %s", response));
            }

            if (CommonUtils.isNotBlank(password)) {
                return password;
            }

            log.error("Unable to Retrieve Password.  If you see a [403] HTTP response code returned from"
                    +" CommonUtils then it most likely means the proxy configuration on the CAS server is not"
                    + " correct.\n\nFull Response from ClearPass was [" + response + "].");
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
                        "service for proxy ticket:  " + proxyTicket, e);
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
