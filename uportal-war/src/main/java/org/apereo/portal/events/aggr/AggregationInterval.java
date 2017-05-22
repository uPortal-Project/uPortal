/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.events.aggr;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeUtils;
import org.joda.time.DurationFieldType;
import org.joda.time.ReadableInstant;

/**
 * Enumeration of all time intervals the event aggregation can handle. All of the example ranges are
 * inclusive on both ends
 *
 */
public enum AggregationInterval {
    /**
     * 1 Minute
     *
     * @see DateTimeFieldType#minuteOfHour()
     */
    MINUTE(DateTimeFieldType.minuteOfHour(), true),
    /** 5 Minutes (0-4,5-9,...,55-59) */
    FIVE_MINUTE(null, true),
    /**
     * 1 Hour
     *
     * @see DateTimeFieldType#hourOfDay()
     */
    HOUR(DateTimeFieldType.hourOfDay(), true),
    /**
     * 1 Day
     *
     * @see DateTimeFieldType#dayOfMonth()
     */
    DAY(DateTimeFieldType.dayOfMonth(), false),
    /**
     * 1 Week
     *
     * @see DateTimeFieldType#weekOfWeekyear()
     */
    WEEK(DateTimeFieldType.weekOfWeekyear(), false),
    /**
     * 1 Calendar month
     *
     * @see DateTimeFieldType#monthOfYear()
     */
    MONTH(DateTimeFieldType.monthOfYear(), false),
    /**
     * As defined by the deployer, divides the calendar into 4 sections. Default configuration is: 3
     * Calendar months (Jan 1 - Mar 31, Apr 1 - Jun 30, Jul 1 - Sep 30, Oct 1 - Dec 31)
     */
    CALENDAR_QUARTER(null, false),
    /** As defined by the deployer, unusable unless term boundaries have been configured. */
    ACADEMIC_TERM(null, false),
    /**
     * 1 Year
     *
     * @see DateTimeFieldType#year()
     */
    YEAR(DateTimeFieldType.year(), false);

    private final DateTimeFieldType dateTimeFieldType;
    private final boolean hasTimePart;

    /** @param dateTimeFieldType */
    private AggregationInterval(DateTimeFieldType dateTimeFieldType, boolean hasTimePart) {
        this.dateTimeFieldType = dateTimeFieldType;
        this.hasTimePart = hasTimePart;
    }

    /** @return true if the interval has a time part, false if the interval is date only */
    public boolean isHasTimePart() {
        return hasTimePart;
    }

    /**
     * @return the {@link DateTimeFieldType} for the {@link AggregationInterval}, null if there is
     *     no mapping
     */
    public DateTimeFieldType getDateTimeFieldType() {
        return this.dateTimeFieldType;
    }

    /**
     * @return If the {@link #determineEnd(DateTime)} and {@link #determineStart(DateTime)} methods
     *     work on this interval
     */
    public boolean isSupportsDetermination() {
        return this.dateTimeFieldType != null || this == FIVE_MINUTE;
    }

    /**
     * Determine the number of intervals between the start and end dates
     *
     * @param start Start, inclusive
     * @param end End, exclusive
     * @return Number of intervals between start and end
     */
    public int determineIntervalsBetween(ReadableInstant start, ReadableInstant end) {
        if (!this.isSupportsDetermination()) {
            throw new IllegalArgumentException(
                    "Cannot compute intervals between for "
                            + this
                            + " please use "
                            + AggregationIntervalHelper.class);
        }

        final DateTimeFieldType dtft;
        final double ratio;
        switch (this) {
            case FIVE_MINUTE:
                {
                    dtft = MINUTE.getDateTimeFieldType();
                    ratio = 5;
                    break;
                }
            default:
                {
                    dtft = dateTimeFieldType;
                    ratio = 1;
                }
        }

        final DurationFieldType durationType = dtft.getDurationType();
        final Chronology chrono = DateTimeUtils.getInstantChronology(start);
        return (int)
                Math.round(
                        durationType
                                        .getField(chrono)
                                        .getDifference(end.getMillis(), start.getMillis())
                                / ratio);
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
            return instant.hourOfDay()
                    .roundFloorCopy()
                    .plusMinutes((instant.getMinuteOfHour() / 5) * 5);
        }

        throw new IllegalArgumentException(
                "Cannot compute interval start time for "
                        + this
                        + " please use "
                        + AggregationIntervalHelper.class);
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
            final DateTime start =
                    instant.hourOfDay()
                            .roundFloorCopy()
                            .plusMinutes((instant.getMinuteOfHour() / 5) * 5);
            return start.plusMinutes(5);
        }

        throw new IllegalArgumentException(
                "Cannot compute interval end time for "
                        + this
                        + " please use "
                        + AggregationIntervalHelper.class);
    }
}
