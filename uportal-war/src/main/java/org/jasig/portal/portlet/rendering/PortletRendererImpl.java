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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.container.om.portlet.SecurityRoleRef;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.portlet.InconsistentPortletModelException;
import org.jasig.portal.portlet.PortletDispatchException;
import org.jasig.portal.portlet.PortletHttpServletRequestWrapper;
import org.jasig.portal.portlet.PortletHttpServletResponseWrapper;
import org.jasig.portal.portlet.PortletLoadFailureException;
import org.jasig.portal.portlet.container.services.AdministrativeRequestListenerController;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.session.PortletSessionAdministrativeRequestListener;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletPortalUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
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
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletWindowRegistry portletWindowRegistry;
    private PortletContainer portletContainer;
    private IPortalUrlProvider portalUrlProvider;
    private PortletDelegationLocator portletDelegationLocator;
    
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
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

    public IPortletWindowId doInit(IPortletEntity portletEntity, IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        final IPortletWindow portletWindow;
        if (portletWindowId != null) {
            portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
            if (portletWindow == null) {
                throw new IllegalArgumentException("Portlet window is null but a portlet window ID has been configured for it: " + portletWindowId);
            }
        }
        else {
            portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindow(httpServletRequest, portletEntityId);
        }
        
        //init the portlet window
        final StringWriter initResultsOutput = new StringWriter();
        final PortletHttpServletResponseWrapper portletHttpServletResponseWrapper = new PortletHttpServletResponseWrapper(httpServletResponse, new PrintWriter(initResultsOutput));
        
        httpServletRequest = this.setupPortletRequest(httpServletRequest, portletWindow, null);
        
        try {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Loading portlet window '" + portletWindow + "'");
            }
            
            this.portletContainer.doLoad(portletWindow, httpServletRequest, portletHttpServletResponseWrapper);
        }
        catch (PortletException pe) {
            throw new PortletLoadFailureException("The portlet window '" + portletWindow + "' threw an exception while being loaded.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletLoadFailureException("The portlet container threw an exception while loading portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletLoadFailureException("The portlet window '" + portletWindow + "' threw an exception while being loaded.", portletWindow, ioe);
        }
        
        final StringBuffer initResults = initResultsOutput.getBuffer();
        if (initResults.length() > 0) {
            throw new PortletLoadFailureException("Content was written to response during loading of portlet window '" + portletWindow + "' . Response Content: " + initResults, portletWindow);
        }
        
        return portletWindow.getPortletWindowId();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.IPortletRenderer#doAction(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public long doAction(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        //Load the parameters to provide to the portlet with the request and update the state and mode
        final Map<String, String[]> parameters = portletWindow.getRequestParameters();
        
        httpServletRequest = this.setupPortletRequest(httpServletRequest, portletWindow, parameters);
        
        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Executing portlet action for window '" + portletWindow + "'");
        }
        
        final long start = System.currentTimeMillis();
        try {
            this.portletContainer.doAction(portletWindow, httpServletRequest, httpServletResponse);
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
    public PortletRenderResult doRender(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Writer printWriter) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        final Map<String, String[]> parameters = portletWindow.getRequestParameters();
        
        //Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest, portletWindow, parameters);

        //Setup the response to capture the output
        final PortletHttpServletResponseWrapper portletHttpServletResponseWrapper = new PortletHttpServletResponseWrapper(httpServletResponse, printWriter); 
            

        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Rendering portlet for window '" + portletWindow + "'");
        }

        final long start = System.currentTimeMillis();
        try {
            this.portletContainer.doRender(portletWindow, httpServletRequest, portletHttpServletResponseWrapper);
            portletHttpServletResponseWrapper.flushBuffer();
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing render.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing render on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing render.", portletWindow, ioe);
        }
        
        
        final String title = (String)httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_TITLE);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Retrieved title '" + title + "' from request for: " + portletWindow);
        }
        
        return new PortletRenderResult(title, System.currentTimeMillis() - start);
    }
    
    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletRenderer#doServeResource(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.Writer)
	 */
	@Override
	public PortletResourceResult doServeResource(
			IPortletWindowId portletWindowId,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
		
		final Map<String, String[]> parameters = portletWindow.getRequestParameters();
        
        //Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest, portletWindow, parameters);
        
	    final long start = System.currentTimeMillis();
		try {
			this.portletContainer.doServeResource(portletWindow, httpServletRequest, httpServletResponse);
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
		return new PortletResourceResult(System.currentTimeMillis() - start);
	}
	
	@Override
    public void doReset(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        portletWindow.setPortletMode(PortletMode.VIEW);
        portletWindow.setRequestParameters(null);
        portletWindow.setExpirationCache(null);
        
        final StringWriter responseOutput = new StringWriter();
        
        httpServletRequest = this.setupPortletRequest(httpServletRequest, portletWindow, null);
        httpServletResponse = new PortletHttpServletResponseWrapper(httpServletResponse, new PrintWriter(responseOutput));
        
        httpServletRequest.setAttribute(AdministrativeRequestListenerController.DEFAULT_LISTENER_KEY_ATTRIBUTE, "sessionActionListener");
        httpServletRequest.setAttribute(PortletSessionAdministrativeRequestListener.ACTION, PortletSessionAdministrativeRequestListener.SessionAction.CLEAR);
        httpServletRequest.setAttribute(PortletSessionAdministrativeRequestListener.SCOPE, PortletSession.PORTLET_SCOPE);
        
        try {
            this.portletContainer.doAdmin(portletWindow, httpServletRequest, httpServletResponse);
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

    protected HttpServletRequest setupPortletRequest(HttpServletRequest httpServletRequest, IPortletWindow portletWindow, Map<String, String[]> parameters) {
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
        
        //Load the person the request is for
        final IPerson person = this.personManager.getPerson(httpServletRequest);
        
        //Load the portlet descriptor for the request
        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        final List<? extends SecurityRoleRef> securityRoleRefs;
        try {
            final IPortletEntity portletEntity = this.portletWindowRegistry.getParentPortletEntity(httpServletRequest, portletWindowId);
            final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletEntity.getPortletEntityId());
            final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinition.getPortletDefinitionId());
            if (portletDescriptor == null) {
                throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "', this usually means the Portlet application is not deployed correctly.", portletWindowId);
            }
            
            securityRoleRefs = portletDescriptor.getSecurityRoleRefs();
        }
        catch (DataRetrievalFailureException pce) {
            throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "' to provide the required SecurityRoleRefDD List to the PortletHttpRequestWrapper.", portletWindowId, pce);
        }
        
        final PortletHttpServletRequestWrapper portletHttpServletRequestWrapper = new PortletHttpServletRequestWrapper(httpServletRequest, parameters, person, securityRoleRefs);
        
        portletHttpServletRequestWrapper.setAttribute(PortletDelegationLocator.PORTLET_DELECATION_LOCATOR_ATTR, this.portletDelegationLocator);
        
        return portletHttpServletRequestWrapper;
    }

    protected void setupPortletWindow(HttpServletRequest httpServletRequest, IPortletWindow portletWindow, IPortletPortalUrl portletUrl) {
        final PortletMode portletMode = portletUrl.getPortletMode();
        if (portletMode != null) {
            if (IPortletRenderer.CONFIG.equals(portletMode)) {
                final IPerson person = this.personManager.getPerson(httpServletRequest);
                
                final EntityIdentifier ei = person.getEntityIdentifier();
                final AuthorizationService authorizationService = AuthorizationService.instance();
                final IAuthorizationPrincipal ap = authorizationService.newPrincipal(ei.getKey(), ei.getType());
                
                final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletWindow.getPortletEntityId());
                final IChannelDefinition channelDefinition = portletDefinition.getChannelDefinition();
                
                if (!ap.canConfigure(channelDefinition.getId())) {
                    throw new AuthorizationException(person.getUserName() + " does not have permission to render '" + channelDefinition.getFName() + "' in " + portletMode + " PortletMode");
                }
            }
            
            portletWindow.setPortletMode(portletMode);
        }
   
        final WindowState windowState = portletUrl.getWindowState();
        if (windowState != null) {
            portletWindow.setWindowState(windowState);
        }
    }
}
