package org.jasig.portal.events.aggr;

import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * Fields that make up the primary key for a {@link BaseAggregation}. {@link BaseAggregation}
 * subclasses may define their own extension of this interface if additional fields are needed
 * 
 * @author Eric Dalquist
 */
public interface BaseAggregationKey {
    /**
     * @return The time of day the aggregation is for
     */
    TimeDimension getTimeDimension();
    
    /**
     * @return The day the aggregation is for
     */
    DateDimension getDateDimension();
    
    /**
     * @return The interval the aggregation is for
     */
    AggregationInterval getInterval();
    
    /**
     * @return The group this aggregation is for, null if it is for all users
     */
    AggregatedGroupMapping getAggregatedGroup();
}