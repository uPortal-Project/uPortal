/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.security.IPerson;

/**
 * Provides methods for creating and accessing {@link IPortletEntity} and related objects.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletEntityRegistry {
    /**
     * Get an existing portlet entity for the entity id. If no entity exists for the id null will be returned.
     * 
     * @param portletEntityId The id of the entity to retrieve
     * @return The portlet entity for the id, null if no entity exists for the id.
     * @throws IllegalArgumentException If portletEntityId is null.
     */
    public IPortletEntity getPortletEntity(IPortletEntityId portletEntityId);
    
    /**
     * Get an existing portlet entity for the channel subscribe id and person. If no entity exists for the parameters
     * null will be returned.
     * 
     * @param channelSubscribeId The layout subscription id for the underlying channel.
     * @param person The person the entity is for.
     * @return The portlet entity for the subscribe id and person, null if no entity exists for the parameters.
     * @throws IllegalArgumentException If channelSubscribeId or person are null.
     */
    public IPortletEntity getPortletEntity(String channelSubscribeId, IPerson person);
    
    /**
     * Creates a new, persisted, portlet entity for the published and subscribed to channel. If an existing
     * {@link IPortletDefinition} can't be found for the portletDefinitionId or an entity already exists for the channel
     * subscribe id and person an exception will be thrown.
     * 
     * @param portletDefinitionId The definition id of the underlying {@link IPortletDefinition}
     * @param channelSubscribeId The layout subscription id for the underlying channel.
     * @param person The person the entity is for.
     * @return A new entity for the parameters
     * @throws IllegalArgumentException If portletDefinitionId, channelSubscribeId, or person are null
     * @throws IllegalArgumentException If no IPortletDefinition exists for the id, or if an entity already exists for the subscribe id & person.
     */
    public IPortletEntity createPortletEntity(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, IPerson person);
    
    /**
     * Convience for {@link #getPortletEntity(String, IPerson)} and {@link #createPortletEntity(IPortletDefinitionId, String, IPerson)}.
     * If the get returns null the entity will be created and returned.
     * 
     * @see #getPortletEntity(String, IPerson)
     * @see #createPortletEntity(IPortletDefinitionId, String, IPerson)
     */
    public IPortletEntity getOrCreatePortletEntity(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, IPerson person);
    
    /**
     * Stores changes made to an existing portlet entity
     * 
     * @param portletEntity The entity to update the persistent store for
     * @throws IllegalArgumentException if portletEntity is null
     */
    public void storePortletEntity(IPortletEntity portletEntity);
    
    /**
     * Removes a portlet entity and all related data from the persistent store.
     * 
     * @param portletEntity The the entity to delete.
     * @throws IllegalArgumentException if portletEntity is null
     */
    public void deletePortletEntity(IPortletEntity portletEntity);
    
    /**
     * Gets the parent portlet definition for the entity specified by the entity id.
     * 
     * @param portletEntityId The entity ID to get the parent definition for.
     * @return The parent portlet definition for the entity, null if no entity exists for the id. 
     * @throws IllegalArgumentException if portletEntityId is null
     */
    public IPortletDefinition getParentPortletDefinition(IPortletEntityId portletEntityId);
}
