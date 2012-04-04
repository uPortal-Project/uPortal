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

package org.jasig.portal.security.remoting;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.ISecurityContext;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPermissionsController {
    
    /**
     * Specifying the fName of a channel here pretty much violates the DRY 
     * principal;  perhaps we can think of a better way in the future.
     */
    private static final String PERMISSIONS_ADMIN_PORTLET_FNAME = "permissionsmanager";
    
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPersonManager personManager;
    private IAuthorizationService authorizationService;
    
    @Autowired
    public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    /*
     * Protected API.
     */

    protected final boolean isAuthorized(HttpServletRequest req) throws Exception {
        
        /*
         * This is sensitive data;  we must verify that the user 
         * has the appropriate level of access to see it... 
         */

        // STEP (1):  Is there an IPerson?  
        final IPerson person = personManager.getPerson((HttpServletRequest) req);
        if (person != null) {
            
            // STEP (2):  Is the person authenticated?
            final ISecurityContext securityContext = person.getSecurityContext();
            if (securityContext != null && securityContext.isAuthenticated()) {
               
                // STEP (3):  Does this user have SUBSCRIBE permission for permissionsAdminChannel?
                IAuthorizationPrincipal principal = authorizationService.newPrincipal((String) person.getAttribute(IPerson.USERNAME), IPerson.class);
                
                final IPortletDefinition permissionsAdminPortlet = portletDefinitionRegistry.getPortletDefinitionByFname(PERMISSIONS_ADMIN_PORTLET_FNAME);
                if (permissionsAdminPortlet == null) {
                    return false;
                }
                
                final String portletId = permissionsAdminPortlet.getPortletDefinitionId().getStringId();
                if (authorizationService.canPrincipalSubscribe(principal, portletId)) {
                    return true;
                }

            }
        }

        return false;

    }
}
