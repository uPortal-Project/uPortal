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

import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.TimeDimension;

/**
 * Base of all aggregations which includes time, date, interval, and duration
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface BaseAggregation {
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
     * @return Duration in minutes of the aggregated time span
     */
    int getDuration();
}
