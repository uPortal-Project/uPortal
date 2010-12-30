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

package org.jasig.portal.portlet.dao;

import java.util.List;

import org.jasig.portal.portlet.om.IPortletType;

public interface IPortletTypeDao {
    /**
     * Creates, initializes and persists a new {@link IPortletType} based on the specified parameters
     * 
     * @param name The name of the channel type
     * @param cpdUri The URI to the CPD file used when publishing channels of this type
     * 
     * @return A newly created, initialized and persisted {@link IPortletType}
     * @throws org.springframework.dao.DataIntegrityViolationException If a IChannelType already exists for the provide arguments
     * @throws IllegalArgumentException If any of the parameters are null
     */
    public IPortletType createPortletType(String name, String cpdUri);
	
    /**
     * Persists changes to a {@link IPortletType}.
     * 
     * @param type The channel type to store the changes for
     * @throws IllegalArgumentException if type is null.
     */
    public IPortletType updatePortletType(IPortletType type);
	
    /**
     * Removes the specified {@link IPortletType} from the persistent store.
     * 
     * @param type The type to remove.
     * @throws IllegalArgumentException if type is null.
     */
    public void deletePortletType(IPortletType type);
	
    /**
     * Get a {@link IPortletType} for the specified id.
     * 
     * @param id The id to get the type for.
     * @return The channel type for the id, null if no type exists for the id.
     */
    public IPortletType getPortletType(int id);
	
    /**
     * Get a {@link IPortletType} for the specified name
     * 
     * @param name The name to get the type for.
     * @return The channel type for the name, null if no type exists for the fname.
     * @throws IllegalArgumentException if name is null.
     */
    public IPortletType getPortletType(String name);
	
    /**
     * @return A {@link List} of all persisted {@link IPortletType}s
     */
    public List<IPortletType> getPortletTypes();

}
