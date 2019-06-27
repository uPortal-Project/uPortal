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
package org.apereo.portal.soffit.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Wrapper for code to encrypt and decrypt JWT tokens. The spec is evolving, so this implementation
 * is expected to change.
 *
 * @since 5.7.0
 */
@Slf4j
@Component
public class JwtEncryptor {
    public static final String ENCRYPT_JWT_YN = "org.apereo.portal.soffit.jwt.encrypt";
    public static final String DEFAULT_ENCRYPT_JWT_YN = "true";

    public static final String ENCRYPTION_PASSWORD_PROPERTY =
            "org.apereo.portal.soffit.jwt.encryptionPassword";
    public static final String DEFAULT_ENCRYPTION_PASSWORD = "CHANGEME";

    @Value("${" + ENCRYPTION_PASSWORD_PROPERTY + ":" + DEFAULT_ENCRYPTION_PASSWORD + "}")
    private String encryptionPassword;

    // BASE64url encoding, per RFC7519
    public static final String BASE64_REGEX = "[\\p{Alnum}-_=]+";
    public static final Pattern JWT_PATTERN =
            Pattern.compile("^" + BASE64_REGEX + "\\." + BASE64_REGEX + "\\." + BASE64_REGEX + "$");

    @Value("${" + ENCRYPT_JWT_YN + ":" + DEFAULT_ENCRYPT_JWT_YN + "}")
    private boolean encryptJwtYN;

    /*
     * NOTE:  There is also a StrongTextEncryptor, but it requires each deployment
     * to download and install the "Java Cryptography Extension (JCE) Unlimited
     * Strength Jurisdiction Policy Files," which sounds like a tremendous PITA.
     * The BasicTextEncryptor supports "normal-strength encryption of texts,"
     * which should be satisfactory for our needs.
     */
    final BasicTextEncryptor textEncryptor = new BasicTextEncryptor();

    @PostConstruct
    public void init() {
        if (encryptJwtYN) {
            // log.info("Encrypting Soffit JWTs");
            // Encryption Password
            if (StringUtils.isBlank(encryptionPassword)) {
                log.error(
                        "The value of required property {} is blank", ENCRYPTION_PASSWORD_PROPERTY);
                throw new IllegalStateException("Missing property " + ENCRYPTION_PASSWORD_PROPERTY);
            } else if (DEFAULT_ENCRYPTION_PASSWORD.equals(encryptionPassword)) {
                log.warn(
                        "Property {} is using the default value;  please change it",
                        ENCRYPTION_PASSWORD_PROPERTY);
            }
            textEncryptor.setPassword(encryptionPassword);
        }
    }

    public String encryptIfConfigured(String jwt) {
        if (encryptJwtYN) {
            return textEncryptor.encrypt(jwt);
        } else {
            return jwt;
        }
    }

    public String decryptIfConfigured(String jwt) {
        if (encryptJwtYN) {
            return textEncryptor.decrypt(jwt);
        }
        return jwt;
    }

    /**
     * Special decrypt testing method used by OIDC code that does not support encryption but may
     * receive Soffit JWTs that do support encryption.
     *
     * <p>OIDC code will support encryption in a future patch. This is to address retain
     * compatibility before the next major release.
     *
     * @param jwt the {code String} from AUTHORIZATION header
     * @return jwt string if it matches the spec; otherwise an attempt to decrypt the string is made
     *     before returning the value
     */
    public String decryptIfInvalidFormat(String jwt) {
        if (encryptJwtYN) {
            Matcher m = JWT_PATTERN.matcher(jwt);
            if (!m.matches()) {
                return textEncryptor.decrypt(jwt);
            }
        }
        return jwt;
    }
}
