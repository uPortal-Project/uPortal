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

package org.jasig.portal.portlet.delegation;

import java.io.IOException;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.api.portlet.DelegateState;
import org.jasig.portal.api.portlet.DelegationActionResponse;
import org.jasig.portal.api.portlet.DelegationRequest;
import org.jasig.portal.api.portlet.DelegationResponse;
import org.jasig.portal.api.portlet.PortletDelegationDispatcher;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.portlet.rendering.PortletOutputHandler;
import org.jasig.portal.portlet.rendering.PortletResourceOutputHandler;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalActionUrlBuilder;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;

/**
 * Implementation of delegation dispatcher
 * 
 * @author Eric Dalquist
 */
public class PortletDelegationDispatcherImpl implements PortletDelegationDispatcher {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final IPortletWindow portletWindow;
    private final int userId;
    
    private final IPortalRequestUtils portalRequestUtils;
    private final IPersonManager personManager;
    private final IPortletRenderer portletRenderer;
    private final IPortalUrlProvider portalUrlProvider;
    private final IPortletDelegationManager portletDelegationManager;
    

    public PortletDelegationDispatcherImpl(IPortletWindow portletWindow, int userId,
            IPortalRequestUtils portalRequestUtils, IPersonManager personManager, IPortletRenderer portletRenderer,
            IPortalUrlProvider portalUrlProvider, IPortletDelegationManager portletDelegationManager) {
        
        Validate.notNull(portletWindow, "portletWindow can not be null");
        Validate.notNull(portalRequestUtils, "portalRequestUtils can not be null");
        Validate.notNull(personManager, "personManager can not be null");
        Validate.notNull(portletRenderer, "portletRenderer can not be null");
        Validate.notNull(portalUrlProvider, "portalUrlProvider can not be null");
        Validate.notNull(portletDelegationManager, "portletDelegationManager can not be null");
        
        this.portletWindow = portletWindow;
        this.userId = userId;
        this.portalRequestUtils = portalRequestUtils;
        this.personManager = personManager;
        this.portletRenderer = portletRenderer;
        this.portalUrlProvider = portalUrlProvider;
        this.portletDelegationManager = portletDelegationManager;
    }

    @Override
    public DelegationActionResponse doAction(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException {
        return this.doAction(actionRequest, actionResponse, null);
    }
    

    @Override
    public DelegationActionResponse doAction(ActionRequest actionRequest, ActionResponse actionResponse, DelegationRequest delegationRequest) throws IOException {
        final HttpServletRequest request = this.portalRequestUtils.getPortletHttpRequest(actionRequest);
        final HttpServletResponse response = this.portalRequestUtils.getOriginalPortalResponse(actionRequest);

        //Sanity check that the dispatch is being called by the same user it was created for
        final IPerson person = this.personManager.getPerson(request);
        if (this.userId != person.getID()) {
            throw new IllegalStateException("This dispatcher was created for userId " + this.userId + " but is being executed for userId " + person.getID());
        }
        
        this.setupDelegateRequestInfo(request, delegationRequest);
        
        final IPortletWindowId portletWindowId = this.portletWindow.getPortletWindowId();
        try {
            //TODO canRender permission checks!
            this.portletRenderer.doAction(portletWindowId, request, response);
        }
        catch (RuntimeException e) {
            this.logger.error("Failed to execute action on delegate", e);
            throw e;
        }
        
        //Get the portal URL builders for this request and check if a redirect was sent
        final IPortalActionUrlBuilder portalActionUrlBuilder = this.portalUrlProvider.getPortalActionUrlBuilder(request);
        final String redirectLocation = portalActionUrlBuilder.getRedirectLocation();
        if (redirectLocation != null) {
            final String renderUrlParamName = portalActionUrlBuilder.getRenderUrlParamName();
            
            //clear out the redirect from the delegate, leave it up to the parent if the redirect should happen
            portalActionUrlBuilder.setRedirectLocation(null, null);
            
            return new DelegationActionResponse(this.getDelegateState(), redirectLocation, renderUrlParamName);
        }
        
        
        //No redirect so get the portlet's url builder and copy the state-changing data into the delegate response 
        final IPortletUrlBuilder portletUrlBuilder = portalActionUrlBuilder.getPortletUrlBuilder(portletWindowId);
        
        final WindowState windowState = portletUrlBuilder.getWindowState();
        final PortletMode portletMode = portletUrlBuilder.getPortletMode();
        final Map<String, String[]> parameters = portletUrlBuilder.getParameters();
        
        return new DelegationActionResponse(this.getDelegateState(), portletMode, windowState, parameters);
    }
    
    @Override
    public DelegationResponse doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException {
        return this.doServeResource(resourceRequest, resourceResponse, null, new ResourceResponsePortletOutputHandler(resourceResponse));
    }

    @Override
    public DelegationResponse doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse,
            DelegationRequest delegationRequest) throws IOException {
        return this.doServeResource(resourceRequest,
                resourceResponse,
                delegationRequest,
                new ResourceResponsePortletOutputHandler(resourceResponse));
    }

