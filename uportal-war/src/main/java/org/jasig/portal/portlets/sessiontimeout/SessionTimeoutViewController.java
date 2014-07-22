/*
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
 * @author Josh Helmer, jhelmer@unicon.net
 * @since 4.1.1
 */
@Controller
@RequestMapping("VIEW")
public class SessionTimeoutViewController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final int DEFAULT_DIALOG_TIME = 60;


    @RenderMapping
    public String writeJsContent(RenderRequest req, RenderResponse resp, Model model) throws IOException {
        if (PortletRequest.RENDER_HEADERS.equals(req.getAttribute(PortletRequest.RENDER_PART))) {
            PortletPreferences prefs = req.getPreferences();

            boolean enabled = getEnabled(prefs);
            int timeout = req.getPortletSession().getMaxInactiveInterval();
            String logoutURL = prefs.getValue("logoutURL", "/Logout");
            String resetURL = prefs.getValue("resetSessionURL", "/api/ajax-success");
            int dialogDisplayTime = getDialogDisplayTime(prefs, timeout);

            model.addAttribute("enabled", enabled);
            model.addAttribute("sessionTimeout", timeout);
            model.addAttribute("dialogDisplayTime", dialogDisplayTime);
            model.addAttribute("logoutURL", logoutURL);
            model.addAttribute("resetSessionURL", resetURL);

            return "jsp/SessionTimeout/header";
        } else {
            return "jsp/SessionTimeout/body";
        }
    }


    private int getDialogDisplayTime(PortletPreferences prefs, int timeout) {
        String dialogTimeStr = prefs.getValue("dialogDisplayTime", "60");

        int time = DEFAULT_DIALOG_TIME;
        try {
            time = Integer.parseInt(dialogTimeStr);
        } catch (NumberFormatException e) {
            logger.debug("Invalid dialogDisplayTime preference: {0}, using default of 60s", dialogTimeStr);
            return DEFAULT_DIALOG_TIME;
        }

        if (time >= timeout) {
            logger.debug("Invalid dialogDisplayTime preference: {0}.  Time can not exceed session timeout.  Using default", dialogTimeStr);
            return DEFAULT_DIALOG_TIME;
        }

        return time;
    }


    private boolean getEnabled(PortletPreferences prefs) {
        String val = prefs.getValue("enabled", "true");
        return "true".equalsIgnoreCase(val);
    }
}
