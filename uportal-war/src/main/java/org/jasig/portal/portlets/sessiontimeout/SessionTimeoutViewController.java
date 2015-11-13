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
package org.jasig.portal.portlets.sessiontimeout;

import java.io.IOException;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;


/**
 * Controller for the session timeout portlet.  This controller mostly just
 * reads the configs and then renders the JSP.
 *
 * @author Josh Helmer, jhelmer@unicon.net
 * @since 4.1.1
 */
@Controller
@RequestMapping("VIEW")
public class SessionTimeoutViewController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final int DEFAULT_DIALOG_DISPLAY_SECONDS = 60;
    private static final String DEFAULT_LOGOUT_URL_FRAGMENT = "/Logout";
    private static final String DEFAULT_RESET_SESSION_FRAGMENT = "/api/ajax-success";

    private static final String HEADER_JSP = "jsp/SessionTimeout/header";
    private static final String BODY_JSP = "jsp/SessionTimeout/body";

    /* Model attribute names */
    public static final String ATTR_ENABLED = "enabled";
    public static final String ATTR_SESSION_TIMEOUT_MS = "sessionTimeoutMS";
    public static final String ATTR_DIALOG_DISPLAY_MS = "dialogDisplayMS";
    public static final String ATTR_LOGOUT_URL_FRAGMENT = "logoutURLFragment";
    public static final String ATTR_RESET_SESSION_URL_FRAGMENT = "resetSessionURLFragment";

    /* Portlet preference attribute names */
    private static final String PREF_ENABLED = "enabled";
    private static final String PREF_DIALOG_DISPLAY_SECONDS = "dialogDisplaySeconds";
    private static final String PREF_LOGOUT_URL_FRAGMENT = "logoutURLFragment";
    private static final String PREF_RESET_SESSION_URL_FRAGMENT = "resetSessionURLFragment";


    @RenderMapping
    public String writeJsContent(RenderRequest req, RenderResponse resp, Model model) throws IOException {
        if (PortletRequest.RENDER_HEADERS.equals(req.getAttribute(PortletRequest.RENDER_PART))) {
            PortletPreferences prefs = req.getPreferences();

            boolean enabled = getEnabled(prefs);
            int timeout = req.getPortletSession().getMaxInactiveInterval();
            String logoutURL = prefs.getValue(PREF_LOGOUT_URL_FRAGMENT, DEFAULT_LOGOUT_URL_FRAGMENT);
            String resetURL = prefs.getValue(PREF_RESET_SESSION_URL_FRAGMENT, DEFAULT_RESET_SESSION_FRAGMENT);
            int dialogDisplayTime = getDialogDisplayTime(prefs, timeout);

            model.addAttribute(ATTR_ENABLED, enabled);
            model.addAttribute(ATTR_SESSION_TIMEOUT_MS, timeout * 1000);
            model.addAttribute(ATTR_DIALOG_DISPLAY_MS, dialogDisplayTime * 1000);
            model.addAttribute(ATTR_LOGOUT_URL_FRAGMENT, logoutURL);
            model.addAttribute(ATTR_RESET_SESSION_URL_FRAGMENT, resetURL);

            return HEADER_JSP;
        } else {
            return BODY_JSP;
        }
    }


    private int getDialogDisplayTime(PortletPreferences prefs, int timeout) {
        String dialogTimeStr = prefs.getValue(PREF_DIALOG_DISPLAY_SECONDS, Integer.toString(DEFAULT_DIALOG_DISPLAY_SECONDS));

        int time = DEFAULT_DIALOG_DISPLAY_SECONDS;
        try {
            time = Integer.parseInt(dialogTimeStr);
        } catch (NumberFormatException e) {
            logger.warn("Invalid dialogDisplayTime preference: {0}, using default of {1}s",
                    dialogTimeStr, DEFAULT_DIALOG_DISPLAY_SECONDS);
            return DEFAULT_DIALOG_DISPLAY_SECONDS;
        }

        if (time >= timeout) {
            logger.warn("Invalid dialogDisplayTime preference: {0}.  Time can not exceed session timeout.  Using session timeout value", dialogTimeStr);
            return timeout;
        }

        return time;
    }


    private boolean getEnabled(PortletPreferences prefs) {
        String val = prefs.getValue(PREF_ENABLED, "true");
        return "true".equalsIgnoreCase(val);
    }
}
