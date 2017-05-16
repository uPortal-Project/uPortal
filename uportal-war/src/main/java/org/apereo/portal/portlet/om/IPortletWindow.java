/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlet.om;

import java.util.Map;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import org.apache.pluto.container.PortletWindow;

/**
 * uPortal extensions to the Pluto {@link PortletWindow} interface. A portlet window represents the
 * actual rendering/interaction layer of the portlet object model.
 *
 */
public interface IPortletWindow extends IPortletWindowDescriptor {
    /**
     * Retrieve this windows unique id which will be used to communicate back to the referencing
     * portal.
     *
     * @return unique id.
     */
    @Override
    public IPortletWindowId getPortletWindowId();

    /** @return The parent portlet entity. */
    public IPortletEntity getPortletEntity();

    /**
     * @return The ID of the parent portlet window that is delegating rendering to this portlet,
     *     null if this portlet is not being delegated to.
     */
    public IPortletWindowId getDelegationParentId();

    /** @return The current WindowState of the portlet */
    public WindowState getWindowState();

    /**
     * @param state The current {@link WindowState} of this PortletWindow
     * @throws IllegalArgumentException If state is null
     */
    public void setWindowState(WindowState state);

    /** @return The current PortletMode of the portlet. */
    public PortletMode getPortletMode();

    /**
     * @param mode The current {@link PortletMode} of this PortletWindow
     * @throws IllegalArgumentException If mode is null
     */
    public void setPortletMode(PortletMode mode);

    /**
     * @param requestParameters The most recent set of private request parameters used for rendering
     *     the portlet
     * @throws IllegalArgumentException if parameters is null.
     */
    public void setRenderParameters(Map<String, String[]> requestParameters);

    /**
     * The most recent set of private request parameters used for rendering the portlet
     *
     * @return The current request parameters for the portlet
     */
    public Map<String, String[]> getRenderParameters();

    /**
     * The most recent set of public request parameters used for rendering the portlet
     *
     * @return The current request parameters for the portlet
     */
    public Map<String, String[]> getPublicRenderParameters();

    /**
     * @param requestParameters The most recent set of public request parameters used for rendering
     *     the portlet
     * @throws IllegalArgumentException if parameters is null.
     */
    public void setPublicRenderParameters(Map<String, String[]> requestParameters);

    /**
     * Sets the expiration timeout for the portlet rendering cache. If null is set the timeout
     * configured in the portlet.xml should be used.
     *
     * @param expirationCache Set the cache expiration length for the portlet in seconds.
     */
    public void setExpirationCache(Integer expirationCache);

    /**
     * @return The expiration timeout for the portlet, if null the value from portlet.xml should be
     *     used.
     */
    public Integer getExpirationCache();

    /** @return The Pluto SPI implementation of a portlet window */
    public PortletWindow getPlutoPortletWindow();

    /**
     * @return Hash code based on the {@link #getPortletEntity()}, {@link #getPortletWindowId()},
     *     and {@link #getDelegationParentId()}
     */
    @Override
    public int hashCode();

    /**
     * Do equals comparison to any other {@link IPortletWindow} based on the {@link
     * #getPortletEntity()}, {@link #getPortletWindowId()}, and {@link #getDelegationParentId()}
     */
    @Override
    public boolean equals(Object obj);
}
