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

package org.jasig.portal.portlet.container.services;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.driver.AdministrativeRequestListener;

/**
 * AdministrativeRequestListener that delegates to another AdministrativeRequestListener based on
 * a request attribute.
 * 
 * <br>
 * Configuration:
 * <table border="1">
 *     <tr>
 *         <th align="left">Property</th>
 *         <th align="left">Description</th>
 *         <th align="left">Required</th>
 *         <th align="left">Default</th>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">listenerKeyAttribute</td>
 *         <td>
 *             The PortletRequest attribute to get the key for the {@link AdministrativeRequestListener} from.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">org.jasig.portal.portlet.common.AdministrativeRequestListenerControler.LISTENER_KEY</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">listeners</td>
 *         <td>
 *             The {@link Map} of {@link String} keys to {@link AdministrativeRequestListener}s.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">{@link new HashMap()}</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">failOnKeyNotFound</td>
 *         <td>
 *             If an exception should be thrown if a <code>listenerKeyAttribute</code> request attribute is not
 *             found. If false a warning will be logged and the method will return.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">false</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">failOnListenerNotFound</td>
 *         <td>
 *             If an exception should be thrown if a {@link AdministrativeRequestListener} isn't found for the request
 *             attribute key. If false a warning will be logged and the method will return.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">false</td>
 *     </tr>
 * </table>
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AdministrativeRequestListenerController implements org.apache.pluto.container.driver.AdministrativeRequestListener {
    public static final String DEFAULT_LISTENER_KEY_ATTRIBUTE = AdministrativeRequestListenerController.class.getName() + ".LISTENER_KEY";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String listenerKeyAttribute = DEFAULT_LISTENER_KEY_ATTRIBUTE;
    private Map<String, AdministrativeRequestListener> listeners = new HashMap<String, AdministrativeRequestListener>();
    private boolean failOnKeyNotFound = false;
    private boolean failOnListenerNotFound = false;

    
    /**
     * @return the failOnKeyNotFound
     */
    public boolean isFailOnKeyNotFound() {
        return this.failOnKeyNotFound;
    }
    /**
     * @param failOnKeyNotFound the failOnKeyNotFound to set
     */
    public void setFailOnKeyNotFound(boolean failOnKeyNotFound) {
        this.failOnKeyNotFound = failOnKeyNotFound;
    }
    /**
     * @return the failOnListenerNotFound
     */
    public boolean isFailOnListenerNotFound() {
        return this.failOnListenerNotFound;
    }
    /**
     * @param failOnListenerNotFound the failOnListenerNotFound to set
     */
    public void setFailOnListenerNotFound(boolean failOnListenerNotFound) {
        this.failOnListenerNotFound = failOnListenerNotFound;
    }
    /**
     * @return the listenerKeyAttribute
     */
    public String getListenerKeyAttribute() {
        return this.listenerKeyAttribute;
    }
    /**
     * @param listenerKeyAttribute the listenerKeyAttribute to set
     */
    public void setListenerKeyAttribute(String listenerKeyAttribute) {
        this.listenerKeyAttribute = listenerKeyAttribute;
    }
    /**
     * @return the listeners
     */
    public Map<String, AdministrativeRequestListener> getListeners() {
        return this.listeners;
    }
    /**
     * @param listeners the listeners to set
     */
    public void setListeners(Map<String, AdministrativeRequestListener> listeners) {
        this.listeners = listeners;
    }


    /**
     * @see org.apache.pluto.spi.optional.AdministrativeRequestListener#administer(javax.portlet.PortletRequest, javax.portlet.PortletResponse)
     */
    public void administer(PortletRequest request, PortletResponse response) {
        final String listenerKey = (String)request.getAttribute(this.listenerKeyAttribute);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Found key '" + listenerKey + "' from PortletRequest attribute '" + this.listenerKeyAttribute + "'");
        }
        
        if (listenerKey == null) {
            if (this.failOnKeyNotFound) {
                this.logger.error("Failed to find PortletRequest attribute '" + this.listenerKeyAttribute + "'");
                throw new IllegalArgumentException("Failed to find PortletRequest attribute '" + this.listenerKeyAttribute + "'");
            }

            this.logger.warn("Failed to find PortletRequest attribute '" + this.listenerKeyAttribute + "'");
            return;
        }
        
        
        final AdministrativeRequestListener administrativeRequestListener = this.listeners.get(listenerKey);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Found AdministrativeRequestListener '" + administrativeRequestListener + "' for key '" + listenerKey + "'");
        }
        
        if (administrativeRequestListener == null) {
            if (this.failOnListenerNotFound) {
                this.logger.error("Failed to find AdministrativeRequestListener for key '" + listenerKey + "'");
                throw new IllegalArgumentException("Failed to find AdministrativeRequestListener for key '" + listenerKey + "'");
            }

            this.logger.warn("Failed to find AdministrativeRequestListener for key '" + listenerKey + "'");
            return;
        }
        
        administrativeRequestListener.administer(request, response);
    }
}
