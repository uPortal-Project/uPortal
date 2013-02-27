/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.rest;

import org.jasig.portal.utils.web.RemoteCookieCheckFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Chris Waymire <cwaymire@unicon.net>
 */

@Controller
public class RemoteCookieCheckController {
    public static final String COOKIE_CHECK_REQUEST_MAPPING = "/cookiecheck";

    @RequestMapping(value=COOKIE_CHECK_REQUEST_MAPPING, method = RequestMethod.GET)
    public ModelAndView verifyCookiesEnabled(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
        final ModelAndView mv = new ModelAndView();

        boolean cookieFound = false;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase(RemoteCookieCheckFilter.COOKIE_NAME)) {
                    cookieFound = true;
                    break;
                }
            }
        }

        if (cookieFound) {
            String referer = (String)request.getSession().getAttribute(RemoteCookieCheckFilter.REFERER_ATTRIBUTE);
            response.sendRedirect(referer);
            return null;
        } else {
            return new ModelAndView("/jsp/PortletError/cookies");
        }
    }
}
