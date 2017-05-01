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
package org.apereo.portal.portlet.registry;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.events.StartElement;
import org.apache.pluto.container.PortletWindow;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntityId;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.utils.Tuple;

/**
 * Provides methods for creating and accessing {@link IPortletWindow} and related objects.
 *
 * <p>Methods require a {@link HttpServletRequest} object due to the nature of IPortletWindows and
 * how they are tracked.
 *
 */
public interface IPortletWindowRegistry {
    /**
     * Converts a Pluto {@link PortletWindow} object to a uPortal {@link IPortletWindow}.
     *
     * @param request The request related to the window objects
     * @param portletWindow The Pluto {@link PortletWindow} to convert from
     * @return The corresponding uPortal {@link IPortletWindow}, will not be null.
     * @throws IllegalArgumentException if request or portletWindow are null
     */
    public IPortletWindow convertPortletWindow(
            HttpServletRequest request, PortletWindow portletWindow);

    /**
     * Get an existing portlet window for the window id. If no window exists for the id null will be
     * returned.
     *
     * @param request The current request.
     * @param portletWindowId The ID of the IPortletWindow to return.
     * @return The requested IPortletWindow, if no window exists for the ID null will be returned.
     * @throws IllegalArgumentException if request or portletWindowId are null.
     */
    public IPortletWindow getPortletWindow(
            HttpServletRequest request, IPortletWindowId portletWindowId);

    /**
     * Creates a delegating portlet window
     *
     * @param portletEntityId The parent entity id for the window if it doesn't already exist
     * @param delegationParentId The ID of the parent portlet window
     * @return The IPortletWindow that is a delegate of the parent
     * @throws IllegalArgumentException If any argument is null
     */
    public IPortletWindow createDelegatePortletWindow(
            HttpServletRequest request,
            IPortletEntityId portletEntityId,
            IPortletWindowId delegationParentId);

    /**
     * Get an existing portlet window for the default window id and parent entity id. If no window
     * exists for the parameters a new window will be created and returned. This is a convenience
     * for {@link #getDefaultPortletWindowId(IPortletEntityId)}, {@link
     * #getPortletWindow(HttpServletRequest, IPortletWindowId)} and {@link
     * #createDefaultPortletWindow(HttpServletRequest, IPortletEntityId)}
     *
     * @param request The current request.
     * @param portletEntityId The parent entity id.
     * @return An existing window if exists or a new window if not.
     * @throws IllegalArgumentException If request, windowInstanceId or portletEntityId are null
     */
    public IPortletWindow getOrCreateDefaultPortletWindow(
            HttpServletRequest request, IPortletEntityId portletEntityId);

    /**
     * Combines {@link IPortletDefinitionRegistry#getPortletDefinitionByFname(String)}, {@link
     * IPortletEntityRegistry#getOrCreatePortletEntity(org.apereo.portal.portlet.om.IPortletDefinitionId,
     * String, int)}, and {@link #getOrCreateDefaultPortletWindow(HttpServletRequest,
     * IPortletEntityId)}
     *
     * <p>Returns null if {@link IPortletDefinitionRegistry#getPortletDefinitionByFname(String)}
     * returns null
     */
    public IPortletWindow getOrCreateDefaultPortletWindowByFname(
            HttpServletRequest request, String fname);

    /**
     * Combines {@link IUserLayoutManager#getNode(String)}, {@link
     * IPortletDefinitionRegistry#getPortletDefinition(String)}, {@link
     * IPortletEntityRegistry#getOrCreatePortletEntity(org.apereo.portal.portlet.om.IPortletDefinitionId,
     * String, int)}, and {@link #getOrCreateDefaultPortletWindow(HttpServletRequest,
     * IPortletEntityId)}
     *
     * <p>If the specified layout node does not exist or is not a portlet null is returned.
     */
    public IPortletWindow getOrCreateDefaultPortletWindowByLayoutNodeId(
            HttpServletRequest request, String layoutNodeId);

    /**
     * Combines {@link
     * IPortletEntityRegistry#getOrCreatePortletEntity(org.apereo.portal.portlet.om.IPortletDefinitionId,
     * String, int)}, and {@link #getOrCreateDefaultPortletWindow(HttpServletRequest,
     * IPortletEntityId)}
     */
    public IPortletWindow getOrCreateDefaultPortletWindow(
            HttpServletRequest request, IPortletDefinitionId portletDefinitionId);

    /**
     * Creates an IPortletWindowId for the specified string identifier
     *
     * @param portletWindowId The string representation of the portlet window ID.
     * @return The IPortletWindowId for the string
     * @throws IllegalArgumentException If portletWindowId is null
     */
    public IPortletWindowId getPortletWindowId(HttpServletRequest request, String portletWindowId);

    /**
     * Creates the default portlet window ID given the ID of the entity the window is based on.
     *
     * @param portletEntityId The id of the entity to base the window ID on
     * @return The default window id for the entity
     * @throws IllegalArgumentException If portletEntityId is null
     */
    public IPortletWindowId getDefaultPortletWindowId(
            HttpServletRequest request, IPortletEntityId portletEntityId);

    /**
     * Create a transient portlet window id based on the specified portlet window id.
     *
     * @param request The current request
     * @param basePortletWindowId The window ID to clone into a transient window
     * @return The stateless window
     */
    public IPortletWindow getOrCreateStatelessPortletWindow(
            HttpServletRequest request, IPortletWindowId basePortletWindowId);

    /**
     * Get all portlet window objects for this portlet entity.
     *
     * @param request The request related to the window objects
     * @param portletEntityId The portlet entity that is a parent of all the windows to be returned
     * @return The set of windows that have been created from the specified entity
     */
    public Set<IPortletWindow> getAllPortletWindowsForEntity(
            HttpServletRequest request, IPortletEntityId portletEntityId);

    /**
     * Get the portlet window object for the rendering pipeline start element and return a
     * replacement start element that contains the portlet window id
     *
     * @return The found portlet window and a StartElement to replace the parameter which will
     *     contain the resolved portlet window id
     */
    public Tuple<IPortletWindow, StartElement> getPortletWindow(
            HttpServletRequest request, StartElement element);

    /**
     * Store changes made to the portlet window
     *
     * @param request The current request
     * @param portletWindow The modified portlet window to store
     */
    public void storePortletWindow(HttpServletRequest request, IPortletWindow portletWindow);

    /** Get all of the portlet windows for all of the portlets in the users layout. */
    public Set<IPortletWindow> getAllLayoutPortletWindows(HttpServletRequest request);

    /** Disable persistent window states for this request */
    public void disablePersistentWindowStates(HttpServletRequest request);
}
