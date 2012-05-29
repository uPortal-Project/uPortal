package org.jasig.portal.events.aggr;

import java.util.Collection;

import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.joda.time.DateTime;

/**
 * Extension of the {@link BaseAggregationDao} that provides create/update operations 
 * 
 * @author Eric Dalquist
 * @param <T> Aggregation type
 */
public interface BaseAggregationPrivateDao<T extends BaseAggregationImpl> extends BaseAggregationDao<T> {
    /**
     * Aggregations that have not been closed for an interval
     * 
     * @param start the start {@link DateTime} of the range, inclusive
     * @param end the end {@link DateTime} of the range, exclusive
     * @param interval the aggregation interval to query for
     */
    Collection<T> getUnclosedAggregations(DateTime start, DateTime end, AggregationInterval interval);
    
    /**
     * Create a new aggregation for the specified date, time, interval and group
     */
    T createAggregation(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval, AggregatedGroupMapping aggregatedGroup);
    
    /**
     * @param aggregation The aggregation to update
     */
    void updateAggregation(T aggregation);
}
