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

package org.jasig.portal.portlet.url;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException;
import org.jasig.portal.url.support.ChannelRequestParameterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages access to portlet request parameters using a request attribute.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletRequestParameterManager implements IPortletRequestParameterManager {
    protected static final String NO_PORTLET_URL_ATTRIBUTE = ChannelRequestParameterManager.class.getName() + ".NO_PORTLET_URL";
    protected static final String TARGETED_PORTLET_URL_ATTRIBUTE = ChannelRequestParameterManager.class.getName() + ".TARGETED_PORTLET_URL";
    protected static final String PORTLET_URL_MAP_ATTRIBUTE = ChannelRequestParameterManager.class.getName() + ".PORTLET_URL_MAP    ";
    
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
    @Autowired(required=true)
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
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
        
        //Do this just to check that processing is complete
        this.getAndCheckRequestInfoMap(request);
        
        final Map<IPortletWindowId, PortletUrl> portletUrlMap = (Map<IPortletWindowId, PortletUrl>)request.getAttribute(PORTLET_URL_MAP_ATTRIBUTE);
        if (portletUrlMap == null) {
            return null;
        }
        
        return portletUrlMap.get(portletWindowId);
    }
    
    @Override
    public void setTargetedPortletUrl(HttpServletRequest request, PortletUrl portletUrl) {
        Validate.notNull(request, "request can not be null");
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        
        final PortletUrl existingPortletUrl = (PortletUrl)request.getAttribute(TARGETED_PORTLET_URL_ATTRIBUTE);
        if (existingPortletUrl != null) {
            throw new IllegalStateException("Portlet request info can only be set once per request");
        }
        
        if (portletUrl != null) {
            request.setAttribute(TARGETED_PORTLET_URL_ATTRIBUTE, portletUrl);
            this.setAdditionalPortletUrl(request, portletUrl);
        }
        else {
            request.setAttribute(NO_PORTLET_URL_ATTRIBUTE, NO_PORTLET_URL_ATTRIBUTE);
        }
    }
    
    @Override
    public void setAdditionalPortletUrl(HttpServletRequest request, PortletUrl portletUrl) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletUrl, "portletUrl can not be null");
        
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        
        Map<IPortletWindowId, PortletUrl> portletUrlMap = (Map<IPortletWindowId, PortletUrl>)request.getAttribute(PORTLET_URL_MAP_ATTRIBUTE);
        if (portletUrlMap == null) {
            portletUrlMap = new ConcurrentHashMap<IPortletWindowId, PortletUrl>();
            request.setAttribute(PORTLET_URL_MAP_ATTRIBUTE, portletUrlMap);
        }
        
        do {
            portletUrlMap.put(portletUrl.getTargetWindowId(), portletUrl);
            portletUrl = portletUrl.getDelegatePortletUrl();
        } while (portletUrl != null);
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
        final PortletUrl portletUrl = (PortletUrl)request.getAttribute(TARGETED_PORTLET_URL_ATTRIBUTE);
        
        if (portletUrl == null && request.getAttribute(NO_PORTLET_URL_ATTRIBUTE) == null) {
            throw new RequestParameterProcessingIncompleteException("No portlet parameter processing has been completed on this request");
        }
        
        return portletUrl;
    }
}
