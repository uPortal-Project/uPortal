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
package org.apereo.portal.rest;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.oauth.IdTokenFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller provides an endpoint through which clients can obtain an ID Token in a format
 * that is aligned with with OpenID Connect (OIDC). <i>Clients</i> in this case are normally content
 * objects embedded within the portal page itself (e.g. portlets, soffits, or JS bundles).
 *
 * <p>It is critical to point out that this endpoint is not a compliant OIDC Identity Provider. It
 * does not implement OAuth Authentication Flows of any sort. The user (<code>sub</code> in the ID
 * Token) is either logged into the portal (ahead of time) or isn't.
 *
 * <p>The value of this endpoint is not (therefore) that it brings support for OIDC to uPortal, but
 * that other modules and services designed to work with uPortal can implement security using
 * standard OIDC approaches.
 *
 * @since 5.1
 */
@RestController
public class OidcUserInfoController {

    public static final String ENDPOINT_URI = "/v5-1/userinfo";
    public static final String CONTENT_TYPE = "application/jwt";

    @Autowired private IPersonManager personManager;

    @Autowired private IdTokenFactory idTokenFactory;

    @RequestMapping(
            value = ENDPOINT_URI,
            produces = CONTENT_TYPE,
            method = {RequestMethod.GET, RequestMethod.POST})
    public String userInfo(HttpServletRequest request) {
        final IPerson person = personManager.getPerson(request);

        List<String> overrideGroups = null;
        if (request.getParameter("groups") != null) {
            String[] tokens = request.getParameter("groups").split("[,]");

            overrideGroups = Arrays.asList(tokens);
        }

        List<String> overrideCustomClaims = null;
        if (request.getParameter("customClaims") != null) {
            String[] tokens = request.getParameter("customClaims").split("[,]");

            overrideCustomClaims = Arrays.asList(tokens);
        }

        return idTokenFactory.createUserInfo(
                person.getUserName(), overrideGroups, overrideCustomClaims);
    }
}
