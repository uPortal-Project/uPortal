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

package org.jasig.portal.portlet.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.PortletWindowID;
import org.apache.pluto.internal.InternalPortletWindow;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.WebUtils;

/**
 * Provides the default implementation of the window registry, the backing for the storage
 * of IPortletWindow objects is a Map stored in the HttpSession for the user.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletWindowRegistryImpl implements IPortletWindowRegistry {
    public static final String TRANSIENT_WINDOW_ID_PREFIX = "tp.";
    public static final String TRANSIENT_PORTLET_WINDOW_MAP_ATTRIBUTE = PortletWindowRegistryImpl.class.getName() + ".TRANSIENT_PORTLET_WINDOW_MAP";
    public static final String PORTLET_WINDOW_MAP_ATTRIBUTE = PortletWindowRegistryImpl.class.getName() + ".PORTLET_WINDOW_MAP";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IUserInstanceManager userInstanceManager;
    
    /**
     * @return the userInstanceManager
     */
    public IUserInstanceManager getUserInstanceManager() {
        return this.userInstanceManager;
    }
    /**
     * @param userInstanceManager the userInstanceManager to set
     */
    @Required
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        Validate.notNull(userInstanceManager);
        this.userInstanceManager = userInstanceManager;
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
        Validate.notNull(portletEntityRegistry);
        this.portletEntityRegistry = portletEntityRegistry;
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
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#convertPortletWindow(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    public IPortletWindow convertPortletWindow(HttpServletRequest request, PortletWindow plutoPortletWindow) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(plutoPortletWindow, "portletWindow can not be null");
        
        //If a pluto InternalPortletWindow, unwrap to the original PorteltWindow
        if (plutoPortletWindow instanceof InternalPortletWindow) {
            plutoPortletWindow = ((InternalPortletWindow)plutoPortletWindow).getOriginalPortletWindow();
        }
        
        //Try a direct cast to IPortletWindow
        if (plutoPortletWindow instanceof IPortletWindow) {
            return (IPortletWindow)plutoPortletWindow;
        }
        
        //Try converting the Pluto ID to a uPortal ID
        final PortletWindowID plutoPortletWindowId = plutoPortletWindow.getId();
        final IPortletWindowId portletWindowId;
        if (plutoPortletWindowId instanceof IPortletWindowId) {
            portletWindowId = (IPortletWindowId)plutoPortletWindowId;
        }
        else {
            portletWindowId = this.getPortletWindowId(plutoPortletWindowId.getStringId());
        }
        
        //Use the converted ID to see if a IPortletWindow exists for it
        final IPortletWindow portletWindow = this.getPortletWindow(request, portletWindowId);
        
        //If null no window exists, throw an exception since somehow Pluto has a PortletWindow object that this container doesn't know about
        if (portletWindow == null) {
            throw new IllegalArgumentException("Could not cast Pluto PortletWindow to uPortal IPortletWindow and no IPortletWindow exists with the id: " + plutoPortletWindow.getId());
        }
        
        return portletWindow;
    }
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#createPortletWindow(javax.servlet.http.HttpServletRequest, java.lang.String, org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletWindow createPortletWindow(HttpServletRequest request, String windowInstanceId, IPortletEntityId portletEntityId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(windowInstanceId, "windowInstanceId can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");

        //Create the window
        final IPortletWindowId portletWindowId = this.createPortletWindowId(windowInstanceId, portletEntityId);
        final IPortletWindow portletWindow = this.createPortletWindow(request, portletWindowId, portletEntityId);
        
        //Store it in the request
        this.storePortletWindow(request, portletWindow);
        
        return portletWindow;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getOrCreatePortletWindow(javax.servlet.http.HttpServletRequest, java.lang.String, org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletWindow getOrCreatePortletWindow(HttpServletRequest request, String windowInstanceId, IPortletEntityId portletEntityId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(windowInstanceId, "windowInstanceId can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        //Try the get
        IPortletWindow portletWindow = this.getPortletWindow(request, windowInstanceId, portletEntityId);
        
        //If nothing returned by the get create a new one.
        if (portletWindow == null) {
            portletWindow = this.createPortletWindow(request, windowInstanceId, portletEntityId);
        }
        
        return portletWindow;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getPortletWindow(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public IPortletWindow getPortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        if (this.isTransient(request, portletWindowId)) {
            final String portletWindowIdString = portletWindowId.getStringId();
            final String portletEntityIdString = portletWindowIdString.substring(TRANSIENT_WINDOW_ID_PREFIX.length());
            
            final IPortletEntityRegistry portletEntityRegistry = this.getPortletEntityRegistry();
            final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityIdString);
            if (portletEntity == null) {
                logger.warn("No IPortletEntity could be found for ID '" + portletEntityIdString + "' from transient window: " + portletWindowId);
                return null;
            }
            
            return this.getTransientPortletWindow(request, portletWindowIdString, portletEntity.getPortletEntityId());
        }
        
        final Map<IPortletWindowId, IPortletWindow> portletWindowMap = this.getPortletWindowMap(request);
        if (portletWindowMap == null) {
            return null;
        }
        
        return portletWindowMap.get(portletWindowId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getPortletWindow(javax.servlet.http.HttpServletRequest, java.lang.String, org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletWindow getPortletWindow(HttpServletRequest request, String windowInstanceId, IPortletEntityId portletEntityId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(windowInstanceId, "windowInstanceId can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        if (windowInstanceId.startsWith(TRANSIENT_WINDOW_ID_PREFIX)) {
            return this.getTransientPortletWindow(request, windowInstanceId, portletEntityId);
        }
        
        final IPortletWindowId portletWindowId = this.createPortletWindowId(windowInstanceId, portletEntityId);
        return this.getPortletWindow(request, portletWindowId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getPortletWindowId(java.lang.String)
     */
    public IPortletWindowId getPortletWindowId(String portletWindowId) {
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        return new PortletWindowIdImpl(portletWindowId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getDefaultPortletWindowId(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletWindowId getDefaultPortletWindowId(IPortletEntityId portletEntityId) {
        final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(portletEntityId);
        final String channelSubscribeId = portletEntity.getChannelSubscribeId();
        return this.createPortletWindowId(channelSubscribeId, portletEntityId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getParentPortletEntity(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public IPortletEntity getParentPortletEntity(HttpServletRequest request, IPortletWindowId portletWindowId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        final IPortletWindow portletWindow = this.getPortletWindow(request, portletWindowId);
        if (portletWindow == null) {
            return null;
        }
        
        final IPortletEntityId parentPortletEntityId = portletWindow.getPortletEntityId();
        final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(parentPortletEntityId);
        return portletEntity;
    }
    
    public IPortletWindow createDefaultPortletWindow(HttpServletRequest request, IPortletEntityId portletEntityId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");

        //Create the window
        final IPortletWindowId portletWindowId = this.getDefaultPortletWindowId(portletEntityId);
        final IPortletWindow portletWindow = this.createPortletWindow(request, portletWindowId, portletEntityId);
        
        //Store it in the request
        this.storePortletWindow(request, portletWindow);
        
        return portletWindow;
    }
    
    public IPortletWindow getOrCreateDefaultPortletWindow(HttpServletRequest request, IPortletEntityId portletEntityId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        final IPortletWindowId portletWindowId = this.getDefaultPortletWindowId(portletEntityId);
        
        //Try the get
        IPortletWindow portletWindow = this.getPortletWindow(request, portletWindowId);
        
        //If nothing returned by the get create a new one.
        if (portletWindow == null) {
            portletWindow = this.createDefaultPortletWindow(request, portletEntityId);
        }
        
        return portletWindow;
    }
    
    public IPortletWindow createDelegatePortletWindow(HttpServletRequest request, IPortletEntityId portletEntityId, IPortletWindowId delegationParentId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        Validate.notNull(delegationParentId, "delegationParentId can not be null");

        //Create the window
        final IPortletWindowId portletWindowId = this.getDefaultPortletWindowId(portletEntityId);
        final IPortletWindow portletWindow = this.createPortletWindow(request, portletWindowId, portletEntityId, delegationParentId);
        
        //Store it in the request
        this.storePortletWindow(request, portletWindow);
        
        return portletWindow;
    }
    
    @Override
    public IPortletWindow getOrCreateDelegatePortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId, IPortletEntityId portletEntityId, IPortletWindowId delegationParentId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        Validate.notNull(delegationParentId, "delegationParentId can not be null");

        //Try the get
        IPortletWindow portletWindow = this.getPortletWindow(request, portletWindowId);
        
        if (portletWindow != null && (portletWindow.getDelegationParent() == null || !portletWindow.getDelegationParent().equals(delegationParentId))) {
            throw new IllegalArgumentException("Delegation parent '" + delegationParentId + "' is not set as the parent for the existing portle window '" + portletWindow + "'");
        }
        
        //If nothing returned by the get create a new one.
        if (portletWindow == null) {
            portletWindow = this.createDelegatePortletWindow(request, portletEntityId, delegationParentId);
        }
        
        return portletWindow;
    }
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.ITransientPortletWindowRegistry#createTransientPortletWindowId(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public IPortletWindowId createTransientPortletWindowId(HttpServletRequest request, IPortletWindowId sourcePortletWindowId) {
        final IPortletWindow sourcePortletWindow = this.getPortletWindow(request, sourcePortletWindowId);
        if (sourcePortletWindow == null) {
            throw new IllegalArgumentException("No IPortletWindow exists for id: " + sourcePortletWindowId);
        }
        
        final IPortletEntityId portletEntityId = sourcePortletWindow.getPortletEntityId();
        
        //Build the transient ID from the prefix and the entity ID
        return new PortletWindowIdImpl(TRANSIENT_WINDOW_ID_PREFIX + portletEntityId.getStringId());
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.ITransientPortletWindowRegistry#isTransient(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public boolean isTransient(HttpServletRequest request, IPortletWindowId portletWindowId) {
        final String windowInstanceId = portletWindowId.getStringId();
        return windowInstanceId.startsWith(TRANSIENT_WINDOW_ID_PREFIX);
    }
    
    
    /**
     * @see #createPortletWindow(IPortletWindowId, IPortletEntityId, IPortletWindowId)
     */
    protected IPortletWindow createPortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId, IPortletEntityId portletEntityId) {
        return this.createPortletWindow(request, portletWindowId, portletEntityId, null);
    }
    
    /**
     * Creates a new {@link IPortletWindow} for the specified window ID and entity ID.
     * 
     * @param windowInstanceId The window instance id.
     * @param portletEntityId The parent entity id.
     * @param delegateParent The id of the parent window delegating to this window, optional.
     * @return A new portlet window
     */
    protected IPortletWindow createPortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId, IPortletEntityId portletEntityId, IPortletWindowId delegateParent) {
        //Get the parent definition to determine the descriptor data
        final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletEntityId);
        final Tuple<String, String> portletDescriptorKeys = this.portletDefinitionRegistry.getPortletDescriptorKeys(portletDefinition);

        final String portletApplicationId = portletDescriptorKeys.first;
        final String portletName = portletDescriptorKeys.second;

        final PortletWindowImpl portletWindow;
        if (delegateParent == null) {
            portletWindow = new PortletWindowImpl(portletWindowId, portletEntityId, portletApplicationId, portletName);
        }
        else {
            portletWindow = new PortletWindowImpl(portletWindowId, portletEntityId, portletApplicationId, portletName, delegateParent);
        }
        
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Created PortletWindow " + portletWindow.getId() + " for PortletEntity " + portletEntityId);
        }
        
        this.initializePortletWindow(request, portletEntityId, portletWindow);
        
        return portletWindow;
    }

    /**
     * Initializes a newly created {@link PortletWindow}, the default implementation sets up the appropriate
     * {@link WindowState} and {@link javax.portlet.PortletMode}
     */
    protected void initializePortletWindow(HttpServletRequest request, IPortletEntityId portletEntityId, PortletWindowImpl portletWindow) {
        if (this.isTransient(request, portletWindow.getPortletWindowId())) {
            return;
        }
        
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final UserPreferences userPreferences = preferencesManager.getUserPreferences();
        final ThemeStylesheetUserPreferences themeStylesheetUserPreferences = userPreferences.getThemeStylesheetUserPreferences();
        
        final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(portletEntityId);
        final String channelSubscribeId = portletEntity.getChannelSubscribeId();
        final String minimized = themeStylesheetUserPreferences.getChannelAttributeValue(channelSubscribeId, "minimized");
        
        if (Boolean.parseBoolean(minimized)) {
            portletWindow.setWindowState(WindowState.MINIMIZED);
        }
    }
    
    /**
     * Generates a new, unique, portlet window ID for the window instance ID & entity id.
     * 
     * @param windowInstanceId The window instance id.
     * @param portletEntityId The parent entity id.
     * @return A portlet window id for the parameters.
     */
    protected IPortletWindowId createPortletWindowId(String windowInstanceId, IPortletEntityId portletEntityId) {
        return new PortletWindowIdImpl(portletEntityId.getStringId() + "." + windowInstanceId);
    }

    /**
     * Get the Map of IPortletWindows for the request.
     * 
     * @param request the current request
     * @return The Map of IPortletWindows managed by this class for the request, null if that Map does not yet exist.
     */
    @SuppressWarnings("unchecked")
    protected ConcurrentMap<IPortletWindowId, IPortletWindow> getPortletWindowMap(HttpServletRequest request) {
        final HttpSession session = this.getSession(request);
        
        final ConcurrentMap<IPortletWindowId, IPortletWindow> portletWindows;
        //Sync on the session to ensure the Map isn't in the process of being created
        synchronized (WebUtils.getSessionMutex(session)) {
            portletWindows = (ConcurrentMap<IPortletWindowId, IPortletWindow>)session.getAttribute(PORTLET_WINDOW_MAP_ATTRIBUTE);
        }
        return portletWindows;
    }
    
    /**
     * @param request
     * @param portletWindow
     */
    @SuppressWarnings("unchecked")
    protected void storePortletWindow(HttpServletRequest request, IPortletWindow portletWindow) {
        final HttpSession session = this.getSession(request);
        
        Map<IPortletWindowId, IPortletWindow> portletWindows;
        //Sync on the session to ensure other threads aren't creating the Map at the same time
        synchronized (WebUtils.getSessionMutex(session)) {
            portletWindows = (Map<IPortletWindowId, IPortletWindow>)session.getAttribute(PORTLET_WINDOW_MAP_ATTRIBUTE);
            if (portletWindows == null) {
                portletWindows = new ConcurrentHashMap<IPortletWindowId, IPortletWindow>();
                session.setAttribute(PORTLET_WINDOW_MAP_ATTRIBUTE, portletWindows);
            }
        }
        
        portletWindows.put(portletWindow.getPortletWindowId(), portletWindow);
    }

    /**
     * Gets the session for the request.
     * 
     * @param request The current request
     * @return The session for the current request, will not return null.
     */
    protected HttpSession getSession(HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalStateException("A HttpSession must already exist for the PortletWindowRegistryImpl to function");
        }
        return session;
    }
    
    @SuppressWarnings("unchecked")
    protected IPortletWindow getTransientPortletWindow(HttpServletRequest request, String windowInstanceId, IPortletEntityId portletEntityId) {
        //Get/create the map from the request attributes with all of the transient portlet windows in it (can there ever be more than one per request?)
        Map<IPortletEntityId, IPortletWindow> transientPortletWindowMap;
        synchronized (PortalWebUtils.getRequestAttributeMutex(request)) {
            transientPortletWindowMap = (Map<IPortletEntityId, IPortletWindow>)request.getAttribute(TRANSIENT_PORTLET_WINDOW_MAP_ATTRIBUTE);
            if (transientPortletWindowMap == null) {
                transientPortletWindowMap = new HashMap<IPortletEntityId, IPortletWindow>();
                request.setAttribute(TRANSIENT_PORTLET_WINDOW_MAP_ATTRIBUTE, transientPortletWindowMap);
            }
        }
        
        //Get/create the transient portlet window
        IPortletWindow transientPortletWindow;
        synchronized (transientPortletWindowMap) {
            transientPortletWindow = transientPortletWindowMap.get(portletEntityId);
            if (transientPortletWindow == null) {
                final PortletWindowIdImpl portletWindowId = new PortletWindowIdImpl(windowInstanceId);
                transientPortletWindow = this.createPortletWindow(request, portletWindowId, portletEntityId);
                transientPortletWindow.setWindowState(WindowState.NORMAL);
                transientPortletWindowMap.put(portletEntityId, transientPortletWindow);
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Created new transient portlet window and cached it as a request attribute: " + transientPortletWindow);
                }
            }
            else {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Using cached transient portlet window: " + transientPortletWindow);
                }
            }
        }
        
        return transientPortletWindow;
    }
}
