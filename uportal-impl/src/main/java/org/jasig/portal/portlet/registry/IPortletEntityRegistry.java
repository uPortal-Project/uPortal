/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.security.IPerson;

/**
 * Provides methods for creating and accessing {@link IPortletEntity} and related objects.
 * 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletEntityRegistry {
    /**
     * Get an existing portlet entity for the entity id. If no entity exists for the id null will be returned.
     * 
     * @param portletEntityId The id of the entity to retrieve
     * @return The portle entity for the subscribe id and person, null if no entity exists for the parameters.
     * @throws IllegalArgumentException If portletEntityId is null.
     */
    public IPortletEntity getPortletEntity(IPortletEntityId portletEntityId);
    
    /**
     * Get an existing portlet entity for the channel subscribe id and person. If no entity exists for the parameters
     * null will be returned.
     * 
     * @param channelSubscribeId The layout subscription id for the underlying channel.
     * @param person The person the entity is for.
     * @return The portle entity for the subscribe id and person, null if no entity exists for the parameters.
     * @throws IllegalArgumentException If channelSubscribeId or person are null.
     */
    public IPortletEntity getPortletEntity(String channelSubscribeId, IPerson person);
    
    /**
     * Creates a new, persisted, portlet entity for the published and subscribed to channel. If the
     * {@link org.jasig.portal.ChannelDefinition} for the channelPublishId can't be found or an entity already exists for
     * the channel subscribe id and person an exception will be thrown.
     * 
     * @param channelPublishId The publish id of the underlying {@link org.jasig.portal.ChannelDefinition}
     * @param channelSubscribeId The layout subscription id for the underlying channel.
     * @param person The person the entity is for.
     * @return A new entity for the parameters
     * @throws IllegalArgumentException If channelPublishId, channelSubscribeId, or person are null, if no
     * {@link org.jasig.portal.ChannelDefinition} exists for the channelPublishId or if an entity already exists for the
     * subscribe id & person.
     */
    public IPortletEntity createPortletEntity(String channelPublishId, String channelSubscribeId, IPerson person);
    
    /**
     * Get an existing portlet entity for the channel subscribe id and person. If no entity exists for the parameters
     * a new entity will be created and returned. This is a convience for {@link #getPortletEntity(String, IPerson)}
     * and {@link #createPortletEntity(String, String, IPerson)}.
     * 
     * @param channelPublishId The publish id of the underlying {@link org.jasig.portal.ChannelDefinition}
     * @param channelSubscribeId The layout subscription id for the underlying channel.
     * @param person The person the entity is for.
     * @return An existing entity if exists or a new entity if not.
     * @throws IllegalArgumentException If channelPublishId, channelSubscribeId, or person are null
     */
    public IPortletEntity getOrCreatePortletEntity(String channelPublishId, String channelSubscribeId, IPerson person);
    
    /**
     * Gets the parent portlet definition for the entity specified by the entity id.
     * 
     * @param portletEntityId The entity ID to get the parent definition for.
     * @return The parent portlet entity for the window, null if no window exists for the id. 
     */
    public IPortletDefinition getParentPortletDefinition(IPortletEntityId portletEntityId);
}
