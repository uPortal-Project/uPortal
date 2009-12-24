/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
import org.springframework.beans.factory.annotation.Required;

/**
 * Manages access to portlet request parameters using a request attribute.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRequestParameterManager implements IPortletRequestParameterManager {
    protected static final String NO_PORTLET_URL_ATTRIBUTE = ChannelRequestParameterManager.class.getName() + ".NO_PORTLET_URL";
    protected static final String PORTLET_URL_ATTRIBUTE = ChannelRequestParameterManager.class.getName() + ".PORTLET_URL";
    
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
     * @see org.jasig.portal.portlet.url.IPortletRequestParameterManager#getTargetedPortletWindowId(javax.servlet.http.HttpServletRequest)
     */
    public IPortletWindowId getTargetedPortletWindowId(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);

        final PortletUrl portletUrl = this.getAndCheckRequestInfoMap(request);
        if (portletUrl == null) {
            return null;
        }
        
        return portletUrl.getTargetWindowId();
    }
    
    @Override
    public PortletUrl getPortletRequestInfo(HttpServletRequest request, IPortletWindowId portletWindowId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);

        PortletUrl portletUrl = this.getAndCheckRequestInfoMap(request);
        if (portletUrl == null) {
            return null;
        }
        
        while (portletUrl != null) {
            if (portletWindowId.equals(portletUrl.getTargetWindowId())) {
                return portletUrl;
            }
            
            portletUrl = portletUrl.getDelegatePortletUrl();
        }
        
        return null;
    }
    
    @Override
    public void setRequestInfo(HttpServletRequest request, PortletUrl portletUrl) {
        Validate.notNull(request, "request can not be null");
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        
        final PortletUrl existingPortletUrl = (PortletUrl)request.getAttribute(PORTLET_URL_ATTRIBUTE);
        if (existingPortletUrl != null) {
            throw new IllegalStateException("Portlet request info can only be set once per request");
        }
        
        if (portletUrl != null) {
            request.setAttribute(PORTLET_URL_ATTRIBUTE, portletUrl);
        }
        else {
            request.setAttribute(NO_PORTLET_URL_ATTRIBUTE, NO_PORTLET_URL_ATTRIBUTE);
        }
    }
    
    /**
     * Gets the List of urls from the request, throws a {@link RequestParameterProcessingIncompleteException} if
     * no attribute exists in the request and returns null if the List is empty
     * 
     * @param request Current request.
     * @return List of PortletUrls, null if the List is empty
     * @throws RequestParameterProcessingIncompleteException if no portlet parameter processing has happened for the request yet.
     */
    protected PortletUrl getAndCheckRequestInfoMap(HttpServletRequest request) {
        final PortletUrl portletUrl = (PortletUrl)request.getAttribute(PORTLET_URL_ATTRIBUTE);
        
        if (portletUrl == null && request.getAttribute(NO_PORTLET_URL_ATTRIBUTE) == null) {
            throw new RequestParameterProcessingIncompleteException("No portlet parameter processing has been completed on this request");
        }
        
        return portletUrl;
    }
}
