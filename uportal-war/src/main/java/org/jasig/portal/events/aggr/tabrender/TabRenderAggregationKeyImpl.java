package org.jasig.portal.events.aggr.tabrender;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationKeyImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMapping;

/**
 * Basic impl of {@link TabRenderAggregationKey}
 * 
 * @author Eric Dalquist
 */
class TabRenderAggregationKeyImpl extends BaseAggregationKeyImpl implements TabRenderAggregationKey {
    private static final long serialVersionUID = 1L;
    
    private final AggregatedTabMapping tabMapping;
    
    public TabRenderAggregationKeyImpl(TabRenderAggregation baseAggregation) {
        super(baseAggregation);
        this.tabMapping = baseAggregation.getTabMapping();
    }

    public TabRenderAggregationKeyImpl(AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping, AggregatedTabMapping tabMapping) {
        super(aggregationInterval, aggregatedGroupMapping);
        this.tabMapping = tabMapping;
    }

    public TabRenderAggregationKeyImpl(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval aggregationInterval, AggregatedGroupMapping aggregatedGroupMapping, AggregatedTabMapping tabMapping) {
        super(dateDimension, timeDimension, aggregationInterval, aggregatedGroupMapping);
        this.tabMapping = tabMapping;
    }

    @Override
    public AggregatedTabMapping getTabMapping() {
        return this.tabMapping;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((tabMapping == null) ? 0 : tabMapping.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TabRenderAggregationKey other = (TabRenderAggregationKey) obj;
        if (tabMapping == null) {
            if (other.getTabMapping() != null)
                return false;
        }
        else if (!tabMapping.equals(other.getTabMapping()))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "TabRenderAggregationKey [dateDimension=" + getDateDimension() + ", timeDimension="
                + getTimeDimension() + ", interval=" + getInterval() + ", aggregatedGroup=" + getAggregatedGroup()
                + ", tabMapping=" + tabMapping + "]";
    }
}