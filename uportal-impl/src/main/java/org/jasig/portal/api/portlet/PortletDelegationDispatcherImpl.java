/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.core.ContainerInvocation;
import org.jasig.portal.channels.portlet.IPortletRenderer;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.url.IPortletRequestParameterManager;
import org.jasig.portal.portlet.url.PortletUrl;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;

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
    private final IPortletRequestParameterManager portletRequestParameterManager;
    
    

    public PortletDelegationDispatcherImpl(IPortletWindow portletWindow, IPortletWindow parentPortletWindow, int userId,
            IPortalRequestUtils portalRequestUtils, IPersonManager personManager, IPortletRenderer portletRenderer,
            IPortletRequestParameterManager portletRequestParameterManager) {
        
        Validate.notNull(portletWindow, "portletWindow can not be null");
        Validate.notNull(parentPortletWindow, "parentPortletWindow can not be null");
        Validate.notNull(portalRequestUtils, "portalRequestUtils can not be null");
        Validate.notNull(personManager, "personManager can not be null");
        Validate.notNull(portletRenderer, "portletRenderer can not be null");
        Validate.notNull(portletRequestParameterManager, "portletRequestParameterManager can not be null");
        
        this.portletWindow = portletWindow;
        this.parentPortletWindow = parentPortletWindow;
        this.userId = userId;
        this.portalRequestUtils = portalRequestUtils;
        this.personManager = personManager;
        this.portletRenderer = portletRenderer;
        this.portletRequestParameterManager = portletRequestParameterManager;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#doAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    @Override
    public DelegateState doAction(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException {
        return this.doAction(actionRequest, actionResponse, null);
    }
    

    @Override
    public DelegateState doAction(ActionRequest actionRequest, ActionResponse actionResponse, DelegationRequest delegationRequest) throws IOException {
        final HttpServletRequest request = this.portalRequestUtils.getOriginalPortalRequest(actionRequest);
        final HttpServletResponse response = this.portalRequestUtils.getOriginalPortalResponse(actionRequest);

        //Sanity check that the dispatch is being called by the same user it was created for
        final IPerson person = this.personManager.getPerson(request);
        if (this.userId != person.getID()) {
            throw new IllegalStateException("This dispatcher was created for userId " + this.userId + " but is being executed for userId " + person.getID());
        }
        
        this.setupDelegateRequestInfo(request, delegationRequest);
        
        final RedirectCapturingResponse capturingResponse = new RedirectCapturingResponse(response);
        
        final ContainerInvocation invocation = ContainerInvocation.getInvocation();
        try {
            this.portletRenderer.doAction(this.portletWindow.getPortletWindowId(), request, capturingResponse);
        }
        catch (RuntimeException e) {
            this.logger.error("Failed to execute action on delegate", e);
            throw e;
        }
        finally {
            if (invocation != null) {
                ContainerInvocation.setInvocation(invocation.getPortletContainer(), invocation.getPortletWindow());
            }
        }
        
        final String redirectLocation = capturingResponse.getRedirectLocation();
        
        //If the delegate portlet sent a redirect use the parent action response to send it
        if (!PortletDelegationManager.DELEGATE_ACTION_REDIRECT_TOKEN.equals(redirectLocation)) {
            actionResponse.sendRedirect(redirectLocation);
        }
        
        return this.getDelegateState();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#doRender(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    @Override
    public DelegateState doRender(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException {
        return this.doRender(renderRequest, renderResponse, null);
    }
    
    @Override
    public DelegateState doRender(RenderRequest renderRequest, RenderResponse renderResponse, DelegationRequest delegationRequest) throws IOException {
        final HttpServletRequest request = this.portalRequestUtils.getOriginalPortalRequest(renderRequest);
        final HttpServletResponse response = this.portalRequestUtils.getOriginalPortalResponse(renderRequest);

        //Sanity check that the dispatch is being called by the same user it was created for
        final IPerson person = this.personManager.getPerson(request);
        if (this.userId != person.getID()) {
            throw new IllegalStateException("This dispatcher was created for userId " + this.userId + " but is being executed for userId " + person.getID());
        }

        this.setupDelegateRequestInfo(request, delegationRequest);
        
        final PrintWriter writer = renderResponse.getWriter();
        final ContainerInvocation invocation = ContainerInvocation.getInvocation();
        try {
            this.portletRenderer.doRender(this.portletWindow.getPortletWindowId(), request, response, writer);
        }
        catch (RuntimeException e) {
            this.logger.error("Failed to render delegate", e);
            throw e;
        }
        finally {
            if (invocation != null) {
                ContainerInvocation.setInvocation(invocation.getPortletContainer(), invocation.getPortletWindow());
            }
            writer.flush();
        }
        
        return this.getDelegateState();
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
        
        final DelegateState delegateState = delegationRequest.getDelegateState();
        if (delegateState != null) {
            final IPortletWindowId portletWindowId = this.portletWindow.getPortletWindowId();
            
            PortletUrl delegatePortletUrl = this.portletRequestParameterManager.getPortletRequestInfo(request, portletWindowId);
            
            //If the delegate URL doesn't exist in the parameter manager add it and insert it in the correct location
            if (delegatePortletUrl == null) {
                delegatePortletUrl = new PortletUrl(portletWindowId);
                
                final IPortletWindowId targetedPortletWindowId = this.portletRequestParameterManager.getTargetedPortletWindowId(request);
                PortletUrl targetedPortletUrl = this.portletRequestParameterManager.getPortletRequestInfo(request, targetedPortletWindowId);
                
                //If there is no targeted portlet just set the delegate as the URL
                if (targetedPortletUrl == null) {
                    this.portletRequestParameterManager.setRequestInfo(request, delegatePortletUrl);
                }
                //Otherwise find the correct place in the URL chain to put the delegate URL
                else {
                    boolean added = false;
                    
                    while (targetedPortletUrl != null) {
                        final PortletUrl oldDelegatePortletUrl = targetedPortletUrl.getDelegatePortletUrl();
                        
                        if (targetedPortletUrl.getTargetWindowId().equals(this.portletWindow.getDelegationParent())) {
                            targetedPortletUrl.setDelegatePortletUrl(delegatePortletUrl);
                            delegatePortletUrl.setDelegatePortletUrl(oldDelegatePortletUrl);
                            
                            added = true;
                            break;
                        }
                        
                        targetedPortletUrl = oldDelegatePortletUrl;
                    }
                    
                    if (!added) {
                        targetedPortletUrl.setDelegatePortletUrl(delegatePortletUrl);
                    }
                }
            }

            final PortletMode mode = delegateState.getPortletMode();
            delegatePortletUrl.setPortletMode(mode);

            final WindowState state = delegateState.getWindowState();
            delegatePortletUrl.setWindowState(state);
        }
        
        final WindowState parentWindowState = delegationRequest.getParentWindowState();
        if (parentWindowState != null) {
            this.parentPortletWindow.setWindowState(parentWindowState);
        }
        final PortletMode parentPortletMode = delegationRequest.getParentPortletMode();
        if (parentPortletMode != null) {
            this.parentPortletWindow.setPortletMode(parentPortletMode);
        }
        final Map<String, List<String>> parentParameters = delegationRequest.getParentParameters();
        if (parentParameters != null) {
            this.parentPortletWindow.setRequestParameters(parentParameters);
        }
    }
}
