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

package org.jasig.portal.events.aggr.login;

import java.util.List;
import java.util.Set;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.joda.time.DateTime;

/**
 * DAO used to query information about login aggregates: Total Logins and Unique Logins per date,time,interval,group
 * 
 * @author Eric Dalquist
 */
public interface LoginAggregationDao<T extends LoginAggregation> {
    
    /**
     * Login aggregations that have not been closed (still have data in the UIDs table) for an interval
     * 
     * @param start the start {@link DateTime} of the range, inclusive
     * @param end the end {@link DateTime} of the range, exclusive
     * @param interval the aggregation interval to query for
     */
    Set<T> getUnclosedLoginAggregations(DateTime start, DateTime end, AggregationInterval interval);
    
    /**
     * Login aggregations in a date range for a specified interval and group ordered by date/time
     * 
     * @param start the start {@link DateTime} of the range, inclusive
     * @param end the end {@link DateTime} of the range, exclusive
     * @param interval the aggregation interval to query for
     * @param aggregatedGroupMapping The groups to get data for
     */
    List<T> getLoginAggregations(DateTime start, DateTime end, AggregationInterval interval, AggregatedGroupMapping aggregatedGroupMapping, AggregatedGroupMapping... aggregatedGroupMappings);

    /**
     * @return All login aggregations for the date, time and interval
     */
    Set<T> getLoginAggregationsForInterval(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval);
    
    /**
     * Get a specific login aggregation
     */
    LoginAggregation getLoginAggregation(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval, AggregatedGroupMapping aggregatedGroup);
}