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
import java.util.List;
import java.util.Map;
import org.apereo.portal.soffit.model.v1_0.PortalRequest;
import org.springframework.stereotype.Service;

/**
 * Responsible for issuing and parsing the {@link PortalRequest} provided by the Soffit Connector.
 *
 * @since 5.0
 */
@Service
public class PortalRequestService extends AbstractJwtService {

    public PortalRequest createPortalRequest(
            Map<String, String> properties,
            Map<String, List<String>> attributes,
            Map<String, List<String>> parameters,
            String username,
            Date expires) {

        final Claims claims = createClaims(PortalRequest.class, username, expires);

        // Properties
        claims.put(JwtClaims.PROPERTIES.getName(), properties);

        // Attributes
        claims.put(JwtClaims.ATTRIBUTES.getName(), attributes);

        // Parameters
        claims.put(JwtClaims.PARAMETERS.getName(), parameters);

        return new PortalRequest(
                generateEncryptedToken(claims), properties, attributes, parameters);
    }

    public PortalRequest parsePortalRequest(String portalRequestToken) {

        final Jws<Claims> claims = parseEncryptedToken(portalRequestToken, PortalRequest.class);

        final String username = claims.getBody().getSubject();

        // Properties
        @SuppressWarnings("unchecked")
        final Map<String, String> properties =
                (Map<String, String>) claims.getBody().get(JwtClaims.PROPERTIES.getName());

        // Attributes
        @SuppressWarnings("unchecked")
        final Map<String, List<String>> attributes =
                (Map<String, List<String>>) claims.getBody().get(JwtClaims.ATTRIBUTES.getName());

        // Parameters
        @SuppressWarnings("unchecked")
        final Map<String, List<String>> parameters =
                (Map<String, List<String>>) claims.getBody().get(JwtClaims.PARAMETERS.getName());

        PortalRequest result =
                new PortalRequest(portalRequestToken, properties, attributes, parameters);
        logger.debug("Produced the following PortalRequest for user '{}':  {}", username, result);
        return result;
    }
}
