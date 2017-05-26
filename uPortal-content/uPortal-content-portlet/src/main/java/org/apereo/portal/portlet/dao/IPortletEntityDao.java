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
package org.apereo.portal.portlet.dao;

import java.util.Set;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletEntityId;
import org.apereo.portal.security.IPerson;

/**
 * Provides APIs for creating, storing and retrieving {@link IPortletEntity} objects.
 *
 */
public interface IPortletEntityDao {
    /**
     * Creates, initializes and persists a new {@link IPortletEntity} based on the specified {@link
     * IPortletDefinitionId}, layout node id and user id.
     *
     * @param portletDefinitionId The id of the {@link
     *     org.apereo.portal.portlet.om.IPortletDefinition} that is the parent of the new entity
     * @param layoutNodeId The layout node id in the user's layout.
     * @param userId The id of the user the entity is for.
     * @return A newly created, initialized and persisted entity.
     * @throws IllegalArgumentException if portletDefinitionId or layoutNodeId are null.
     * @throws org.springframework.dao.DataIntegrityViolationException If an entity already exists
     *     for the layout node id and userId pair
     * @throws org.springframework.dao.DataRetrievalFailureException If no {@link
     *     org.apereo.portal.portlet.om.IPortletDefinition} exists for the specified {@link
     *     IPortletDefinitionId}
     */
    IPortletEntity createPortletEntity(
            IPortletDefinitionId portletDefinitionId, String layoutNodeId, int userId);

    /**
     * Persists changes to a {@link IPortletEntity}.
     *
     * @param portletEntity The portlet entity to store the changes for
     * @throws IllegalArgumentException if portletEntity is null.
     */
    void updatePortletEntity(IPortletEntity portletEntity);

    /**
     * Get a {@link IPortletEntity} for the specified {@link IPortletEntityId}.
     *
     * @param portletEntityId The id to get the entity for.
     * @return The portlet entity for the id, null if no entity exists for the id.
     * @throws IllegalArgumentException if portletEntityId is null.
     */
    IPortletEntity getPortletEntity(IPortletEntityId portletEntityId);

    /**
     * Check if a {@link IPortletEntity} actually exists in the database.
     *
     * @param portletEntityId The id of the entity to check for.
     */
    boolean portletEntityExists(IPortletEntityId portletEntityId);

    /**
     * Get a {@link IPortletEntity} for the specified layout node id and user id.
     *
     * @param layoutNodeId The node id from the user's layout to get the entity for
     * @param userId The user id to get the entity for
     * @return The portlet entity for the id, null if no entity exists for the ids.
     * @throws IllegalArgumentException if layoutNodeId is null.
     */
    IPortletEntity getPortletEntity(String layoutNodeId, int userId);

    /**
     * Get all {@link IPortletEntity}s based on the specified {@link IPortletDefinitionId}.
     *
     * @param portletDefinitionId The ID of the parent portlet definition
     * @return A set of all entities based on the specified portlet definition id, will be empty if
     *     no entities exist for the id, will never be null.
     * @throws IllegalArgumentException if portletDefinitionId is null.
     */
    Set<IPortletEntity> getPortletEntities(IPortletDefinitionId portletDefinitionId);

    /**
     * Get all {@link IPortletEntity}s that exist for the specified user id. (From {@link
     * IPerson#getID()}.
     *
     * @param userId The id of the user to get the entities for.
     * @return A set of all entities base on the specified user id, will be empty if no entities
     *     exist for the id, will never be null.
     */
    Set<IPortletEntity> getPortletEntitiesForUser(int userId);

    /**
     * Removes the specified {@link IPortletEntity} from the persistent store.
     *
     * @param portletEntity The entity to remove.
     * @throws IllegalArgumentException if portletEntity is null.
     */
    void deletePortletEntity(IPortletEntity portletEntity);
}
