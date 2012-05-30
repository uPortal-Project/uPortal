package org.jasig.portal.events.aggr;

import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * Basic impl of {@link BaseAggregationKey}
 * 
 * @author Eric Dalquist
 */
public class BaseAggregationKeyImpl implements BaseAggregationKey {
    private final TimeDimension timeDimension;
    private final DateDimension dateDimension;
    private final AggregationInterval aggregationInterval;
    private final AggregatedGroupMapping aggregatedGroupMapping;

    public BaseAggregationKeyImpl(AggregationInterval aggregationInterval, AggregatedGroupMapping aggregatedGroupMapping) {
        this(null, null, aggregationInterval, aggregatedGroupMapping);
    }
    
    public BaseAggregationKeyImpl(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval aggregationInterval) {
        this(dateDimension, timeDimension, aggregationInterval, null);
    }
    
    public BaseAggregationKeyImpl(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval aggregationInterval, AggregatedGroupMapping aggregatedGroupMapping) {
        this.timeDimension = timeDimension;
        this.dateDimension = dateDimension;
        this.aggregationInterval = aggregationInterval;
        this.aggregatedGroupMapping = aggregatedGroupMapping;
    }

    @Override
    public TimeDimension getTimeDimension() {
        return this.timeDimension;
    }

    @Override
    public DateDimension getDateDimension() {
        return this.dateDimension;
    }

    @Override
    public AggregationInterval getInterval() {
        return this.aggregationInterval;
    }

    @Override
    public AggregatedGroupMapping getAggregatedGroup() {
        return this.aggregatedGroupMapping;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregatedGroupMapping == null) ? 0 : aggregatedGroupMapping.hashCode());
        result = prime * result + ((aggregationInterval == null) ? 0 : aggregationInterval.hashCode());
        result = prime * result + ((dateDimension == null) ? 0 : dateDimension.hashCode());
        result = prime * result + ((timeDimension == null) ? 0 : timeDimension.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof BaseAggregationKey))
            return false;
        BaseAggregationKey other = (BaseAggregationKey) obj;
        if (aggregatedGroupMapping == null) {
            if (other.getAggregatedGroup() != null)
                return false;
        }
        else if (!aggregatedGroupMapping.equals(other.getAggregatedGroup()))
            return false;
        if (aggregationInterval != other.getInterval())
            return false;
        if (dateDimension == null) {
            if (other.getDateDimension() != null)
                return false;
        }
        else if (!dateDimension.equals(other.getDateDimension()))
            return false;
        if (timeDimension == null) {
            if (other.getTimeDimension() != null)
                return false;
        }
        else if (!timeDimension.equals(other.getTimeDimension()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BaseAggregationKeyImpl [dateDimension=" + dateDimension + ", timeDimension=" + timeDimension
                + ", aggregationInterval=" + aggregationInterval + ", aggregatedGroupMapping=" + aggregatedGroupMapping
                + "]";
    }
}