    @Override
    public DelegationResponse doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse, DelegationRequest delegationRequest, PortletResourceOutputHandler portletOutputHandler) throws IOException {
        final HttpServletRequest request = this.portalRequestUtils.getPortletHttpRequest(resourceRequest);
        final HttpServletResponse response = this.portalRequestUtils.getOriginalPortalResponse(resourceRequest);

        //Sanity check that the dispatch is being called by the same user it was created for
        final IPerson person = this.personManager.getPerson(request);
        if (this.userId != person.getID()) {
            throw new IllegalStateException("This dispatcher was created for userId " + this.userId + " but is being executed for userId " + person.getID());
        }

        this.setupDelegateRequestInfo(request, delegationRequest);
        
        try {
            //TODO canRender permission checks!
            this.portletRenderer.doServeResource(this.portletWindow.getPortletWindowId(), request, response, portletOutputHandler);
        }
        catch (RuntimeException e) {
            this.logger.error("Failed to render delegate", e);
            throw e;
        }
        
        return new DelegationResponse(this.getDelegateState());
    }

    @Override
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException {
        return this.doRender(renderRequest, renderResponse, null, new RenderResponsePortletOutputHandler(renderResponse));
    }

    @Override
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse, PortletOutputHandler portletOutputHandler) throws IOException {
        return this.doRender(renderRequest, renderResponse, null, portletOutputHandler);
    }
    
    @Override
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse, DelegationRequest delegationRequest) throws IOException {
        return this.doRender(renderRequest, renderResponse, delegationRequest, new RenderResponsePortletOutputHandler(renderResponse));
    }

    @Override
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse,
            DelegationRequest delegationRequest, PortletOutputHandler portletOutputHandler) throws IOException {
        
        final HttpServletRequest request = this.portalRequestUtils.getPortletHttpRequest(renderRequest);
        final HttpServletResponse response = this.portalRequestUtils.getOriginalPortalResponse(renderRequest);

        //Sanity check that the dispatch is being called by the same user it was created for
        final IPerson person = this.personManager.getPerson(request);
        if (this.userId != person.getID()) {
            throw new IllegalStateException("This dispatcher was created for userId " + this.userId + " but is being executed for userId " + person.getID());
        }

        this.setupDelegateRequestInfo(request, delegationRequest);
        
        try {
            //TODO canRender permission checks!
            this.portletRenderer.doRenderMarkup(this.portletWindow.getPortletWindowId(), request, response, portletOutputHandler);
        }
        catch (RuntimeException e) {
            this.logger.error("Failed to render delegate", e);
            throw e;
        }
        finally {
            portletOutputHandler.flushBuffer();
        }
        
        return new DelegationResponse(this.getDelegateState());
    }

    @Override
    public DelegateState getDelegateState() {
        return new DelegateState(this.portletWindow.getPortletMode(), this.portletWindow.getWindowState());
    }

    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindow.getPortletWindowId();
    }

    protected void setupDelegateRequestInfo(HttpServletRequest request, DelegationRequest delegationRequest) {
        if (delegationRequest == null) {
            return;
        }
        
        final DelegateState delegateState = delegationRequest.getDelegateState();
        if (delegateState != null) {
            final PortletMode portletMode = delegateState.getPortletMode();
            if (portletMode != null) {
                this.portletWindow.setPortletMode(portletMode);
            }
            
            final WindowState windowState = delegateState.getWindowState();
            if (windowState != null) {
                this.portletWindow.setWindowState(windowState);
            }
        }
        
        final IPortletWindowId portletWindowId = this.portletWindow.getPortletWindowId();
        
        //Store the DelegationRequest so it can be accessed elsewhere
        this.portletDelegationManager.setDelegationRequest(request, portletWindowId, delegationRequest);
    }
}
