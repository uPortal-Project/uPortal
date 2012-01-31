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

import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.joda.time.DateMidnight;

/**
 * DAO used to query information about login aggregates: Total Logins and Unique Logins per date,time,interval,group
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface LoginAggregationDao<T extends LoginAggregation> {
    
    /**
     * login aggregations in a date range for a specified interval and group
     */
    List<LoginAggregation> getLoginAggregations(DateMidnight start, DateMidnight end, AggregationInterval interval, AggregatedGroupMapping... aggregatedGroupMapping);

    /**
     * @return All login aggregations for the date, time and interval
     */
    Set<T> getLoginAggregationsForInterval(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval);
    
    LoginAggregation getLoginAggregation(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval, AggregatedGroupMapping aggregatedGroup);
}