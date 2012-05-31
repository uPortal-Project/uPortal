package org.jasig.portal.events.aggr;

import java.util.Collection;

import org.joda.time.DateTime;

/**
 * Extension of the {@link BaseAggregationDao} that provides create/update operations 
 * 
 * @author Eric Dalquist
 * @param <T> Aggregation type
 * @param <K> The key type for the aggregation
 */
public interface BaseAggregationPrivateDao<
            T extends BaseAggregationImpl, 
            K extends BaseAggregationKey>
        extends BaseAggregationDao<T, K> {
    
    /**
     * Aggregations that have not been closed for an interval
     * 
     * @param start the start {@link DateTime} of the range, inclusive
     * @param end the end {@link DateTime} of the range, exclusive
     * @param interval the interval to look in
     */
    Collection<T> getUnclosedAggregations(DateTime start, DateTime end, AggregationInterval interval);
    
    /**
     * Create a new aggregation for the specified key
     */
    T createAggregation(K key);
    
    /**
     * @param aggregation The aggregation to update
     */
    void updateAggregation(T aggregation);
    
    /**
     * @param aggregations The aggregations to update
     */
    void updateAggregations(Iterable<T> aggregations);
}
