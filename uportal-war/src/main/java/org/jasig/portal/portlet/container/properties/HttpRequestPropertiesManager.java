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

package org.jasig.portal.portlet.container.properties;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.utils.Populator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides some extra information from the {@link HttpServletRequest} to the portlet as properties.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("httpRequestPropertiesManager")
public class HttpRequestPropertiesManager extends BaseRequestPropertiesManager {
    private IPortalRequestUtils portalRequestUtils;
    
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Override
    public <P extends Populator<String, String>> void populateRequestProperties(HttpServletRequest portletRequest,
            IPortletWindow portletWindow, P propertiesPopulator) {

        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getOriginalPortalRequest(portletRequest);
        
        final String remoteAddr = httpServletRequest.getRemoteAddr();
        if (remoteAddr != null) {
            propertiesPopulator.put("REMOTE_ADDR", remoteAddr);
        }
        
        final String remoteHost = httpServletRequest.getRemoteHost();
        if (remoteHost != null) {
            propertiesPopulator.put("REMOTE_HOST", remoteHost);
        }
        
        final String method = httpServletRequest.getMethod();
        if (method != null) {
            propertiesPopulator.put("REQUEST_METHOD", method);
        }
        
        @SuppressWarnings("unchecked")
        final Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            
            @SuppressWarnings("unchecked")
            final Enumeration<String> values = httpServletRequest.getHeaders(name);
            while (values.hasMoreElements()) {
                propertiesPopulator.put(name, values.nextElement());
            }
            
        }
    }
}
