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
package org.apereo.portal.events.aggr;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Set;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.session.EventSession;
import org.joda.time.DateTime;

/**
 * {@link EventSession} that provides a filtered view of another {@link EventSession} based on an
 * {@link AggregatedGroupConfig}
 *
 */
class FilteredEventSession implements EventSession {
    private static final long serialVersionUID = 1L;

    private final EventSession parent;
    private final AggregatedGroupConfig aggregatedGroupConfig;
    private final Set<AggregatedGroupMapping> filteredGroupMappings;

    FilteredEventSession(EventSession parent, AggregatedGroupConfig aggregatedGroupConfig) {
        this.parent = parent;
        this.aggregatedGroupConfig = aggregatedGroupConfig;

        final Builder<AggregatedGroupMapping> filteredGroupMappingsBuilder = ImmutableSet.builder();
        for (final AggregatedGroupMapping aggregatedGroupMapping : parent.getGroupMappings()) {
            if (FilteredEventSession.this.aggregatedGroupConfig.isIncluded(
                    aggregatedGroupMapping)) {
                filteredGroupMappingsBuilder.add(aggregatedGroupMapping);
            }
        }
        this.filteredGroupMappings = filteredGroupMappingsBuilder.build();
    }

    @Override
    public void recordAccess(DateTime eventDate) {
        parent.recordAccess(eventDate);
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((this.aggregatedGroupConfig == null)
                                ? 0
                                : this.aggregatedGroupConfig.hashCode());
        result = prime * result + ((this.parent == null) ? 0 : this.parent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        FilteredEventSession other = (FilteredEventSession) obj;
        if (this.aggregatedGroupConfig == null) {
            if (other.aggregatedGroupConfig != null) return false;
        } else if (!this.aggregatedGroupConfig.equals(other.aggregatedGroupConfig)) return false;
        if (this.parent == null) {
            if (other.parent != null) return false;
        } else if (!this.parent.equals(other.parent)) return false;
        return true;
    }

    @Override
    public String toString() {
        return parent.toString();
    }
}
