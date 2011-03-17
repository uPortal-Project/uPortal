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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.ParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
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
    
    
    /**
     * @return the portalRequestUtils
     */
    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.BaseRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)
     */
    @Override
    public Map<String, String[]> getRequestProperties(HttpServletRequest portletRequest, IPortletWindow portletWindow) {
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getOriginalPortalRequest(portletRequest);
        
        final Map<String, String[]> properties = new ParameterMap();

        properties.put("REMOTE_ADDR", new String[] { httpServletRequest.getRemoteAddr() });
        properties.put("REQUEST_METHOD", new String[] { httpServletRequest.getMethod() });
        
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> values = httpServletRequest.getHeaders(name);
            List<String> v = new ArrayList<String>();
            while (values.hasMoreElements()) {
                v.add(values.nextElement());
            }
            properties.put(name, v.toArray(new String[v.size()]));
        }

        return properties;
    }
    
    /**
     * Returns {@link Ordered#HIGHEST_PRECEDENCE}.
     * 
     * @see org.springframework.core.Ordered#getOrder()
     */
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
    
    
}
