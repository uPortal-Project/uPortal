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
package org.jasig.portal.api.sso;

/**
 * Cross-context (i.e. "shared") API for JVM-locally deployed code to interact
 * with "SSO Tickets", i.e. trusted login tokens.
 *
 * <p>Design notes:</p>
 *
 * <p>{@link #issueTicket(String,String)} issues a {@link SsoTicket} to any caller
 * who can produce the correct shared secret (the {@code secret} arg in
 * {@link #issueTicket(String,String)}). The implementation will typically
 * not be able to execute a portal permission check because under typical usage
 * there isn't an actual, authenticated user when these tickets are issued. So the
 * implementation will typically just trust the calling context to have verified
 * <em>its</em> caller in some way (a la
 * {@link org.jasig.portal.security.sso.mvc.SsoController}) and require that the
 * caller be in possession of the secret mentioned above. This mechanism is not terribly
 * dissimilar to the {@code key} used by SpringSecurity's {@code RunAsManagerImpl}.</p>
 *
 * <p>The shared secret isn't hugely secure by itself since not-even-very-clever use
 * of reflection can get at the expected value (though a SecurityManager could likely
 * help prevent this). But we do need <em>something</em> because this service is
 * specifically intended to expose ticket issuance functionality outside of the portal
 * classloader and thus the audience is much more broad than the now-legacy
 * {@link org.jasig.portal.security.sso.mvc.SsoController}. But the
 * calling code still effectively needs to be deployed to the same VM, which
 * is protection of a sort. And we're specifically <em>not</em> exposing this
 * service as a REST endpoint. So we believe the shared secret is good enough for now</p>
 *
 */
public interface SsoTicketService {

	/**
	 * Portlet requests can access the currently registered implementation of this interface
	 * by accessing the portlet context attribute having this name.
	 */
	static final String PORTLET_CONTEXT_ATTRIBUTE_NAME =
			SsoTicketService.class.getName()
					+ ".PORTLET_CONTEXT_ATTRIBUTE_NAME";

	/**
	 * This indirection exists as an attempt to allow for injection of
	 * security constraints in the future, esp on {@link #set(SsoTicketService)},
	 * e.g. to control via a {@code SecurityManager} which components can
	 * set the current impl.
	 */
	static final class SsoTicketServiceAccessor {
		private static volatile SsoTicketService IMPL;
		public SsoTicketService get() {
			return IMPL;
		}
		public void set(SsoTicketService impl) {
			IMPL = impl;
		}
	}

	/** Allows access to the SsoTicketService impl to non-Portlet requests */
	static final SsoTicketServiceAccessor IMPL = new SsoTicketServiceAccessor();

	/**
	 * Create a new SSO ticket for the given username and shared secret. The resulting
	 * ticket is essentially a short-lived token that can be passed to the portal
	 * login service as a HTTP query param to assert a trusted login request. See the
	 * portal's {@link org.jasig.portal.security.sso.mvc.SsoController} for a usage
	 * example, abeit one that relies on lower-level implementation details. See
	 * additional design notes in class comments above.
	 *
	 * @param username The end-user logical identifier to associate with the ticket.
	 *                 Do not depend on the implementation to validate this value
	 *                 in any way.
	 * @param secret A shared secret that entitles the calling code to request a new
	 *               ticket.
	 * @return
	 */
	SsoTicket issueTicket(String username, String secret) throws SecurityException;

}
