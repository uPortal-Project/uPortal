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
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.soffit.Headers;
import org.apereo.portal.soffit.service.AbstractJwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * This concrete implementation of <code>AbstractPreAuthenticatedProcessingFilter</code> (Spring
 * Security) allows REST APIs within Soffits and similar modules to know the identity of a portal
 * users simply and securely. The approach is inspired by (and closely aligned with) OpenID Connect
 * (OIDC). This filter essentially requests and receives an OIDC ID Token from the portal. The token
 * is available if the user has previously signed in.
 *
 * @since 5.1
 */
public class SoffitApiPreAuthenticatedProcessingFilter
        extends AbstractPreAuthenticatedProcessingFilter {

    private static final String USER_DETAILS_REQUEST_ATTRIBUTE =
            SoffitApiPreAuthenticatedProcessingFilter.class.getName()
                    + ".userDetailsRequestAttribute";

    @Value(
            "${"
                    + AbstractJwtService.SIGNATURE_KEY_PROPERTY
                    + ":"
                    + AbstractJwtService.DEFAULT_SIGNATURE_KEY
                    + "}")
    private String signatureKey;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SoffitApiPreAuthenticatedProcessingFilter() {
        // Check we have the correct principal on each request
        setCheckForPrincipalChanges(true);
    }

    @PostConstruct
    public void init() {
        // Log a warning if a custom signature key is not specified...
        if (AbstractJwtService.DEFAULT_SIGNATURE_KEY.equals(signatureKey)) {
            logger.warn(
                    "A custom value for '{}' has not been specified;  the default value will be used.  This configuration is not production-safe!",
                    AbstractJwtService.SIGNATURE_KEY_PROPERTY);
        }
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isBlank(authHeader)
                || !authHeader.startsWith(Headers.BEARER_TOKEN_PREFIX)) {
            /*
             * We have no opinion if (1) the Authorization header is not set or (2) the value isn't
             * a Bearer token.
             */
            return null;
        }

        final String bearerToken = authHeader.substring(Headers.BEARER_TOKEN_PREFIX.length());

        try {
            // Validate & parse the JWT
            final Jws<Claims> claims =
                    Jwts.parser().setSigningKey(signatureKey).parseClaimsJws(bearerToken);

            logger.debug("Found the following pre-authenticated user:  {}", claims.toString());

            final UserDetails rslt = new SoffitApiUserDetails(claims.getBody().getSubject());
            request.setAttribute(USER_DETAILS_REQUEST_ATTRIBUTE, rslt);
            return rslt;
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
