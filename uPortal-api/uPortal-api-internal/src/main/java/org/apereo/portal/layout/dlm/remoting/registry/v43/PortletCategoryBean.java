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
package org.apereo.portal.layout.dlm.remoting.registry.v43;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apereo.portal.portlet.om.PortletCategory;

/**
 * Gets serialized into JSON representing a category for the 4.3 version of the portletRegistry.json
 * API.
 *
 * @since 4.3
 */
public final class PortletCategoryBean implements Comparable<PortletCategoryBean>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Set<PortletCategoryBean> EMPTY_CATEGORIES = Collections.emptySet();
    private static final Set<PortletDefinitionBean> EMPTY_PORTLETS = Collections.emptySet();

    private final String id;
    private String name; // Setter provided so it can be localized after initialization
    private final String description;
    private final SortedSet<PortletCategoryBean> subcategories;
    private final SortedSet<PortletDefinitionBean> portlets;

    public static PortletCategoryBean fromPortletCategory(
            PortletCategory pc,
            Set<PortletCategoryBean> subcategories,
            Set<PortletDefinitionBean> portlets) {
        return new PortletCategoryBean(
                pc,
                subcategories != null ? subcategories : EMPTY_CATEGORIES,
                portlets != null ? portlets : EMPTY_PORTLETS);
    }

    /** Creates a new bean based on the provided inputs. */
    public static PortletCategoryBean create(
            String id,
            String name,
            String description,
            Set<PortletCategoryBean> subcategories,
            Set<PortletDefinitionBean> portlets) {
        return new PortletCategoryBean(id, name, description, subcategories, portlets);
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public SortedSet<PortletCategoryBean> getSubcategories() {
        return this.subcategories;
    }

    public SortedSet<PortletDefinitionBean> getPortlets() {
        return this.portlets;
    }

    @Override
    public int compareTo(PortletCategoryBean category) {
        return new CompareToBuilder().append(this.name, category.getName()).toComparison();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PortletCategoryBean)) {
            return false;
        }
        PortletCategoryBean rhs = (PortletCategoryBean) object;
        return new EqualsBuilder().append(this.id, rhs.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143).append(this.id).toHashCode();
    }

    /*
     * Implementation
     */

    private PortletCategoryBean(
            PortletCategory category,
            Set<PortletCategoryBean> subcategories,
            Set<PortletDefinitionBean> portlets) {
        this(
                category.getId(),
                category.getName(),
                category.getDescription(),
                subcategories,
                portlets);
    }

    private PortletCategoryBean(
            String id,
            String name,
            String description,
            Set<PortletCategoryBean> subcategories,
            Set<PortletDefinitionBean> portlets) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.subcategories = new TreeSet<>(subcategories);
        this.portlets = new TreeSet<>(portlets);
    }
}
