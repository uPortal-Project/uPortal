package org.jasig.portal.events.aggr;

import java.util.Set;

import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.session.EventSession;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

class FilteringEventSession implements EventSession {
    private final EventSession parent;
    private final AggregatedGroupConfig aggregatedGroupConfig;
    private final Set<AggregatedGroupMapping> filteredGroupMappings;
    
    FilteringEventSession(EventSession parent, AggregatedGroupConfig aggregatedGroupConfig) {
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
