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
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

/**
 * JPA implementation of the IPortletType interface.
 *
 */
@Entity
@Table(name = "UP_PORTLET_TYPE")
@SequenceGenerator(
    name = "UP_PORTLET_TYPE_GEN",
    sequenceName = "UP_PORTLET_TYPE_SEQ",
    allocationSize = 1
)
@TableGenerator(name = "UP_PORTLET_TYPE_GEN", pkColumnValue = "UP_PORTLET_TYPE", allocationSize = 1)
@NaturalIdCache(region = "org.apereo.portal.portlet.dao.jpa.PortletTypeImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PortletTypeImpl implements Serializable, IPortletType {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_PORTLET_TYPE_GEN")
    @Column(name = "TYPE_ID")
    private final int internalId;

    @SuppressWarnings("unused")
    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    //Hidden reference to the child portlet definitions, used to allow cascading deletes where when a portlet type is deleted all associated definitions are also deleted
    //MUST BE LAZY FETCH, this set should never actually be populated at runtime or performance will be TERRIBLE
    @SuppressWarnings("unused")
    @OneToMany(
        mappedBy = "portletType",
        targetEntity = PortletDefinitionImpl.class,
        cascade = {CascadeType.ALL},
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private transient Set<IPortletDefinition> portletDefinitions = null;

    @NaturalId
    @Column(name = "TYPE_NAME", length = 70)
    private final String name;

    @Column(name = "TYPE_DESCR", length = 2000)
    private String descr;

    @Column(name = "TYPE_DEF_URI", length = 255, nullable = false)
    private String cpdUri;

    /** Default constructor used by hibernate */
    @SuppressWarnings("unused")
    private PortletTypeImpl() {
        this.internalId = -1;
        this.entityVersion = -1;
        this.name = null;
    }

    public PortletTypeImpl(String name, String cpdUri) {
        this.internalId = -1;
        this.entityVersion = -1;
        this.name = name;
        this.cpdUri = cpdUri;
    }

    // Getter methods

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.IPortletType#getId()
     */
    @Override
    public int getId() {
        return this.internalId;
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.IPortletType#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.IPortletType#getDescription()
     */
    @Override
    public String getDescription() {
        return descr;
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.IPortletType#getCpdUri()
     */
    @Override
    public String getCpdUri() {
        return cpdUri;
    }

    // Setter methods

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.IPortletType#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String descr) {
        this.descr = descr;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.channel.IPortletType#setCpdUri(java.lang.String)
     */
    @Override
    public void setCpdUri(String cpdUri) {
        this.cpdUri = cpdUri;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!IPortletType.class.isAssignableFrom(obj.getClass())) return false;
        IPortletType other = (IPortletType) obj;
        if (this.name == null) {
            if (other.getName() != null) return false;
        } else if (!this.name.equals(other.getName())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "PortletTypeImpl [internalId="
                + this.internalId
                + ", name="
                + this.name
                + ", descr="
                + this.descr
                + ", cpdUri="
                + this.cpdUri
                + "]";
    }

    @Override
    public String getDataId() {
        return Integer.toString(getId());
    }

    @Override
    public String getDataTitle() {
        return name;
    }

    @Override
    public String getDataDescription() {
        return descr;
    }
}
