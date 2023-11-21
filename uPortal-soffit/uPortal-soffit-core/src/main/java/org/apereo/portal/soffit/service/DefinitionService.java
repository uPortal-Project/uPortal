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
import org.apereo.portal.soffit.model.v1_0.Definition;
import org.springframework.stereotype.Service;

/**
 * Responsible for issuing and parsing Definition tokens.
 *
 * @since 5.0
 */
@Service
public class DefinitionService extends AbstractJwtService {

    public Definition createDefinition(
            String title,
            String fname,
            String description,
            List<String> categories,
            Map<String, List<String>> parameters,
            String username,
            Date expires) {

        final Claims claims = createClaims(Definition.class, username, expires);

        // Title
        claims.put(JwtClaims.TITLE.getName(), title);

        // FName
        claims.put(JwtClaims.FNAME.getName(), fname);

        // Description
        claims.put(JwtClaims.DESCRIPTION.getName(), description);

        // Categories
        claims.put(JwtClaims.CATEGORIES.getName(), categories);

        // Parameters
        claims.put(JwtClaims.PARAMETERS.getName(), parameters);

        return new Definition(
                generateEncryptedToken(claims), title, fname, description, categories, parameters);
    }

    public Definition parseDefinition(String definitionToken) {

        final Jws<Claims> claims = parseEncryptedToken(definitionToken, Definition.class);

        final String username = claims.getBody().getSubject();

        // Title
        final String title = (String) claims.getBody().get(JwtClaims.TITLE.getName());

        // FName
        final String fname = (String) claims.getBody().get(JwtClaims.FNAME.getName());

        // Description
        final String description = (String) claims.getBody().get(JwtClaims.DESCRIPTION.getName());

        // Categories
        @SuppressWarnings("unchecked")
        final List<String> categories =
                (List<String>) claims.getBody().get(JwtClaims.CATEGORIES.getName());

        // Parameters
        @SuppressWarnings("unchecked")
        final Map<String, List<String>> parameters =
                (Map<String, List<String>>) claims.getBody().get(JwtClaims.PARAMETERS.getName());

        Definition result =
                new Definition(definitionToken, title, fname, description, categories, parameters);
        logger.debug("Produced the following Definition for user '{}':  {}", username, result);
        return result;
    }
}
