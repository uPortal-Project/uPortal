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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.oauth.IdTokenFactory;
import org.apereo.portal.services.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller provides endpoints through which clients can obtain an ID Token in a format that
 * is aligned with OpenID Connect (OIDC). <i>Clients</i> in this case are normally content objects
 * embedded within the portal page itself (e.g. portlets, soffits, or JS bundles).
 *
 * <p>It is critical to point out that this endpoint is not a compliant OIDC Identity Provider. It
 * does not implement OAuth Authentication Flows of any sort.
 *
 * <p>The value of this endpoint is not (therefore) that it brings support for OIDC to uPortal, but
 * that other modules and services designed to work with uPortal can implement security using
 * standard OIDC approaches.
 *
 * @since 5.1
 */
@RestController
public class OidcUserInfoController {

    public static final String USERINFO_ENDPOINT_URI = "/v5-1/userinfo";
    public static final String USERINFO_CONTENT_TYPE = "application/jwt";
    public static final String TOKEN_ENDPOINT_URI = "/v5-5/oauth/token";

    @Autowired private IPersonManager personManager;

    @Autowired private IdTokenFactory idTokenFactory;

    @Autowired(required = false)
    private List<OAuthClient> clientList = Collections.emptyList();

    private Map<String, OAuthClient> clientMap = Collections.emptyMap();

    @Autowired private PersonService personService;

    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.timeoutSeconds:300}")
    private long timeoutSeconds;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        final Map<String, OAuthClient> map =
                clientList.stream()
                        .collect(Collectors.toMap(OAuthClient::getClientId, Function.identity()));
        this.clientMap = Collections.unmodifiableMap(map);
    }

    /** Obtain an OIDC Id token for the current user. */
    @RequestMapping(
            value = USERINFO_ENDPOINT_URI,
            produces = USERINFO_CONTENT_TYPE,
            method = {RequestMethod.GET, RequestMethod.POST})
    public String userInfo(
            HttpServletRequest request,
            @RequestParam(value = "claims", required = false) String claims,
            @RequestParam(value = "groups", required = false) String groups) {

        final IPerson person = personManager.getPerson(request);
        return createToken(request, person, claims, groups);
    }

    /**
     * Obtain an OIDC Id token for the specified <code>client_id</code>. At least one bean of type
     * {@link OAuthClient} is required to use this endpoint.
     *
     * <p>This token strategy supports Spring's <code>OAuth2RestTemplate</code> for accessing
     * uPortal REST APIs from external systems. Use a <code>ClientCredentialsResourceDetails</code>
     * with <code>clientAuthenticationScheme=AuthenticationScheme.form</code>, together with a
     * <code>ClientCredentialsAccessTokenProvider</code>.
     *
     * @since 5.5
     */
    @PostMapping(value = TOKEN_ENDPOINT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity oauthToken(
            @RequestParam(value = "client_id") String clientId,
            @RequestParam(value = "client_secret") String clientSecret,
            @RequestParam(
                            value = "grant_type",
                            required = false,
                            defaultValue = "client_credentials")
                    String grantType,
            @RequestParam(value = "scope", required = false, defaultValue = "/all") String scope,
            @RequestParam(value = "claims", required = false) String claims,
            @RequestParam(value = "groups", required = false) String groups,
            HttpServletRequest request) {

        /*
         * NB:  Several of this method's parameters are not consumed (yet) in any way.  They are
         * defined to match a two-legged OAuth strategy and for future use.
         */

        final String msg =
                "Processing request for OAuth access token;  client_id='{}', client_secret='{}', "
                        + "grant_type='{}', scope='{}', claims='{}', groups='{}'";
        logger.debug(
                msg,
                clientId,
                StringUtils.repeat("*", clientSecret.length()),
                grantType,
                scope,
                claims,
                groups);

        // STEP 1:  identify the client
        final OAuthClient oAuthClient = clientMap.get(clientId);
        if (oAuthClient == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("message", "client_id not found"));
        }

        logger.debug(
                "Selected known OAuthClient with client_id='{}' for access token request",
                oAuthClient.getClientId());

        // STEP 2:  validate the client_secret
        if (!oAuthClient.getClientSecret().equals(clientSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("message", "authentication failed"));
        }

        // STEP 3:  obtain the specified user
        final IPerson person = personService.getPerson(oAuthClient.getPortalUserAccount());
        if (person == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Collections.singletonMap(
                                    "message",
                                    "portal user account not found: "
                                            + oAuthClient.getPortalUserAccount()));
        }

        logger.debug(
                "Selected portal Person with username='{}' for client_id='{}'",
                person.getUserName(),
                oAuthClient.getClientId());

        // STEP 4:  build a standard OAuth2 access token response
        final String token = createToken(request, person, claims, groups);
        final Map<String, Object> result = new HashMap<>();
        result.put("access_token", token);
        result.put("token_type", "bearer");
        result.put(
                "expires_in",
                timeoutSeconds > 2 ? timeoutSeconds - 2L /* fudge factor */ : timeoutSeconds);
        result.put("scope", scope);

        logger.debug(
                "Produced the following access token for client_id='{}':  {}",
                oAuthClient.getClientId(),
                result);

        return ResponseEntity.ok(result);
    }

    private String createToken(
            HttpServletRequest request, IPerson person, String claims, String groups) {

        Set<String> claimsToInclude = null;
        if (claims != null) {
            String[] tokens = claims.split("[,]");
            claimsToInclude = new HashSet<>(Arrays.asList(tokens));
        }

        Set<String> groupsToInclude = null;
        if (groups != null) {
            String[] tokens = groups.split("[,]");
            groupsToInclude = new HashSet<>(Arrays.asList(tokens));
        }

        return idTokenFactory.createUserInfo(
                request, person.getUserName(), claimsToInclude, groupsToInclude);
    }
}
