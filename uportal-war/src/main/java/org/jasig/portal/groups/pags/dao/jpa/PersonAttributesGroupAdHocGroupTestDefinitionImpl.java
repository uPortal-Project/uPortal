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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.dom4j.DocumentHelper;
import org.dom4j.QName;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalIdCache;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupAdHocGroupTestDefinition;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;

import javax.persistence.*;

/**
 * @author Benito J. Gonzalez <bgonzalez@unicon.net>
 */
@Entity
@Table(name = "UP_PAGS_ADHOC_GROUP_TEST")
@SequenceGenerator(
        name="UP_PAGS_ADHOC_GROUP_TEST_GEN",
        sequenceName="UP_PAGS_ADHOC_GROUP_TEST_SEQ",
        allocationSize=5
    )
@TableGenerator(
        name="UP_PAGS_ADHOC_GROUP_TEST_GEN",
        pkColumnValue="UP_PAGS_ADHOC_GROUP_TEST",
        allocationSize=5
    )
@NaturalIdCache(region = "org.jasig.portal.groups.pags.dao.jpa.PersonAttributesGroupAdHocGroupTestDefinitionImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersonAttributesGroupAdHocGroupTestDefinitionImpl implements IPersonAttributesGroupAdHocGroupTestDefinition {

    @Id
    @GeneratedValue(generator = "UP_PAGS_ADHOC_GROUP_TEST_GEN")
    @Column(name = "PAGS_ADHOC_GROUP_TEST_ID")
    private long internalPersonAttributesGroupTestDefinitionId;

    @Version
    @Column(name = "ENTITY_VERSION")
    private long entityVersion;

    @Column(name = "GROUP_NAME", length=500)
    private String groupName;

    @Column(name = "IS_EXCLUDE")
    private Boolean isExclude;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity=PersonAttributesGroupTestGroupDefinitionImpl.class)
    @JoinColumn(name = "PAGS_TEST_GROUP_ID", nullable = false)
    private IPersonAttributesGroupTestGroupDefinition testGroup;

    private PersonAttributesGroupAdHocGroupTestDefinitionImpl() {super();}

    public PersonAttributesGroupAdHocGroupTestDefinitionImpl(IPersonAttributesGroupTestGroupDefinition testGroup, String groupName, Boolean isExclude) {
        super();
        this.testGroup = testGroup;
        this.groupName = groupName;
        this.isExclude = isExclude;
    }

    @Override
    public EntityIdentifier getEntityIdentifier() {
        return new EntityIdentifier(String.valueOf(this.internalPersonAttributesGroupTestDefinitionId), PersonAttributesGroupAdHocGroupTestDefinitionImpl.class);
    }

    @Override
    public long getId() {
        return internalPersonAttributesGroupTestDefinitionId;
    }

    @Override
    public String getGroupName() {
        return this.groupName;
    }

    @Override
    public Boolean getIsExclude() {
        return this.isExclude;
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
            throw new IllegalArgumentException("Argument 'parent' cannot be null.");
        }

        String qname = isExclude ? "not-member-of-group" : "member-of-group";
        org.dom4j.Element elAdHocGroupTest = DocumentHelper.createElement(new QName(qname));
        elAdHocGroupTest.addText(this.getGroupName());
        parent.add(elAdHocGroupTest);
    }

}
