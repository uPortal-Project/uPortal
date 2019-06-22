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
package org.apereo.portal.groups.pags.dao.jpa;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;
import org.dom4j.DocumentHelper;
import org.dom4j.QName;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalIdCache;

@Entity
@Table(name = "UP_PAGS_TEST_GROUP")
@SequenceGenerator(
        name = "UP_PAGS_TEST_GROUP_GEN",
        sequenceName = "UP_PAGS_TEST_GROUP_SEQ",
        allocationSize = 5)
@TableGenerator(
        name = "UP_PAGS_TEST_GROUP_GEN",
        pkColumnValue = "UP_PAGS_TEST_GROUP",
        allocationSize = 5)
@NaturalIdCache(
        region =
                "org.apereo.portal.groups.pags.dao.jpa.PersonAttributesGroupTestGroupDefinitionImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersonAttributesGroupTestGroupDefinitionImpl
        implements IPersonAttributesGroupTestGroupDefinition {
    public PersonAttributesGroupTestGroupDefinitionImpl() {
        super();
    }

    public PersonAttributesGroupTestGroupDefinitionImpl(IPersonAttributesGroupDefinition group) {
        super();
        this.group = group;
    }

    @Id
    @GeneratedValue(generator = "UP_PAGS_TEST_GROUP_GEN")
    @Column(name = "PAGS_TEST_GROUP_ID")
    private long id = -1L;

    @Version
    @Column(name = "ENTITY_VERSION")
    private long entityVersion;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = PersonAttributesGroupDefinitionImpl.class)
    @JoinColumn(name = "PAGS_GROUP_ID", nullable = false)
    @JsonBackReference // Addresses infinite recursion by excluding from serialization
    private IPersonAttributesGroupDefinition group;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            mappedBy = "testGroup",
            targetEntity = PersonAttributesGroupTestDefinitionImpl.class,
            orphanRemoval = true)
    @JsonManagedReference // Managing infinite recursion;  this is a "forward" reference and WILL be
    // included
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<IPersonAttributesGroupTestDefinition> tests =
            new HashSet<IPersonAttributesGroupTestDefinition>(0);

    @Override
    @JsonIgnore
    public long getId() {
        return id;
    }

    @Override
    public Set<IPersonAttributesGroupTestDefinition> getTests() {
        // Defensive copy...
        return new HashSet<IPersonAttributesGroupTestDefinition>(tests);
    }

    @Override
    public void setTests(Set<IPersonAttributesGroupTestDefinition> tests) {
        // We need to replace the contents of the collection, not the reference
        // to the collection itself;  otherwise we mess with Hibernate.
        this.tests.clear();
        this.tests.addAll(tests);
    }

    @Override
    public IPersonAttributesGroupDefinition getGroup() {
        return group;
    }

    @Override
    public void setGroup(IPersonAttributesGroupDefinition group) {
        this.group = group;
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public void toElement(org.dom4j.Element parent) {
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        org.dom4j.Element elementTestGroup = DocumentHelper.createElement(new QName("test-group"));
        for (IPersonAttributesGroupTestDefinition test : tests) {
            test.toElement(elementTestGroup);
        }
        parent.add(elementTestGroup);
    }
}
