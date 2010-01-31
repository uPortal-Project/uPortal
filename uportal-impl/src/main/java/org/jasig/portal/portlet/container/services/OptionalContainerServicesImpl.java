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

package org.jasig.portal.portlet.container.services;

import org.apache.pluto.core.DefaultOptionalContainerServices;
import org.apache.pluto.core.DefaultPortalAdministrationService;
import org.apache.pluto.spi.optional.PortalAdministrationService;
import org.apache.pluto.spi.optional.PortletEnvironmentService;
import org.apache.pluto.spi.optional.PortletPreferencesService;
import org.apache.pluto.spi.optional.RequestAttributeService;
import org.apache.pluto.spi.optional.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("optionalContainerServices")
public class OptionalContainerServicesImpl extends DefaultOptionalContainerServices {
    private UserInfoService userInfoService;
    private PortalAdministrationService portalAdministrationService = new DefaultPortalAdministrationService();
    private PortletPreferencesService portletPreferencesService;
    private PortletEnvironmentService portletEnvironmentService;
    private RequestAttributeService requestAttributeService;
    
    /**
     * @param userInfoService the userInfoService to set
     */
    @Autowired(required=true)
    public void setUserInfoService(@Qualifier("main") UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }
    
    /**
     * @param portalAdministrationService the portalAdministrationService to set
     */
    @Autowired(required=true)
    public void setPortalAdministrationService(PortalAdministrationService portalAdministrationService) {
        this.portalAdministrationService = portalAdministrationService;
    }
    
    /**
     * @param portletPreferencesService the portletPreferencesService to set
     */
    @Autowired(required=true)
    public void setPortletPreferencesService(PortletPreferencesService portletPreferencesService) {
        this.portletPreferencesService = portletPreferencesService;
    }

    /**
     * @param portletEnvironmentService the portletEnvironmentService to set
     */
    @Autowired(required=true)
    public void setPortletEnvironmentService(PortletEnvironmentService portletEnvironmentService) {
        this.portletEnvironmentService = portletEnvironmentService;
    }
    
    /**
     * @param requestAttributeService the requestAttributeService to set
     */
    @Autowired(required=true)
    public void setRequestAttributeService(RequestAttributeService requestAttributeService) {
        this.requestAttributeService = requestAttributeService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.core.DefaultOptionalContainerServices#getPortalAdministrationService()
     */
    @Override
    public PortalAdministrationService getPortalAdministrationService() {
        return this.portalAdministrationService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.core.DefaultOptionalContainerServices#getPortletPreferencesService()
     */
    @Override
    public PortletPreferencesService getPortletPreferencesService() {
        return this.portletPreferencesService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.core.DefaultOptionalContainerServices#getUserInfoService()
     */
    @Override
    public UserInfoService getUserInfoService() {
        return this.userInfoService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.core.DefaultOptionalContainerServices#getPortletEnvironmentService()
     */
    @Override
    public PortletEnvironmentService getPortletEnvironmentService() {
        return this.portletEnvironmentService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.core.DefaultOptionalContainerServices#getRequestAttributeService()
     */
    @Override
    public RequestAttributeService getRequestAttributeService() {
        return this.requestAttributeService;
    }

}
