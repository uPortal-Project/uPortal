/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException;
import org.jasig.portal.url.support.ChannelRequestParameterManager;

/**
 * Manages access to portlet request parameters using a request attribute.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRequestParameterManager implements IPortletRequestParameterManager {
    protected static final String PORTLET_REQUEST_MAP_ATTRIBUTE = ChannelRequestParameterManager.class.getName() + ".PORTLET_REQUEST_MAP";
    protected static final Map<IPortletWindowId, PortletRequestInfo> NO_PARAMETERS = Collections.emptyMap();


    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#getPortletRequestInfo(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public PortletRequestInfo getPortletRequestInfo(HttpServletRequest request, IPortletWindowId portletId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletId, "portletId can not be null");

        final Map<IPortletWindowId, PortletRequestInfo> requestInfoMap = this.getAndCheckRequestInfoMap(request);
        
        if (requestInfoMap == null) {
            return null;
        }
        
        return requestInfoMap.get(portletId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#getTargetedPortletWindowIds(javax.servlet.http.HttpServletRequest)
     */
    public Set<IPortletWindowId> getTargetedPortletWindowIds(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");

        final Map<IPortletWindowId, PortletRequestInfo> requestInfoMap = this.getAndCheckRequestInfoMap(request);
        
        if (requestInfoMap == null) {
            return Collections.emptySet();
        }
        
        return Collections.unmodifiableSet(requestInfoMap.keySet());
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#isPortletTargeted(javax.servlet.http.HttpServletRequest)
     */
    public boolean isAnyPortletTargeted(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");

        final Map<IPortletWindowId, PortletRequestInfo> requestInfoMap = this.getAndCheckRequestInfoMap(request);
        
        return requestInfoMap != null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#setNoPortletRequest(javax.servlet.http.HttpServletRequest)
     */
    public void setNoPortletRequest(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");

        final Map<IPortletWindowId, PortletRequestInfo> requestInfoMap = this.getRequestInfoMap(request);

        if (requestInfoMap != null) {
            throw new IllegalStateException("Cannot set no portlet parameters after setRequestType(HttpServletRequest, IPortletWindowId, PortletRequestInfo) has been called.");
        }

        request.setAttribute(PORTLET_REQUEST_MAP_ATTRIBUTE, NO_PARAMETERS);
    }

    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#setRequestInfo(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId, org.jasig.portal.portlet.url.PortletRequestInfo)
     */
    public void setRequestInfo(HttpServletRequest request, IPortletWindowId portletId, PortletRequestInfo portletRequestInfo) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletId, "portletId can not be null");
        Validate.notNull(portletRequestInfo, "portletRequestInfo can not be null");

        Map<IPortletWindowId, PortletRequestInfo> requestInfoMap = this.getRequestInfoMap(request);

        if (requestInfoMap == NO_PARAMETERS) {
            throw new IllegalStateException("Cannot set request type after setNoPortletRequest(HttpServletRequest) has been called.");
        }
        else if (requestInfoMap == null) {
            requestInfoMap = new HashMap<IPortletWindowId, PortletRequestInfo>();
        }

        requestInfoMap.put(portletId, portletRequestInfo);

        request.setAttribute(PORTLET_REQUEST_MAP_ATTRIBUTE, requestInfoMap);
    }

    /**
     * Gets the Map of request info from the request, throws a {@link RequestParameterProcessingIncompleteException} if
     * no attribute exists in the request and returns null if the NO_PARAMETERS map has been set.
     * 
     * @param request Current request.
     * @return Map of portlet id to request info, null if {@link #NO_PARAMETERS} object is set.
     * @throws RequestParameterProcessingIncompleteException if no portlet parameter processing has happened for the request yet.
     */
    protected Map<IPortletWindowId, PortletRequestInfo> getAndCheckRequestInfoMap(HttpServletRequest request) {
        final Map<IPortletWindowId, PortletRequestInfo> requestInfoMap = this.getRequestInfoMap(request);
        
        if (requestInfoMap == null) {
            throw new RequestParameterProcessingIncompleteException("No portlet parameter processing has been completed on this request");
        }
        //Do a reference equality check against no parameters Map
        else if (requestInfoMap == NO_PARAMETERS) {
            return null;
        }
        
        return requestInfoMap;
    }

    /**
     * Get the Map of request info from the request, hiding the generics casting warning.
     */
    @SuppressWarnings("unchecked")
    protected Map<IPortletWindowId, PortletRequestInfo> getRequestInfoMap(HttpServletRequest request) {
        return (Map<IPortletWindowId, PortletRequestInfo>)request.getAttribute(PORTLET_REQUEST_MAP_ATTRIBUTE);
    }
}
