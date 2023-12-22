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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apereo.portal.soffit.model.v1_0.Preferences;
import org.springframework.stereotype.Service;

/**
 * Responsible for issuing and parsing the collection of preferences.
 *
 * @since 5.0
 */
@Service
public class PreferencesService extends AbstractJwtService {

    public Preferences createPreferences(
            Map<String, List<String>> preferencesMap, String username, Date expires) {

        final Claims claims = createClaims(Preferences.class, username, expires);

        // PreferencesMap
        for (Map.Entry<String, List<String>> y : preferencesMap.entrySet()) {
            claims.put(y.getKey(), y.getValue());
        }

        return new Preferences(generateEncryptedToken(claims), preferencesMap);
    }

    public Preferences parsePreferences(String preferencesToken) {

        final Jws<Claims> claims = parseEncryptedToken(preferencesToken, Preferences.class);

        final String username = claims.getBody().getSubject();

        final Map<String, List<String>> preferencesMap = new HashMap<>();
        for (Map.Entry<String, Object> y : claims.getBody().entrySet()) {
            final String key = y.getKey();
            if (JwtClaims.forName(key) != null) {
                // Skip these;  we handle these differently
                continue;
            }

            if (y.getValue() instanceof List) {
                @SuppressWarnings("unchecked")
                final List<String> values = (List<String>) y.getValue();
                preferencesMap.put(key, values);
            } else {
                logger.warn("Unexpected claim '{}' was not a List;  skipping", key);
            }
        }

        Preferences result = new Preferences(preferencesToken, preferencesMap);
        logger.debug("Produced the following Preferences for user '{}':  {}", username, result);
        return result;
    }
}
