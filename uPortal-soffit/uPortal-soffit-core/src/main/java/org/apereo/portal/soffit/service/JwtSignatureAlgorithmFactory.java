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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory that returns the JWT {@code MacAlgorithm} to sign with, based on configuration. Provides
 * a default if the configuration value is invalid or unsupported.
 *
 * @since 5.6.1
 */
@Component
@Slf4j
public class JwtSignatureAlgorithmFactory {

    public static final String SIGNATURE_ALGORITHM_PROPERTY =
            "org.apereo.portal.soffit.jwt.signatureAlgorithm";
    public static final String SIGNATURE_ALGORITHM_DEFAULT = "HS512";

    @Value("${" + SIGNATURE_ALGORITHM_PROPERTY + ":" + SIGNATURE_ALGORITHM_DEFAULT + "}")
    private String algorithmStr;

    private MacAlgorithm algorithm = Jwts.SIG.HS512;

    @PostConstruct
    public void init() {
        final SecureDigestAlgorithm<?, ?> configured = Jwts.SIG.get().get(algorithmStr);
        if (configured instanceof MacAlgorithm) {
            algorithm = (MacAlgorithm) configured;
        } else {
            log.warn(
                    "Value '{}' of property {} is not a supported HMAC algorithm",
                    algorithmStr,
                    SIGNATURE_ALGORITHM_PROPERTY);
            log.warn("Default JWT signature algorithm is {}", SIGNATURE_ALGORITHM_DEFAULT);
        }
    }

    public MacAlgorithm getAlgorithm() {
        return algorithm;
    }
}
