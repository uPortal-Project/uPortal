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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


/**
 * Delegates properties related calls to a {@link List} of {@link IRequestPropertiesManager}s in order. No
 * exception handling is done so any exception in a child IRequestPropertiesManager will cause the call to fail.
 * <br/>
 * <br/>
 * When {@link #getRequestProperties(HttpServletRequest, IPortletWindow)} is called the returned Maps are overlayed
 * in order to produce the final Map that is returned. This means IRequestPropertiesManagers lower in the
 * propertiesManagers List can over-write properties from IRequestPropertiesManagers higher in the list.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("requestPropertiesManager")
@Qualifier("main")
public class RequestPropertiesManagerBroker implements IRequestPropertiesManager {
    protected final Log logger = LogFactory.getLog(this.getClass());
    private List<IRequestPropertiesManager> propertiesManagers = Collections.emptyList();
    
    /**
     * @return the propertiesManagers
     */
    public List<IRequestPropertiesManager> getPropertiesManagers() {
        return propertiesManagers;
    }

    /**
     * @param propertiesManagers the propertiesManagers to set
     */
	@Autowired
    public void setPropertiesManagers(List<IRequestPropertiesManager> propertiesManagers) {
        this.propertiesManagers = propertiesManagers;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.IRequestPropertiesManager#addResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)
     */
    public void addResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        for (final IRequestPropertiesManager propertiesManager : this.propertiesManagers) {
            propertiesManager.addResponseProperty(request, portletWindow, property, value);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.IRequestPropertiesManager#setResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)
     */
    public void setResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        for (final IRequestPropertiesManager propertiesManager : this.propertiesManagers) {
            propertiesManager.setResponseProperty(request, portletWindow, property, value);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.IRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)
     */
    public Map<String, String[]> getRequestProperties(HttpServletRequest request, IPortletWindow portletWindow) {
        final Map<String, String[]> properties = new HashMap<String, String[]>();
        
        for (final IRequestPropertiesManager propertiesManager : this.propertiesManagers) {
            final Map<String, String[]> newProperties = propertiesManager.getRequestProperties(request, portletWindow);
            
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Retrieved properties '" + newProperties + "' from manager: " + propertiesManager);
            }
            
            properties.putAll(newProperties);
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Returning properties '" + properties + "' for portlet " +  portletWindow + " and request " + request);
        }
        
        return properties;
    }
    
}
