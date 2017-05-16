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

import java.util.List;
import org.joda.time.DateTime;

/**
 * Calculates the correct {@link AggregationIntervalInfo} that contains the spcified {@link
 * DateTime} for the specified {@link AggregationInterval}.
 *
 */
public interface AggregationIntervalHelper {
    /**
     * @param interval {@link AggregationInterval} to get info about
     * @param date Date that the interval should contain
     * @return Information about the calculated interval, null if the specified interval is not
     *     currently supported
     */
    AggregationIntervalInfo getIntervalInfo(AggregationInterval interval, DateTime date);

    /**
     * Get the number of intervals between
     *
     * @param interval
     * @param start
     * @param end
     * @return
     */
    int intervalsBetween(AggregationInterval interval, DateTime start, DateTime end);

    /** @see #getIntervalStartDateTimesBetween(AggregationInterval, DateTime, DateTime, int) */
    List<DateTime> getIntervalStartDateTimesBetween(
            AggregationInterval interval, DateTime start, DateTime end);

    /**
     * Get list of {@link DateTime}s for the start of every interval between the start and end
     *
     * @param interval The interval to get times for
     * @param start The start of the range (inclusive)
     * @param end The end of the range (exclusive)
     * @param maxTimes Maximum number of DateTime objects to return. If this limit is hit an
     *     exception is thrown. -1 disables limit checks
     * @return List of DateTime objects in chronological order
     */
    List<DateTime> getIntervalStartDateTimesBetween(
            AggregationInterval interval, DateTime start, DateTime end, int maxTimes);

    //    /**
    //     * Fill in any missing timepoints in a list of data with zero-value aggregation
    //     * objects.
    //     *
    //     * @param <T>
    //     * @param interval {@AggregationInterval} used in aggregation
    //     * @param start start Date for the interval
    //     * @param end end Date for the interval
    //     * @param data List of data to fill in
    //     * @param missingDataCreator Function used to create zero-value aggregation objects
    //     * @return
    //     */
    //    <T extends BaseAggregation<?>> List<T> fillInBlanks(AggregationInterval interval, DateTime start, DateTime end, List<T> data, Function<AggregationIntervalInfo, T> missingDataCreator);

}
