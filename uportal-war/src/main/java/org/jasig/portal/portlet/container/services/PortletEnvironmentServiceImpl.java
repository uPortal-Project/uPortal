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
import javax.portlet.ActionResponse;
import javax.portlet.Event;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.pluto.container.PortletActionResponseContext;
import org.apache.pluto.container.PortletEnvironmentService;
import org.apache.pluto.container.PortletEventResponseContext;
import org.apache.pluto.container.PortletMimeResponseContext;
import org.apache.pluto.container.PortletRenderResponseContext;
import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.PortletResourceRequestContext;
import org.apache.pluto.container.PortletResourceResponseContext;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.impl.ActionRequestImpl;
import org.apache.pluto.container.impl.ActionResponseImpl;
import org.apache.pluto.container.impl.EventRequestImpl;
import org.apache.pluto.container.impl.EventResponseImpl;
import org.apache.pluto.container.impl.RenderRequestImpl;
import org.apache.pluto.container.impl.RenderResponseImpl;
import org.apache.pluto.container.impl.ResourceRequestImpl;
import org.apache.pluto.container.impl.ResourceResponseImpl;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.session.ScopingPortletSessionImpl;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides custom portlet session instance to use a different scoping attribute value
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portletEnvironmentService")
public class PortletEnvironmentServiceImpl implements PortletEnvironmentService {
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
    
    @Override
	public PortletSession createPortletSession(PortletContext portletContext, PortletWindow portletWindow, HttpSession session) {
		// TODO pluto 1.1 PortletEnvironmentService#createPortletSession passed in the request; now use IPortalRequestUtils#getCurrentPortalRequest()?
		final HttpServletRequest request = portalRequestUtils.getCurrentPortalRequest();
		final IPortletWindow internalPortletWindow = this.portletWindowRegistry.convertPortletWindow(request, portletWindow);
		final IPortletEntity portletEntity = internalPortletWindow.getPortletEntity();
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        
		return new ScopingPortletSessionImpl(portletEntityId, portletContext, portletWindow, session);
	}

    @Override
    public ActionRequest createActionRequest(PortletRequestContext requestContext,
            PortletActionResponseContext responseContext) {
        
        return new ExtendedActionRequestImpl(portletPreferencesFactory, requestContext, responseContext);
    }
    
    @Override
    public EventRequest createEventRequest(PortletRequestContext requestContext,
            PortletEventResponseContext responseContext, Event event) {
        
        return new ExtendedEventRequestImpl(portletPreferencesFactory, requestContext, responseContext, event);
    }
    
    @Override
    public RenderRequest createRenderRequest(PortletRequestContext requestContext,
            PortletRenderResponseContext responseContext) {
        
        return new ExtendedRenderRequestImpl(portletPreferencesFactory, requestContext, responseContext);
    }
    
    @Override
    public ResourceRequest createResourceRequest(PortletResourceRequestContext requestContext,
            PortletResourceResponseContext responseContext) {
        
        return new ExtendedResourceRequestImpl(portletPreferencesFactory, requestContext, responseContext);
    }
    
    
    @Override
    public ActionResponse createActionResponse(PortletActionResponseContext responseContext) {
        return new ActionResponseImpl(responseContext);
    }
    
    @Override
    public EventResponse createEventResponse(PortletEventResponseContext responseContext) {
        return new EventResponseImpl(responseContext);
    }
    
    @Override
    public RenderResponse createRenderResponse(PortletRenderResponseContext responseContext) {
        return new ExtendedRenderResponseImpl(responseContext);
    }
    
    @Override
    public ResourceResponse createResourceResponse(PortletResourceResponseContext responseContext, String requestCacheLevel) {
        return new ExtendedResourceResponseImpl(responseContext, requestCacheLevel);
    }

    private static final class ExtendedActionRequestImpl extends ActionRequestImpl {
        private final PortletPreferencesFactory portletPreferencesFactory;
        private PortletPreferences portletPreferences;

        private ExtendedActionRequestImpl(PortletPreferencesFactory portletPreferencesFactory,
                PortletRequestContext requestContext,
                PortletActionResponseContext responseContext) {
            
            super(requestContext, responseContext);
            this.portletPreferencesFactory = portletPreferencesFactory;
        }

        @Override
        public PortletPreferences getPreferences() {
            if (this.portletPreferences == null) {
                final PortletRequestContext requestContext = this.getRequestContext();
                this.portletPreferences = portletPreferencesFactory.createPortletPreferences(requestContext, false);
            }
            return this.portletPreferences;
        }
    }

    private static final class ExtendedEventRequestImpl extends EventRequestImpl {
        private final PortletPreferencesFactory portletPreferencesFactory;
        private PortletPreferences portletPreferences;

        private ExtendedEventRequestImpl(PortletPreferencesFactory portletPreferencesFactory,
                PortletRequestContext requestContext,
                PortletEventResponseContext responseContext, 
                Event event) {
            super(requestContext, responseContext, event);
            this.portletPreferencesFactory = portletPreferencesFactory;
        }

        @Override
        public PortletPreferences getPreferences() {
            if (this.portletPreferences == null) {
                final PortletRequestContext requestContext = this.getRequestContext();
                this.portletPreferences = portletPreferencesFactory.createPortletPreferences(requestContext, false);
            }
            return this.portletPreferences;
        }
    }

    private static final class ExtendedRenderRequestImpl extends RenderRequestImpl {
        private final PortletPreferencesFactory portletPreferencesFactory;
        private PortletPreferences portletPreferences;

        private ExtendedRenderRequestImpl(PortletPreferencesFactory portletPreferencesFactory,
                PortletRequestContext requestContext,
                PortletRenderResponseContext responseContext) {
            super(requestContext, responseContext);
            this.portletPreferencesFactory = portletPreferencesFactory;
        }

        @Override
        public PortletPreferences getPreferences() {
            if (this.portletPreferences == null) {
                final PortletRequestContext requestContext = this.getRequestContext();
                this.portletPreferences = portletPreferencesFactory.createPortletPreferences(requestContext, true);
            }
            return this.portletPreferences;
        }
    }

    private static final class ExtendedResourceRequestImpl extends ResourceRequestImpl {
        private final PortletPreferencesFactory portletPreferencesFactory;
        private PortletPreferences portletPreferences;

        private ExtendedResourceRequestImpl(PortletPreferencesFactory portletPreferencesFactory,
                PortletResourceRequestContext requestContext,
                PortletResourceResponseContext responseContext) {
            super(requestContext, responseContext);
            this.portletPreferencesFactory = portletPreferencesFactory;
        }

        @Override
        public PortletPreferences getPreferences() {
            if (this.portletPreferences == null) {
                final PortletRequestContext requestContext = this.getRequestContext();
                this.portletPreferences = portletPreferencesFactory.createPortletPreferences(requestContext, false);
            }
            return this.portletPreferences;
        }
    }

    private static final class ExtendedRenderResponseImpl extends RenderResponseImpl {
        private ExtendedRenderResponseImpl(PortletRenderResponseContext responseContext) {
            super(responseContext);
        }

        @Override
        public String getContentType() {
            return ((PortletMimeResponseContext)this.getResponseContext()).getContentType();
        }
    }

    private static final class ExtendedResourceResponseImpl extends ResourceResponseImpl {
        private ExtendedResourceResponseImpl(PortletResourceResponseContext responseContext, String requestCacheLevel) {
            super(responseContext, requestCacheLevel);
        }

        @Override
        public String getContentType() {
            return ((PortletMimeResponseContext)this.getResponseContext()).getContentType();
        }
    }
}
