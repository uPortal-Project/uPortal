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

package org.jasig.portal.portlets.login;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.jasig.portal.security.mvc.LoginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

@Controller
@RequestMapping("VIEW")
public class LoginPortletController {
    
    @RequestMapping
    public ModelAndView getLoginForm(PortletRequest request) {

        Map<String,Object> map = new HashMap<String,Object>();
        
        PortletSession session = request.getPortletSession();
        
        String authenticationAttempted = (String) session.getAttribute(LoginController.AUTH_ATTEMPTED_KEY, PortletSession.APPLICATION_SCOPE);
        map.put("attempted", Boolean.valueOf(authenticationAttempted));
        
        String attemptedUserName = (String)session.getAttribute(LoginController.ATTEMPTED_USERNAME_KEY, PortletSession.APPLICATION_SCOPE);
        map.put("attemtpedUsername", attemptedUserName);

        return new ModelAndView("/jsp/Login/login", map);
    }

}
