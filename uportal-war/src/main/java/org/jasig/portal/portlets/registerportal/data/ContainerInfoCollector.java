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

package org.jasig.portal.portlets.registerportal.data;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.jasig.portal.portlets.registerportal.IPortalDataCollector;
import org.springframework.web.context.ServletContextAware;

/**
 * Gathers servlet container info
 * 
 * @author Eric Dalquist
 * @version $Revision: 45528 $
 */
public class ContainerInfoCollector implements IPortalDataCollector, ServletContextAware {
    private ServletContext servletContext;
    
    /* (non-Javadoc)
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getData()
     */
    public Map<String, String> getData() {
        final Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put("serverInfo", this.servletContext.getServerInfo());
        data.put("majorVersion", Integer.toString(this.servletContext.getMajorVersion()));
        data.put("minorVersion", Integer.toString(this.servletContext.getMinorVersion()));
        
        return data;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getKey()
     */
    public String getKey() {
        return "ServletContainer";
    }
}
