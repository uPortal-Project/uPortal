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

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IWritableHttpServletRequest;
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

    private IPortalUrlProvider portalUrlProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }



    /* (non-Javadoc)
     * @see org.jasig.portal.url.processing.IRequestParameterProcessor#processParameters(org.jasig.portal.url.IWritableHttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean processParameters(IWritableHttpServletRequest request, HttpServletResponse response) {
        final IPortalRequestInfo portalRequestInfo = this.portalUrlProvider.getPortalRequestInfo(request);

        final IPortletWindowId targetedPortletWindowId = portalRequestInfo.getTargetedPortletWindowId();
        if(targetedPortletWindowId == null) {
    		return true;
        }
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, targetedPortletWindowId);
        if (portletWindow == null) {
            this.logger.warn("No IPortletWindow exists for IPortletWindowId='" + targetedPortletWindowId
                    + "'. Request parameters for this IPortletWindowId will be ignored. Ignored parameters: "
                    + portalRequestInfo);
        }
        else {
            
            //TODO set parameters on target portlet window
            
//            final PortletMode portletMode = portletUrl.getPortletMode();
//            if (portletMode != null) {
//                portletWindow.setPortletMode(portletMode);
//            }
//    
//            final WindowState windowState = portletUrl.getWindowState();
//            if (windowState != null) {
//                portletWindow.setWindowState(windowState);
//            }
            
        }

//        final Map<String, List<String>> parameters = portletUrl.getParameters();
//        final TYPE requestType = portletUrl.getRequestType();
//        final PortletRequestInfo portletRequestInfo = new PortletRequestInfo(requestType, parameters);
//
//        this.portletRequestParameterManager.setRequestInfo(request, targetedPortletWindowId, portletRequestInfo);

        return true;
    }
}
