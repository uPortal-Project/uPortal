/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.events.aggr;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.joda.time.DateTime;

/**
 * Base DAO APIs shared between all {@link BaseAggregation} implementation DAOs
 * 
 * @author Eric Dalquist
 * @param <T> Aggregation type
 */
public interface BaseAggregationDao<
            T extends BaseAggregation<K>, 
            K extends BaseAggregationKey> {
    /**
     * Aggregations in a date range for a specified interval and group(s) ordered by date/time
     * 
     * @param start the start {@link DateTime} of the range, inclusive
     * @param end the end {@link DateTime} of the range, exclusive
     * @param key The {@link BaseAggregationKey#getDateDimension()} and {@link BaseAggregationKey#getTimeDimension()} fields on the key are ignored for this method
     * @param aggregatedGroupMapping Groups in addition to the group specified by {@link BaseAggregationKey#getAggregatedGroup()} to get aggregations for
     */
    List<T> getAggregations(DateTime start, DateTime end, K key, AggregatedGroupMapping... aggregatedGroupMappings);

    /**
     * Get all aggregations regardless of associated {@link AggregatedGroupMapping} 
     * @param key The {@link BaseAggregationKey#getAggregatedGroup()} field is ignored for this method
     * @return All aggregations for the date, time and interval
     */
    Map<K, T> getAggregationsForInterval(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval);
    
    /**
     * @return All aggregation intervals that have aggregation data associated with them 
     */
    Set<AggregationInterval> getAggregationIntervals();
    
    /**
     * @return All aggregated groups that have aggregation data associated with them
     */
    Set<AggregatedGroupMapping> getAggregatedGroupMappings();
    
    /**
     * Get a specific aggregation 
     */
    T getAggregation(K key);
}