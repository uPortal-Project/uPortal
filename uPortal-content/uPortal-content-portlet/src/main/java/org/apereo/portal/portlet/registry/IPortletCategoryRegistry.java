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
package org.apereo.portal.portlet.registry;

import java.util.Set;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.PortletCategory;

public interface IPortletCategoryRegistry {

    /**
     * Gets all child portlet categories for a parent category.
     *
     * @return portletCategories the children categories
     */
    Set<PortletCategory> getAllChildCategories(PortletCategory parent);

    /**
     * Gets all parent portlet categories for a child category
     *
     * @param child
     * @return portletCategories the parentcategories
     */
    Set<PortletCategory> getAllParentCategories(PortletCategory child);

    /**
     * Gets all child portlet definitions for a parent category.
     *
     * @return portletDefinitions the children portlet definitions
     * @throws java.sql.SQLException
     */
    Set<IPortletDefinition> getAllChildPortlets(PortletCategory parent);

    /**
     * Gets an existing portlet category.
     *
     * @param portletCategoryId the id of the category to get
     * @return portletCategory the portlet category
     */
    PortletCategory getPortletCategory(String portletCategoryId);

    /**
     * Gets all child portlet categories for a parent category.
     *
     * @return portletCategories the children categories
     */
    Set<PortletCategory> getChildCategories(PortletCategory parent);

    /**
     * Gets all child portlet definitions for a parent category.
     *
     * @return portletDefinitions the children portlet definitions
     * @throws java.sql.SQLException
     */
    Set<IPortletDefinition> getChildPortlets(PortletCategory parent);

    /**
     * Gets the immediate parent categories of this category.
     *
     * @return parents, the parent categories.
     */
    Set<PortletCategory> getParentCategories(PortletCategory child);

    /**
     * Gets the immediate parent categories of this portlet definition.
     *
     * @return parents, the parent categories.
     */
    Set<PortletCategory> getParentCategories(IPortletDefinition child);

    /**
     * Gets top level portlet category
     *
     * @return portletCategories the new portlet category
     */
    PortletCategory getTopLevelPortletCategory();
}
