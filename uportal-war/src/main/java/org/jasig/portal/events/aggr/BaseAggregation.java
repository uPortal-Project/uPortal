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

import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.joda.time.DateTime;

import java.io.Serializable;


/**
 * Base of all aggregations which includes time, date, interval, group, and duration
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface BaseAggregation<K extends BaseAggregationKey, D extends BaseGroupedAggregationDiscriminator> extends Serializable {
    /**
     * @return The {@link DateTime} the aggregation is for, short cut for getting the same info from {@link #getDateDimension()} and {@link #getTimeDimension()}
     */
    DateTime getDateTime();
    
    /**
     * @return The day the aggregation is for
     */
    DateDimension getDateDimension();
    
    /**
     * @return The time of day the aggregation is for
     */
    TimeDimension getTimeDimension();
    
    /**
     * @return The interval the aggregation is for
     */
    AggregationInterval getInterval();
    
    /**
     * @return The group this aggregation is for, null if it is for all users
     */
    AggregatedGroupMapping getAggregatedGroup();
    
    /**
     * @return Duration in minutes of the aggregated time span
     */
    int getDuration();
    
    /**
     * @return The key for this aggregation
     */
    K getAggregationKey();

    /**
     * Return a discriminator used for organizing aggregation data into separate columns for reporting purposes
     * @return aggregation discriminator
     */
    D getAggregationDiscriminator();


}
