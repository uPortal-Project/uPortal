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

import java.util.Collections;
import java.util.Map;

import javax.annotation.Resource;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.security.mvc.LoginController;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

/**
 * Helper methods for the login portlet webflow.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
@Service
public class LoginPortletHelper {

    private IUserInstanceManager userInstanceManager;
    
    @Autowired(required=true)
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        Validate.notNull(userInstanceManager);
        this.userInstanceManager = userInstanceManager;
    }

    private IPortalRequestUtils portalRequestUtils;
    
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Autowired(required=true)
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }

    private Map<String,String> mappings = Collections.<String,String>emptyMap();
    
    @Required
    @Resource(name="profileKeyMappings")
    public void setProfileMappings(Map<String,String> mappings) {
        Validate.notNull(mappings);
        this.mappings = mappings;
    }


    /**
     * Get the profile that should be pre-selected in the local login form.
     * 
     * @param request
     * @return
     */
    public String getSelectedProfile(PortletRequest request) {

        // if a profile selection exists in the session, use it
        final PortletSession session = request.getPortletSession();
        String profileName = (String) session.getAttribute(
                LoginController.REQUESTED_PROFILE_KEY,
                PortletSession.APPLICATION_SCOPE);
        
        // otherwise, set the selected profile to the one currently in use by
        // the user
        if (profileName == null) {
            // get the profile for the current request
            final HttpServletRequest httpServletRequest = portalRequestUtils.getPortletHttpRequest(request);
            final IUserInstance ui = userInstanceManager.getUserInstance(httpServletRequest);
            final IUserProfile profile = ui.getPreferencesManager().getUserProfile();
            
            // check to see if the profile's fname matches one of the entries in
            // the profile key map used by the session attribute profile mapper
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (entry.getValue().equals(profile.getProfileFname())) {
                    profileName = entry.getKey();
                    break;
                }
            }
        }


        return profileName;
    }

}
