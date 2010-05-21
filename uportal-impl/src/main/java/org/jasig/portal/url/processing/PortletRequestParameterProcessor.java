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
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.url.IPortletRequestParameterManager;
import org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider;
import org.jasig.portal.portlet.url.PortletRequestInfo;
import org.jasig.portal.portlet.url.PortletUrl;
import org.jasig.portal.url.IWritableHttpServletRequest;
import org.jasig.portal.utils.Tuple;
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

    private IPortletUrlSyntaxProvider portletUrlSyntaxProvider;
    private IPortletRequestParameterManager portletRequestParameterManager;
    private IPortletWindowRegistry portletWindowRegistry;
    

    /**
     * @return the portletUrlSyntaxProvider
     */
    public IPortletUrlSyntaxProvider getPortletUrlSyntaxProvider() {
        return portletUrlSyntaxProvider;
    }
    /**
     * @param portletUrlSyntaxProvider the portletUrlSyntaxProvider to set
     */
    @Autowired(required=true)
    public void setPortletUrlSyntaxProvider(IPortletUrlSyntaxProvider portletUrlSyntaxProvider) {
        this.portletUrlSyntaxProvider = portletUrlSyntaxProvider;
    }

    /**
     * @return the portletRequestParameterManager
     */
    public IPortletRequestParameterManager getPortletRequestParameterManager() {
        return portletRequestParameterManager;
    }
    /**
     * @param portletRequestParameterManager the portletRequestParameterManager to set
     */
    @Autowired(required=true)
    public void setPortletRequestParameterManager(IPortletRequestParameterManager portletRequestParameterManager) {
        this.portletRequestParameterManager = portletRequestParameterManager;
    }

    /**
     * @return the portletWindowRegistry
     */
    public IPortletWindowRegistry getPortletWindowRegistry() {
        return portletWindowRegistry;
    }
    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Autowired(required=true)
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.url.processing.IRequestParameterProcessor#processParameters(org.jasig.portal.url.IWritableHttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean processParameters(IWritableHttpServletRequest request, HttpServletResponse response) {
    	final PortletUrl portletUrl = this.portletUrlSyntaxProvider.parsePortletUrl(request);
    	 
        //TODO need a way to mark which portlet generated the URL

        if(portletUrl == null) {
    		this.portletRequestParameterManager.setNoPortletRequest(request);
    		return true;
    	
        }
        final IPortletWindowId targetWindowId = portletUrl.getTargetWindowId();
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, targetWindowId);
        if (portletWindow == null) {
            this.logger.warn("No IPortletWindow exists for IPortletWindowId='" + targetWindowId
                    + "'. Request parameters for this IPortletWindowId will be ignored. Ignored parameters: "
                    + portletUrl);
        }
        else {
            final PortletMode portletMode = portletUrl.getPortletMode();
            if (portletMode != null) {
                portletWindow.setPortletMode(portletMode);
            }
    
            final WindowState windowState = portletUrl.getWindowState();
            if (windowState != null) {
                portletWindow.setWindowState(windowState);
            }
            
        }

        final Map<String, List<String>> parameters = portletUrl.getParameters();
        final TYPE requestType = portletUrl.getRequestType();
        final PortletRequestInfo portletRequestInfo = new PortletRequestInfo(requestType, parameters);

        this.portletRequestParameterManager.setRequestInfo(request, targetWindowId, portletRequestInfo);

        return true;
    }
}
