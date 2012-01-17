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

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletType;

/**
 * Provides APIs for creating, storing and retrieving {@link IPortletDefinition} objects.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletDefinitionDao {
    /**
     * Persists changes to a {@link IPortletDefinition}.
     * 
     * @param portletDefinition The portlet definition to store the changes for
     * @throws IllegalArgumentException if portletDefinition is null.
     */
    public IPortletDefinition updatePortletDefinition(IPortletDefinition portletDefinition);

	public void deletePortletDefinition(IPortletDefinition definition);

    /**
     * Get a {@link IPortletDefinition} for the specified {@link IPortletDefinitionId}.
     * 
     * @param portletDefinitionId The id to get the definition for.
     * @return The portlet definition for the id, null if no definition exists for the id.
     * @throws IllegalArgumentException if portletDefinitionId is null.
     */
    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId);
    
    public IPortletDefinition getPortletDefinition(String portletDefinitionIdString);
    
    public IPortletDefinition getPortletDefinitionByFname(String fname);

    public IPortletDefinition getPortletDefinitionByName(String name);

    public List<IPortletDefinition> getPortletDefinitions();

    public List<IPortletDefinition> searchForPortlets(String term, boolean allowPartial);

    public IPortletDefinition createPortletDefinition(IPortletType portletType, String fname, String name, String title,
            String applicationId, String portletName, boolean isFramework);

}
