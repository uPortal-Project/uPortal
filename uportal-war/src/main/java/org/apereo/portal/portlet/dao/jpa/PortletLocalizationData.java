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
package org.apereo.portal.portlet.dao.jpa;

import java.io.Serializable;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * ChannelLocalizationData represents locale-specific ChannelDefinition metadata. This class is
 * intended to be used in a JPA/Hibernate-managed map and does not include the locale itself as part
 * of its data model.
 *
 */
@Embeddable
@Table(name = "UP_PORTLET_MDATA")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PortletLocalizationData implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "PORTLET_NAME", length = 128)
    private String name;

    @Column(name = "PORTLET_TITLE", length = 128)
    private String title;

    @Column(name = "PORTLET_DESC", length = 255)
    private String description;

    /** Default constructor */
    public PortletLocalizationData() {}

    // Public getters

    /**
     * Get the name for this locale.
     *
     * @return localized name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the title for this locale.
     *
     * @return localized title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Get the description for this locale.
     *
     * @return localized description
     */
    public String getDescription() {
        return this.description;
    }

    // Public setters

    /**
     * Set the name for this locale
     *
     * @param name localized name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the title for this locale
     *
     * @param title localized title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set the description for this localized
     *
     * @param description localized description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
