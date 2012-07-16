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

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

/**
 * Enumeration of all time intervals the event aggregation can handle. All of the example ranges are inclusive on both ends
 * 
 * @author Eric Dalquist
 */
public enum AggregationInterval {
    /**
     * 1 Minute
     * @see DateTimeFieldType#minuteOfHour()
     */
    MINUTE(DateTimeFieldType.minuteOfHour()),
    /**
     * 5 Minutes (0-4,5-9,...,55-59)
     */
    FIVE_MINUTE(null),
    /**
     * 1 Hour
     * @see DateTimeFieldType#hourOfDay()
     */
    HOUR(DateTimeFieldType.hourOfDay()),
    /**
     * 1 Day
     * @see DateTimeFieldType#dayOfMonth()
     */
    DAY(DateTimeFieldType.dayOfMonth()),
    /**
     * 1 Week
     * @see DateTimeFieldType#weekOfWeekyear()
     */
    WEEK(DateTimeFieldType.weekOfWeekyear()),
    /**
     * 1 Calendar month 
     * @see DateTimeFieldType#monthOfYear()
     */
    MONTH(DateTimeFieldType.monthOfYear()),
    /**
     * As defined by the deployer, divides the calendar into 4 sections. 
     * Default configuration is: 3 Calendar months (Jan 1 - Mar 31, Apr 1 - Jun 30, Jul 1 - Sep 30, Oct 1 - Dec 31)
     */
    CALENDAR_QUARTER(null),
    /**
     * As defined by the deployer, unusable unless term boundaries have been configured.
     */
    ACADEMIC_TERM(null),
    /**
     * 1 Year
     * @see DateTimeFieldType#year() 
     */
    YEAR(DateTimeFieldType.year());
    
    private final DateTimeFieldType dateTimeFieldType;

    /**
     * @param dateTimeFieldType
     */
    private AggregationInterval(DateTimeFieldType dateTimeFieldType) {
        this.dateTimeFieldType = dateTimeFieldType;
    }

    /**
     * @return the {@link DateTimeFieldType} for the {@link AggregationInterval}, null if there is no mapping
     */
    public DateTimeFieldType getDateTimeFieldType() {
        return this.dateTimeFieldType;
    }
    
    /**
     * Determine the starting DateTime (inclusive) of an interval based on an instant in time
     * 
     * @param instant The instant in time to get the interval starting value for
     * @return The start of this interval in relation to the provided instant
     */
    public DateTime determineStart(DateTime instant) {
        if (this.dateTimeFieldType != null) {
            return instant.property(this.dateTimeFieldType).roundFloorCopy();
        }
        
        if (this == AggregationInterval.FIVE_MINUTE) {
            return instant.hourOfDay().roundFloorCopy().plusMinutes((instant.getMinuteOfHour() / 5) * 5);
        }
        
        throw new IllegalArgumentException("Cannot compute interval start time for " + this + " please use " + AggregationIntervalHelper.class);
    }
    
    /**
     * Determine the ending DateTime (exclusive) of an interval based on an instant in time
     * 
     * @param instant The start of an instant
     * @return
     */
    public DateTime determineEnd(DateTime instant) {
        if (this.dateTimeFieldType != null) {
            final DateTime start = instant.property(this.dateTimeFieldType).roundFloorCopy();
            return start.property(this.dateTimeFieldType).addToCopy(1);
        }
        
        if (this == AggregationInterval.FIVE_MINUTE) {
            final DateTime start = instant.hourOfDay().roundFloorCopy().plusMinutes((instant.getMinuteOfHour() / 5) * 5);
            return start.plusMinutes(5);
        }
        
        throw new IllegalArgumentException("Cannot compute interval end time for " + this + " please use " + AggregationIntervalHelper.class);
    }
}