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

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apereo.portal.soffit.service.AbstractJwtService;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Produces OIDC ID Tokens for the OIDC userinfo endpoint. Supports nearly all the Standard Claims
 * as defined by OpenID Connect Core 1.0 (http://openid.net/specs/openid-connect-core-1_0.html).
 *
 * @since 5.1
 */
@Component
public class IdTokenFactory {

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
     * Mapping to uPortal user attributes;  defaults (where specified) are based on the Lightweight
     * Directory Access Protocol (LDAP): Schema for User Applications
     * (https://tools.ietf.org/html/rfc4519) and the eduPerson Object Class Specification
     * (http://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201310.html), except
     * 'username' and 'displayName' (which are a uPortal standards).
     */

    /** JSON data type 'string' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.sub:username}")
    private String subAttr;

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
     * NB:  The 'address' claim requires additional complexity b/c it's type is JSON object.
     */

    /** JSON data type 'number' */
    @Value("${org.apereo.portal.security.oauth.IdTokenFactory.mapping.updated_at:}")
    private String updatedAtAttributeName;

    private List<ClaimMapping> mappings;

    @PostConstruct
    public void init() {
        final List<ClaimMapping> list = new ArrayList<>();

        list.add(new ClaimMapping("sub", subAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("name", nameAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("given_name", givenNameAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("family_name", familyNameAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("middle_name", middleNameAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("nickname", nicknameAttr, DataTypeConverter.STRING));
        list.add(
                new ClaimMapping(
                        "preferred_username", preferredUsernameAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("profile", profileAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("picture", pictureAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("website", websiteAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("email", emailAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("email_verified", emailVerifiedAttr, DataTypeConverter.BOOLEAN));
        list.add(new ClaimMapping("gender", genderAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("birthdate", birthdateAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("zoneinfo", zoneinfoAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("locale", localeAttr, DataTypeConverter.STRING));
        list.add(new ClaimMapping("phone_number", phoneNumberAttr, DataTypeConverter.STRING));
        list.add(
                new ClaimMapping(
                        "phone_number_verified",
                        phoneNumberVerifiedAttr,
                        DataTypeConverter.BOOLEAN));
        list.add(new ClaimMapping("updated_at", updatedAtAttributeName, DataTypeConverter.NUMBER));

        mappings = Collections.unmodifiableList(list);
    }

    public String createUserInfo(String username) {

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

        mappings.stream()
                .forEach(
                        item -> {
                            final Object value = person.getAttributeValue(item.getAttributeName());
                            if (value != null) {
                                builder.claim(
                                        item.getClaimName(),
                                        item.getDataTypeConverter().convert(value));
                            }
                        });

        return builder.signWith(SignatureAlgorithm.HS512, signatureKey).compact();
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
    }
}
