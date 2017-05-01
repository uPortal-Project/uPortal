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
package org.apereo.portal.events.aggr.session;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import org.apache.commons.lang.Validate;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMappingImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 */
@Entity
@Table(name = "UP_EVENT_SESSION")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(
    name = "UP_EVENT_SESSION_GEN",
    sequenceName = "UP_EVENT_SESSION_SEQ",
    allocationSize = 100
)
@TableGenerator(
    name = "UP_EVENT_SESSION_GEN",
    pkColumnValue = "UP_EVENT_SESSION_PROP",
    allocationSize = 100
)
@org.hibernate.annotations.Table(
    appliesTo = "UP_EVENT_SESSION",
    indexes =
            @Index(
                name = "IDX_UP_EVENT_SESSION_DATE",
                columnNames = {"LAST_ACCESSED"}
            )
)
@NaturalIdCache(region = "org.apereo.portal.events.aggr.session.EventSessionImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EventSessionImpl implements EventSession, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_EVENT_SESSION_GEN")
    @Column(name = "ID")
    private final long id;

    @NaturalId
    @Column(name = "SESSION_ID", length = 500, nullable = false, updatable = false)
    private final String eventSessionId;

    @ManyToMany(targetEntity = AggregatedGroupMappingImpl.class, fetch = FetchType.EAGER)
    @JoinTable(
        name = "UP_EVENT_SESSION_GROUPS",
        inverseJoinColumns = @JoinColumn(name = "GROUP_ID")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Set<AggregatedGroupMapping> groupMappings;

    @Column(name = "LAST_ACCESSED", nullable = false)
    @Type(type = "dateTime")
    private DateTime lastAccessed;

    @Transient private Set<AggregatedGroupMapping> unmodifiableGroupMappings;

    @SuppressWarnings("unused")
    private EventSessionImpl() {
        this.id = -1;
        this.eventSessionId = null;
        this.groupMappings = null;
    }

    EventSessionImpl(
            String eventSessionId, DateTime eventDate, Set<AggregatedGroupMapping> groupMappings) {
        Validate.notNull(eventSessionId);
        Validate.notNull(groupMappings);

        this.id = -1;
        this.eventSessionId = eventSessionId;
        this.groupMappings = groupMappings;
        this.lastAccessed = eventDate;
    }

    @Override
    public void recordAccess(DateTime eventDate) {
        this.lastAccessed = eventDate;
    }

    @Override
    public String getEventSessionId() {
        return this.eventSessionId;
    }

    @Override
    public Set<AggregatedGroupMapping> getGroupMappings() {
        Set<AggregatedGroupMapping> ugm = this.unmodifiableGroupMappings;
        if (ugm == null) {
            ugm = Collections.unmodifiableSet(this.groupMappings);
            this.unmodifiableGroupMappings = ugm;
        }
        return ugm;
    }

    public void addGroupMappings(Set<AggregatedGroupMapping> groupMappings) {
        this.groupMappings.addAll(groupMappings);
        this.unmodifiableGroupMappings = null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventSessionId == null) ? 0 : eventSessionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        EventSessionImpl other = (EventSessionImpl) obj;
        if (eventSessionId == null) {
            if (other.eventSessionId != null) return false;
        } else if (!eventSessionId.equals(other.eventSessionId)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "EventSessionImpl [id="
                + this.id
                + ", eventSessionId="
                + this.eventSessionId
                + ", lastAccessed="
                + this.lastAccessed
                + "]";
    }
}
