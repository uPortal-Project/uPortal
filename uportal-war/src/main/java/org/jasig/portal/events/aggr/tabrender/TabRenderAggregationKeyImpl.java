package org.jasig.portal.events.aggr.tabrender;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationKeyImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * Basic impl of {@link TabRenderAggregationKey}
 * 
 * @author Eric Dalquist
 */
public class TabRenderAggregationKeyImpl extends BaseAggregationKeyImpl implements TabRenderAggregationKey {
    private final String tabName;
    
    public TabRenderAggregationKeyImpl(AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping, String tabName) {
        super(aggregationInterval, aggregatedGroupMapping);
        this.tabName = tabName;
    }

    public TabRenderAggregationKeyImpl(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval aggregationInterval, String tabName) {
        super(dateDimension, timeDimension, aggregationInterval);
        this.tabName = tabName;
    }

    public TabRenderAggregationKeyImpl(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval aggregationInterval, AggregatedGroupMapping aggregatedGroupMapping, String tabName) {
        super(dateDimension, timeDimension, aggregationInterval, aggregatedGroupMapping);
        this.tabName = tabName;
    }

    @Override
    public String getTabName() {
        return this.tabName;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((tabName == null) ? 0 : tabName.hashCode());
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
        TabRenderAggregationKeyImpl other = (TabRenderAggregationKeyImpl) obj;
        if (tabName == null) {
            if (other.tabName != null)
                return false;
        }
        else if (!tabName.equals(other.tabName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TabRenderAggregationKeyImpl [tabName=" + tabName + ", getDateDimension()=" + getDateDimension()
                + ", getTimeDimension()=" + getTimeDimension() + ", getInterval()=" + getInterval()
                + ", getAggregatedGroup()=" + getAggregatedGroup() + "]";
    }
}