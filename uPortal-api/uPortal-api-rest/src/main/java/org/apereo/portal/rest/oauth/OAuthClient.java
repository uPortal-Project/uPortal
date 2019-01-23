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
package org.apereo.portal.rest.oauth;

/**
 * Objects that implement this interface work with the <code>/uPortal/api/v5-5/oauth/token</code>
 * endpoint defined by the {@link OidcUserInfoController} to provide access tokens to authorized
 * clients.  Every authorized client must have a bean in the <code>ApplicationContext</code> that
 * contains its metadata.  The {@link OidcUserInfoController} will discover and use these beans
 * automatically.
 *
 * @since 5.5
 */
public interface OAuthClient {

    /**
     * The OAuth <code>client_id</code>.
     */
    String getClientId();

    /**
     * The OAuth <code>client_secret</code>.
     */
    String getClientSecret();

    /**
     * A successful HTTP request to the <code>/uPortal/api/v5-5/oauth/token</code> URI will receive
     * a valid OIDC Id token.  This method specifies the portal user account that the Id token will
     * represent.
     */
    String getPortalUserAccount();

}
