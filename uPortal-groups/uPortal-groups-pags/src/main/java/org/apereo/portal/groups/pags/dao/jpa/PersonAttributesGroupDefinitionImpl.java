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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;
import org.apereo.portal.groups.pags.dao.PagsService;
import org.apereo.portal.security.IPerson;
import org.dom4j.DocumentHelper;
import org.dom4j.QName;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NaturalIdCache;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Slf4j
@Entity
@Table(name = "UP_PAGS_GROUP")
@SequenceGenerator(
        name = "UP_PAGS_GROUP_GEN",
        sequenceName = "UP_PAGS_GROUP_SEQ",
        allocationSize = 5)
@TableGenerator(name = "UP_PAGS_GROUP_GEN", pkColumnValue = "UP_PAGS_GROUP", allocationSize = 5)
@NaturalIdCache(
        region =
                "org.apereo.portal.groups.pags.dao.jpa.PersonAttributesGroupDefinitionImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersonAttributesGroupDefinitionImpl implements IPersonAttributesGroupDefinition {
    public PersonAttributesGroupDefinitionImpl() {
        super();
    }

    public PersonAttributesGroupDefinitionImpl(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }

    @Id
    @GeneratedValue(generator = "UP_PAGS_GROUP_GEN")
    @Column(name = "PAGS_GROUP_ID")
    private long id = -1L;

    @Version
    @Column(name = "ENTITY_VERSION")
    private long entityVersion;

    /**
     * Per rules for groups and entities in general, the name needs to be unique and non-null. (In
     * face it needs to be unique across all groups, not merely this table.)
     */
    @EqualsAndHashCode.Include
    @Column(name = "NAME", length = 500, unique = true, nullable = false)
    private String name;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @ManyToMany(cascade = CascadeType.ALL, targetEntity = PersonAttributesGroupDefinitionImpl.class)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "UP_PAGS_GROUP_MEMBERS",
            joinColumns = {@JoinColumn(name = "PAGS_GROUP_ID")},
            inverseJoinColumns = {@JoinColumn(name = "PAGS_GROUP_MEMBER_ID")})
    @JsonSerialize(using = PagsDefinitionJsonUtils.DefinitionLinkJsonSerializer.class)
    @JsonDeserialize(using = PagsDefinitionJsonUtils.DefinitionLinkJsonDeserializer.class)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<IPersonAttributesGroupDefinition> members =
            new HashSet<IPersonAttributesGroupDefinition>(0);

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "group",
            targetEntity = PersonAttributesGroupTestGroupDefinitionImpl.class,
            orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonDeserialize(
            using =
                    PagsDefinitionJsonUtils.TestGroupJsonDeserializer
                            .class) // Auto-serialization of interface references works;
    // deserialization doesn't (besides we have
    // some extra work to do)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<IPersonAttributesGroupTestGroupDefinition> testGroups =
            new HashSet<IPersonAttributesGroupTestGroupDefinition>(0);

    @Override
    @JsonIgnore
    public long getId() {
        return id;
    }

    @Override
    @JsonIgnore
    public EntityIdentifier getCompositeEntityIdentifierForGroup() {
        return new EntityIdentifier(
                PagsService.SERVICE_NAME_PAGS + "." + this.getName(), IPerson.class);
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
    public Set<IPersonAttributesGroupDefinition> getMembers() {
        // Defensive copy...
        assert (!this.members.contains(this));
        return new HashSet<IPersonAttributesGroupDefinition>(members);
    }

    @Override
    public Set<IPersonAttributesGroupDefinition> getDeepMembers() {
        assert (!this.members.contains(this));
        Set<IPersonAttributesGroupDefinition> seen =
                new HashSet<IPersonAttributesGroupDefinition>();
        return getDeepMembers(seen);
    }

    /* Adding deep members to passed parameter and passing collection as return value. */
    @Override
    public Set<IPersonAttributesGroupDefinition> getDeepMembers(
            Set<IPersonAttributesGroupDefinition> seen) {
        assert (!this.members.contains(this));
        if (seen.contains(this)) {
            log.debug("already processing/visited this {}", this.name);
            return seen;
        }

        /* set of members NOT in alreadySeen */
        Set<IPersonAttributesGroupDefinition> membersNotSeen =
                members.stream().filter(e -> !seen.contains(e)).collect(Collectors.toSet());
        /* get deep members not in alreadySeen */
        seen.addAll(
                membersNotSeen.stream()
                        .flatMap(
                                e -> {
                                    Stream<IPersonAttributesGroupDefinition> p =
                                            e.getDeepMembers(seen).stream();
                                    seen.add(e);
                                    return p;
                                })
                        .collect(Collectors.toSet()));
        return seen;
    }

    @Override
    public void setMembers(Set<IPersonAttributesGroupDefinition> members) {
        // We need to replace the contents of the collection, not the reference
        // to the collection itself;  otherwise we mess with Hibernate.
        assert (this.name != null);
        assert (!members.contains(this));
        Set<IPersonAttributesGroupDefinition> checkedMembers =
                new HashSet<IPersonAttributesGroupDefinition>();
        for (IPersonAttributesGroupDefinition group : members) {
            if (group.getDeepMembers().contains(this)) {
                final String message =
                        "Recursion of group member found for "
                                + name
                                + ", member "
                                + group.getName();
                log.error(message);
                throw new IllegalArgumentException(message);
            } else {
                checkedMembers.add(group);
            }
        }
        this.members.clear();
        this.members.addAll(checkedMembers);
    }

    @Override
    public Set<IPersonAttributesGroupTestGroupDefinition> getTestGroups() {
        // Defensive copy...
        return new HashSet<IPersonAttributesGroupTestGroupDefinition>(testGroups);
    }

    @Override
    public void setTestGroups(Set<IPersonAttributesGroupTestGroupDefinition> testGroups) {
        // We need to replace the contents of the collection, not the reference
        // to the collection itself;  otherwise we mess with Hibernate.
        this.testGroups.clear();
        this.testGroups.addAll(testGroups);
    }

    @Override
    public void toElement(org.dom4j.Element parent) {

        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        parent.addElement("name").addText(this.getName());
        parent.addElement("description").addText(this.getDescription());
        if (!members.isEmpty()) {
            org.dom4j.Element elementMembers = DocumentHelper.createElement(new QName("members"));
            for (IPersonAttributesGroupDefinition member : members) {
                elementMembers.addElement("member-name").addText(member.getName());
            }
            parent.add(elementMembers);
        }

        if (!testGroups.isEmpty()) {
            org.dom4j.Element elementSelectionTest =
                    DocumentHelper.createElement(new QName("selection-test"));
            for (IPersonAttributesGroupTestGroupDefinition testGroup : testGroups) {
                testGroup.toElement(elementSelectionTest);
            }
            parent.add(elementSelectionTest);
        }
    }
}
