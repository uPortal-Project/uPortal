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

import java.util.List;

import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.utils.Tuple;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Provides methods for creating and accessing {@link IPortletDefinition} and related objects.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletDefinitionRegistry {
    /**
     * Get an existing portlet definition for the definition id. If no definition exists for the id null will be returned.
     * 
     * @param portletDefinitionId The id of the definition to retrieve
     * @return The portlet definition for the id, null if no definition exists for the id.
     * @throws IllegalArgumentException If portletDefinitionId is null.
     */
    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId);

    public IPortletDefinition getPortletDefinition(String portletDefinitionIdString);

    /**
     * @param fname The functional name of the portlet definition
     * @return The definition or null of none exists for the specified fname
     */
    public IPortletDefinition getPortletDefinitionByFname(String fname);

    public IPortletDefinition getPortletDefinitionByName(String name);

    public List<IPortletDefinition> getAllPortletDefinitions();
    
    /**
     * Persists changes to a IPortletDefinition.
     * 
     * @param portletDefinition The IPortletDefinition to store changes to.
     * @throws IllegalArgumentException If portletDefinition is null
     */
    public IPortletDefinition updatePortletDefinition(IPortletDefinition portletDefinition);

    public IPortletDefinition createPortletDefinition(IPortletType portletType, String fname, String name, String title, String applicationId, String portletName, boolean isFramework);
    
    public void deletePortletDefinition(IPortletDefinition portletDefinition);

    public List<IPortletDefinition> searchForPortlets(String term, boolean allowPartial);

    /**
     * Gets the parent portlet descriptor for the entity specified by the definition id.
     * 
     * @param portletDefinitionId The definition ID to get the parent descriptor for.
     * @return The parent portlet descriptor for the definition, null if no definition exists for the id. 
     * @throws IllegalArgumentException if portletDefinitionId is null
     * @throws DataRetrievalFailureException if the {@link PortletDefinition} cannot be found for the {@link IPortletDefinition}
     */
    public PortletDefinition getParentPortletDescriptor(IPortletDefinitionId portletDefinitionId);

    /**
     * Gets the parent portlet application descriptor for the entity specified by the definition id.
     * 
     * @param portletDefinitionId The definition ID to get the parent application descriptor for.
     * @return The parent portlet descriptor for the application definition, null if no definition exists for the id. 
     * @throws IllegalArgumentException if portletDefinitionId is null
     * @throws DataRetrievalFailureException if the {@link PortletApplicationDefinition} cannot be found for the {@link IPortletDefinition}
     */
    public PortletApplicationDefinition getParentPortletApplicationDescriptor(IPortletDefinitionId portletDefinitionId);
    
    /**
     * Get the portletApplicationId and portletName for the specified portlet definition. The portletApplicationId
     * will be {@link Tuple#first} and the portletName will be {@link Tuple#second}
     */
    public Tuple<String, String> getPortletDescriptorKeys(IPortletDefinition portletDefinition);
}
