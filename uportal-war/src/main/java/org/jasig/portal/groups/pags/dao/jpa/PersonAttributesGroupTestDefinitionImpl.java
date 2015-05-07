/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.groups.pags.dao.jpa;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.dom4j.DocumentHelper;
import org.dom4j.QName;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalIdCache;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
@Entity
@Table(name = JpaPersonAttributesGroupTestDefinitionDao.TABLENAME_PREFIX)
@SequenceGenerator(
        name=JpaPersonAttributesGroupTestDefinitionDao.TABLENAME_PREFIX + "_GEN",
        sequenceName="UP_PAGS_TEST_SEQ",
        allocationSize=5
    )
@TableGenerator(
        name=JpaPersonAttributesGroupTestDefinitionDao.TABLENAME_PREFIX + "_GEN",
        pkColumnValue="UP_PAGS_TEST",
        allocationSize=5
    )
@NaturalIdCache(region = "org.jasig.portal.groups.pags.dao.jpa.PersonAttributesGroupTestDefinitionImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersonAttributesGroupTestDefinitionImpl implements IPersonAttributesGroupTestDefinition {
    public PersonAttributesGroupTestDefinitionImpl() {
        super();
    }
    public PersonAttributesGroupTestDefinitionImpl(
            IPersonAttributesGroupTestGroupDefinition testGroup, String attributeName,
            String testerClass, String testValue, Set<String> includes,
            Set<String> excludes) {
        super();
        this.testGroup = testGroup;
        this.attributeName = attributeName;
        this.testerClass = testerClass;
        this.testValue = testValue;
        this.includes = new HashSet<String>(includes);  // defensive copy
        this.excludes = new HashSet<String>(excludes);      // defensive copy
    }

    @Id
    @GeneratedValue(generator = "UP_PAGS_TEST_GEN")
    @Column(name = "PAGS_TEST_ID")
    private long internalPersonAttributesGroupTestDefinitionId;

    @Version
    @Column(name = "ENTITY_VERSION")
    private long entityVersion;

    @Column(name = "ATTRIBUTE_NAME", length=500, nullable = true)
    private String attributeName;

    @Column(name = "TESTER_CLASS", length=500, nullable = true)
    private String testerClass;

    @Column(name = "TEST_VALUE", length=500, nullable = true)
    private String testValue;

    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name=JpaPersonAttributesGroupTestDefinitionDao.TABLENAME_PREFIX + "_INCLUDES")
    private Set<String> includes = Collections.emptySet();

    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name=JpaPersonAttributesGroupTestDefinitionDao.TABLENAME_PREFIX + "_EXCLUDES")
    private Set<String> excludes = Collections.emptySet();

    @ManyToOne(fetch = FetchType.EAGER, targetEntity=PersonAttributesGroupTestGroupDefinitionImpl.class)
    @JoinColumn(name = "PAGS_TEST_GROUP_ID", nullable = false)
    private IPersonAttributesGroupTestGroupDefinition testGroup;

    @Override
    public EntityIdentifier getEntityIdentifier() {
        return new EntityIdentifier(String.valueOf(this.internalPersonAttributesGroupTestDefinitionId), PersonAttributesGroupTestDefinitionImpl.class);
    }

    @Override
    public long getId() {
        return internalPersonAttributesGroupTestDefinitionId;
    }

    @Override
    public String getAttributeName() {
        return this.attributeName;
    }

    @Override
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String getTesterClassName() {
        return this.testerClass;
    }

    @Override
    public void setTesterClassName(String className) {
        this.testerClass = className;
    }

    @Override
    public String getTestValue() {
        return this.testValue;
    }

    @Override
    public void setTestValue(String testValue) {
        this.testValue= testValue;
    }

    @Override
    public Set<String> getIncludes() {
        return Collections.unmodifiableSet(includes);
    }

    @Override
    public void setIncludes(Set<String> includes) {
        // Need to keep the JPA-managed collection
        this.includes.clear();
        this.includes.addAll(includes);
    }

    @Override
    public Set<String> getExcludes() {
        return Collections.unmodifiableSet(excludes);
    }

    @Override
    public void setExcludes(Set<String> excludes) {
        // Need to keep the JPA-managed collection
        this.excludes.clear();
        this.excludes.addAll(excludes);
    }

    @Override
    public IPersonAttributesGroupTestGroupDefinition getTestGroup() {
        return testGroup;
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public void setTestGroup(IPersonAttributesGroupTestGroupDefinition testGroup) {
        this.testGroup = testGroup;
    }
    @Override
    public void toElement(org.dom4j.Element parent) {

        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        org.dom4j.Element elementTest = DocumentHelper.createElement(new QName("test"));
        elementTest.addElement("attribute-name").addText(this.getAttributeName());
        elementTest.addElement("tester-class").addText(this.getTesterClassName());
        elementTest.addElement("test-value").addText(this.getTestValue());
        for (String incl : includes) {
            elementTest.addElement("includes").addText(incl);
        }
        for (String excl : excludes) {
            elementTest.addElement("excludes").addText(excl);
        }
        parent.add(elementTest);
    }

}
