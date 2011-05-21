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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SessionRESTController {

    private IPersonManager personManager;
    
    @Autowired(required = true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @RequestMapping(value="/session", method = RequestMethod.GET)
    public ModelAndView isAuthenticated(HttpServletRequest request, HttpServletResponse response) {
        final ModelAndView mv = new ModelAndView();
        
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        
        else {
            final IPerson person = personManager.getPerson(request);
            final Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("userName", person.getUserName());
            attributes.put("displayName", person.getFullName());
            mv.addObject("person", attributes);
        }
        
        return mv;

    }
}
