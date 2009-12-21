/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.channels.portlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.common.SecurityRoleRefDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.url.IPortletRequestParameterManager;
import org.jasig.portal.portlet.url.PortletUrl;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.AttributeScopingHttpServletRequestWrapper;

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
        httpServletRequest = new AttributeScopingHttpServletRequestWrapper(httpServletRequest);
        
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
        final ContentRedirectingHttpServletResponse contentRedirectingHttpServletResponse = new ContentRedirectingHttpServletResponse(httpServletResponse, new PrintWriter(initResultsOutput));
        
        try {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Loading portlet window '" + portletWindow + "'");
            }
            
            this.portletContainer.doLoad(portletWindow, httpServletRequest, contentRedirectingHttpServletResponse);
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
        httpServletRequest = new AttributeScopingHttpServletRequestWrapper(httpServletRequest);
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        //Load the parameters to provide to the portlet with the request and update the state and mode
        Map<String, List<String>> parameters = null;
        final PortletUrl portletUrl = this.portletRequestParameterManager.getPortletRequestInfo(httpServletRequest, portletWindowId);
        if (portletUrl != null) {
            parameters = portletUrl.getParameters();
            
            final PortletMode portletMode = portletUrl.getPortletMode();
            if (portletMode != null) {
                portletWindow.setPortletMode(portletMode);
            }
    
            final WindowState windowState = portletUrl.getWindowState();
            if (windowState != null) {
                portletWindow.setWindowState(windowState);
            }
        }
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
        
        //Load the person the request is for
        final IPerson person = this.personManager.getPerson(httpServletRequest);
        
        //Load the portlet descriptor for the request
        final List<SecurityRoleRefDD> securityRoleRefs;
        try {
            final PortletDD portletDescriptor = this.getPortletDD(httpServletRequest, portletWindowId);
            if (portletDescriptor == null) {
                throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "', this usually means the Portlet application is not deployed correctly.", portletWindowId);
            }
            
            securityRoleRefs = portletDescriptor.getSecurityRoleRefs();
        }
        catch (PortletContainerException pce) {
            throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "' to provide the required SecurityRoleRefDD List to the PortletHttpRequestWrapper.", portletWindowId, pce);
        }
        
        //Setup the request and response
        final PortletHttpRequestWrapper parameterRequestWrapper = new PortletHttpRequestWrapper(httpServletRequest, parameters, person, securityRoleRefs);
        
        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Executing portlet action for window '" + portletWindow + "'");
        }
        
        try {
            this.portletContainer.doAction(portletWindow, parameterRequestWrapper, httpServletResponse);
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

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.IPortletRenderer#doRender(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.PrintWriter)
     */
    @Override
    public void doRender(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, PrintWriter printWriter) {
        httpServletRequest = new AttributeScopingHttpServletRequestWrapper(httpServletRequest);
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        //Setup the response to capture the output
        final ContentRedirectingHttpServletResponse contentRedirectingHttpServletResponse = new ContentRedirectingHttpServletResponse(httpServletResponse, printWriter); 
            
        //Load the parameters to provide with the request
        final PortletUrl portletUrl = this.portletRequestParameterManager.getPortletRequestInfo(httpServletRequest, portletWindowId);
        
        //Current portlet isn't targeted, use parameters from previous request
        Map<String, List<String>> parameters;
        if (portletUrl == null) {
            parameters = portletWindow.getRequestParameters();
        }
        //Current portlet is targeted, set parameters and update state/mode
        else {
            parameters = portletUrl.getParameters();
            if (parameters != null) {
                portletWindow.setRequestParameters(parameters);
            }
            
            final PortletMode portletMode = portletUrl.getPortletMode();
            if (portletMode != null) {
                portletWindow.setPortletMode(portletMode);
            }
    
            final WindowState windowState = portletUrl.getWindowState();
            if (windowState != null) {
                portletWindow.setWindowState(windowState);
            }
        }
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
        
        //Load the person the request is for
        final IPerson person = this.personManager.getPerson(httpServletRequest);
        
        //Load the portlet descriptor for the request
        final List<SecurityRoleRefDD> securityRoleRefs;
        try {
            final PortletDD portletDescriptor = this.getPortletDD(httpServletRequest, portletWindowId);
            if (portletDescriptor == null) {
                throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "', this usually means the Portlet application is not deployed correctly.", portletWindowId);
            }
            
            securityRoleRefs = portletDescriptor.getSecurityRoleRefs();
        }
        catch (PortletContainerException pce) {
            throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "' to provide the required SecurityRoleRefDD List to the PortletHttpRequestWrapper.", portletWindowId, pce);
        }
        
        //Setup the request and response
        final PortletHttpRequestWrapper parameterRequestWrapper = new PortletHttpRequestWrapper(httpServletRequest, parameters, person, securityRoleRefs);

        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Rendering portlet for window '" + portletWindow + "'");
        }

        try {
            this.portletContainer.doRender(portletWindow, parameterRequestWrapper, contentRedirectingHttpServletResponse);
            contentRedirectingHttpServletResponse.flushBuffer();
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
        
        

        final String title = (String)parameterRequestWrapper.getAttribute(IPortletAdaptor.ATTRIBUTE__PORTLET_TITLE);
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Retrieved title '" + title + "' from request for: " + portletWindow);
        }
    }

    protected PortletDD getPortletDD(final HttpServletRequest httpServletRequest, final IPortletWindowId portletWindowId) throws PortletContainerException {
        final IPortletEntity portletEntity = this.portletWindowRegistry.getParentPortletEntity(httpServletRequest, portletWindowId);
        final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletEntity.getPortletEntityId());
        return this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinition.getPortletDefinitionId());
    }

}
