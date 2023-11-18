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
package org.apereo.portal.soffit.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.soffit.Headers;
import org.apereo.portal.soffit.service.AbstractJwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * This concrete implementation of <code>AbstractPreAuthenticatedProcessingFilter</code> (Spring
 * Security) may be employed by REST APIs within Soffits (and similar modules) to discover the
 * identity of portal users simply and securely. (It is not used inside the uPortal webapp at all.)
 * The approach is inspired by (and closely aligned with) OpenID Connect (OIDC). This filter honors
 * an OIDC ID Token (typically from the portal) presented as a <code>Bearer</code> token in the
 * <code>Authorization</code> header. A suitable token may be obtained (via JavaScript, etc.) from
 * the portal's <code>userinfo</code> endpoint if the user has previously signed in.
 *
 * <p><strong>Important!</strong> In a module that contains portlets, this filter must not be
 * defined as a top-level bean in the Spring Application Context. If it is, it will become a member
 * of the standard filter chain and will be executed for every request, including portlet requests
 * via a <code>RequestDispatcher</code>. The best way to use this filter in a project that contains
 * portlets is in Java configuration via <code>HttpSecurity.addFilter()</code> <em>without</em>
 * obtaining the instance of this filter from a method annotated with <code>@Bean</code>.
 *
 * @since 5.1
 */
public class SoffitApiPreAuthenticatedProcessingFilter
        extends AbstractPreAuthenticatedProcessingFilter {

    private static final String USER_DETAILS_REQUEST_ATTRIBUTE =
            SoffitApiPreAuthenticatedProcessingFilter.class.getName()
                    + ".userDetailsRequestAttribute";

    private final String signatureKey;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SoffitApiPreAuthenticatedProcessingFilter(String signatureKey) {
        // Key for signing JWT.  Must match the provider (typically the portal).
        this.signatureKey = signatureKey;
        if (AbstractJwtService.DEFAULT_SIGNATURE_KEY.equals(signatureKey)) {
            logger.warn(
                    "A custom value for '{}' has not been specified;  the default value will be "
                            + "used.  This configuration is not production-safe!",
                    AbstractJwtService.SIGNATURE_KEY_PROPERTY);
        }
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isBlank(authHeader)
                || !authHeader.startsWith(Headers.BEARER_TOKEN_PREFIX)) {
            /*
             * In authenticating the user, this filter has no opinion if either (1) the
             * Authorization header is not set or (2) the value isn't a Bearer token.
             */
            return null;
        }

        final String bearerToken = authHeader.substring(Headers.BEARER_TOKEN_PREFIX.length());

        try {
            // Validate & parse the JWT
            final Jws<Claims> claims =
                    Jwts.parser().setSigningKey(signatureKey).parseClaimsJws(bearerToken);

            logger.debug("Found the following pre-authenticated user:  {}", claims.toString());

            final List<String> groupsClaim = claims.getBody().get("groups", List.class);
            final List<String> groupsList =
                    groupsClaim != null ? groupsClaim : Collections.emptyList();
            final UserDetails result =
                    new SoffitApiUserDetails(claims.getBody().getSubject(), groupsList);
            request.setAttribute(USER_DETAILS_REQUEST_ATTRIBUTE, result);
            return result;
        } catch (Exception e) {
            logger.info("The following Bearer token is unusable:  '{}'", bearerToken);
            logger.debug("Failed to validate and/or parse the specified Bearer token", e);
        }

        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return request.getAttribute(USER_DETAILS_REQUEST_ATTRIBUTE) != null ? "N/A" : null;
    }
}
