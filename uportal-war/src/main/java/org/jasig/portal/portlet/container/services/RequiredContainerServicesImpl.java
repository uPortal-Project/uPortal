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

import org.apache.pluto.container.EventCoordinationService;
import org.apache.pluto.container.FilterManagerService;
import org.apache.pluto.container.PortletRequestContextService;
import org.apache.pluto.container.PortletURLListenerService;
import org.apache.pluto.container.driver.RequiredContainerServices;
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
	
    private PortalContext portalContext;
    private EventCoordinationService eventCoordinationService;
    private FilterManagerService filterManagerService;
    private PortletRequestContextService portletRequestContextService;
    private PortletURLListenerService portletURLListenerService;

    /* (non-Javadoc)
     * @see org.apache.pluto.RequiredContainerServices#getPortalContext()
     */
    public PortalContext getPortalContext() {
        return this.portalContext;
    }
    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.driver.RequiredContainerServices#getEventCoordinationService()
     */
	@Override
	public EventCoordinationService getEventCoordinationService() {
		return this.eventCoordinationService;
	}
	/*
	 * (non-Javadoc)
	 * @see org.apache.pluto.container.driver.RequiredContainerServices#getFilterManagerService()
	 */
	@Override
	public FilterManagerService getFilterManagerService() {
		return this.filterManagerService;
	}
	/*
	 * (non-Javadoc)
	 * @see org.apache.pluto.container.driver.RequiredContainerServices#getPortletRequestContextService()
	 */
	@Override
	public PortletRequestContextService getPortletRequestContextService() {
		return this.portletRequestContextService;
	}
	/*
	 * (non-Javadoc)
	 * @see org.apache.pluto.container.driver.RequiredContainerServices#getPortletURLListenerService()
	 */
	@Override
	public PortletURLListenerService getPortletURLListenerService() {
		return this.portletURLListenerService;
	}

	 /**
     * @param portalContext the portalContext to set
     */
    @Autowired(required=true)
    public void setPortalContext(PortalContext portalContext) {
        this.portalContext = portalContext;
    }
    /**
     * 
     * @param eventCoordinationService
     */
    @Autowired(required=true)
	public void setEventCoordinationService(
			EventCoordinationService eventCoordinationService) {
		this.eventCoordinationService = eventCoordinationService;
	}
    /**
     * 
     * @param filterManagerService
     */
    @Autowired(required=true)
	public void setFilterManagerService(FilterManagerService filterManagerService) {
		this.filterManagerService = filterManagerService;
	}
    /**
     * 
     * @param portletRequestContextService
     */
    @Autowired(required=true)
	public void setPortletRequestContextService(
			PortletRequestContextService portletRequestContextService) {
		this.portletRequestContextService = portletRequestContextService;
	}
    /**
     * 
     * @param portletURLListenerService
     */
    @Autowired(required=true)
	public void setPortletURLListenerService(
			PortletURLListenerService portletURLListenerService) {
		this.portletURLListenerService = portletURLListenerService;
	}
}
