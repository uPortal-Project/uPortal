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

import org.apache.pluto.container.PortletEnvironmentService;
import org.apache.pluto.container.PortletPreferencesService;
import org.apache.pluto.container.UserInfoService;
import org.apache.pluto.container.driver.PortalAdministrationService;
import org.apache.pluto.driver.container.DefaultOptionalContainerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Basic subclass of {@link DefaultOptionalContainerServices}, annotated
 * for participation in auto-wiring.
 * 
 * Implementations {@link UserInfoService}, {@link PortalAdministrationService}
 * and {@link PortletPreferencesService} beans are required.
 * If no {@link PortletEnvironmentService} is defined, defaults to {@link PortletEnvironmentServiceImpl}.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("optionalContainerServices")
public class OptionalContainerServicesImpl extends DefaultOptionalContainerServices {
    private UserInfoService userInfoService;
    private PortalAdministrationService portalAdministrationService;
    private PortletPreferencesService portletPreferencesService;
    private PortletEnvironmentService portletEnvironmentService = new PortletEnvironmentServiceImpl();
    
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
    @Autowired(required=false)
    public void setPortletEnvironmentService(PortletEnvironmentService portletEnvironmentService) {
        this.portletEnvironmentService = portletEnvironmentService;
    }

	/*
     * (non-Javadoc)
     * @see org.apache.pluto.driver.container.DefaultOptionalContainerServices#getPortalAdministrationService()
     */
    @Override
    public PortalAdministrationService getPortalAdministrationService() {
        return this.portalAdministrationService;
    }

   /*
    * (non-Javadoc)
    * @see org.apache.pluto.driver.container.DefaultOptionalContainerServices#getPortletPreferencesService()
    */
    @Override
    public PortletPreferencesService getPortletPreferencesService() {
        return this.portletPreferencesService;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.driver.container.DefaultOptionalContainerServices#getUserInfoService()
     */
    @Override
    public UserInfoService getUserInfoService() {
        return this.userInfoService;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.driver.container.DefaultOptionalContainerServices#getPortletEnvironmentService()
     */
    @Override
    public PortletEnvironmentService getPortletEnvironmentService() {
        return this.portletEnvironmentService;
    }
}
