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
    protected static final Map<IPortletWindowId, RequestType> NO_PARAMETERS = Collections.emptyMap();


    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#getPortletRequestType(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public RequestType getPortletRequestType(HttpServletRequest request, IPortletWindowId portletId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletId, "portletId can not be null");

        final Map<IPortletWindowId, RequestType> requestTypeMap = this.getAndCheckRequestTypeMap(request);
        
        if (requestTypeMap == null) {
            return null;
        }
        
        return requestTypeMap.get(portletId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#getTargetedPortletWindowIds(javax.servlet.http.HttpServletRequest)
     */
    public Set<IPortletWindowId> getTargetedPortletWindowIds(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");

        final Map<IPortletWindowId, RequestType> requestTypeMap = this.getAndCheckRequestTypeMap(request);
        
        if (requestTypeMap == null) {
            return Collections.emptySet();
        }
        
        return Collections.unmodifiableSet(requestTypeMap.keySet());
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#isPortletTargeted(javax.servlet.http.HttpServletRequest)
     */
    public boolean isPortletTargeted(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");

        final Map<IPortletWindowId, RequestType> requestTypeMap = this.getAndCheckRequestTypeMap(request);
        
        return requestTypeMap != null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#setNoPortletRequest(javax.servlet.http.HttpServletRequest)
     */
    public void setNoPortletRequest(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");

        final Map<IPortletWindowId, RequestType> requestTypeMap = this.getRequestTypeMap(request);

        if (requestTypeMap != null) {
            throw new IllegalStateException("Cannot set no portlet parameters after setRequestType(HttpServletRequest, IPortletWindowId, RequestType) has been called.");
        }

        request.setAttribute(PORTLET_REQUEST_MAP_ATTRIBUTE, NO_PARAMETERS);

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#setRequestType(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId, org.jasig.portal.portlet.url.RequestType)
     */
    public void setRequestType(HttpServletRequest request, IPortletWindowId portletId, RequestType type) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletId, "portletId can not be null");
        Validate.notNull(type, "type can not be null");

        Map<IPortletWindowId, RequestType> requestTypeMap = this.getRequestTypeMap(request);

        if (requestTypeMap == NO_PARAMETERS) {
            throw new IllegalStateException("Cannot set request type after setNoPortletRequest(HttpServletRequest) has been called.");
        }
        else if (requestTypeMap == null) {
            requestTypeMap = new HashMap<IPortletWindowId, RequestType>();
        }

        requestTypeMap.put(portletId, type);

        request.setAttribute(PORTLET_REQUEST_MAP_ATTRIBUTE, requestTypeMap);
    }

    /**
     * Gets the Map of request types from the request, throws a {@link RequestParameterProcessingIncompleteException} if
     * no attribute exists in the request and returns null if the NO_PARAMETERS map has been set.
     * 
     * @param request Current request.
     * @return Map of portlet id to request types, null if {@link #NO_PARAMETERS} object is set.
     * @throws RequestParameterProcessingIncompleteException if no portlet parameter processing has happened for the request yet.
     */
    protected Map<IPortletWindowId, RequestType> getAndCheckRequestTypeMap(HttpServletRequest request) {
        final Map<IPortletWindowId, RequestType> requestTypeMap = this.getRequestTypeMap(request);
        
        if (requestTypeMap == null) {
            throw new RequestParameterProcessingIncompleteException("No portlet parameter processing has been completed on this request");
        }
        //Do a reference equality check against no parameters Map
        else if (requestTypeMap == NO_PARAMETERS) {
            return null;
        }
        
        return requestTypeMap;
    }

    /**
     * Get the Map of request types from the request, hiding the generics casting warning.
     */
    @SuppressWarnings("unchecked")
    protected Map<IPortletWindowId, RequestType> getRequestTypeMap(HttpServletRequest request) {
        return (Map<IPortletWindowId, RequestType>)request.getAttribute(PORTLET_REQUEST_MAP_ATTRIBUTE);
    }
}
