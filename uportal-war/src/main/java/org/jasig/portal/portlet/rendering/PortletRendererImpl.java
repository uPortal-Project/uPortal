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

package org.jasig.portal.portlet.rendering;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.portlet.Event;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.portlet.OutputCapturingHttpServletResponseWrapper;
import org.jasig.portal.portlet.PortletDispatchException;
import org.jasig.portal.portlet.container.services.AdministrativeRequestListenerController;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.session.PortletSessionAdministrativeRequestListener;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.utils.web.PortletHttpServletRequestWrapper;
import org.jasig.portal.utils.web.PortletHttpServletResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Executes methods on portlets using Pluto
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletRendererImpl implements IPortletRenderer {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPersonManager personManager;
    private IPortletWindowRegistry portletWindowRegistry;
    private PortletContainer portletContainer;
    private PortletDelegationLocator portletDelegationLocator;
    
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    @Autowired
    public void setPortletContainer(PortletContainer portletContainer) {
        this.portletContainer = portletContainer;
    }
    @Autowired
    public void setPortletDelegationLocator(PortletDelegationLocator portletDelegationLocator) {
        this.portletDelegationLocator = portletDelegationLocator;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.IPortletRenderer#doAction(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public long doAction(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);
        
        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Executing portlet action for window '" + portletWindow + "'");
        }
        
        final long start = System.currentTimeMillis();
        try {
            this.portletContainer.doAction(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing action.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing action on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing action.", portletWindow, ioe);
        }
        
        return System.currentTimeMillis() - start;
    }
    
    @Override
    public long doEvent(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, Event event) {
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);
        
        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Executing portlet event for window '" + portletWindow + "'");
        }
        
        final long start = System.currentTimeMillis();
        try {
            this.portletContainer.doEvent(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse, event);
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing event.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing event on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing event.", portletWindow, ioe);
        }
        
        return System.currentTimeMillis() - start;
    }
    @Override
    public PortletRenderResult doRenderMarkup(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Writer writer) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        //Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);

        //Set the writer to capture the response
        httpServletRequest.setAttribute(ATTRIBUTE__PORTLET_PRINT_WRITER, new PrintWriter(writer));

        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Rendering portlet body for window '" + portletWindow + "'");
        }

        final long start = System.currentTimeMillis();
        try {
        	httpServletRequest.setAttribute(PortletRequest.RENDER_PART, PortletRequest.RENDER_MARKUP);
            this.portletContainer.doRender(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing renderMarkup.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing renderMarkup on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing renderMarkup.", portletWindow, ioe);
        }
        
        
        final String title = (String)httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_TITLE);
        final String newItemCountString = (String)httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_NEW_ITEM_COUNT);
        final int newItemCount;
        if (newItemCountString != null && StringUtils.isNumeric(newItemCountString)) {
            newItemCount = Integer.parseInt(newItemCountString);
        } else {
            newItemCount = 0;
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Retrieved title '" + title + "' from request for: " + portletWindow);
        }
        
        return new PortletRenderResult(title, newItemCount, System.currentTimeMillis() - start);
    }
    
    
    
    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletRenderer#doRenderHeader(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.Writer)
	 */
	@Override
	public PortletRenderResult doRenderHeader(IPortletWindowId portletWindowId,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, Writer writer) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        //Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);

        //Set the writer to capture the response
        httpServletRequest.setAttribute(ATTRIBUTE__PORTLET_PRINT_WRITER, new PrintWriter(writer));

        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Rendering portlet header for window '" + portletWindow + "'");
        }

        final long start = System.currentTimeMillis();
        try {
        	httpServletRequest.setAttribute(PortletRequest.RENDER_PART, PortletRequest.RENDER_HEADERS);
            this.portletContainer.doRender(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing renderHeader.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing renderHeader on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing renderHeader.", portletWindow, ioe);
        }
        
        
        final String title = (String)httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_TITLE);
        final String newItemCountString = (String)httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_NEW_ITEM_COUNT);
        final int newItemCount;
        if (newItemCountString != null && StringUtils.isNumeric(newItemCountString)) {
            newItemCount = Integer.parseInt(newItemCountString);
        } else {
            newItemCount = 0;
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Retrieved title '" + title + "' from request for: " + portletWindow);
        }
        
        return new PortletRenderResult(title, newItemCount, System.currentTimeMillis() - start);
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletRenderer#doServeResource(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.Writer)
	 */
	@Override
	public long doServeResource(
			IPortletWindowId portletWindowId,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
		
        //Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);
        
	    final long start = System.currentTimeMillis();
		try {
			this.portletContainer.doServeResource(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
		}
		catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing serveResource.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing serveResource on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing serveResource.", portletWindow, ioe);
        }
		return System.currentTimeMillis() - start;
	}
	
	@Override
    public void doReset(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        portletWindow.setPortletMode(PortletMode.VIEW);
        portletWindow.setRenderParameters(null);
        portletWindow.setExpirationCache(null);
        
        final StringWriter responseOutput = new StringWriter();
        
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = new OutputCapturingHttpServletResponseWrapper(httpServletResponse, new PrintWriter(responseOutput));
        
        httpServletRequest.setAttribute(AdministrativeRequestListenerController.DEFAULT_LISTENER_KEY_ATTRIBUTE, "sessionActionListener");
        httpServletRequest.setAttribute(PortletSessionAdministrativeRequestListener.ACTION, PortletSessionAdministrativeRequestListener.SessionAction.CLEAR);
        httpServletRequest.setAttribute(PortletSessionAdministrativeRequestListener.SCOPE, PortletSession.PORTLET_SCOPE);
        
        try {
            this.portletContainer.doAdmin(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing admin command to clear session.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing admin command to clear session on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing admin command to clear session.", portletWindow, ioe);
        }
        
        final StringBuffer initResults = responseOutput.getBuffer();
        if (initResults.length() > 0) {
            throw new PortletDispatchException("Content was written to response during reset of portlet window '" + portletWindow + "'. Response Content: " + initResults, portletWindow);
        }
        
    }

    protected HttpServletRequest setupPortletRequest(HttpServletRequest httpServletRequest) {
        final PortletHttpServletRequestWrapper portletHttpServletRequestWrapper = new PortletHttpServletRequestWrapper(httpServletRequest);
        portletHttpServletRequestWrapper.setAttribute(PortletDelegationLocator.PORTLET_DELECATION_LOCATOR_ATTR, this.portletDelegationLocator);
        
        return portletHttpServletRequestWrapper;
    }

    protected HttpServletResponse setupPortletResponse(HttpServletResponse httpServletResponse) {
        final PortletHttpServletResponseWrapper portletHttpServletResponseWrapper = new PortletHttpServletResponseWrapper(httpServletResponse);
        
        return portletHttpServletResponseWrapper;
    }
    
    protected void setupPortletWindow(HttpServletRequest httpServletRequest, IPortletWindow portletWindow, IPortletUrlBuilder portletUrl) {
        final PortletMode portletMode = portletUrl.getPortletMode();
        if (portletMode != null) {
            if (IPortletRenderer.CONFIG.equals(portletMode)) {
                final IPerson person = this.personManager.getPerson(httpServletRequest);
                
                final EntityIdentifier ei = person.getEntityIdentifier();
                final AuthorizationService authorizationService = AuthorizationService.instance();
                final IAuthorizationPrincipal ap = authorizationService.newPrincipal(ei.getKey(), ei.getType());
                
                final IPortletEntity portletEntity = portletWindow.getPortletEntity();
                final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
                
                if (!ap.canConfigure(portletDefinition.getPortletDefinitionId().getStringId())) {
                    throw new AuthorizationException(person.getUserName() + " does not have permission to render '" + portletDefinition.getFName() + "' in " + portletMode + " PortletMode");
                }
            }
        }
    }
}
