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

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

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
    @RenderMapping
    public String writeJsContent(RenderRequest req, RenderResponse resp, Model model) throws IOException {
        if (PortletRequest.RENDER_HEADERS.equals(req.getAttribute(PortletRequest.RENDER_PART))) {
            int timeout = req.getPortletSession().getMaxInactiveInterval();
            int buffer = timeout / 2;

            model.addAttribute("timeout", timeout);
            model.addAttribute("buffer", buffer);

            return "jsp/SessionTimeout/header";
        } else {
            return "jsp/SessionTimeout/body";
        }
    }
}
