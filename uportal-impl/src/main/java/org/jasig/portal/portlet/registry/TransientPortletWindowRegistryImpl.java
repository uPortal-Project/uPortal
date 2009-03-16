/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.registry;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.utils.web.PortalWebUtils;

/**
 * Caches transient portlet window instances as request attributes, passes all operations on non-transient windows to the
 * parent {@link PortletWindowRegistryImpl}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TransientPortletWindowRegistryImpl extends PortletWindowRegistryImpl implements ITransientPortletWindowRegistry {
    public static final String TRANSIENT_WINDOW_ID_PREFIX = "tp.";
    public static final String TRANSIENT_PORTLET_WINDOW_MAP_ATTRIBUTE = TransientPortletWindowRegistryImpl.class.getName() + ".TRANSIENT_PORTLET_WINDOW_MAP";
    
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
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getPortletWindow(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    @Override
    public IPortletWindow getPortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        final String windowInstanceId = portletWindowId.getStringId();
        if (windowInstanceId.startsWith(TRANSIENT_WINDOW_ID_PREFIX)) {
            final String portletEntityIdString = windowInstanceId.substring(TRANSIENT_WINDOW_ID_PREFIX.length());
            
            final IPortletEntityRegistry portletEntityRegistry = this.getPortletEntityRegistry();
            final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityIdString);
            
            return this.getTransientPortletWindow(request, windowInstanceId, portletEntity.getPortletEntityId());
        }
        
        return super.getPortletWindow(request, portletWindowId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getPortletWindow(javax.servlet.http.HttpServletRequest, java.lang.String, org.jasig.portal.portlet.om.IPortletEntityId)
     */
    @Override
    public IPortletWindow getPortletWindow(HttpServletRequest request, String windowInstanceId, IPortletEntityId portletEntityId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(windowInstanceId, "windowInstanceId can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        if (windowInstanceId.startsWith(TRANSIENT_WINDOW_ID_PREFIX)) {
            return this.getTransientPortletWindow(request, windowInstanceId, portletEntityId);
        }

        return super.getPortletWindow(request, windowInstanceId, portletEntityId);
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
                transientPortletWindow = this.createPortletWindow(portletWindowId, portletEntityId);
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