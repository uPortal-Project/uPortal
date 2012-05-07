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

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.ParameterMap;
import org.jasig.portal.utils.Populator;
import org.springframework.core.Ordered;

/**
 * Manager that has a single backing Map of properties.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockRequestPropertiesManager implements IRequestPropertiesManager, Ordered {
    private Map<String, String[]> properties = new ParameterMap();
    private int order;
    
    /**
     * @return the properties
     */
    public Map<String, String[]> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, String[]> properties) {
        this.properties = properties;
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    public void setOrder(int order) {
        this.order = order;
    }

    public boolean addResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        String[] values = this.properties.get(property);
        values = (String[])ArrayUtils.add(values, value);
        this.properties.put(property, values);
        return true;
    }

    public boolean setResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        this.properties.put(property, new String[] { value });
        return true;
    }
    
    @Override
    public <P extends Populator<String, String>> void populateRequestProperties(HttpServletRequest portletRequest,
            IPortletWindow portletWindow, P propertiesPopulator) {
        for (final Entry<String, String[]> propEntry : this.properties.entrySet()) {
            final String name = propEntry.getKey();
            for (final String value : propEntry.getValue()) {
                propertiesPopulator.put(name, value);
            }
        }
    }
}
