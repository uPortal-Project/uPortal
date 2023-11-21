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

/**
 * Set of all the JWT claims in use within Soffit.
 *
 * @since 5.0
 */
public enum JwtClaims {

    /*
     * Registered Claims (https://tools.ietf.org/html/rfc7519#section-4.1)
     *
     * All RFC 7519 registered claims, whether we use them or not
     */

    ISSUER("iss"),
    SUBJECT("sub"),
    AUDIENCE("aud"),
    EXPIRATION_TIME("exp"),
    NOT_BEFORE("nbf"),
    ISSUED_AT("iat"),
    JWT_ID("jti"),

    /*
     * Custom Claims
     */

    /** Concrete Java class to which the JWT deserializes; used by all data model objects. */
    CLASS("class"),

    /** List of group names to which the user belongs; used by Bearer objects. */
    GROUPS("groups"),

    /** List of group names to which the user belongs; used by Bearer objects. */
    PROPERTIES("properties"),

    /** List of group names to which the user belongs; used by Bearer objects. */
    ATTRIBUTES("attributes"),

    /**
     * Used both by by Definition objects (publication parameters of the content object configured
     * in the Portlet Manager) and by PortalRequest objects (request parameters).
     */
    PARAMETERS("parameters"),

    /**
     * Title of the content object configured in the Portlet Manager; used by Definition objects.
     */
    TITLE("title"),

    /**
     * FName of the content object configured in the Portlet Manager; used by Definition objects.
     */
    FNAME("fname"),

    /**
     * Description of the content object configured in the Portlet Manager; used by Definition
     * objects.
     */
    DESCRIPTION("description"),

    /**
     * Categories to which the content object belongs, as configured in the Portlet Manager; used by
     * Definition objects.
     */
    CATEGORIES("categories");

    /*
     * Implementation
     */

    private final String name;

    private JwtClaims(String name) {
        this.name = name;
    }

    public static JwtClaims forName(String name) {
        JwtClaims result = null; // default
        for (JwtClaims claim : JwtClaims.values()) {
            if (claim.getName().equals(name)) {
                result = claim;
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }
}
