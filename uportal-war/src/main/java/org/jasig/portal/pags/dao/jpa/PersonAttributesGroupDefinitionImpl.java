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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
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
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NaturalIdCache;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.pags.om.IPersonAttributesGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupStoreDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupTestGroupDefinition;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
@Entity
@Table(name = "UP_PAGS_GROUP")
@SequenceGenerator(
        name="UP_PAGS_GROUP_GEN",
        sequenceName="UP_PAGS_GROUP_SEQ",
        allocationSize=5
    )
@TableGenerator(
        name="UP_PAGS_GROUP_GEN",
        pkColumnValue="UP_PAGS_GROUP",
        allocationSize=5
    )
@NaturalIdCache
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersonAttributesGroupDefinitionImpl implements IPersonAttributesGroupDefinition, Serializable {
    public PersonAttributesGroupDefinitionImpl() {
        super();
    }
    public PersonAttributesGroupDefinitionImpl(IPersonAttributesGroupStoreDefinition store, String name, String description) {
        super();
        this.store = store;
        this.name = name;
        this.description = description;
    }

    @Id
    @GeneratedValue(generator = "UP_PAGS_GROUP_GEN")
    @Column(name = "PAGS_GROUP_ID")
    private long internalPersonAttributesGroupDefinitionId;
    
    @Version
    @Column(name = "ENTITY_VERSION")
    private long entityVersion;
    
    @Column(name = "NAME", length=500, nullable = true, updatable = true)
    private String name;
    
    @Column(name = "DESCRIPTION", length=500, nullable = true, updatable = true)
    private String description;
    
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = PersonAttributesGroupStoreDefinitionImpl.class)
    @JoinColumn(name = "PAGS_STORE_ID", nullable = true)
    private IPersonAttributesGroupStoreDefinition store;
    
    @ManyToMany(cascade=CascadeType.ALL, targetEntity=PersonAttributesGroupDefinitionImpl.class)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name="UP_PAGS_GROUP_MEMBERS", joinColumns = {@JoinColumn(name="PAGS_GROUP_ID")}, inverseJoinColumns={@JoinColumn(name="PAGS_GROUP_MEMBER_ID")})  
    private List<IPersonAttributesGroupDefinition> members = new ArrayList<IPersonAttributesGroupDefinition>(0);
    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="group", targetEntity=PersonAttributesGroupTestGroupDefinitionImpl.class, orphanRemoval=true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<IPersonAttributesGroupTestGroupDefinition> testGroups = new ArrayList<IPersonAttributesGroupTestGroupDefinition>(0);

    @Override
    public EntityIdentifier getEntityIdentifier() {
        return new EntityIdentifier(String.valueOf(this.internalPersonAttributesGroupDefinitionId), PersonAttributesGroupDefinitionImpl.class);
    }

    @Override
    public long getId() {
        return internalPersonAttributesGroupDefinitionId;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
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
    public List<IPersonAttributesGroupDefinition> getMembers() {
        return members;
    }

    @Override
    public void setMembers(List<IPersonAttributesGroupDefinition> members) {
        this.members = members;
    }

    @Override
    public List<IPersonAttributesGroupTestGroupDefinition> getTestGroups() {
        return testGroups;
    }

    @Override
    public void setTestGroups(List<IPersonAttributesGroupTestGroupDefinition> testGroups) {
        this.testGroups = testGroups;
    }

    public IPersonAttributesGroupStoreDefinition getStore() {
        return this.store;
    }
 
    public void setStore(IPersonAttributesGroupStoreDefinition store) {
        this.store = store;
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
