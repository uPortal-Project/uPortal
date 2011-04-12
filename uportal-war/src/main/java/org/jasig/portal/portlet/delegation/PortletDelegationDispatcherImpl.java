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
import java.io.Writer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
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
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletDelegationDispatcherImpl implements PortletDelegationDispatcher {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final IPortletWindow portletWindow;
    private final IPortletWindow parentPortletWindow;
    private final int userId;
    
    private final IPortalRequestUtils portalRequestUtils;
    private final IPersonManager personManager;
    private final IPortletRenderer portletRenderer;
    private final IPortalUrlProvider portalUrlProvider;
    private final IPortletDelegationManager portletDelegationManager;
    

    public PortletDelegationDispatcherImpl(IPortletWindow portletWindow, IPortletWindow parentPortletWindow, int userId,
            IPortalRequestUtils portalRequestUtils, IPersonManager personManager, IPortletRenderer portletRenderer,
            IPortalUrlProvider portalUrlProvider, IPortletDelegationManager portletDelegationManager) {
        
        Validate.notNull(portletWindow, "portletWindow can not be null");
        Validate.notNull(parentPortletWindow, "parentPortletWindow can not be null");
        Validate.notNull(portalRequestUtils, "portalRequestUtils can not be null");
        Validate.notNull(personManager, "personManager can not be null");
        Validate.notNull(portletRenderer, "portletRenderer can not be null");
        Validate.notNull(portalUrlProvider, "portalUrlProvider can not be null");
        Validate.notNull(portletDelegationManager, "portletDelegationManager can not be null");
        
        this.portletWindow = portletWindow;
        this.parentPortletWindow = parentPortletWindow;
        this.userId = userId;
        this.portalRequestUtils = portalRequestUtils;
        this.personManager = personManager;
        this.portletRenderer = portletRenderer;
        this.portalUrlProvider = portalUrlProvider;
        this.portletDelegationManager = portletDelegationManager;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#doAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    @Override
    public DelegationActionResponse doAction(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException {
        return this.doAction(actionRequest, actionResponse, null);
    }
    

    @Override
    public DelegationActionResponse doAction(ActionRequest actionRequest, ActionResponse actionResponse, DelegationRequest delegationRequest) throws IOException {
        final HttpServletRequest request = this.portalRequestUtils.getOriginalPortalRequest(actionRequest);
        final HttpServletResponse response = this.portalRequestUtils.getOriginalPortalResponse(actionRequest);

        //Sanity check that the dispatch is being called by the same user it was created for
        final IPerson person = this.personManager.getPerson(request);
        if (this.userId != person.getID()) {
            throw new IllegalStateException("This dispatcher was created for userId " + this.userId + " but is being executed for userId " + person.getID());
        }
        
        this.setupDelegateRequestInfo(request, delegationRequest);
        
        final RedirectCapturingResponse capturingResponse = new RedirectCapturingResponse(response);
        
        //final ContainerInvocation invocation = ContainerInvocation.getInvocation();
        try {
            
            //TODO canRender permission checks!
            
            this.portletRenderer.doAction(this.portletWindow.getPortletWindowId(), request, capturingResponse);
        }
        catch (RuntimeException e) {
            this.logger.error("Failed to execute action on delegate", e);
            throw e;
        }
        /*finally {
        	
            if (invocation != null) {
                ContainerInvocation.setInvocation(invocation.getPortletContainer(), invocation.getPortletWindow());
            }
            
        } */
        
        final String redirectLocation = capturingResponse.getRedirectLocation();
        
        //If the delegate portlet sent a redirect use the parent action response to send it
        if (!IPortletDelegationManager.DELEGATE_ACTION_REDIRECT_TOKEN.equals(redirectLocation)) {
            actionResponse.sendRedirect(redirectLocation);
            return new DelegationActionResponse(this.getDelegateState(), redirectLocation);
        }
        
        final IPortalUrlBuilder portletUrl = this.portletDelegationManager.getDelegatePortletActionRedirectUrl(actionRequest);
        return new DelegationActionResponse(this.getDelegateState(), portletUrl);
    }

    @Override
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException {
        return this.doRender(renderRequest, renderResponse, null, renderResponse.getWriter());
    }

    @Override
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse, Writer writer) throws IOException {
        return this.doRender(renderRequest, renderResponse, null, writer);
    }
    
    @Override
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse, DelegationRequest delegationRequest) throws IOException {
        return this.doRender(renderRequest, renderResponse, delegationRequest, renderResponse.getWriter());
    }

    @Override
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse, DelegationRequest delegationRequest, Writer writer) throws IOException {
        final HttpServletRequest request = this.portalRequestUtils.getOriginalPortalRequest(renderRequest);
        final HttpServletResponse response = this.portalRequestUtils.getOriginalPortalResponse(renderRequest);

        //Sanity check that the dispatch is being called by the same user it was created for
        final IPerson person = this.personManager.getPerson(request);
        if (this.userId != person.getID()) {
            throw new IllegalStateException("This dispatcher was created for userId " + this.userId + " but is being executed for userId " + person.getID());
        }

        this.setupDelegateRequestInfo(request, delegationRequest);
        
        //final ContainerInvocation invocation = ContainerInvocation.getInvocation();
        try {
            
            //TODO canRender permission checks!
            this.portletRenderer.doRenderMarkup(this.portletWindow.getPortletWindowId(), request, response, writer);
        }
        catch (RuntimeException e) {
            this.logger.error("Failed to render delegate", e);
            throw e;
        }
        finally {
        	/*
            if (invocation != null) {
                ContainerInvocation.setInvocation(invocation.getPortletContainer(), invocation.getPortletWindow());
            }
            */
            writer.flush();
        }
        
        return new DelegationResponse(this.getDelegateState());
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#getDelegateState()
     */
    @Override
    public DelegateState getDelegateState() {
        return new DelegateState(this.portletWindow.getPortletMode(), this.portletWindow.getWindowState());
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#getPortletWindowId()
     */
    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindow.getPortletWindowId();
    }

    protected void setupDelegateRequestInfo(HttpServletRequest request, DelegationRequest delegationRequest) {
        if (delegationRequest == null) {
            return;
        }
        
        //Get or create the parent portlet URL
        final IPortletWindowId parentPortletWindowId = this.parentPortletWindow.getPortletWindowId();
//        final IPortletPortalUrl parentPortletUrl = new PortletUrl(parentPortletWindowId);
//        this.portletDelegationManager.setParentPortletUrl(request, parentPortletUrl);
//        
//        final DelegateState delegateState = delegationRequest.getDelegateState();
//        if (delegateState != null) {
//            final IPortletWindowId portletWindowId = this.portletWindow.getPortletWindowId();
//            
//            //Get or create the delegate portlet URL
//            IPortletPortalUrl delegatePortletUrl = this.portletRequestParameterManager.getPortletRequestInfo(request, portletWindowId);
//            if (delegatePortletUrl == null) {
//                delegatePortletUrl = new PortletUrl(portletWindowId);
//                this.portletRequestParameterManager.setAdditionalPortletUrl(request, delegatePortletUrl);
//            }
//            parentPortletUrl.setDelegatePortletUrl(delegatePortletUrl);
//
//            final PortletMode mode = delegateState.getPortletMode();
//            delegatePortletUrl.setPortletMode(mode);
//
//            final WindowState state = delegateState.getWindowState();
//            delegatePortletUrl.setWindowState(state);
//        }
//        
//        final WindowState parentWindowState = delegationRequest.getParentWindowState();
//        if (parentWindowState != null) {
//            parentPortletUrl.setWindowState(parentWindowState);
//        }
//        final PortletMode parentPortletMode = delegationRequest.getParentPortletMode();
//        if (parentPortletMode != null) {
//            parentPortletUrl.setPortletMode(parentPortletMode);
//        }
//        final Map<String, List<String>> parentParameters = delegationRequest.getParentParameters();
//        if (parentParameters != null) {
//            parentPortletUrl.setParameters(parentParameters);
//        }
    }
}
