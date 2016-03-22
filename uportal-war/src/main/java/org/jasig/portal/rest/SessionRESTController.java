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
package org.jasig.portal.rest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.events.IPortalEventFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.IdentitySwapperManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SessionRESTController {

    private IPersonManager personManager;
    private IPortalEventFactory portalEventFactory;
    private IdentitySwapperManager swapperManager;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private String uPortalVersion;
    
    @Value("${org.jasig.portal.version}")
    public void setVersion(String version) {
        this.uPortalVersion = version;
    }
    
    @Autowired(required = true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    
    @Autowired(required = true)
    public void setSwapperManager(IdentitySwapperManager ism) {
      this.swapperManager = ism;
    }
    
    @Autowired
    public void setPortalEventFactory(IPortalEventFactory pef) {
        this.portalEventFactory = pef;
    }

    @RequestMapping(value="/session.json", method = RequestMethod.GET)
    public ModelAndView isAuthenticated(HttpServletRequest request, HttpServletResponse response) {
        final ModelAndView mv = new ModelAndView();
        
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        
        else {
            final IPerson person = personManager.getPerson(request);
            final String key = portalEventFactory.getPortalEventSessionId(request, person);
            final Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("userName", person.getUserName());
            attributes.put("displayName", person.isGuest() ? "Guest" : person.getFullName());
            attributes.put("sessionKey", person.isGuest() ? null: key); //only provide keys to non guest users
            attributes.put("version", uPortalVersion);
            if(swapperManager != null) {
              String originalUsername = swapperManager.getOriginalUsername(session);
              if(originalUsername != null) {
                attributes.put("originalUsername", originalUsername);
              }
            }
            
            try {
                attributes.put("serverName", InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                logger.warn("Wasn't able to get server information", e);
                attributes.put("serverName", "unknown");
            }
            mv.addObject("person", attributes);
        }

        mv.setViewName("json");
        return mv;

    }
}
