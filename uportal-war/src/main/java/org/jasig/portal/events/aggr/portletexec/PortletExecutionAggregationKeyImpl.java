package org.jasig.portal.events.aggr.portletexec;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationKeyImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;

/**
 * Basic impl of {@link PortletExecutionAggregationKey}
 * 
 * @author Eric Dalquist
 */
public class PortletExecutionAggregationKeyImpl extends BaseAggregationKeyImpl implements PortletExecutionAggregationKey {
    private static final long serialVersionUID = 1L;
    
    private final AggregatedPortletMapping portletMapping;
    private final ExecutionType executionType;
    
    public PortletExecutionAggregationKeyImpl(AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping, AggregatedPortletMapping portletMapping, ExecutionType executionType) {
        super(aggregationInterval, aggregatedGroupMapping);
        this.portletMapping = portletMapping;
        this.executionType = executionType;
    }

    public PortletExecutionAggregationKeyImpl(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval aggregationInterval, AggregatedPortletMapping portletMapping, ExecutionType executionType) {
        super(dateDimension, timeDimension, aggregationInterval);
        this.portletMapping = portletMapping;
        this.executionType = executionType;
    }

    public PortletExecutionAggregationKeyImpl(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval aggregationInterval, AggregatedGroupMapping aggregatedGroupMapping, AggregatedPortletMapping portletMapping, ExecutionType executionType) {
        super(dateDimension, timeDimension, aggregationInterval, aggregatedGroupMapping);
        this.portletMapping = portletMapping;
        this.executionType = executionType;
    }

    @Override
    public AggregatedPortletMapping getPortletMapping() {
        return this.portletMapping;
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
        result = prime * result + ((portletMapping == null) ? 0 : portletMapping.hashCode());
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
        if (portletMapping == null) {
            if (other.portletMapping != null)
                return false;
        }
        else if (!portletMapping.equals(other.portletMapping))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletExecutionAggregationKeyImpl [portletMapping=" + portletMapping + ", executionType=" + executionType
                + ", getTimeDimension=" + getTimeDimension() + ", getDateDimension=" + getDateDimension()
                + ", getInterval=" + getInterval() + ", getAggregatedGroup=" + getAggregatedGroup() + "]";
    }
}