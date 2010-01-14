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

package org.jasig.portal.channel.dao;

import java.util.List;

import org.jasig.portal.channel.IChannelType;

public interface IChannelTypeDao {
    /**
     * Creates, initializes and persists a new {@link IChannelType} based on the specified parameters
     * 
     * @param name The name of the channel type
     * @param clazz The Java Class the type represents
     * @param cpdUri The URI to the CPD file used when publishing channels of this type
     * 
     * @return A newly created, initialized and persisted {@link IChannelType}
     * @throws org.springframework.dao.DataIntegrityViolationException If a IChannelType already exists for the provide arguments
     * @throws IllegalArgumentException If any of the parameters are null
     */
    public IChannelType createChannelType(String name, String clazz, String cpdUri);
	
    /**
     * Persists changes to a {@link IChannelType}.
     * 
     * @param type The channel type to store the changes for
     * @throws IllegalArgumentException if type is null.
     */
    public IChannelType updateChannelType(IChannelType type);
	
    /**
     * Removes the specified {@link IChannelType} from the persistent store.
     * 
     * @param type The type to remove.
     * @throws IllegalArgumentException if type is null.
     */
    public void deleteChannelType(IChannelType type);
	
    /**
     * Get a {@link IChannelType} for the specified id.
     * 
     * @param id The id to get the type for.
     * @return The channel type for the id, null if no type exists for the id.
     */
    public IChannelType getChannelType(int id);
	
    /**
     * Get a {@link IChannelType} for the specified name
     * 
     * @param name The name to get the type for.
     * @return The channel type for the name, null if no type exists for the fname.
     * @throws IllegalArgumentException if name is null.
     */
    public IChannelType getChannelType(String name);
	
    /**
     * @return A {@link List} of all persisted {@link IChannelType}s
     */
    public List<IChannelType> getChannelTypes();

}
