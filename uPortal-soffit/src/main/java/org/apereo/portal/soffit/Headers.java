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
package org.apereo.portal.soffit;

/**
 * Set of HTTP headers, both standard and custom, in use within Soffit.
 *
 * @since 5.0
 */
public enum Headers {

    /*
     * Standard HTTP headers
     */

    /**
     * The Authorization field value consists of credentials containing the authentication
     * information of the user agent for the realm of the resource being requested.
     *
     * @since 5.0
     */
    AUTHORIZATION("Authorization"),

    /**
     * Used to specify directives that MUST be obeyed by all caching mechanisms along the
     * request/response chain. The directives specify behavior intended to prevent caches from
     * adversely interfering with the request or response.
     *
     * @since 5.0
     */
    CACHE_CONTROL("Cache-Control"),

    /*
     * Custom headers
     */

    /**
     * HTTP header sent by the {@link SoffitConnectorController} containing the {@link
     * PortalRequest} for the Soffit in the form of an encrypted JWT.
     *
     * @since 5.0
     */
    PORTAL_REQUEST("X-Soffit-PortalRequest"),

    /**
     * HTTP header sent by the {@link SoffitConnectorController} containing preferences for the
     * Soffit in the form of an encrypted JWT.
     *
     * @since 5.0
     */
    PREFERECES("X-Soffit-Preferences"),

    /**
     * HTTP header sent by the {@link SoffitConnectorController} containing preferences for the
     * Soffit in the form of an encrypted JWT.
     *
     * @since 5.0
     */
    DEFINITION("X-Soffit-Definition");

    /**
     * Prepended to the Authorization HTTP header to indicate that the value (the rest of it) is a
     * Bearer Token.
     */
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";

    /**
     * The default value for the <code>Cache-Control</code> header is "no-store," which indicates
     * the response should never be cached. Currently, this header value will be sent if the Soffit
     * does not specify a value for <strong>both</strong> scope or max-age.
     */
    public static final String CACHE_CONTROL_NOSTORE = "no-store";

    /**
     * Indicates the response may be cached with validation caching based on Last-Modified or ETag.
     * These features are not currently implemented.
     */
    public static final String CACHE_CONTROL_NOCACHE = "no-cache";

    /*
     * Implementation
     */

    private final String name;

    private Headers(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
