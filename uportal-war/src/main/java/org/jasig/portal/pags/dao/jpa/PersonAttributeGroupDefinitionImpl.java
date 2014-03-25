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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NaturalIdCache;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.pags.om.IPersonAttributeGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributeGroupStoreDefinition;
import org.jasig.portal.pags.om.IPersonAttributeGroupTestGroupDefinition;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
@Entity
@Table(name = "UP_PAG_DEF")
@SequenceGenerator(
        name="UP_PAG_DEF_GEN",
        sequenceName="UP_PAG_DEF_SEQ",
        allocationSize=5
    )
@TableGenerator(
        name="UP_PAG_DEF_GEN",
        pkColumnValue="UP_PAG_DEF",
        allocationSize=5
    )
@NaturalIdCache
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersonAttributeGroupDefinitionImpl implements IPersonAttributeGroupDefinition {
    public PersonAttributeGroupDefinitionImpl() {
        super();
    }
    public PersonAttributeGroupDefinitionImpl(IPersonAttributeGroupStoreDefinition store, String name, String description) {
        super();
        this.store = store;
        this.name = name;
        this.description = description;
    }

    @Id
    @GeneratedValue(generator = "UP_PAG_DEF_GEN")
    @Column(name = "PAG_DEF_ID")
    private long internalPersonAttributeGroupDefinitionId;
    
    @Version
    @Column(name = "ENTITY_VERSION")
    private long entityVersion;
    
    @Column(name = "NAME", length=500, nullable = true, updatable = true)
    private String name;
    
    @Column(name = "DESCRIPTION", length=500, nullable = true, updatable = true)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = PersonAttributeGroupStoreDefinitionImpl.class)
    @JoinColumn(name = "PAG_STORE_DEF_ID", nullable = false)
    private IPersonAttributeGroupStoreDefinition store;
    
    @ManyToMany(cascade=CascadeType.ALL, targetEntity=PersonAttributeGroupDefinitionImpl.class)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name="UP_PAG_MEMBERS_DEF", joinColumns = {@JoinColumn(name="PAG_DEF_ID")}, inverseJoinColumns={@JoinColumn(name="PAG_DEF_MEMBER_ID")})  
    private List<IPersonAttributeGroupDefinition> members = new ArrayList<IPersonAttributeGroupDefinition>(0);
    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="group", targetEntity=PersonAttributeGroupTestGroupDefinitionImpl.class)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<IPersonAttributeGroupTestGroupDefinition> testGroups = new ArrayList<IPersonAttributeGroupTestGroupDefinition>(0);

    @Override
    public EntityIdentifier getEntityIdentifier() {
        return new EntityIdentifier(String.valueOf(this.internalPersonAttributeGroupDefinitionId), PersonAttributeGroupDefinitionImpl.class);
    }

    @Override
    public long getId() {
        return internalPersonAttributeGroupDefinitionId;
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
    public List<IPersonAttributeGroupDefinition> getMembers() {
        return members;
    }

    @Override
    public void setMembers(List<IPersonAttributeGroupDefinition> members) {
        this.members = members;
    }

    @Override
    public List<IPersonAttributeGroupTestGroupDefinition> getTestGroups() {
        return testGroups;
    }

    @Override
    public void setTestGroups(List<IPersonAttributeGroupTestGroupDefinition> testGroups) {
        this.testGroups = testGroups;
    }

    public IPersonAttributeGroupStoreDefinition getStore() {
        return this.store;
    }
 
    public void setStore(IPersonAttributeGroupStoreDefinition store) {
        this.store = store;
    }

}
