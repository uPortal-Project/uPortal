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

import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.PortletExecutionManager;
import org.jasig.portal.url.IBasePortalUrl;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletPortalUrl;
import org.jasig.portal.url.IPortletRequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Controller
@RequestMapping("/*")
public class RenderingController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private IPortalRenderingPipeline portalRenderingPipeline;
    private PortletExecutionManager portletExecutionManager;
    private IPortalUrlProvider portalUrlProvider;
    
    @Autowired
    public void setPortalRenderingPipeline(IPortalRenderingPipeline portalRenderingPipeline) {
        this.portalRenderingPipeline = portalRenderingPipeline;
    }
    @Autowired
    public void setPortletExecutionManager(PortletExecutionManager portletExecutionManager) {
        this.portletExecutionManager = portletExecutionManager;
    }
    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }
    
    
    @RequestMapping(headers="org.jasig.portal.url.UrlType=RENDER")
    public void renderRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.portalRenderingPipeline.renderState(request, response);
    }
    
    @RequestMapping(headers="org.jasig.portal.url.UrlType=ACTION")
    public void actionRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final IPortalRequestInfo portalRequestInfo = this.portalUrlProvider.getPortalRequestInfo(request);
        final IPortletRequestInfo portletRequestInfo = portalRequestInfo.getPortletRequestInfo();
        if (portletRequestInfo != null) {
            final IPortletWindowId targetWindowId = portletRequestInfo.getTargetWindowId();
            try {
                this.portletExecutionManager.doPortletAction(targetWindowId, request, response);
            }
            catch (RuntimeException e) {
                this.logger.error("Exception thrown while executing portlet action for: " + portletRequestInfo, e);
                
                final IPortletPortalUrl portletUrl = this.portalUrlProvider.getPortletUrl(TYPE.RENDER, request, targetWindowId);
                portletUrl.setPortalParameter("portletActionError", targetWindowId.toString());
                
                sendRedirect(portletUrl, response);
            }
        }
        else {
            final String targetedLayoutNodeId = portalRequestInfo.getTargetedLayoutNodeId();
            
            final IBasePortalUrl portalUrl;
            if (targetedLayoutNodeId != null) {
                portalUrl = this.portalUrlProvider.getFolderUrlByNodeId(request, targetedLayoutNodeId);
            }
            else {
                portalUrl = this.portalUrlProvider.getDefaultUrl(request);
            }
            
            sendRedirect(portalUrl, response);
        }
    }
    private void sendRedirect(final IBasePortalUrl portalUurl, HttpServletResponse response) throws IOException {
        final String url = portalUurl.getUrlString();
        final String encodedUrl = response.encodeRedirectURL(url);
        response.sendRedirect(encodedUrl);
    }
}
