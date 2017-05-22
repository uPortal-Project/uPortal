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
package org.apereo.portal.layout.dao.jpa;

import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import org.apereo.portal.layout.om.ILayoutAttributeDescriptor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 */
@Entity
@Table(name = "UP_SS_DESC_LAY_ATTR")
@SequenceGenerator(
    name = "UP_SS_DESC_LAY_ATTR_GEN",
    sequenceName = "UP_SS_DESC_LAY_ATTR_SEQ",
    allocationSize = 5
)
@TableGenerator(
    name = "UP_SS_DESC_LAY_ATTR_GEN",
    pkColumnValue = "UP_SS_DESC_LAY_ATTR",
    allocationSize = 5
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LayoutAttributeDescriptorImpl extends AbstractStylesheetDataImpl
        implements ILayoutAttributeDescriptor {
    @Id
    @GeneratedValue(generator = "UP_SS_DESC_LAY_ATTR_GEN")
    @Column(name = "SS_DESC_LAYOUT_ATTR_ID")
    private final long id;

    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "UP_SS_DESC_LAY_ATTR_ELMS", joinColumns = @JoinColumn(name = "ATTR_ID"))
    @Column(name = "ELEMENT_NAME")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Set<String> targetElementNames;

    //Required by hibernate for reflective creation
    @SuppressWarnings("unused")
    private LayoutAttributeDescriptorImpl() {
        this.id = -1;
        this.entityVersion = -1;
    }

    public LayoutAttributeDescriptorImpl(String name, Scope scope) {
        super(name, scope);
        this.id = -1;
        this.entityVersion = -1;
    }

    @Override
    public long getId() {
        return this.id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.ILayoutAttributeDescriptor#getTargetElementNames()
     */
    @Override
    public Set<String> getTargetElementNames() {
        return this.targetElementNames;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.ILayoutAttributeDescriptor#setTargetElementNames(java.util.Set)
     */
    @Override
    public void setTargetElementNames(Set<String> targetElementNames) {
        this.targetElementNames = targetElementNames;
    }

    @Override
    public String toString() {
        return "LayoutAttributeDescriptorImpl [getId()="
                + this.getId()
                + ", entityVersion="
                + this.entityVersion
                + ", getName()="
                + this.getName()
                + ", getDefaultValue()="
                + this.getDefaultValue()
                + ", getScope()="
                + this.getScope()
                + ", getDescription()="
                + this.getDescription()
                + "targetElementNames="
                + this.targetElementNames
                + "]";
    }
}
