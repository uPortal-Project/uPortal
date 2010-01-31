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

import javax.portlet.PortalContext;

import org.apache.commons.lang.Validate;
import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.spi.PortalCallbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Required service locator bean which is provided to Pluto for access to the
 * callbacks needed to render portlets.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("requiredContainerServices")
public class RequiredContainerServicesImpl implements RequiredContainerServices {
    private PortalCallbackService portalCallbackService;
    private PortalContext portalContext;
    
    /**
     * @param portalCallbackService the portalCallbackService to set
     */
    @Autowired
    public void setPortalCallbackService(PortalCallbackService portalCallbackService) {
        Validate.notNull(portalCallbackService, "portalCallbackService can not be null");
        this.portalCallbackService = portalCallbackService;
    }

    /**
     * @param portalContext the portalContext to set
     */
    @Autowired
    public void setPortalContext(PortalContext portalContext) {
        Validate.notNull(portalContext, "portalContext can not be null");
        this.portalContext = portalContext;
    }

    
    /* (non-Javadoc)
     * @see org.apache.pluto.RequiredContainerServices#getPortalCallbackService()
     */
    public PortalCallbackService getPortalCallbackService() {
        return this.portalCallbackService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.RequiredContainerServices#getPortalContext()
     */
    public PortalContext getPortalContext() {
        return this.portalContext;
    }

}
