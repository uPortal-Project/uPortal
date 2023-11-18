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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.*;
import org.apereo.portal.soffit.model.v1_0.Bearer;
import org.springframework.stereotype.Service;

/**
 * Responsible for issuing and parsing Bearer tokens.
 *
 * @since 5.0
 */
@Service
public class BearerService extends AbstractJwtService {

    public Bearer createBearer(
            String username,
            Map<String, List<String>> attributes,
            List<String> groups,
            Date expires) {

        final Claims claims = createClaims(Bearer.class, username, expires);

        /*
         * User attributes; attribute names that match registered attributes
         * (https://www.iana.org/assignments/jwt/jwt.xhtml) will be
         * automatically portable.
         */
        for (Map.Entry<String, List<String>> y : attributes.entrySet()) {
            final String name = y.getKey();
            switch (y.getValue().size()) {
                case 0:
                    // Do nothing...
                    break;
                case 1:
                    // Model as a single value (in this a good idea?)
                    claims.put(name, y.getValue().get(0));
                    break;
                default:
                    // Retain the collection
                    claims.put(name, y.getValue());
                    break;
            }
        }

        // Groups
        claims.put(JwtClaims.GROUPS.getName(), groups);

        return new Bearer(generateEncryptedToken(claims), username, attributes, groups);
    }

    public Bearer parseBearerToken(String bearerToken) {

        final Jws<Claims> claims = parseEncryptedToken(bearerToken, Bearer.class);

        final String username = claims.getBody().getSubject();

        final Map<String, List<String>> attributes = new HashMap<>();
        for (Map.Entry<String, Object> y : claims.getBody().entrySet()) {
            final String key = y.getKey();
            if (JwtClaims.forName(key) != null) {
                // Skip these;  we handle these differently
                continue;
            }

            if (y.getValue() instanceof List) {
                @SuppressWarnings("unchecked")
                final List<String> values = (List<String>) y.getValue();
                attributes.put(key, values);
            } else if (y.getValue() instanceof String) {
                // Convert (back) to a single-item list
                final String value = (String) y.getValue();
                attributes.put(key, Collections.singletonList(value));
            }
        }

        @SuppressWarnings("unchecked")
        final List<String> groups = (List<String>) claims.getBody().get(JwtClaims.GROUPS.getName());

        Bearer result = new Bearer(bearerToken, username, attributes, groups);
        logger.debug("Produced the following Bearer for user '{}':  {}", username, result);
        return result;
    }
}
