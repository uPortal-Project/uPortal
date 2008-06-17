/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.PortletWindowID;
import org.apache.pluto.internal.InternalPortletWindow;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.utils.Tuple;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provides the default implementation of the window registry, the backing for the storage
 * of IPortletWindow objects is a Map stored in the HttpSession for the user.
 * 
 * TODO may have to do more synchronization work in here
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletWindowRegistryImpl implements IPortletWindowRegistry {
    private static final String PORTLET_WINDOW_MAP_ATTRIBUTE = PortletWindowRegistryImpl.class.getName() + ".PORTLET_WINDOW_MAP";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    

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
        
        //Get the parent definition to determine the descriptor data
        final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletEntityId);
        final Tuple<String, String> portletDescriptorKeys = this.portletDefinitionRegistry.getPortletDescriptorKeys(portletDefinition);

        final String portletApplicationId = portletDescriptorKeys.first;
        final String portletName = portletDescriptorKeys.second;
        final IPortletWindow portletWindow = new PortletWindowImpl(portletWindowId, portletEntityId, portletApplicationId, portletName);
        
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
        
        final IPortletWindowId portletWindowId = this.createPortletWindowId(windowInstanceId, portletEntityId);
        final IPortletWindow portletWindow = this.getPortletWindow(request, portletWindowId);
        return portletWindow;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getPortletWindowId(java.lang.String)
     */
    public IPortletWindowId getPortletWindowId(String portletWindowId) {
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        return new PortletWindowIdImpl(portletWindowId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getParentPortletEntity(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public IPortletEntity getParentPortletEntity(HttpServletRequest request, IPortletWindowId portletWindowId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        final IPortletWindow portletWindow = this.getPortletWindow(request, portletWindowId);
        final IPortletEntityId parentPortletEntityId = portletWindow.getPortletEntityId();
        final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(parentPortletEntityId);
        return portletEntity;
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
    protected Map<IPortletWindowId, IPortletWindow> getPortletWindowMap(HttpServletRequest request) {
        final HttpSession session = this.getSession(request);
        
        final Map<IPortletWindowId, IPortletWindow> portletWindows;
        //Sync on the session to ensure the Map isn't in the process of being created
        synchronized (session) {
            portletWindows = (Map<IPortletWindowId, IPortletWindow>)session.getAttribute(PORTLET_WINDOW_MAP_ATTRIBUTE);
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
        synchronized (session) {
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
}
