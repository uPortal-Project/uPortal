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

package org.jasig.portal.pags.dao.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalIdCache;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.pags.om.IPersonAttributesGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupStoreDefinition;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
@Entity
@Table(name = "UP_PAGS_STORE")
@SequenceGenerator(
        name="UP_PAGS_STORE_GEN",
        sequenceName="UP_PAGS_STORE_SEQ",
        allocationSize=5
    )
@TableGenerator(
        name="UP_PAGS_STORE_GEN",
        pkColumnValue="UP_PAGS_STORE",
        allocationSize=5
    )
@NaturalIdCache
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersonAttributesGroupStoreDefinitionImpl implements IPersonAttributesGroupStoreDefinition, Serializable {
    public PersonAttributesGroupStoreDefinitionImpl() {
        super();
    }
    public PersonAttributesGroupStoreDefinitionImpl(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }

    @Id
    @GeneratedValue(generator = "UP_PAGS_STORE_GEN")
    @Column(name = "PAGS_STORE_ID")
    private long internalPersonAttributesGroupStoreDefinitionId;
    
    @Version
    @Column(name = "ENTITY_VERSION")
    private long entityVersion;
    
    @Column(name = "NAME", length=500, nullable = true, updatable = true)
    private String name;
    
    @Column(name = "DESCRIPTION", length=500, nullable = true, updatable = true)
    private String description;
    
    
    @OneToMany(fetch = FetchType.EAGER, mappedBy="store", cascade=CascadeType.ALL, targetEntity=PersonAttributesGroupDefinitionImpl.class, orphanRemoval=true)
    private List<IPersonAttributesGroupDefinition> personAttributesGroupDefinitions = new ArrayList<IPersonAttributesGroupDefinition>(0);

    @Override
    public EntityIdentifier getEntityIdentifier() {
        return new EntityIdentifier(String.valueOf(this.internalPersonAttributesGroupStoreDefinitionId), PersonAttributesGroupStoreDefinitionImpl.class);
    }

    @Override
    public long getId() {
        return internalPersonAttributesGroupStoreDefinitionId;
    }
    
    @Override
    public String getDataId() {
        return this.name;
    }


    @Override
    public String getDataTitle() {
        return this.name;
    }


    @Override
    public String getDataDescription() {
        return this.description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String storeName) {
        this.name = storeName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String storeDescription) {
        this.description = storeDescription;
    }
    
    @Override
    public List<IPersonAttributesGroupDefinition> getGroups() {
        return personAttributesGroupDefinitions;
    }
    
    @Override
    public void setGroups(List<IPersonAttributesGroupDefinition> groups) {
        this.personAttributesGroupDefinitions = groups;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
