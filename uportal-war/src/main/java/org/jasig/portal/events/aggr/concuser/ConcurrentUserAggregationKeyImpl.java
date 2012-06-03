package org.jasig.portal.events.aggr.concuser;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationKeyImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

class ConcurrentUserAggregationKeyImpl extends BaseAggregationKeyImpl implements ConcurrentUserAggregationKey {
    private static final long serialVersionUID = 1L;
    
    public ConcurrentUserAggregationKeyImpl(ConcurrentUserAggregation concurrentUserAggregation) {
        super(concurrentUserAggregation);
    }

    public ConcurrentUserAggregationKeyImpl(AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping) {
        super(aggregationInterval, aggregatedGroupMapping);
    }

    public ConcurrentUserAggregationKeyImpl(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval aggregationInterval, AggregatedGroupMapping aggregatedGroupMapping) {
        super(dateDimension, timeDimension, aggregationInterval, aggregatedGroupMapping);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ConcurrentUserAggregationKey))
            return false;
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "ConcurrentUserAggregationKey [dateDimension=" + getDateDimension() + ", timeDimension="
                + getTimeDimension() + ", interval=" + getInterval() + ", aggregatedGroup=" + getAggregatedGroup()
                + "]";
    }
}
