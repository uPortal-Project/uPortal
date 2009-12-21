/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.url;

import java.util.Collections;
import java.util.List;

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
    protected static final String PORTLET_URLS_ATTRIBUTE = ChannelRequestParameterManager.class.getName() + ".PORTLET_REQUEST_MAP";
    
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

        final List<PortletUrl> portletUrls = this.getAndCheckRequestInfoMap(request);
        if (portletUrls == null) {
            return null;
        }
        
        final PortletUrl targetedPortletUrl = portletUrls.get(0);
        return targetedPortletUrl.getTargetWindowId();
    }
    
    @Override
    public PortletUrl getPortletRequestInfo(HttpServletRequest request, IPortletWindowId portletWindowId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);

        final List<PortletUrl> portletUrls = this.getAndCheckRequestInfoMap(request);
        if (portletUrls == null) {
            return null;
        }
        
        for (final PortletUrl portletUrl : portletUrls) {
            if (portletWindowId.equals(portletUrl.getTargetWindowId())) {
                return portletUrl;
            }
        }
        
        return null;
    }
    
    @Override
    public List<PortletUrl> getAllRequestInfo(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);

        final List<PortletUrl> portletUrls = this.getAndCheckRequestInfoMap(request);
        if (portletUrls == null) {
            return null;
        }
        
        return portletUrls;
    }
    
    
    @Override
    public void setRequestInfo(HttpServletRequest request, List<PortletUrl> portletUrls) {
        Validate.notNull(request, "request can not be null");
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        
        final List<PortletUrl> existingPortletUrls = (List<PortletUrl>)request.getAttribute(PORTLET_URLS_ATTRIBUTE);
        if (existingPortletUrls != null && existingPortletUrls.size() > 0) {
            throw new IllegalStateException("Portlet request info can only be set once per request");
        }
        
        if (portletUrls == null) {
            portletUrls = Collections.emptyList();
        }
        
        request.setAttribute(PORTLET_URLS_ATTRIBUTE, portletUrls);
    }
    
    /**
     * Gets the List of urls from the request, throws a {@link RequestParameterProcessingIncompleteException} if
     * no attribute exists in the request and returns null if the List is empty
     * 
     * @param request Current request.
     * @return List of PortletUrls, null if the List is empty
     * @throws RequestParameterProcessingIncompleteException if no portlet parameter processing has happened for the request yet.
     */
    protected List<PortletUrl> getAndCheckRequestInfoMap(HttpServletRequest request) {
        final List<PortletUrl> portletUrls = (List<PortletUrl>)request.getAttribute(PORTLET_URLS_ATTRIBUTE);
        
        if (portletUrls == null) {
            throw new RequestParameterProcessingIncompleteException("No portlet parameter processing has been completed on this request");
        }
        
        //Do a reference equality check against no parameters Map
        if (portletUrls.size() == 0) {
            return null;
        }
        
        return portletUrls;
    }
}
