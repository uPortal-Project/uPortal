/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException;
import org.jasig.portal.url.support.ChannelRequestParameterManager;
import org.jasig.portal.utils.Tuple;
import org.springframework.beans.factory.annotation.Required;

/**
 * Manages access to portlet request parameters using a request attribute.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRequestParameterManager implements IPortletRequestParameterManager {
    protected static final String PORTLET_REQUEST_MAP_ATTRIBUTE = ChannelRequestParameterManager.class.getName() + ".PORTLET_REQUEST_MAP";
    protected static final Tuple<IPortletWindowId, PortletRequestInfo> NO_PARAMETERS = new Tuple<IPortletWindowId, PortletRequestInfo>(null, null);
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortalRequestUtils portalRequestUtils;
    
    
    /**
     * @return the portalRequestUtils
     */
    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Required
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#getPortletRequestInfo(javax.servlet.http.HttpServletRequest)
     */
    public PortletRequestInfo getPortletRequestInfo(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);

        final Tuple<IPortletWindowId, PortletRequestInfo> requestInfoMap = this.getAndCheckRequestInfoMap(request);
        
        if (requestInfoMap == null) {
            return null;
        }
        
        return requestInfoMap.second;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#getTargetedPortletWindowId(javax.servlet.http.HttpServletRequest)
     */
    public IPortletWindowId getTargetedPortletWindowId(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);

        final Tuple<IPortletWindowId, PortletRequestInfo> requestInfoMap = this.getAndCheckRequestInfoMap(request);
        
        if (requestInfoMap == null) {
            return null;
        }
        
        return requestInfoMap.first;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#setNoPortletRequest(javax.servlet.http.HttpServletRequest)
     */
    public void setNoPortletRequest(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);

        final Tuple<IPortletWindowId, PortletRequestInfo> requestInfoMap = this.getRequestInfo(request);

        if (requestInfoMap != null) {
            return;
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
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);

        Tuple<IPortletWindowId, PortletRequestInfo> requestInfo = this.getRequestInfo(request);

        if (requestInfo != null && this.logger.isInfoEnabled()) {
            this.logger.info("Request info already exists '" + requestInfo + "' and will be replaced by '" + portletId + "', '" + portletRequestInfo + "'");
        }

        requestInfo = new Tuple<IPortletWindowId, PortletRequestInfo>(portletId, portletRequestInfo);
        request.setAttribute(PORTLET_REQUEST_MAP_ATTRIBUTE, requestInfo);
    }

    /**
     * Gets the Map of request info from the request, throws a {@link RequestParameterProcessingIncompleteException} if
     * no attribute exists in the request and returns null if the NO_PARAMETERS map has been set.
     * 
     * @param request Current request.
     * @return Map of portlet id to request info, null if {@link #NO_PARAMETERS} object is set.
     * @throws RequestParameterProcessingIncompleteException if no portlet parameter processing has happened for the request yet.
     */
    protected Tuple<IPortletWindowId, PortletRequestInfo> getAndCheckRequestInfoMap(HttpServletRequest request) {
        final Tuple<IPortletWindowId, PortletRequestInfo> requestInfoMap = this.getRequestInfo(request);
        
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
    protected Tuple<IPortletWindowId, PortletRequestInfo> getRequestInfo(HttpServletRequest request) {
        return (Tuple<IPortletWindowId, PortletRequestInfo>)request.getAttribute(PORTLET_REQUEST_MAP_ATTRIBUTE);
    }
}
