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

package org.jasig.portal.portlet.registry;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.PortletWindow;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Provides methods for creating and accessing {@link IPortletWindow} and related objects.
 * 
 * Methods require a {@link HttpServletRequest} object due to the nature of IPortletWindows and how they are tracked.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletWindowRegistry {
    /**
     * Get an existing portlet window for the window id. If no window exists for the id null will be returned.
     * 
     * @param request The current request.
     * @param portletWindowId The ID of the IPortletWindow to return.
     * @return The requested IPortletWindow, if no window exists for the ID null will be returned.
     * @throws IllegalArgumentException if request or portletWindowId are null.
     */
    public IPortletWindow getPortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId);
    
    /**
     * Get an existing portlet window for the channel window instance id and entity id. If no window exists for the
     * parameters null will be returned.
     * 
     * @param request The current request.
     * @param windowInstanceId The identifier for the instance of the window, such as an id for an inline window and an id for a detached window.
     * @param portletEntityId The parent entity id.
     * @return The existing window, if no window exists for the instance id and entity id null will be returned.
     * @throws IllegalArgumentException If request, windowInstanceId or portletEntityId are null.
     */
    public IPortletWindow getPortletWindow(HttpServletRequest request, String windowInstanceId, IPortletEntityId portletEntityId);
    
    /**
     * Creates a new portlet window for the window instance id and parent entity id. If the parent
     * {@link org.jasig.portal.portlet.om.IPortletEntity} for the portletEntityId can't be found or a window already
     * exists for the window instance id and entity id an exception will be thrown. 
     * 
     * @param request The current request.
     * @param windowInstanceIdThe identifier for the instance of the window, such as an id for an inline window and an id for a detached window.
     * @param portletEntityId The parent entity id.
     * @return A new window for the parameters
     * @throws IllegalArgumentException If request, windowInstanceId or portletEntityId are null, if no
     * {@link org.jasig.portal.ChannelDefinition} exists for the channelPublishId or if an entity already exists for the
     * subscribe id & person.
     */
    public IPortletWindow createPortletWindow(HttpServletRequest request, String windowInstanceId, IPortletEntityId portletEntityId);
    
    /**
     * Get an existing portlet window for the window instance id and parent entity id. If no window exists for the parameters
     * a new window will be created and returned. This is a convenience for {@link #getPortletWindow(HttpServletRequest, String, IPortletEntityId)}
     * and {@link #createPortletWindow(HttpServletRequest, String, IPortletEntityId)}
     * 
     * @param request The current request.
     * @param windowInstanceId The identifier for the instance of the window, such as an id for an inline window and an id for a detached window.
     * @param portletEntityId The parent entity id.
     * @return An existing window if exists or a new window if not.
     * @throws IllegalArgumentException If request, windowInstanceId or portletEntityId are null
     */
    public IPortletWindow getOrCreatePortletWindow(HttpServletRequest request, String windowInstanceId, IPortletEntityId portletEntityId);
    
    /**
     * Creates a new portlet window that will have the ID returned by {@link #getDefaultPortletWindowId(IPortletEntityId)}
     * from the specified parent entity id. If the parent {@link org.jasig.portal.portlet.om.IPortletEntity} for the portletEntityId
     * can't be found or a window already exists for the default window id and entity id an exception will be thrown. 
     * 
     * @param request The current request.
     * @param portletEntityId The parent entity id.
     * @return A new window for the parameters
     * @throws IllegalArgumentException If request or portletEntityId are null, if no
     * {@link org.jasig.portal.ChannelDefinition} exists for the channelPublishId or if an entity already exists for the
     * default window id & person.
     */
    public IPortletWindow createDefaultPortletWindow(HttpServletRequest request, IPortletEntityId portletEntityId);
    
    /**
     * Get an existing portlet window for the default window id and parent entity id. If no window exists for the parameters
     * a new window will be created and returned. This is a convenience for {@link #getDefaultPortletWindowId(IPortletEntityId)},
     * {@link #getPortletWindow(HttpServletRequest, IPortletWindowId)} and {@link #createDefaultPortletWindow(HttpServletRequest, IPortletEntityId)}
     * 
     * @param request The current request.
     * @param portletEntityId The parent entity id.
     * @return An existing window if exists or a new window if not.
     * @throws IllegalArgumentException If request, windowInstanceId or portletEntityId are null
     */
    public IPortletWindow getOrCreateDefaultPortletWindow(HttpServletRequest request, IPortletEntityId portletEntityId);
    
    /**
     * Combines {@link IPortletDefinitionRegistry#getPortletDefinitionByFname(String)},
     * {@link IPortletEntityRegistry#getOrCreatePortletEntity(org.jasig.portal.portlet.om.IPortletDefinitionId, String, int)}, and
     * {@link #getOrCreateDefaultPortletWindow(HttpServletRequest, IPortletEntityId)} 
     */
    public IPortletWindow getOrCreateDefaultPortletWindowByFname(HttpServletRequest request, String fname);
    
    /**
     * Combines {@link IUserLayoutManager#getNode(String)},
     * {@link IPortletDefinitionRegistry#getPortletDefinition(String)},
     * {@link IPortletEntityRegistry#getOrCreatePortletEntity(org.jasig.portal.portlet.om.IPortletDefinitionId, String, int)}, and
     * {@link #getOrCreateDefaultPortletWindow(HttpServletRequest, IPortletEntityId)}
     * 
     * If the specified layout node does not exist or is not a portlet null is returned.
     */
    public IPortletWindow getOrCreateDefaultPortletWindowBySubscribeId(HttpServletRequest request, String subscribeId);
    
    /**
     * Combines {@link IPortletEntityRegistry#getOrCreatePortletEntity(org.jasig.portal.portlet.om.IPortletDefinitionId, String, int)}, and
     * {@link #getOrCreateDefaultPortletWindow(HttpServletRequest, IPortletEntityId)} 
     */
    public IPortletWindow getOrCreateDefaultPortletWindow(HttpServletRequest request, IPortletDefinition portletDefinition);
    
    /**
     * Creates a delegating portlet window
     * 
     * @param portletEntityId The parent entity id for the window if it doesn't already exist
     * @param delegationParentId The ID of the parent portlet window
     * @return The IPortletWindow that is a delegate of the parent
     * @throws IllegalArgumentException If any argument is null
     */
    public IPortletWindow createDelegatePortletWindow(HttpServletRequest request, IPortletEntityId portletEntityId, IPortletWindowId delegationParentId);

    /**
     * Gets or Creates a delegating portlet window.
     * 
     * @param portletWindowId The ID of the delegate portlet window
     * @param portletEntityId The parent entity id for the window if it doesn't already exist
     * @param delegationParentId The ID of the parent portlet window
     * @return The IPortletWindow that is a delegate of the parent
     * @throws IllegalArgumentException If any argument is null
     */
    public IPortletWindow getOrCreateDelegatePortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId, IPortletEntityId portletEntityId, IPortletWindowId delegationParentId);

    /**
     * Creates an ID for a transient portlet window
     * 
     * @param portletWindowId The string represenation of the portlet window ID.
     * @return The IPortletWindowId for the string
     * @throws IllegalArgumentException If portletWindowId is null
     */
    public IPortletWindowId createTransientPortletWindowId(HttpServletRequest request, IPortletWindowId sourcePortletWindowId);
    
    /**
     * @param request The current portal request
     * @param portletWindowId The window ID to check
     * @return true if the window id is for a transient window.
     */
    public boolean isTransient(HttpServletRequest request, IPortletWindowId portletWindowId);
    
    /**
     * Converts a Pluto {@link PortletWindow} object to a uPortal {@link IPortletWindow}.
     * 
     * @param request The request related to the window objects
     * @param portletWindow The Pluto {@link PortletWindow} to convert from
     * @return The corresponding uPortal {@link IPortletWindow}, will not be null.
     * @throws IllegalArgumentException if request or portletWindow are null
     */
    public IPortletWindow convertPortletWindow(HttpServletRequest request, PortletWindow portletWindow);

    /**
     * Creates an IPortletWindowId for the specified string identifier
     * 
     * @param portletWindowId The string represenation of the portlet window ID.
     * @return The IPortletWindowId for the string
     * @throws IllegalArgumentException If portletWindowId is null
     */
    public IPortletWindowId getPortletWindowId(String portletWindowId);
    
    /**
     * Creates the default portlet window ID given the ID of the entity the window is based on. 
     * 
     * @param portletEntityId The id of the entity to base the window ID on
     * @return The default window id for the entity
     * @throws IllegalArgumentException If portletEntityId is null
     */
    public IPortletWindowId getDefaultPortletWindowId(IPortletEntityId portletEntityId);
    
    /**
     * Gets the parent portlet entity for the window specified by the window id.
     * 
     * @param request The request related to the window objects
     * @param portletWindowId The window ID to get the parent entity for.
     * @return The parent portlet entity for the window, null if no window exists for the id. 
     */
    public IPortletEntity getParentPortletEntity(HttpServletRequest request, IPortletWindowId portletWindowId);
    
    /**
     * Get all portlet window objects for this portlet entity.
     * 
     * @param request The request related to the window objects
     * @param portletEntityId The portlet entity that is a parent of all the windows to be returned
     * @return The set of windows that have been created from the specified entity
     */
    public Set<IPortletWindow> getAllPortletWindows(HttpServletRequest request, IPortletEntityId portletEntityId);
}
