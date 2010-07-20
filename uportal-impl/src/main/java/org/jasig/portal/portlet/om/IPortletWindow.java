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

package org.jasig.portal.portlet.om;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.container.PortletWindow;

/**
 * uPortal extensions to the Pluto {@link PortletWindow} interface. A portlet window
 * represents the actual rendering/interaction layer of the portlet object model.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletWindow extends PortletWindow, Serializable {
    /**
     * Retrieve this windows unique id which will be
     *  used to communicate back to the referencing portal.
     * @return unique id.
     */
    public IPortletWindowId getPortletWindowId();
    
    /**
     * @return The ID of the parent portlet entity.
     */
    public IPortletEntityId getPortletEntityId();
    
    /**
     * @param state The current {@link WindowState} of this PortletWindow
     * @throws IllegalArgumentException If state is null
     */
    public void setWindowState(WindowState state);
    
    /**
     * @param mode The current {@link PortletMode} of this PortletWindow
     * @throws IllegalArgumentException If mode is null
     */
    public void setPortletMode(PortletMode mode);
    
    /**
     * @param requestParameters The current request parameters for the portlet
     * @throws IllegalArgumentException if parameters is null.
     */
    public void setRequestParameters(Map<String, String[]> requestParameters);
    
    /**
     * @return The current request parameters for the portlet
     */
    public Map<String, String[]> getRequestParameters();
    
    /**
     * Sets the expiration timeout for the portlet rendering cache. If null is set
     * the timeout configured in the portlet.xml should be used.
     * 
     * @param expirationCache Set the cache expiration length for the portlet in seconds.
     */
    public void setExpirationCache(Integer expirationCache);

    /**
     * @return The expiration timeout for the portlet, if null the value from portlet.xml should be used.
     */
    public Integer getExpirationCache();
    
    /**
     * @return The ID of the parent portlet window that is delegating rendering to this portlet, null if
     *      this portlet is not being delegated to.
     */
    public IPortletWindowId getDelegationParent();
}
