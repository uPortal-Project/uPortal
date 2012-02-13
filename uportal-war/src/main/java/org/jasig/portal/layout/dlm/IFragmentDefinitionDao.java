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

package org.jasig.portal.layout.dlm;

import java.util.List;

/**
 * Defines the service calls for DLM fragment definition DAO implementations.
 * 
 * @author awills
 */
public interface IFragmentDefinitionDao {
    
    /**
     * Obtains the complete set of {@link FragmentDefinition} objects contained 
     * within this data source.
     * 
     * @return The fragment with the corresponding name, or <code>null</code>
     */
    List<FragmentDefinition> getAllFragments();
    
    /**
     * Obtains the {@link FragmentDefinition} object with the specified name.
     * 
     * @param name The unique name of a fragment
     * @return The fragment with the corresponding name, or <code>null</code>
     */
    FragmentDefinition getFragmentDefinition(String name);
    
    /**
     * Obtains the {@link FragmentDefinition} object belonging to the specified 
     * owner.
     * 
     * @param OWNERiD The username of the fragment 
     * @return The fragment with the corresponding name, or <code>null</code>
     */
    FragmentDefinition getFragmentDefinitionByOwner(String ownerId);

    /**
     * Updates the specified {@link FragmentDefinition} within the data source.
     * 
     * @param fd A fragment definition that has been modified
     */
    void updateFragmentDefinition(FragmentDefinition fd);
    
    /**
     * Deletes the specified {@link FragmentDefinition} within the data source.
     * 
     * @param fd A fragment definition that exists within this data source
     */
    void removeFragmentDefinition(FragmentDefinition fd);
    
}
