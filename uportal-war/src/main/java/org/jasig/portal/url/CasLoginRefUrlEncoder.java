/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.url;

import org.jasig.portal.security.mvc.LoginController;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Encode the originally requested URL for use as a RefUrl parameter for the CAS login use case. This handles the
 * additional encoding needed to encode the originally-requested URL since the originally requested URL will be decoded
 * twice (decoded once when the browser sends it to CAS, then again when the browser sends it back to uPortal after
 * coming from CAS).
 * 
 * @author James Wennmacher, jwennmacher@unicon.net
 */
public class CasLoginRefUrlEncoder implements LoginRefUrlEncoder {

    private String casLoginUrl;

    @Required
    public void setCasLoginUrl(String casLoginUrl) {
        this.casLoginUrl = casLoginUrl;
    }

    public String getCasLoginUrl() {
        return casLoginUrl;
    }

    @Override
    public String encodeLoginAndRefUrl(HttpServletRequest request) throws UnsupportedEncodingException {
        final String requestEncoding = request.getCharacterEncoding();
        final StringBuilder loginRedirect = new StringBuilder();

        loginRedirect.append(casLoginUrl);
        loginRedirect.append(URLEncoder.encode("?", requestEncoding));
        loginRedirect.append(URLEncoder.encode(LoginController.REFERER_URL_PARAM + "=", requestEncoding));

        loginRedirect.append(URLEncoder.encode(request.getRequestURI(), requestEncoding));

        final String queryString = request.getQueryString();
        if (queryString != null) {
            String firstEncoding = URLEncoder.encode("?"+queryString, requestEncoding);
            loginRedirect.append(URLEncoder.encode(firstEncoding, requestEncoding));
        }

        return loginRedirect.toString();
    }

}
