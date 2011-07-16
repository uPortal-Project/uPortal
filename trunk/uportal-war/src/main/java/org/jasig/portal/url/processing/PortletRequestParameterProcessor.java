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

package org.jasig.portal.url.processing;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortletRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.url.ParameterMap;
import org.jasig.portal.url.UrlState;
import org.jasig.portal.url.UrlType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Uses the {@link IPortletUrlSyntaxProvider} to parse the portlet parameters from the request into {@link PortletUrl}s.
 * The WindowState, PortletMode and parameter Map is set directly on the {@link IPortletWindow}. The {@link org.jasig.portal.portlet.url.RequestType}
 * is tracked in the {@link IPortletRequestParameterManager}.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portletRequestParameterProcessor")
public class PortletRequestParameterProcessor implements IRequestParameterProcessor {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private IUrlSyntaxProvider urlSyntaxProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    
    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.processing.IRequestParameterProcessor#processParameters(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean processParameters(HttpServletRequest request, HttpServletResponse response) {
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        
        final IPortletWindowId targetedPortletWindowId = portalRequestInfo.getTargetedPortletWindowId();
        
        for (final IPortletRequestInfo portletRequestInfo : portalRequestInfo.getPortletRequestInfoMap().values()) {
            final IPortletWindowId portletWindowId = portletRequestInfo.getPortletWindowId();
            final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, targetedPortletWindowId);
            
            final UrlType urlType = portalRequestInfo.getUrlType();
            switch (urlType) {
                case RENDER: {
                    final Map<String, List<String>> portletParameters = portletRequestInfo.getPortletParameters();
                    portletWindow.setRenderParameters(ParameterMap.convertListMap(portletParameters));
                    
                    //fall through, render uses state/mode info
                }
                case ACTION: {
                    final WindowState windowState = portletRequestInfo.getWindowState();
                    if (windowState != null) {
                        portletWindow.setWindowState(windowState);
                    }
                    
                    final PortletMode portletMode = portletRequestInfo.getPortletMode();
                    if (portletMode != null) {
                        portletWindow.setPortletMode(portletMode);
                    }
                    
                    break;
                }
            }
            
            //Override the window state of the targeted portlet window based on the url state
            if (portletWindowId.equals(targetedPortletWindowId)) {
                final UrlState urlState = portalRequestInfo.getUrlState();
                switch (urlState) {
                    case MAX: {
                        portletWindow.setWindowState(WindowState.MAXIMIZED);
                        break;
                    } 
                    case DETACHED: {
                        portletWindow.setWindowState(IPortletRenderer.DETACHED);
                        break;
                    } 
                    case EXCLUSIVE: {
                        portletWindow.setWindowState(IPortletRenderer.EXCLUSIVE);
                        break;
                    }
                }
            }
            
            this.portletWindowRegistry.storePortletWindow(request, portletWindow);
        }
        
        return true;
    }
}
