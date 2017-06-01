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
package org.apereo.portal.url;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.security.mvc.LoginController;

/**
 * Encode the originally requested URL for use as a RefUrl parameter for the local (uPortal) login
 * use case.
 *
 */
public class LocalLoginRefUrlEncoder implements LoginRefUrlEncoder {

    @Override
    public String encodeLoginAndRefUrl(HttpServletRequest request)
            throws UnsupportedEncodingException {
        final StringBuilder loginRedirect = new StringBuilder();

        final String loginUrl = request.getContextPath() + "/Login";

        loginRedirect.append(loginUrl);
        loginRedirect.append("?");
        loginRedirect.append(LoginController.REFERER_URL_PARAM + "=");

        final String requestEncoding = request.getCharacterEncoding();
        loginRedirect.append(URLEncoder.encode(request.getRequestURI(), requestEncoding));

        String queryString = request.getQueryString();
        if (queryString != null) {
            loginRedirect.append(URLEncoder.encode("?", requestEncoding));
            loginRedirect.append(URLEncoder.encode(queryString, requestEncoding));
        }

        return loginRedirect.toString();
    }
}
