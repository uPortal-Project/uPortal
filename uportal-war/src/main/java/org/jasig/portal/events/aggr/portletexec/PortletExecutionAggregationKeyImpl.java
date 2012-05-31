package org.jasig.portal.events.aggr.portletexec;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationKeyImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * Basic impl of {@link PortletExecutionAggregationKey}
 * 
 * @author Eric Dalquist
 */
public class PortletExecutionAggregationKeyImpl extends BaseAggregationKeyImpl implements PortletExecutionAggregationKey {
    private final String fname;
    private final ExecutionType executionType;
    
    public PortletExecutionAggregationKeyImpl(AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping, String fname, ExecutionType executionType) {
        super(aggregationInterval, aggregatedGroupMapping);
        this.fname = fname;
        this.executionType = executionType;
    }

    public PortletExecutionAggregationKeyImpl(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval aggregationInterval, String fname, ExecutionType executionType) {
        super(dateDimension, timeDimension, aggregationInterval);
        this.fname = fname;
        this.executionType = executionType;
    }

    public PortletExecutionAggregationKeyImpl(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval aggregationInterval, AggregatedGroupMapping aggregatedGroupMapping, String fname, ExecutionType executionType) {
        super(dateDimension, timeDimension, aggregationInterval, aggregatedGroupMapping);
        this.fname = fname;
        this.executionType = executionType;
    }

    @Override
    public String getFname() {
        return this.fname;
    }
    
    @Override
    public ExecutionType getExecutionType() {
        return this.executionType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((executionType == null) ? 0 : executionType.hashCode());
        result = prime * result + ((fname == null) ? 0 : fname.hashCode());
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
        PortletExecutionAggregationKeyImpl other = (PortletExecutionAggregationKeyImpl) obj;
        if (executionType != other.executionType)
            return false;
        if (fname == null) {
            if (other.fname != null)
                return false;
        }
        else if (!fname.equals(other.fname))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletExecutionAggregationKeyImpl [fname=" + fname + ", executionType=" + executionType
                + ", getTimeDimension=" + getTimeDimension() + ", getDateDimension=" + getDateDimension()
                + ", getInterval=" + getInterval() + ", getAggregatedGroup=" + getAggregatedGroup() + "]";
    }
}