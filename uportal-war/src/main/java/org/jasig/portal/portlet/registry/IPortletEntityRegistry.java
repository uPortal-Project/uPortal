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

import java.util.concurrent.locks.Lock;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.user.IUserInstance;

/**
 * Provides methods for creating and accessing {@link IPortletEntity} and related objects.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletEntityRegistry {
    /**
     * @param portletEntityId The ID of the portlet entity to get a lock for
     * @return The Lock for the Portlet Entity
     */
    public Lock getPortletEntityLock(HttpServletRequest request, IPortletEntityId portletEntityId);
    
    /**
     * Get an existing portlet entity for the entity id. If no entity exists for the id null will be returned.
     * 
     * @param portletEntityId The id of the entity to retrieve
     * @return The portlet entity for the id, null if no entity exists for the id.
     * @throws IllegalArgumentException If portletEntityId is null.
     */
    public IPortletEntity getPortletEntity(HttpServletRequest request, IPortletEntityId portletEntityId);
    
    /**
     * Get an existing portlet entity for the String version of the entity id. If an exception occurs while parsing
     * the String into an {@link IPortletEntityId} an {@link IllegalArgumentException} will be thrown. If no entity
     * exists for the id null will be returned.
     * 
     * @param portletEntityIdString The id of the entity to retrieve
     * @return The portlet entity for the id, null if no entity exists for the id.
     * @throws IllegalArgumentException If portletEntityIdString is null or cannot be parsed into a {@link IPortletEntityId}.
     */
    public IPortletEntity getPortletEntity(HttpServletRequest request, String portletEntityIdString);
    
//    /**
//     * Get an existing portlet entity for the channel subscribe id and person. If no entity exists for the parameters
//     * null will be returned.
//     * 
//     * @param layoutNodeId The layout subscription id for the underlying channel.
//     * @param userId The if of the person the entity is for.
//     * @return The portlet entity for the subscribe id and person, null if no entity exists for the parameters.
//     * @throws IllegalArgumentException If layoutNodeId is null.
//     */
//    public IPortletEntity getPortletEntity(String layoutNodeId, int userId);
    
//    /**
//     * Creates a new, persisted, portlet entity for the published and subscribed to channel. If an existing
//     * {@link IPortletDefinition} can't be found for the portletDefinitionId or an entity already exists for the channel
//     * subscribe id and person an exception will be thrown.
//     * 
//     * @param portletDefinitionId The definition id of the underlying {@link IPortletDefinition}
//     * @param layoutNodeId The layout subscription id for the underlying channel.
//     * @param userId The id of the person the entity is for.
//     * @return A new entity for the parameters
//     * @throws IllegalArgumentException If portletDefinitionId or layoutNodeId are null
//     * @throws org.springframework.dao.DataIntegrityViolationException If an entity already exists for the channel
//     *         subscribe id and userId pair
//     * @throws org.springframework.dao.DataRetrievalFailureException If no {@link org.jasig.portal.portlet.om.IPortletDefinition}
//     *         exists for the specified {@link IPortletDefinitionId} 
//     */
//    public IPortletEntity createPortletEntity(IPortletDefinitionId portletDefinitionId, String layoutNodeId, int userId);
    
    /**
     * 
     */
    public IPortletEntity getOrCreateDefaultPortletEntity(HttpServletRequest request, IPortletDefinitionId portletDefinitionId);
    
    /**
     * Convenience for {@link #getPortletEntity(String, int)} and {@link #createPortletEntity(IPortletDefinitionId, String, int)}.
     * If the get returns null the entity will be created and returned.
     * 
     * @see #getPortletEntity(String, int)
     * @see #createPortletEntity(IPortletDefinitionId, String, int)
     */
    public IPortletEntity getOrCreatePortletEntity(HttpServletRequest request, IPortletDefinitionId portletDefinitionId, String layoutNodeId, int userId);
    
    /**
     * Convenience that looks up the portlet definition by subscribe ID then gets or creates the portlet entity for the subscription
     */
    public IPortletEntity getOrCreatePortletEntity(HttpServletRequest request, IUserInstance userInstance, String layoutNodeId);
    
    /**
     * Gets or creates a portlet entity for a specific fname.
     */
    public IPortletEntity getOrCreatePortletEntityByFname(HttpServletRequest request, IUserInstance userInstance, String fname);
    /**
     * Same as {@link #getOrCreatePortletEntityByFname(IUserInstance, String)} but also provides a preferred channel subscribe id.
     * If the specified subscribe ID can be found in the user's layout it is used, if not the functionality provided by
     * {@link #getOrCreatePortletEntityByFname(IUserInstance, String)} is used.
     */
    public IPortletEntity getOrCreatePortletEntityByFname(HttpServletRequest request, IUserInstance userInstance, String fname, String preferredChannelSubscribeId);
    
    /**
     * Stores changes made to an existing portlet entity
     * 
     * @param portletEntity The entity to update the persistent store for
     * @throws IllegalArgumentException if portletEntity is null
     */
    public void storePortletEntity(HttpServletRequest request, IPortletEntity portletEntity);
    
//    /**
//     * Get all {@link IPortletEntity}s that exist for the specified user id. (From {@link org.jasig.portal.security.IPerson#getID()}.
//     * 
//     * @param userId The id of the user to get the entities for.
//     * @return A set of all entities base on the specified user id, will be empty if no entities exist for the id, will never be null.
//     */
//    public Set<IPortletEntity> getPortletEntitiesForUser(int userId);
    
//    /**
//     * Removes a portlet entity and all related data from the persistent store.
//     * 
//     * @param portletEntity The the entity to delete.
//     * @throws IllegalArgumentException if portletEntity is null
//     */
//    public void deletePortletEntity(IPortletEntity portletEntity);
}
