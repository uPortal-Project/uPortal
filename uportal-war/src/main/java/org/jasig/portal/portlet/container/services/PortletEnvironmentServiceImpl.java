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

import javax.portlet.ActionRequest;
import javax.portlet.Event;
import javax.portlet.EventRequest;
import javax.portlet.PortletContext;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.filter.ActionRequestWrapper;
import javax.portlet.filter.EventRequestWrapper;
import javax.portlet.filter.RenderRequestWrapper;
import javax.portlet.filter.ResourceRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.pluto.container.PortletActionResponseContext;
import org.apache.pluto.container.PortletEventResponseContext;
import org.apache.pluto.container.PortletRenderResponseContext;
import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.PortletResourceRequestContext;
import org.apache.pluto.container.PortletResourceResponseContext;
import org.apache.pluto.container.PortletWindow;
import org.jasig.portal.api.permissions.ApiPermissionsService;
import org.jasig.portal.api.permissions.PermissionsService;
import org.jasig.portal.api.sso.SsoTicketService;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.session.ScopingPortletSessionImpl;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides custom portlet session instance to use a different scoping attribute value
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portletEnvironmentService")
public class PortletEnvironmentServiceImpl extends org.apache.pluto.container.impl.PortletEnvironmentServiceImpl
implements InitializingBean, DisposableBean {
    private PortletPreferencesFactory portletPreferencesFactory;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    public void setPortletPreferencesFactory(PortletPreferencesFactory portletPreferencesFactory) {
        this.portletPreferencesFactory = portletPreferencesFactory;
    }
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    @Autowired
	public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
		this.portalRequestUtils = portalRequestUtils;
	}
    
    @Autowired
    private PermissionsService apiPermissionsService;

	@Autowired
	private SsoTicketService ssoTicketService;


	@Override
	public void afterPropertiesSet() {
		// Allows access to the PermissionsService impl to non-Portlet requests
		PermissionsService.IMPL.set(apiPermissionsService);
		SsoTicketService.IMPL.set(ssoTicketService);
	}

	@Override
	public void destroy() {
		PermissionsService.IMPL.set(null);
		SsoTicketService.IMPL.set(null);
	}
    
    @Override
	public PortletSession createPortletSession(PortletContext portletContext, PortletWindow portletWindow, HttpSession session) {
		// TODO pluto 1.1 PortletEnvironmentService#createPortletSession passed in the request; now use IPortalRequestUtils#getCurrentPortalRequest()?
		final HttpServletRequest request = portalRequestUtils.getCurrentPortalRequest();
		final IPortletWindow internalPortletWindow = this.portletWindowRegistry.convertPortletWindow(request, portletWindow);
		final IPortletEntity portletEntity = internalPortletWindow.getPortletEntity();
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        
        portletContext.setAttribute(PermissionsService.PORTLET_CONTEXT_ATTRIBUTE_NAME, apiPermissionsService);
        portletContext.setAttribute(SsoTicketService.PORTLET_CONTEXT_ATTRIBUTE_NAME, ssoTicketService);

		return new ScopingPortletSessionImpl(portletEntityId, portletContext, portletWindow, session);
	}

    @Override
    public ActionRequest createActionRequest(final PortletRequestContext requestContext,
            PortletActionResponseContext responseContext) {
        
        final ActionRequest actionRequest = super.createActionRequest(requestContext, responseContext);
        
        return new ActionRequestWrapper(actionRequest) {
            private PortletPreferences portletPreferences;
            
            @Override
            public PortletPreferences getPreferences() {
                if (this.portletPreferences == null) {
                    this.portletPreferences = portletPreferencesFactory.createPortletPreferences(requestContext, false);
                }
                return this.portletPreferences;
            }
        };
    }
    
    @Override
    public EventRequest createEventRequest(final PortletRequestContext requestContext,
            PortletEventResponseContext responseContext, Event event) {

        final EventRequest eventRequest = super.createEventRequest(requestContext, responseContext, event);
        
        return new EventRequestWrapper(eventRequest) {
            private PortletPreferences portletPreferences;
            
            @Override
            public PortletPreferences getPreferences() {
                if (this.portletPreferences == null) {
                    this.portletPreferences = portletPreferencesFactory.createPortletPreferences(requestContext, false);
                }
                return this.portletPreferences;
            }
        };
    }
    
    @Override
    public RenderRequest createRenderRequest(final PortletRequestContext requestContext,
            PortletRenderResponseContext responseContext) {
        
        final RenderRequest renderRequest = super.createRenderRequest(requestContext, responseContext);
        
        return new RenderRequestWrapper(renderRequest) {
            private PortletPreferences portletPreferences;
            
            @Override
            public PortletPreferences getPreferences() {
                if (this.portletPreferences == null) {
                    this.portletPreferences = portletPreferencesFactory.createPortletPreferences(requestContext, true);
                }
                return this.portletPreferences;
            }
        };
    }
    
    @Override
    public ResourceRequest createResourceRequest(final PortletResourceRequestContext requestContext,
            PortletResourceResponseContext responseContext) {
        
        final ResourceRequest resourceRequest = super.createResourceRequest(requestContext, responseContext);
        
        return new ResourceRequestWrapper(resourceRequest) {
            private PortletPreferences portletPreferences;
            
            @Override
            public PortletPreferences getPreferences() {
                if (this.portletPreferences == null) {
                    this.portletPreferences = portletPreferencesFactory.createPortletPreferences(requestContext, false);
                }
                return this.portletPreferences;
            }
        };
    }
}
