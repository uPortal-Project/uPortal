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

package org.jasig.portal.events.aggr.session;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.jasig.portal.events.aggr.AggregatedGroupConfig;
import org.jasig.portal.events.aggr.IPortalEventAggregator;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMappingImpl;
import org.jasig.portal.utils.Tuple;
import org.joda.time.DateTime;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_EVENT_SESSION")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UP_EVENT_SESSION_GEN",
        sequenceName="UP_EVENT_SESSION_SEQ",
        allocationSize=100
    )
@TableGenerator(
        name="UP_EVENT_SESSION_GEN",
        pkColumnValue="UP_EVENT_SESSION_PROP",
        allocationSize=100
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EventSessionImpl implements EventSession, Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(generator = "UP_EVENT_SESSION_GEN")
    @Column(name="ID")
    private final long id;
    
    @NaturalId
    @Column(name="SESSION_ID", length=500, nullable=false, updatable=false)
    private final String eventSessionId;
    
    @ManyToMany(targetEntity=AggregatedGroupMappingImpl.class, fetch=FetchType.EAGER)
    @JoinTable(name="UP_EVENT_SESSION_GROUPS", inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Set<AggregatedGroupMapping> groupMappings;
    
    @Column(name="LAST_ACCESSED", nullable=false)
    @Type(type="dateTime")
    private DateTime lastAccessed;
    
    @Transient
    private Set<AggregatedGroupMapping> unmodifiableGroupMappings;
    
    @Transient
    private final Map<Class<? extends IPortalEventAggregator>, Tuple<AggregatedGroupConfig, EventSession>> filteredEventSessionCache 
            = new ConcurrentHashMap<Class<? extends IPortalEventAggregator>, Tuple<AggregatedGroupConfig,EventSession>>();
    
    @SuppressWarnings("unused")
    private EventSessionImpl() {
        this.id = -1;
        this.eventSessionId = null;
        this.groupMappings = null;
    }
    
    EventSessionImpl(String eventSessionId, Set<AggregatedGroupMapping> groupMappings) {
        Validate.notNull(eventSessionId);
        Validate.notNull(groupMappings);
        
        this.id = -1;
        this.eventSessionId = eventSessionId;
        this.groupMappings = groupMappings;
        this.lastAccessed = DateTime.now();
    }
    
    void recordAccess() {
        this.lastAccessed = DateTime.now();
    }
    
    @Override
    public EventSession getFilteredEventSession(AggregatedGroupConfig groupConfig) {
        return getFilteredEventSession(this, groupConfig);
    }
    
    private EventSession getFilteredEventSession(EventSession session, AggregatedGroupConfig groupConfig) {
        Tuple<AggregatedGroupConfig, EventSession> tuple = this.filteredEventSessionCache.get(groupConfig.getAggregatorType());
        if (tuple == null || tuple.first.getVersion() < groupConfig.getVersion()) {
            //Cached version doesn't exist or is old
            tuple = new Tuple<AggregatedGroupConfig, EventSession>(groupConfig, 
                    new FilteringEventSession(session, groupConfig));
            this.filteredEventSessionCache.put(groupConfig.getAggregatorType(), tuple);
        }
        return tuple.second;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventSessionId == null) ? 0 : eventSessionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventSessionImpl other = (EventSessionImpl) obj;
        if (eventSessionId == null) {
            if (other.eventSessionId != null)
                return false;
        }
        else if (!eventSessionId.equals(other.eventSessionId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EventSessionImpl [id=" + this.id + ", eventSessionId=" + this.eventSessionId + ", lastAccessed="
                + this.lastAccessed + "]";
    }
    
    private class FilteringEventSession implements EventSession {
        private final EventSession parent;
        private final AggregatedGroupConfig aggregatedGroupConfig;
        private final Set<AggregatedGroupMapping> filteredGroupMappings;
        
        private FilteringEventSession(EventSession parent, AggregatedGroupConfig aggregatedGroupConfig) {
            this.parent = parent;
            this.aggregatedGroupConfig = aggregatedGroupConfig;
            
            this.filteredGroupMappings = Sets.filter(parent.getGroupMappings(), new Predicate<AggregatedGroupMapping>() {
                @Override
                public boolean apply(AggregatedGroupMapping input) {
                    return FilteringEventSession.this.aggregatedGroupConfig.isIncluded(input);
                }
            });
        }

        @Override
        public String getEventSessionId() {
            return parent.getEventSessionId();
        }

        @Override
        public Set<AggregatedGroupMapping> getGroupMappings() {
            return this.filteredGroupMappings;
        }

        @Override
        public EventSession getFilteredEventSession(AggregatedGroupConfig groupConfig) {
            return EventSessionImpl.this.getFilteredEventSession(this, groupConfig);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((this.aggregatedGroupConfig == null) ? 0 : this.aggregatedGroupConfig.hashCode());
            result = prime * result + ((this.parent == null) ? 0 : this.parent.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FilteringEventSession other = (FilteringEventSession) obj;
            if (this.aggregatedGroupConfig == null) {
                if (other.aggregatedGroupConfig != null)
                    return false;
            }
            else if (!this.aggregatedGroupConfig.equals(other.aggregatedGroupConfig))
                return false;
            if (this.parent == null) {
                if (other.parent != null)
                    return false;
            }
            else if (!this.parent.equals(other.parent))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return parent.toString();
        }
    }
}
