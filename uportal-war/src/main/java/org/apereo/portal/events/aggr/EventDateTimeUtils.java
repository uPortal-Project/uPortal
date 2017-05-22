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

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import org.apereo.portal.events.aggr.dao.jpa.QuarterDetailImpl;
import org.joda.time.MonthDay;
import org.joda.time.ReadableInstant;

/**
 * Utilities for working with the various date/time data types involved in event aggregation
 *
 */
public final class EventDateTimeUtils {
    private EventDateTimeUtils() {}

    /**
     * Determines if an instant is between a start (inclusive) and end (exclusive).
     *
     * @param start start of range (inclusive)
     * @param end end of range (exclusive)
     * @param instant instant
     * @return Returns 0 if the instant is contained in the range: (getStart() <= instant <
     *     getEnd()) Returns -1 if the range comes before the instant: (getEnd() <= instant) Returns
     *     1 if the range comes after the instant (instant < getStart())
     * @see Comparable#compareTo(Object)
     */
    public static int compareTo(
            ReadableInstant start, ReadableInstant end, ReadableInstant instant) {
        if (instant.isBefore(start)) {
            return 1;
        }

        if (end.isAfter(instant)) {
            return 0;
        }

        return -1;
    }

    /**
     * Validates the collection of quarters is valid. Validity is defined as:
     *
     * <ul>
     *   <li>Contains 4 QuarterDetail
     *   <li>Once placed in order the start of the current quarter is equal to the end of the
     *       previous quarter
     *   <li>Once placed in order the quarterId values go from 0 to 3 in order
     * </ul>
     *
     * @param quarters
     * @return A new list of the quarters sorted by natural id
     */
    public static List<QuarterDetail> validateQuarters(Collection<QuarterDetail> quarters) {
        if (quarters.size() != 4) {
            throw new IllegalArgumentException("Exactly 4 QuarterDetail must be set: " + quarters);
        }

        final List<QuarterDetail> sortedQuarters = new ArrayList<QuarterDetail>(quarters);
        Collections.sort(sortedQuarters);

        MonthDay previousEnd = sortedQuarters.get(3).getEnd();
        final Iterator<QuarterDetail> itr = sortedQuarters.iterator();
        for (int i = 0; i < 4; i++) {
            final QuarterDetail q = itr.next();
            if (i != q.getQuarterId()) {
                throw new IllegalArgumentException(
                        "Quarter " + i + " has an illegal id of " + q.getQuarterId());
            }

            if (!q.getStart().equals(previousEnd)) {
                throw new IllegalArgumentException(
                        "Quarter "
                                + i
                                + " start date of "
                                + q.getStart()
                                + " is not adjacent to previous quarter's end date of "
                                + previousEnd);
            }
            previousEnd = q.getEnd();
        }

        return sortedQuarters;
    }

    /**
     * Validates the collection of terms is valid. Validity is defined as:
     *
     * <ul>
     *   <li>Once placed in order no two terms overlap
     * </ul>
     *
     * @param academicTerms
     * @return A new list of the terms sorted by natural id order
     */
    public static List<AcademicTermDetail> validateAcademicTerms(
            Collection<AcademicTermDetail> academicTerms) {
        final List<AcademicTermDetail> sortedTerms =
                new ArrayList<AcademicTermDetail>(academicTerms);
        Collections.sort(sortedTerms);

        AcademicTermDetail prevTermDetail = null;
        for (final AcademicTermDetail termDetail : sortedTerms) {
            if (prevTermDetail != null) {
                if (termDetail.getStart().isBefore(prevTermDetail.getEnd())) {
                    throw new IllegalArgumentException(
                            termDetail + " overlaps with " + prevTermDetail);
                }
            }

            prevTermDetail = termDetail;
        }

        return sortedTerms;
    }

    /**
     * Find the {@link DateRange} that contains the specified instant. The first DateRange which
     * contains the instant is returned, null is returned if no range contains the instant.
     *
     * @param instant The instant to check
     * @param dateRanges The date ranges to check
     * @return The {@link DateRange} that contains the instant, null if no date range contains the
     *     instant
     */
    public static <DR extends DateRange<DT>, DT> DR findDateRange(
            ReadableInstant instant, Collection<DR> dateRanges) {
        if (dateRanges.isEmpty()) {
            return null;
        }

        for (final DR dateRange : dateRanges) {
            if (dateRange.compareTo(instant) == 0) {
                return dateRange;
            }
        }

        return null;
    }

    /**
     * Same function as {@link #findDateRange(ReadableInstant, Collection)} optimized for working on
     * a pre-sorted List of date ranges by doing a binary search. The List must be sorted by {@link
     * DateRange#getStart()}
     */
    public static <DR extends DateRange<DT>, DT> DR findDateRangeSorted(
            ReadableInstant instant, List<DR> dateRanges) {
        if (dateRanges.isEmpty()) {
            return null;
        }

        if (!(dateRanges instanceof RandomAccess)) {
            //Not random access not much use doing a binary search
            return findDateRange(instant, dateRanges);
        }

        int low = 0;
        int high = dateRanges.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            final DR dateRange = dateRanges.get(mid);
            final int cmp = dateRange.compareTo(instant);

            if (cmp == -1) low = mid + 1;
            else if (cmp == 1) high = mid - 1;
            else return dateRange;
        }

        return null;
    }

    /**
     * Create a new set of quarter details in the standard 1/1-4/1, 4/1-7/1, 7/1-10/1, 10/1-1/1
     * arrangement
     */
    public static List<QuarterDetail> createStandardQuarters() {
        return ImmutableList.<QuarterDetail>of(
                new QuarterDetailImpl(new MonthDay(1, 1), new MonthDay(4, 1), 0),
                new QuarterDetailImpl(new MonthDay(4, 1), new MonthDay(7, 1), 1),
                new QuarterDetailImpl(new MonthDay(7, 1), new MonthDay(10, 1), 2),
                new QuarterDetailImpl(new MonthDay(10, 1), new MonthDay(1, 1), 3));
    }
}
