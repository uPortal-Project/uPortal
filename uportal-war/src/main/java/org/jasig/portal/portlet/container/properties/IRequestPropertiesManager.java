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

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.utils.Populator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Encapsulates logic related to handling properties that can be retrieved from and
 * set on {@link javax.portlet.PortletRequest} and {@link javax.portlet.PortletResponse}
 * objects.
 * 
 * For the add/set methods configured property managers are called in turn until one returns
 * true. If ordering in property handling is important the manager should either implement {@link Ordered}
 * or be annotated with {@link Order} 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IRequestPropertiesManager {

    /**
     * Called when a portlet sets a property via {@link javax.portlet.PortletResponse#setProperty(String, String)}. This
     * method should follow the same rules as the PortletResponse method and overwrite any existing property of the same
     * name.
     * 
     * @param portletRequest The request the setProperty call was made during
     * @param portletWindow The PortletWindow representing the portlet calling setProperty
     * @param property The name of the property to set, will not be null.
     * @param value The value of the property to set, may be null.
     * @return true if this manager handled the property, false if not
     */
    public boolean setResponseProperty(HttpServletRequest portletRequest, IPortletWindow portletWindow, String property, String value);
    
    /**
     * Called when a portlet sets a property via {@link javax.portlet.PortletResponse#addProperty(String, String)}. This
     * method should follow the same rules as the PortletResponse method and add the value to any existing property of
     * the same name.
     * 
     * @param portletRequest The request the addProperty call was made during
     * @param portletWindow The PortletWindow representing the portlet calling addProperty
     * @param property The name of the property to add, will not be null.
     * @param value The value of the property to add, may be null.
     * @return true if this manager handled the property, false if not
     */
    public boolean addResponseProperty(HttpServletRequest portletRequest, IPortletWindow portletWindow, String property, String value);

    /**
     * Called when a portlet gets the request properties via {@link javax.portlet.PortletRequest#getProperties(String)},
     * {@link javax.portlet.PortletRequest#getProperty(String)}, or {@link javax.portlet.PortletRequest#getPropertyNames()}.
     * 
     * @param portletRequest The request the get call was made during
     * @param portletWindow The PortletWindow representing the portlet calling addProperty
     * @param propertiesPopulator Populator to add properties to, for multi-valued properties call {@link Populator#put(Object, Object)} multiple times
     */
    public <P extends Populator<String, String>> void populateRequestProperties(HttpServletRequest portletRequest, IPortletWindow portletWindow, P propertiesPopulator);
}
