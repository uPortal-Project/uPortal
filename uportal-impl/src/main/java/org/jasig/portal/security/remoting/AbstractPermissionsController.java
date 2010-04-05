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
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.provider.AuthorizationImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

public abstract class AbstractPermissionsController {
    
    /**
     * Specifying the fName of a channel here pretty much violates the DRY 
     * principal;  perhaps we can think of a better way in the future.
     */
    private static final String PERMISSIONS_ADMIN_PORTLET_FNAME = "permissionsmanager";
    private static final ModelAndView NOT_AUTHORIZED_RESPONSE = new ModelAndView("jsonView", "ERROR", "Not Authorized");
    
    private IChannelRegistryStore channelRegistryStore;
    private IChannelDefinition permissionsAdminChannel;
    private IPersonManager personManager;
    
    /*
     * Public API.
     */

    public static final String OWNER_PARAMETER = "owner";
    public static final String PRINCIPAL_PARAMETER = "principal";
    public static final String ACTIVITY_PARAMETER = "activity";
    public static final String TARGET_PARAMETER = "target";

    @Autowired(required=true)
    public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
        this.channelRegistryStore = channelRegistryStore;
    }

    @Autowired(required=true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    /*
     * Protected API.
     */

    @RequestMapping(method = RequestMethod.GET)
    protected final ModelAndView invoke(HttpServletRequest req, HttpServletResponse res) throws Exception {
        
        ModelAndView rslt = NOT_AUTHORIZED_RESPONSE;  // default...
        
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
                IAuthorizationService authServ = AuthorizationImpl.singleton();
                IAuthorizationPrincipal principal = authServ.newPrincipal((String) person.getAttribute(IPerson.USERNAME), IPerson.class);
                IChannelDefinition chnl = getAdminChannel();
                if (authServ.canPrincipalSubscribe(principal, chnl.getId())) {

                    // The user is authorized to perform the requested action...
                    rslt = invokeSensative(req, res);
                    
                }

            }
        }

        return rslt;

    }

    protected abstract ModelAndView invokeSensative(HttpServletRequest req, HttpServletResponse res) throws Exception;

    /*
     * Private Stuff.
     */
    
    private final IChannelDefinition getAdminChannel() {
        if (permissionsAdminChannel == null) {
            permissionsAdminChannel = channelRegistryStore.getChannelDefinition(PERMISSIONS_ADMIN_PORTLET_FNAME);
        }
        return permissionsAdminChannel;
    }

}
