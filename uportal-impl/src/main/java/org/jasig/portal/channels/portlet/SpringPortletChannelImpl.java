/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.common.SecurityRoleRefDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.session.IPortletSessionActionManager;
import org.jasig.portal.portlet.url.IPortletRequestParameterManager;
import org.jasig.portal.portlet.url.PortletRequestInfo;
import org.jasig.portal.portlet.url.RequestType;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of ISpringPortletChannel that delegates rendering a portlet to the injected {@link PortletContainer}.
 * The portlet to render is determined in  {@link #initSession(ChannelStaticData, PortalControlStructures)} using the
 * channel publish and subscribe ids.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SpringPortletChannelImpl implements ISpringPortletChannel {
    protected static final String PORTLET_WINDOW_ID_PARAM = SpringPortletChannelImpl.class.getName() + ".portletWindowId";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPersonManager personManager;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletWindowRegistry portletWindowRegistry;
    private PortletContainer portletContainer;
    private IPortletRequestParameterManager portletRequestParameterManager;
    private IPortletSessionActionManager portletSessionActionManager;
    
    
    /**
     * @return the personManager
     */
    public IPersonManager getPersonManager() {
        return this.personManager;
    }
    /**
     * @param personManager the personManager to set
     */
    @Required
    public void setPersonManager(IPersonManager personManager) {
        Validate.notNull(personManager);
        this.personManager = personManager;
    }
    /**
     * @return the portletDefinitionRegistry
     */
    public IPortletDefinitionRegistry getPortletDefinitionRegistry() {
        return portletDefinitionRegistry;
    }
    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
    @Required
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        Validate.notNull(portletDefinitionRegistry);
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
    /**
     * @return the portletEntityRegistry
     */
    public IPortletEntityRegistry getPortletEntityRegistry() {
        return portletEntityRegistry;
    }
    /**
     * @param portletEntityRegistry the portletEntityRegistry to set
     */
    @Required
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    /**
     * @return the portletWindowRegistry
     */
    public IPortletWindowRegistry getPortletWindowRegistry() {
        return portletWindowRegistry;
    }
    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Required
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        Validate.notNull(portletWindowRegistry);
        this.portletWindowRegistry = portletWindowRegistry;
    }

    /**
     * @return the portletContainer
     */
    public PortletContainer getPortletContainer() {
        return portletContainer;
    }
    /**
     * @param portletContainer the portletContainer to set
     */
    @Required
    public void setPortletContainer(PortletContainer portletContainer) {
        Validate.notNull(portletContainer);
        this.portletContainer = portletContainer;
    }

    /**
     * @return the portletRequestParameterManager
     */
    public IPortletRequestParameterManager getPortletRequestParameterManager() {
        return portletRequestParameterManager;
    }
    /**
     * @param portletRequestParameterManager the portletRequestParameterManager to set
     */
    @Required
    public void setPortletRequestParameterManager(IPortletRequestParameterManager portletRequestParameterManager) {
        Validate.notNull(portletRequestParameterManager);
        this.portletRequestParameterManager = portletRequestParameterManager;
    }
    
    /**
     * @return the portletSessionActionManager
     */
    public IPortletSessionActionManager getPortletSessionActionManager() {
        return this.portletSessionActionManager;
    }
    /**
     * @param portletSessionActionManager the portletSessionActionManager to set
     */
    @Required
    public void setPortletSessionActionManager(IPortletSessionActionManager portletSessionActionManager) {
        Validate.notNull(portletSessionActionManager);
        this.portletSessionActionManager = portletSessionActionManager;
    }

    
    //***** Helper methods for the class *****//
    
    /**
     * Get the id for the window instance described by the channel static data and portal control structures.
     * 
     * @param channelStaticData The static description data for the channel.
     * @param portalControlStructures Information about the current request/reponse.
     * @return The window instance id.
     */
    protected String getWindowInstanceId(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures) {
        return this.getWindowInstanceId(channelStaticData, portalControlStructures, null);
    }

    /**
     * Get the id for the window instance described by the channel static data, portal control structures, and channel
     * runtime data.
     * 
     * @param channelStaticData The static description data for the channel.
     * @param portalControlStructures Information about the current request/reponse.
     * @param channelRuntimeData Portal provided information for the current request.
     * @return The window instance id.
     */
    protected String getWindowInstanceId(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData) {
        return channelStaticData.getChannelSubscribeId();
    }
    
    /**
     * Sets the portlet window ID to use for the specified channel static data.
     * 
     * @param channelStaticData The static description data for the channel.
     * @param portletWindowId The ID to associate with the static data.
     */
    protected void setPortletWidnowId(ChannelStaticData channelStaticData, IPortletWindowId portletWindowId) {
        //Probably not 'ok' to put data directly to the Hashtable that ChannelStaticData extends but it is the only way
        //an object scoped to just this channel instance can be stored
        channelStaticData.put(PORTLET_WINDOW_ID_PARAM, portletWindowId);
    }

    /**
     * Gets the portlet window ID to use for the specified channel static data.
     * 
     * @param channelStaticData The static description data for the channel.
     * @return The ID associated with the static data, will not be null.
     */
    protected IPortletWindowId getPortletWindowId(ChannelStaticData channelStaticData) {
        final IPortletWindowId portletWindowId = (IPortletWindowId)channelStaticData.get(PORTLET_WINDOW_ID_PARAM);
        
        if (portletWindowId == null) {
            throw new InconsistentPortletModelException("No IPortletWindowId exists in the ChannelStaticData, has initSession been called? ChannelStaticData: " + channelStaticData, portletWindowId);
        }
        
        return portletWindowId;
    }
    
    //***** ISpringPortletChannel methods *****//

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#initSession(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures)
     */
    public void initSession(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures) {
        //Get/create the portlet entity to init
        final String channelPublishId = channelStaticData.getChannelPublishId();
        final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinition(Integer.parseInt(channelPublishId));
        if (portletDefinition == null) {
            throw new InconsistentPortletModelException("No IPortletDefinition exists for channelPublishId: " + channelPublishId, null);
        }
        
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        final String channelSubscribeId = channelStaticData.getChannelSubscribeId();
        final IPerson person = channelStaticData.getPerson();
        final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(portletDefinitionId, channelSubscribeId, person.getID());

        //Get/create the portlet window to init
        final HttpServletRequest httpServletRequest = portalControlStructures.getHttpServletRequest();
        final String windowInstanceId = this.getWindowInstanceId(channelStaticData, portalControlStructures);
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreatePortletWindow(httpServletRequest, windowInstanceId, portletEntityId);
        
        this.setPortletWidnowId(channelStaticData, portletWindow.getPortletWindowId());

        //init the portlet window
        final HttpServletResponse httpServletResponse = portalControlStructures.getHttpServletResponse();
        final StringWriter initResultsOutput = new StringWriter();
        final ContentRedirectingHttpServletResponse contentRedirectingHttpServletResponse = new ContentRedirectingHttpServletResponse(httpServletResponse, new PrintWriter(initResultsOutput));
        
        try {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Loading portlet window '" + portletWindow + "' with " + this.getChannelLogInfo(channelStaticData, portletWindow));
            }
            
            this.portletContainer.doLoad(portletWindow, httpServletRequest, contentRedirectingHttpServletResponse);
        }
        catch (PortletException pe) {
            throw new PortletLoadFailureException("The portlet window '" + portletWindow + "' threw an exception while being loaded. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletLoadFailureException("The portlet container threw an exception while loading portlet window '" + portletWindow + "'. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletLoadFailureException("The portlet window '" + portletWindow + "' threw an exception while being loaded. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, ioe);
        }
        
        final StringBuffer initResults = initResultsOutput.getBuffer();
        if (initResults.length() > 0) {
            throw new PortletLoadFailureException("Content was written to response during loading of portlet window '" + portletWindow + "' with " + this.getChannelLogInfo(channelStaticData, portletWindow) + ". Response Content: " + initResults, portletWindow);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#action(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData)
     */
    public void action(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData) {
        //Get the portlet window
        final IPortletWindowId portletWindowId = this.getPortletWindowId(channelStaticData);
        final HttpServletRequest httpServletRequest = portalControlStructures.getHttpServletRequest();
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        //Load the parameters to provide to the portlet with the request
        Map<String, String[]> parameters = Collections.emptyMap();
        final IPortletWindowId targetedPortletWindowId = this.portletRequestParameterManager.getTargetedPortletWindowId(httpServletRequest);
        if (portletWindowId.equals(targetedPortletWindowId)) {
            final PortletRequestInfo portletRequestInfo = this.portletRequestParameterManager.getPortletRequestInfo(httpServletRequest);
            parameters = portletRequestInfo.getParameters();
        }
        
        //Load the person the request is for
        final IPerson person = this.personManager.getPerson(httpServletRequest);
        
        //Load the portlet descriptor for the request
        final List<SecurityRoleRefDD> securityRoleRefs;
        try {
            final PortletDD portletDescriptor = this.getPortletDD(httpServletRequest, portletWindowId);
            if (portletDescriptor == null) {
                throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "', this usually means the Portlet application is not deployed correctly. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindowId);
            }
            
            securityRoleRefs = portletDescriptor.getSecurityRoleRefs();
        }
        catch (PortletContainerException pce) {
            throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "' to provide the required SecurityRoleRefDD List to the PortletHttpRequestWrapper. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindowId, pce);
        }
        
        //Setup the request and response
        final PortletHttpRequestWrapper parameterRequestWrapper = new PortletHttpRequestWrapper(httpServletRequest, parameters, person, securityRoleRefs);
        final HttpServletResponse httpServletResponse = portalControlStructures.getHttpServletResponse();
        
        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Executing portlet action for window '" + portletWindow + "' with " + this.getChannelLogInfo(channelStaticData, portletWindow));
        }
        
        try {
            this.portletContainer.doAction(portletWindow, parameterRequestWrapper, httpServletResponse);
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing action. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing action on portlet window '" + portletWindow + "'. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing action. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, ioe);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#generateKey(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData)
     */
    public ChannelCacheKey generateKey(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData) {
        final ChannelCacheKey cacheKey = new ChannelCacheKey();
        cacheKey.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
        cacheKey.setKey("INSTANCE_EXPIRATION_CACHE_KEY");
        cacheKey.setKeyValidity(System.currentTimeMillis());
        
        return cacheKey;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#isCacheValid(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData, java.lang.Object)
     */
    public boolean isCacheValid(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData, Object validity) {
        //Get the portlet window
        final HttpServletRequest httpServletRequest = portalControlStructures.getHttpServletRequest();
        final IPortletWindowId portletWindowId = this.getPortletWindowId(channelStaticData);
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        //If this window is targeted invalidate the cache
        final IPortletWindowId targetedPortletWindowId = this.portletRequestParameterManager.getTargetedPortletWindowId(httpServletRequest);
        if (portletWindowId.equals(targetedPortletWindowId)) {
            return false;
        }
        
        final PortletDD portletDescriptor;
        try {
            portletDescriptor = this.getPortletDD(httpServletRequest, portletWindowId);
        }
        catch (PortletContainerException pce) {
            this.logger.warn("Could not retrieve PortletDD for portlet window '" + portletWindowId + "' to determine caching configuration. Marking content cache invalid and continuing.", pce);
            return false;
        }
        
        if (portletDescriptor == null) {
            this.logger.warn("Could not retrieve PortletDD for portlet window '" + portletWindow + "' to determine caching configuration. Marking content cache invalid and continuing.");
            return false;
        }
        
        //If the descriptor value is unset return immediately
        final int descriptorExpirationCache = portletDescriptor.getExpirationCache();
        if (descriptorExpirationCache == PortletDD.EXPIRATION_CACHE_UNSET) {
            return false;
        }
        
        //Use value from window if it is set
        final Integer windowExpirationCache = portletWindow.getExpirationCache();
        final int expirationCache;
        if (windowExpirationCache != null) {
            expirationCache = windowExpirationCache;
        }
        else {
            expirationCache = descriptorExpirationCache;
        }
        
        //set to 0 (never cache)
        if (expirationCache == 0) {
            return false;
        }
        //set to -1 (never expire)
        else if (expirationCache == -1) {
            return true;
        }
        //If no validity object re-render to be safe
        else if (validity == null) {
            return false;
        }
        
        final long lastRenderTime = ((Long)validity).longValue();
        final long now = System.currentTimeMillis();
        
        //If the expiration time since last render has not passed return true
        return lastRenderTime + (expirationCache * 1000) >= now;
    }

    protected PortletDD getPortletDD(final HttpServletRequest httpServletRequest, final IPortletWindowId portletWindowId) throws PortletContainerException {
        final IPortletEntity portletEntity = this.portletWindowRegistry.getParentPortletEntity(httpServletRequest, portletWindowId);
        final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletEntity.getPortletEntityId());
        return this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinition.getPortletDefinitionId());
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#render(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData, java.io.PrintWriter)
     */
    public void render(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData, PrintWriter printWriter) {
        //Get the portlet window
        final HttpServletRequest httpServletRequest = portalControlStructures.getHttpServletRequest();
        final IPortletWindowId portletWindowId = this.getPortletWindowId(channelStaticData);
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        //Setup the response to capture the output
        final HttpServletResponse httpServletResponse = portalControlStructures.getHttpServletResponse();
        final ContentRedirectingHttpServletResponse contentRedirectingHttpServletResponse = new ContentRedirectingHttpServletResponse(httpServletResponse, printWriter); 
            
        //Load the parameters to provide with the request
        final IPortletWindowId targetedPortletWindowId = this.portletRequestParameterManager.getTargetedPortletWindowId(httpServletRequest);
        final PortletRequestInfo portletRequestInfo = this.portletRequestParameterManager.getPortletRequestInfo(httpServletRequest);
        Map<String, String[]> parameters;
        if (!portletWindowId.equals(targetedPortletWindowId) || portletRequestInfo == null || (parameters = portletRequestInfo.getParameters()) == null) {
            parameters = portletWindow.getRequestParameers();
            
            if (parameters == null) {
                parameters = Collections.emptyMap();
            }
        }
        else {
            portletWindow.setRequestParameters(parameters);
        }
        
        //Load the person the request is for
        final IPerson person = this.personManager.getPerson(httpServletRequest);
        
        //Load the portlet descriptor for the request
        final List<SecurityRoleRefDD> securityRoleRefs;
        try {
            final PortletDD portletDescriptor = this.getPortletDD(httpServletRequest, portletWindowId);
            if (portletDescriptor == null) {
                throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "', this usually means the Portlet application is not deployed correctly. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindowId);
            }
            
            securityRoleRefs = portletDescriptor.getSecurityRoleRefs();
        }
        catch (PortletContainerException pce) {
            throw new InconsistentPortletModelException("Could not retrieve PortletDD for portlet window '" + portletWindowId + "' to provide the required SecurityRoleRefDD List to the PortletHttpRequestWrapper. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindowId, pce);
        }
        
        //Setup the request and response
        final PortletHttpRequestWrapper parameterRequestWrapper = new PortletHttpRequestWrapper(httpServletRequest, parameters, person, securityRoleRefs);

        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Rendering portlet for window '" + portletWindow + "' with " + this.getChannelLogInfo(channelStaticData, portletWindow));
        }

        try {
            this.portletContainer.doRender(portletWindow, parameterRequestWrapper, contentRedirectingHttpServletResponse);
            contentRedirectingHttpServletResponse.flushBuffer();
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing action. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing action on portlet window '" + portletWindow + "'. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing action. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, ioe);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#getTitle(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData)
     */
    public String getTitle(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData) {
        final HttpServletRequest httpServletRequest = portalControlStructures.getHttpServletRequest();
        final String title = (String)httpServletRequest.getAttribute(IPortletAdaptor.ATTRIBUTE__PORTLET_TITLE);
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Retrieved title '" + title + "' from request for: " + channelStaticData);
        }
        
        return title;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#portalEvent(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.PortalEvent)
     */
    public void portalEvent(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, PortalEvent portalEvent) {
        switch (portalEvent.getEventNumber()) {
            case PortalEvent.SESSION_DONE: {
                //TODO invalidate portlet's session
            }
            break;
            
            case PortalEvent.UNSUBSCRIBE: {
                final String channelSubscribeId = channelStaticData.getChannelSubscribeId();
                final IPerson person = channelStaticData.getPerson();
                final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(channelSubscribeId, person.getID());
                
                //TODO delete portlet windows for entity from the windowRegistry since there is no cascade from entity to window?
                
                this.portletEntityRegistry.deletePortletEntity(portletEntity);
            }
            break;
            
            //All of these events require the portlet re-render so the portlet parameter
            //manager is notified of the render request targing the portlet
            case PortalEvent.MINIMIZE_EVENT:
            case PortalEvent.NORMAL_EVENT:
            case PortalEvent.MAXIMIZE_EVENT:
            case PortalEvent.EDIT_BUTTON_EVENT:
            case PortalEvent.HELP_BUTTON_EVENT:
            case PortalEvent.ABOUT_BUTTON_EVENT: {
                final HttpServletRequest httpServletRequest = portalControlStructures.getHttpServletRequest();
                final IPortletWindowId portletWindowId = this.getPortletWindowId(channelStaticData);
                final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
                
                // If the portlet doesn't have request info associated with it add some to ensure it is re-rendered
                IPortletWindowId targetedPortletWindowId = null;
                try {
                    targetedPortletWindowId = this.portletRequestParameterManager.getTargetedPortletWindowId(httpServletRequest);
                }
                catch (RequestParameterProcessingIncompleteException rppie) {
                    //OK, processing isn't complete yet so just assume it isn't targeted
                }
                
                if (targetedPortletWindowId == null) {
                    this.portletRequestParameterManager.setRequestInfo(httpServletRequest, portletWindowId, new PortletRequestInfo(RequestType.RENDER));
                }
                else if (!portletWindowId.equals(targetedPortletWindowId)) {
                    this.logger.warn("A PortalEvent targeting portlet window id '" + portletWindowId + "' was made but this request already targets portlet window id '" + targetedPortletWindowId + "'. The event will be handled but the portlet may not re-render due to caching.");
                }

                switch (portalEvent.getEventNumber()) {
                    case PortalEvent.MINIMIZE_EVENT: {
                        portletWindow.setWindowState(WindowState.MINIMIZED);
                    }
                    break;
                    case PortalEvent.NORMAL_EVENT: {
                        portletWindow.setWindowState(WindowState.NORMAL);
                    }
                    break;
                    case PortalEvent.MAXIMIZE_EVENT: {
                        portletWindow.setWindowState(WindowState.MAXIMIZED);
                    }
                    break;
                    case PortalEvent.EDIT_BUTTON_EVENT: {
                        portletWindow.setPortletMode(PortletMode.EDIT);
                    }
                    break;
                    case PortalEvent.HELP_BUTTON_EVENT: {
                        portletWindow.setPortletMode(PortletMode.HELP);
                    }
                    break;
                    case PortalEvent.ABOUT_BUTTON_EVENT: {
                        portletWindow.setPortletMode(IPortletAdaptor.ABOUT);
                    }
                    break;
                }
            }
            break;
            
            default: {
                this.logger.info("Don't know how to handle event of type: " + portalEvent.getEventName() + "(" + portalEvent.getEventNumber() + ")");
            }
            break;
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#prepareForRefresh(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData)
     */
    public void prepareForRefresh(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData) {
        final HttpServletRequest httpServletRequest = portalControlStructures.getHttpServletRequest();
        final IPortletWindowId portletWindowId = this.getPortletWindowId(channelStaticData);
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        portletWindow.setRequestParameters(null);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#prepareForReset(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData)
     */
    public void prepareForReset(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData) {
        final HttpServletRequest httpServletRequest = portalControlStructures.getHttpServletRequest();
        final IPortletWindowId portletWindowId = this.getPortletWindowId(channelStaticData);
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        portletWindow.setWindowState(WindowState.NORMAL);
        portletWindow.setPortletMode(PortletMode.VIEW);
        portletWindow.setRequestParameters(null);
        portletWindow.setExpirationCache(null);
        
        final HttpServletResponse httpServletResponse = portalControlStructures.getHttpServletResponse();
        final StringWriter initResultsOutput = new StringWriter();
        final ContentRedirectingHttpServletResponse contentRedirectingHttpServletResponse = new ContentRedirectingHttpServletResponse(httpServletResponse, new PrintWriter(initResultsOutput));
        
        try {
            this.portletSessionActionManager.clear(portletWindow, httpServletRequest, contentRedirectingHttpServletResponse);
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing admin command to clear session. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing admin command to clear session on portlet window '" + portletWindow + "'. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing admin command to clear session. " + this.getChannelLogInfo(channelStaticData, portletWindow), portletWindow, ioe);
        }
        
        final StringBuffer initResults = initResultsOutput.getBuffer();
        if (initResults.length() > 0) {
            throw new PortletLoadFailureException("Content was written to response during reset of portlet window '" + portletWindow + "' with " + this.getChannelLogInfo(channelStaticData, portletWindow) + ". Response Content: " + initResults, portletWindow);
        }
    }
    
    /**
     * Build a 'nice' summary string of relavent ChannelStaticData information
     */
    protected String getChannelLogInfo(ChannelStaticData channelStaticData, IPortletWindow portletWindow) {
        final IPerson person = channelStaticData.getPerson();
        return  "[channelPublishId=" + channelStaticData.getChannelPublishId() + ", " +
                "channelSubscribeId=" + channelStaticData.getChannelSubscribeId() + ", " +
                "portletApplicationId=" + portletWindow.getContextPath() + ", " +
                "portletName=" + portletWindow.getPortletName() + ", " +
                "user=" + person.getAttribute(IPerson.USERNAME) + "]";
    }
}
