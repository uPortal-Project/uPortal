/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.common.SecurityRoleRefDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.channels.portlet.InconsistentPortletModelException;
import org.jasig.portal.channels.portlet.PortletDispatchException;
import org.jasig.portal.channels.portlet.PortletHttpServletRequestWrapper;
import org.jasig.portal.channels.portlet.PortletHttpServletResponseWrapper;
import org.jasig.portal.channels.portlet.PortletLoadFailureException;
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
import org.jasig.portal.portlet.url.IPortletRequestParameterManager;
import org.jasig.portal.portlet.url.PortletUrl;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;

/**
 * Executes methods on portlets using Pluto
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRendererImpl implements IPortletRenderer {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPersonManager personManager;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletWindowRegistry portletWindowRegistry;
    private PortletContainer portletContainer;
    private IPortletRequestParameterManager portletRequestParameterManager;
    

    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    public void setPortletContainer(PortletContainer portletContainer) {
        this.portletContainer = portletContainer;
    }

    public void setPortletRequestParameterManager(IPortletRequestParameterManager portletRequestParameterManager) {
        this.portletRequestParameterManager = portletRequestParameterManager;
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
    public void doAction(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        //Load the parameters to provide to the portlet with the request and update the state and mode
        Map<String, List<String>> parameters = null;
        final PortletUrl portletUrl = this.portletRequestParameterManager.getPortletRequestInfo(httpServletRequest, portletWindowId);
        if (portletUrl != null) {
            parameters = portletUrl.getParameters();
            
            this.setupPortletWindow(httpServletRequest, portletWindow, portletUrl);
        }
        
        httpServletRequest = this.setupPortletRequest(httpServletRequest, portletWindow, parameters);
        
        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Executing portlet action for window '" + portletWindow + "'");
        }
        
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
    }

    @Override
    public PortletRenderResult doRender(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Writer printWriter) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        //Load the parameters to provide with the request
        final PortletUrl portletUrl = this.portletRequestParameterManager.getPortletRequestInfo(httpServletRequest, portletWindowId);
        
        Map<String, List<String>> parameters;
        //Current portlet isn't targeted, use parameters from previous request
        if (portletUrl == null) {
            parameters = portletWindow.getRequestParameters();
        }
        //Current portlet is targeted, set parameters and update state/mode
        else {
            parameters = portletUrl.getParameters();
            if (parameters != null) {
                portletWindow.setRequestParameters(parameters);
            }
            
            this.setupPortletWindow(httpServletRequest, portletWindow, portletUrl);
        }
        
        //Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest, portletWindow, parameters);

        //Setup the response to capture the output
        final PortletHttpServletResponseWrapper portletHttpServletResponseWrapper = new PortletHttpServletResponseWrapper(httpServletResponse, printWriter); 
            

        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Rendering portlet for window '" + portletWindow + "'");
        }

        try {
            this.portletContainer.doRender(portletWindow, httpServletRequest, portletHttpServletResponseWrapper);
            portletHttpServletResponseWrapper.flushBuffer();
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing render.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing action on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing render.", portletWindow, ioe);
        }
        
        
        final String title = (String)httpServletRequest.getAttribute(IPortletAdaptor.ATTRIBUTE__PORTLET_TITLE);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Retrieved title '" + title + "' from request for: " + portletWindow);
        }
        
        return new PortletRenderResult(title);
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

    protected HttpServletRequest setupPortletRequest(HttpServletRequest httpServletRequest, IPortletWindow portletWindow, Map<String, List<String>> parameters) {
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
        
        //Load the person the request is for
        final IPerson person = this.personManager.getPerson(httpServletRequest);
        
        //Load the portlet descriptor for the request
        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        final List<SecurityRoleRefDD> securityRoleRefs;
        try {
            final IPortletEntity portletEntity = this.portletWindowRegistry.getParentPortletEntity(httpServletRequest, portletWindowId);
            final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletEntity.getPortletEntityId());
            final PortletDD portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinition.getPortletDefinitionId());
            if (portletDescriptor == null) {
                throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "', this usually means the Portlet application is not deployed correctly.", portletWindowId);
            }
            
            securityRoleRefs = portletDescriptor.getSecurityRoleRefs();
        }
        catch (PortletContainerException pce) {
            throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "' to provide the required SecurityRoleRefDD List to the PortletHttpRequestWrapper.", portletWindowId, pce);
        }
        
        return new PortletHttpServletRequestWrapper(httpServletRequest, parameters, person, securityRoleRefs);
    }

    protected void setupPortletWindow(HttpServletRequest httpServletRequest, IPortletWindow portletWindow, PortletUrl portletUrl) {
        final PortletMode portletMode = portletUrl.getPortletMode();
        if (portletMode != null) {
            if (IPortletAdaptor.CONFIG.equals(portletMode)) {
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
