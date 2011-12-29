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

/**
 * Enumeration of all time intervals the event aggregation can handle. All of the example ranges are inclusive on both ends
 * 
 * @author Eric Dalquist
 */
public enum Interval {
    /**
     * 1 Minute
     */
    MINUTE,
    /**
     * 5 Minutes (0-4,5-9,...,55-59)
     */
    FIVE_MINUTE,
    /**
     * 1 Hour (minutes 0-59)
     */
    HOUR,
    /**
     * 1 Day (hours 0-23)
     */
    DAY,
    /**
     * 1 Week (7 days, Sunday-Saturday)
     */
    WEEK,
    /**
     * 1 Calendar month (date 1 - last date of month)
     */
    MONTH,
    /**
     * 3 Calendar months (Jan 1 - Mar 31, Apr 1 - Jun 30, Jul 1 - Sep 30, Oct 1 - Dec 31)
     */
    CALENDAR_QUARTER,
    /**
     * As defined by the deployer, unusable unless term boundaries have been configured.
     */
    ACADEMIC_TERM,
    /**
     * 1 Year (Jan 1 - Dec 31) 
     */
    YEAR;
}