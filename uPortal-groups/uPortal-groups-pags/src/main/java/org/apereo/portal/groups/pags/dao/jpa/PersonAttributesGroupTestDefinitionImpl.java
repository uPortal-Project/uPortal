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
import javax.persistence.Cacheable;
import javax.persistence.Column;
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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;
import org.dom4j.DocumentHelper;
import org.dom4j.QName;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalIdCache;

@Entity
@Table(name = "UP_PAGS_TEST")
@SequenceGenerator(name = "UP_PAGS_TEST_GEN", sequenceName = "UP_PAGS_TEST_SEQ", allocationSize = 5)
@TableGenerator(name = "UP_PAGS_TEST_GEN", pkColumnValue = "UP_PAGS_TEST", allocationSize = 5)
@NaturalIdCache(
        region =
                "org.apereo.portal.groups.pags.dao.jpa.PersonAttributesGroupTestDefinitionImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersonAttributesGroupTestDefinitionImpl
        implements IPersonAttributesGroupTestDefinition {
    public PersonAttributesGroupTestDefinitionImpl() {
        super();
    }

    public PersonAttributesGroupTestDefinitionImpl(
            IPersonAttributesGroupTestGroupDefinition testGroup,
            String attributeName,
            String testerClass,
            String testValue) {
        super();
        this.testGroup = testGroup;
        this.attributeName = attributeName;
        this.testerClass = testerClass;
        this.testValue = testValue;
    }

    @Id
    @GeneratedValue(generator = "UP_PAGS_TEST_GEN")
    @Column(name = "PAGS_TEST_ID")
    private long id = -1L;

    @Version
    @Column(name = "ENTITY_VERSION")
    private long entityVersion;

    @Column(name = "ATTRIBUTE_NAME", length = 500, nullable = true)
    private String attributeName;

    @Column(name = "TESTER_CLASS", length = 500, nullable = true)
    private String testerClass;

    @Column(name = "TEST_VALUE", length = 500, nullable = true)
    private String testValue;

    @ManyToOne(
            fetch = FetchType.EAGER,
            targetEntity = PersonAttributesGroupTestGroupDefinitionImpl.class)
    @JoinColumn(name = "PAGS_TEST_GROUP_ID", nullable = false)
    @JsonBackReference // Addresses infinite recursion by excluding from serialization
    private IPersonAttributesGroupTestGroupDefinition testGroup;

    @Override
    @JsonIgnore
    public long getId() {
        return id;
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
        this.testValue = testValue;
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
        parent.add(elementTest);
    }
}
