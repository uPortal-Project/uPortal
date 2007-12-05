/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.PortletWindow;
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
     * Creates a new portlet entity for the window instance id and parent entity id. If the parent
     * {@link org.jasig.portal.portlet.om.IPortletEntity} for the portletEntityId can't be found or a window already
     * exists fpr the window instance id and entity id an exception will be thrown. 
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
     * a new window will be created and returned. This is a convience for {@link #getPortletWindow(HttpServletRequest, String, IPortletEntityId)}
     * and {@link #createPortletWindow(HttpServletRequest, String, IPortletEntityId)}
     * 
     * @param request The current request.
     * @param windowInstanceIdThe identifier for the instance of the window, such as an id for an inline window and an id for a detached window.
     * @param portletEntityId The parent entity id.
     * @return An existing window if exists or a new window if not.
     * @throws IllegalArgumentException If request, windowInstanceId or portletEntityId are null
     */
    public IPortletWindow getOrCreatePortletWindow(HttpServletRequest request, String windowInstanceId, IPortletEntityId portletEntityId);
    
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
     * Gets the parent portlet entity for the window specified by the window id.
     * 
     * @param request The request related to the window objects
     * @param portletWindowId The window ID to get the parent entity for.
     * @return The parent portlet entity for the window, null if no window exists for the id. 
     */
    public IPortletEntity getParentPortletEntity(HttpServletRequest request, IPortletWindowId portletWindowId);
}
