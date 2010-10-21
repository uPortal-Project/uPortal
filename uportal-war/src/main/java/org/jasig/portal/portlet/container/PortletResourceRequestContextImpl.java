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

package org.jasig.portal.portlet.container;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletResourceRequestContext;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortletRequestInfo;
import org.jasig.portal.url.ParameterMap;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletResourceRequestContextImpl extends PortletRequestContextImpl implements PortletResourceRequestContext {

	protected final Log logger = LogFactory.getLog(this.getClass());
	
    public PortletResourceRequestContextImpl(
            PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortalRequestInfo portalRequestInfo) {
        
        super(portletContainer, portletWindow, 
                containerRequest, containerResponse, 
                requestPropertiesManager, portalRequestInfo);
    }
        

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceRequestContext#getCacheability()
     */
    @Override
    public String getCacheability() {
        final IPortletRequestInfo portletRequestInfo = this.portalRequestInfo.getPortletRequestInfo();
        if (portletRequestInfo != null) {
            return portletRequestInfo.getCacheability();
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceRequestContext#getPrivateRenderParameterMap()
     */
    @Override
    public Map<String, String[]> getPrivateRenderParameterMap() {
        // previous set of render parameters
    	final IPortletRequestInfo portletRequestInfo = this.portalRequestInfo.getPortletRequestInfo();
        if (portletRequestInfo != null) {
        	return ParameterMap.convertListMap(portletRequestInfo.getPortletParameters());
        } else {
        	// since this is a resource request, portletRequestInfo should never be null
        	// warn log about programming error
        	logger.warn("portletRequestInfo was null inside getPrivateRenderParameterMap for a resource request, programming error");
        	return Collections.emptyMap();
        }
    }
    
    @Override
    public Map<String, String[]> getPrivateParameterMap() {
        //TODO create another parameter class for portlet resource parameters
    	final IPortletRequestInfo portletRequestInfo = this.portalRequestInfo.getPortletRequestInfo();
        if (portletRequestInfo != null) {
        	return ParameterMap.convertListMap(portletRequestInfo.getResourceParameters());
        } else {
        	// since this is a resource request, portletRequestInfo should never be null
        	// warn log about programming error
        	logger.warn("portletRequestInfo was null inside getPrivateParameterMap for a resource request, programming error");
        	return Collections.emptyMap();
        }
        
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceRequestContext#getResourceID()
     */
    @Override
    public String getResourceID() {
        final IPortletRequestInfo portletRequestInfo = this.portalRequestInfo.getPortletRequestInfo();
        if (portletRequestInfo != null) {
            return portletRequestInfo.getResourceId();
        }
        
        return null;
    }
}
