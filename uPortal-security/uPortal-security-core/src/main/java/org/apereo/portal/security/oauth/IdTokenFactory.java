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
package org.apereo.portal.security.oauth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.soffit.Headers;
import org.apereo.portal.soffit.service.AbstractJwtService;
import org.apereo.portal.soffit.service.JwtEncryptor;
import org.apereo.portal.soffit.service.JwtSignatureAlgorithmFactory;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Produces OIDC ID Tokens for the OIDC userinfo endpoint. Supports nearly all the Standard Claims
 * as defined by OpenID Connect Core 1.0 (http://openid.net/specs/openid-connect-core-1_0.html).
 *
 * @since 5.1
 */
@Component
public class IdTokenFactory {

    private static final String LIST_SEPARATOR = ",";

    @Value("${portal.protocol.server.context}")
    private String issuer;

    @Autowired private IPersonAttributeDao personAttributeDao;

    @Value(
            "${"
                    + AbstractJwtService.SIGNATURE_KEY_PROPERTY
                    + ":"
                    + AbstractJwtService.DEFAULT_SIGNATURE_KEY
                    + "}")
    private String signatureKey;

    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.timeoutSeconds:300}")
    private long timeoutSeconds;

    /*
     * OpenID Standard Claims
     *
     * Mapping to uPortal user attributes;  defaults (where specified) are based on the Lightweight
     * Directory Access Protocol (LDAP): Schema for User Applications
     * (https://tools.ietf.org/html/rfc4519) and the eduPerson Object Class Specification
     * (http://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201310.html), except
     * 'username' and 'displayName' (which are a uPortal standards).
     */

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.name:displayName}")
    private String nameAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.given_name:givenName}")
    private String givenNameAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.family_name:sn}")
    private String familyNameAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.middle_name:}")
    private String middleNameAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.nickname:eduPersonNickname}")
    private String nicknameAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.preferred_username:}")
    private String preferredUsernameAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.profile:}")
    private String profileAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.picture:}")
    private String pictureAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.website:}")
    private String websiteAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.email:mail}")
    private String emailAttr;

    /** JSON data type 'boolean' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.email_verified:}")
    private String emailVerifiedAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.gender:}")
    private String genderAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.birthdate:}")
    private String birthdateAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.zoneinfo:}")
    private String zoneinfoAttr;

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.locale:}")
    private String localeAttr;

    /** JSON data type 'string' */
    @Value(
            "${org.apereo.portal.security.oauth.IdTokenFactory.mapping.phone_number:telephoneNumber}")
    private String phoneNumberAttr;

    /** JSON data type 'boolean' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.phone_number_verified:}")
    private String phoneNumberVerifiedAttr;

    /*
     * NB:  The 'address' claim requires additional complexity b/c it's type is JSON object.  In
     * light of that, and because most portals don't have address info in the user attributes
     * collection, we'll skip it (for now),
     */

    /** JSON data type 'number' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.updated_at:}")
    private String updatedAtAttributeName;

    /*
     * uPortal Custom Claims
     */

    /**
     * The custom claim <code>groups</code> may contain some or all of the user's group
     * affiliations. Use the Spring property <code>
     * org.apereo.portal.security.oauth.IdTokenFactory.groups.whitelist</code> to control which
     * portal groups are included in the claim.
     */
    @Value(
            "${org.apereo.portal.security.oauth.IdTokenFactory.groups.whitelist:Students,Faculty,Staff,Portal Administrators}")
    private String groupsWhitelistProperty;

    /**
     * Additional user attributes in the portal may be included in the ID Token as custom claims.
     * Use the Spring property <code>org.apereo.portal.security.oauth.IdTokenFactory.customClaims
     * </code> to specify which additional attributes to include. The claim name will always be the
     * same as the attribute name. The JSON type of a custom claim will be inferred from it's value.
     */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.customClaims:}")
    private String customClaimsProperty;

    private Set<ClaimMapping> mappings;

    private Set<String> groupsWhitelist;

    private Set<String> customClaims;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired private JwtSignatureAlgorithmFactory algorithmFactory;

    @Autowired private JwtEncryptor jwtEncryptor;

    @PostConstruct
    public void init() {

        // Mappings for Standard Claims
        final Set<ClaimMapping> set = new HashSet<>();
        set.add(new ClaimMapping("name", nameAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("given_name", givenNameAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("family_name", familyNameAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("middle_name", middleNameAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("nickname", nicknameAttr, DataTypeConverter.STRING));
        set.add(
                new ClaimMapping(
                        "preferred_username", preferredUsernameAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("profile", profileAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("picture", pictureAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("website", websiteAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("email", emailAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("email_verified", emailVerifiedAttr, DataTypeConverter.BOOLEAN));
        set.add(new ClaimMapping("gender", genderAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("birthdate", birthdateAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("zoneinfo", zoneinfoAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("locale", localeAttr, DataTypeConverter.STRING));
        set.add(new ClaimMapping("phone_number", phoneNumberAttr, DataTypeConverter.STRING));
        set.add(
                new ClaimMapping(
                        "phone_number_verified",
                        phoneNumberVerifiedAttr,
                        DataTypeConverter.BOOLEAN));
        set.add(new ClaimMapping("updated_at", updatedAtAttributeName, DataTypeConverter.NUMBER));
        mappings = Collections.unmodifiableSet(set);

        if (logger.isInfoEnabled()) {
            final StringBuilder msg = new StringBuilder();
            msg.append("Using the following mappings for OIDC Standard Claims:");
            set.forEach(mapping -> msg.append("\n\t").append(mapping));
            logger.info(msg.toString());
        }

        // Portal Groups ('groups' custom claim)
        groupsWhitelist =
                Collections.unmodifiableSet(
                        Arrays.stream(groupsWhitelistProperty.split(LIST_SEPARATOR))
                                .map(String::trim)
                                .filter(item -> item.length() != 0)
                                .collect(Collectors.toSet()));
        logger.info(
                "Using the following portal groups to build the custom 'groups' claim:  {}",
                groupsWhitelist);

        // Other Custom Claims (a.k.a user attributes)
        customClaims =
                Collections.unmodifiableSet(
                        Arrays.stream(customClaimsProperty.split(LIST_SEPARATOR))
                                .map(String::trim)
                                .filter(item -> item.length() != 0)
                                .collect(Collectors.toSet()));
        logger.info("Using the following custom claims:  {}", customClaims);
    }

    public String createUserInfo(String username) {
        return this.createUserInfo(username, null, null);
    }

    public String createUserInfo(
            String username, Set<String> claimsToInclude, Set<String> groupsToInclude) {

        final Date now = new Date();
        final Date expires = new Date(now.getTime() + (timeoutSeconds * 1000L));

        final JwtBuilder builder =
                Jwts.builder()
                        .setIssuer(issuer)
                        .setSubject(username)
                        .setAudience(issuer)
                        .setExpiration(expires)
                        .setIssuedAt(now);

        final IPersonAttributes person = personAttributeDao.getPerson(username);

        // Attribute mappings
        mappings.stream()
                .filter(mapping -> includeClaim(mapping.getClaimName(), claimsToInclude))
                .forEach(
                        item -> {
                            final Object value = person.getAttributeValue(item.getAttributeName());
                            if (value != null) {
                                builder.claim(
                                        item.getClaimName(),
                                        item.getDataTypeConverter().convert(value));
                            }
                        });

        // Groups
        final List<String> groups = new ArrayList<>();
        final IGroupMember groupMember = GroupService.getGroupMember(username, IPerson.class);
        if (groupMember != null) {
            final Set<IEntityGroup> ancestors = groupMember.getAncestorGroups();
            for (IEntityGroup g : ancestors) {
                if (includeGroup(g, groupsToInclude)) {
                    groups.add(g.getName());
                }
            }
        }
        if (!groups.isEmpty()) {
            /*
             * If a Claim is not returned, that Claim Name SHOULD be omitted from the JSON object
             * representing the Claims; it SHOULD NOT be present with a null or empty string value.
             */
            builder.claim("groups", groups);
        }

        // Default custom claims defined by uPortal.properties
        customClaims.stream()
                .filter(claimName -> includeClaim(claimName, claimsToInclude))
                .map(
                        attributeName ->
                                new CustomClaim(
                                        attributeName, person.getAttributeValues(attributeName)))
                .filter(claim -> claim.getClaimValue() != null)
                .forEach(claim -> builder.claim(claim.getClaimName(), claim.getClaimValue()));

        final String rslt =
                builder.signWith(algorithmFactory.getAlgorithm(), signatureKey).compact();

        logger.debug("Produced the following JWT for username='{}':  {}", username, rslt);

        return rslt;
    }

    /**
     * Convenience method for obtaining an OIDC Id Token from a request, if present.
     *
     * @return A fully-parsed JWT if a valid bearer token is present in the <code>Authorization
     *     </code> header, otherwise <code>null</code>
     */
    public Jws<Claims> getUserInfo(HttpServletRequest request) {
        final String bearerToken = getBearerToken(request);
        return StringUtils.isNotBlank(bearerToken) ? parseBearerToken(bearerToken) : null;
    }

    public String getBearerToken(HttpServletRequest request) {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        logger.debug("{} header value:  {}", HttpHeaders.AUTHORIZATION, authorization);
        return StringUtils.isNotBlank(authorization)
                        && authorization.length() > Headers.BEARER_TOKEN_PREFIX.length()
                ? authorization.substring(Headers.BEARER_TOKEN_PREFIX.length())
                : null;
    }

    public Jws<Claims> parseBearerToken(String bearerToken) {
        try {
            final String jwt = jwtEncryptor.decryptIfTokenEncrypted(bearerToken);
            return Jwts.parser().setSigningKey(signatureKey).parseClaimsJws(jwt);
        } catch (Exception e) {
            logger.warn("Unsupported bearerToken:  {}", bearerToken);
            logger.debug("Stack trace", e);
        }
        return null;
    }

    private boolean includeClaim(String claimName, Set<String> claimsToInclude) {
        boolean rslt = true; // default
        if (claimsToInclude != null && !claimsToInclude.contains(claimName)) {
            /*
             * This group is included in the deployed configuration,
             * but is not wanted by the REST request.
             */
            rslt = false;
        }
        return rslt;
    }

    private boolean includeGroup(IEntityGroup group, Set<String> groupsToInclude) {
        boolean rslt = groupsWhitelist.contains(group.getName()); // default
        if (rslt && groupsToInclude != null && !groupsToInclude.contains(group.getName())) {
            /*
             * This group is included in the deployed configuration,
             * but is not wanted by the REST request.
             */
            rslt = false;
        }
        return rslt;
    }

    /*
     * Nested Types
     */

    enum DataTypeConverter {
        STRING {
            @Override
            Object convert(Object inpt) {
                return inpt.toString();
            }
        },

        BOOLEAN {
            @Override
            Object convert(Object inpt) {
                return Boolean.valueOf(inpt.toString());
            }
        },

        NUMBER {
            @Override
            Object convert(Object inpt) {
                return new BigDecimal(inpt.toString());
            }
        };

        abstract Object convert(Object inpt);
    }

    private static final class ClaimMapping {

        private final String claimName;
        private final String attributeName;
        private final DataTypeConverter dataTypeConverter;

        public ClaimMapping(
                String claimName, String attributeName, DataTypeConverter dataTypeConverter) {
            this.claimName = claimName;
            this.attributeName = attributeName;
            this.dataTypeConverter = dataTypeConverter;
        }

        public String getClaimName() {
            return claimName;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public DataTypeConverter getDataTypeConverter() {
            return dataTypeConverter;
        }

        @Override
        public String toString() {
            return "ClaimMapping{"
                    + "claimName='"
                    + claimName
                    + '\''
                    + ", attributeName='"
                    + attributeName
                    + '\''
                    + ", dataTypeConverter="
                    + dataTypeConverter
                    + '}';
        }
    }

    private static final class CustomClaim {

        private final String claimName;
        private final Object claimValue;

        public CustomClaim(String claimName, Object claimValue) {
            this.claimName = claimName;
            this.claimValue = claimValue;
        }

        public String getClaimName() {
            return claimName;
        }

        public Object getClaimValue() {
            return claimValue;
        }
    }
}
