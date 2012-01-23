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

package org.jasig.portal.rendering;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.IPortletExecutionManager;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.url.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Controller
@RequestMapping(value = "/**")
public class PortalController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private IPortalRenderingPipeline portalRenderingPipeline;
    private IPortletExecutionManager portletExecutionManager;
    private IPortalUrlProvider portalUrlProvider;
    private IUrlSyntaxProvider urlSyntaxProvider;
    
    @Autowired
    @Qualifier("main")
    public void setPortalRenderingPipeline(IPortalRenderingPipeline portalRenderingPipeline) {
        this.portalRenderingPipeline = portalRenderingPipeline;
    }
    @Autowired
    public void setPortletExecutionManager(IPortletExecutionManager portletExecutionManager) {
        this.portletExecutionManager = portletExecutionManager;
    }
    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }
    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }
    
    @RequestMapping(headers={"org.jasig.portal.url.UrlType=RENDER", "org.jasig.portal.url.UrlState.EXCLUSIVE=true"})
    public void renderExclusive(HttpServletRequest request, HttpServletResponse response) {
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        final IPortletRequestInfo portletRequestInfo = portalRequestInfo.getTargetedPortletRequestInfo();
        
        if (portletRequestInfo == null) {
            throw new IllegalArgumentException("A portlet must be targeted when using the EXCLUSIVE WindowState: " + portalRequestInfo);
        }
        
        final IPortletWindowId portletWindowId = portletRequestInfo.getPortletWindowId();
        this.portletExecutionManager.getPortletOutput(portletWindowId, request, response);
    }
    
    @RequestMapping(headers={"org.jasig.portal.url.UrlType=RENDER"})
    public void renderRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.portalRenderingPipeline.renderState(request, response);
    }
    
    @RequestMapping(headers={"org.jasig.portal.url.UrlType=ACTION"})
    public void actionRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        final IPortletRequestInfo portletRequestInfo = portalRequestInfo.getTargetedPortletRequestInfo();
        
        
        final IPortalUrlBuilder actionRedirectUrl;
        if (portletRequestInfo != null) {
            final IPortletWindowId targetWindowId = portletRequestInfo.getPortletWindowId();
            actionRedirectUrl = this.portalUrlProvider.getPortalUrlBuilderByPortletWindow(request, targetWindowId, UrlType.RENDER);
        }
        else {
            final String targetedLayoutNodeId = portalRequestInfo.getTargetedLayoutNodeId();
            
            if (targetedLayoutNodeId != null) {
                actionRedirectUrl = this.portalUrlProvider.getPortalUrlBuilderByLayoutNode(request, targetedLayoutNodeId, UrlType.RENDER);
            }
            else {
                actionRedirectUrl = this.portalUrlProvider.getDefaultUrl(request);
            }
        }
        
        //Stuff the action-redirect URL builder into the request so other code can use it during request processing
        this.portalUrlProvider.convertToPortalActionUrlBuilder(request, actionRedirectUrl);

        if (portletRequestInfo != null) {
            final IPortletWindowId targetWindowId = portletRequestInfo.getPortletWindowId();
            
            
            try {
                this.portletExecutionManager.doPortletAction(targetWindowId, request, response);
            }
            catch (RuntimeException e) {
                this.logger.error("Exception thrown while executing portlet action for: " + portletRequestInfo, e);
                
                //TODO this should be a constant right?
                actionRedirectUrl.setParameter("portletActionError", targetWindowId.toString());
            }
        }
        
        sendRedirect(actionRedirectUrl, response);
    }
    
    @RequestMapping(headers={"org.jasig.portal.url.UrlType=RESOURCE"})
    public void resourceRequest(HttpServletRequest request, HttpServletResponse response) {
    	final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        final IPortletRequestInfo portletRequestInfo = portalRequestInfo.getTargetedPortletRequestInfo();
        if (portletRequestInfo != null) {
            final IPortletWindowId targetWindowId = portletRequestInfo.getPortletWindowId();
            this.portletExecutionManager.doPortletServeResource(targetWindowId, request, response);
        } else {
        	this.logger.error("portletRequestInfo was null for resourceRequest");
        }
    }
    
    private void sendRedirect(final IPortalUrlBuilder portalUurl, HttpServletResponse response) throws IOException {
        final String url = portalUurl.getUrlString();
        final String encodedUrl = response.encodeRedirectURL(url);
        response.sendRedirect(encodedUrl);
    }
}
