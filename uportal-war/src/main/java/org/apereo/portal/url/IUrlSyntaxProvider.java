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
package org.apereo.portal.url;

import javax.servlet.http.HttpServletRequest;

/**
 * Generates a URL string from a {@link IPortalUrlBuilder} or parses a request URL into {@link
 * IPortalRequestInfo}
 *
 */
public interface IUrlSyntaxProvider {
    /**
     * Get the portal request information for the specified request.
     *
     * @param request The current portal request
     * @return Information about the current request
     */
    public IPortalRequestInfo getPortalRequestInfo(HttpServletRequest request);

    /**
     * Get the canonical url for this portal request.
     *
     * @param request The current portal request
     * @return The canonical URL for the request
     */
    public String getCanonicalUrl(HttpServletRequest request);

    /**
     * @param request The current request
     * @param portalUrlBuilder The URL object to create a URL string from
     * @return A URL to be used in markup or as a redirect. The URL will be absolute, starting with
     *     a / or with a protocol such as http://
     */
    public String generateUrl(HttpServletRequest request, IPortalUrlBuilder portalUrlBuilder);

    /**
     * @param request The current request
     * @param portalActionUrlBuilder The URL object to create a URL string from
     * @return A URL to be used in markup or as a redirect. The URL will be absolute, starting with
     *     a / or with a protocol such as http://
     */
    public String generateUrl(
            HttpServletRequest request, IPortalActionUrlBuilder portalActionUrlBuilder);

    /**
     * Attempts to answer whether the two URLs <strong>definitely refer to different</strong>
     * content in the portal. Returns FALSE if they refer to the same content, or if the
     * IUrlSyntaxProvider can't reasonably make that determination.
     *
     * @return TRUE if the two URLs certainly refer to different content; otherwise FALSE
     */
    public boolean doesRequestPathReferToSpecificAndDifferentContentVsCanonicalPath(
            String requestPath, String canonicalPath);
}
