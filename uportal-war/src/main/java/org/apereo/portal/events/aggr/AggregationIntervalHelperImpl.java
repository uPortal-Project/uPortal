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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.events.aggr.dao.DateDimensionDao;
import org.apereo.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.apereo.portal.events.aggr.dao.TimeDimensionDao;
import org.apereo.portal.events.aggr.dao.jpa.AcademicTermDetailImpl;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 */
@Service
public class AggregationIntervalHelperImpl implements AggregationIntervalHelper {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private TimeDimensionDao timeDimensionDao;
    private DateDimensionDao dateDimensionDao;
    private IEventAggregationManagementDao eventAggregationManagementDao;

    @Autowired
    public void setEventAggregationManagementDao(
            IEventAggregationManagementDao eventAggregationManagementDao) {
        this.eventAggregationManagementDao = eventAggregationManagementDao;
    }

    @Autowired
    public void setTimeDimensionDao(TimeDimensionDao timeDimensionDao) {
        this.timeDimensionDao = timeDimensionDao;
    }

    @Autowired
    public void setDateDimensionDao(DateDimensionDao dateDimensionDao) {
        this.dateDimensionDao = dateDimensionDao;
    }

    @Override
    public List<DateTime> getIntervalStartDateTimesBetween(
            AggregationInterval interval, DateTime start, DateTime end) {
        return getIntervalStartDateTimesBetween(interval, start, end, -1);
    }

    public int intervalsBetween(AggregationInterval interval, DateTime start, DateTime end) {
        //For intervals that support native determination
        if (interval.isSupportsDetermination()) {
            return interval.determineIntervalsBetween(start, end);
        }

        //Special handling for intervals that don't support determination

        if (interval == AggregationInterval.ACADEMIC_TERM) {
            //Since terms can have gaps all terms must be checked

            //Find the first term than is in the range via binary search
            final List<AcademicTermDetail> terms = getAcademicTermsAfter(start);

            //Count all the terms that are in the range, breaking the loop on the first non-matching term
            int count = 0;
            for (final AcademicTermDetail academicTerm : terms) {
                final DateMidnight termStart = academicTerm.getStart();
                if (end.isAfter(termStart) && !start.isAfter(termStart)) {
                    count++;
                } else if (count > 0) {
                    //getAcademicTermDetails returns the list in order, after at least one match has been found
                    //a term that doesn't match means no more matches will be found
                    break;
                }
            }
            return count;
        }

        //Fallback for any other interval type that needs explicit iteration
        AggregationIntervalInfo nextInterval = this.getIntervalInfo(interval, start);

        int count = 0;
        while (nextInterval.getStart().isBefore(end)) {
            //Needed to make sure we don't count a partial first interval
            if (!start.isAfter(nextInterval.getStart())) {
                count++;
            }

            nextInterval = this.getIntervalInfo(interval, nextInterval.getEnd());
        }

        return count;
    }

    @Override
    public List<DateTime> getIntervalStartDateTimesBetween(
            AggregationInterval interval, DateTime start, DateTime end, int maxTimes) {
        if (interval.isSupportsDetermination()) {
            //Get the interval count for the date-time field type and verify it is in the valid range.
            final int intervals = interval.determineIntervalsBetween(start, end);
            verifyIntervalCount(start, end, maxTimes, intervals);

            //Result list
            final List<DateTime> result = new ArrayList<DateTime>(intervals);

            //Check if first interval in the range
            DateTime intervalStart = interval.determineStart(start);
            if (!intervalStart.isBefore(start)) {
                result.add(intervalStart);
            }

            //Move one step forward in the range
            DateTime intervalEnd = interval.determineEnd(intervalStart);
            intervalStart = interval.determineStart(intervalEnd);

            //Step through the interval start/end values to build the full list
            while (intervalStart.isBefore(end)) {
                result.add(intervalStart);

                intervalEnd = interval.determineEnd(intervalStart);
                intervalStart = interval.determineStart(intervalEnd);
            }

            return result;
        }

        //Special handling for intervals that don't support determination
        if (interval == AggregationInterval.ACADEMIC_TERM) {
            //Since terms can have gaps all terms must be checked
            final List<AcademicTermDetail> terms = getAcademicTermsAfter(start);

            //Use all the terms that are in the range, breaking the loop on the first non-matching term
            final List<DateTime> result = new ArrayList<DateTime>(terms.size());
            for (final AcademicTermDetail academicTerm : terms) {
                final DateMidnight termStart = academicTerm.getStart();
                if (end.isAfter(termStart) && !start.isAfter(termStart)) {
                    result.add(start);
                } else if (!result.isEmpty()) {
                    //getAcademicTermDetails returns the list in order, after at least one match has been found
                    //a term that doesn't match means no more matches will be found
                    break;
                }
            }
            return result;
        }

        //Fallback for any other interval type that needs explicit iteration
        AggregationIntervalInfo nextInterval = this.getIntervalInfo(interval, start);

        final List<DateTime> result = new ArrayList<DateTime>();
        while (nextInterval.getStart().isBefore(end)) {
            //Needed to make sure we don't count a partial first interval
            if (!start.isAfter(nextInterval.getStart())) {
                result.add(nextInterval.getStart());

                if (maxTimes > 0 && result.size() > maxTimes) {
                    throw new IllegalArgumentException(
                            "There more than "
                                    + result.size()
                                    + " intervals between "
                                    + start
                                    + " and "
                                    + end
                                    + " which is more than the specified maximum of "
                                    + maxTimes);
                }
            }

            nextInterval = this.getIntervalInfo(interval, nextInterval.getEnd());
        }
        return result;
    }

    @Override
    public AggregationIntervalInfo getIntervalInfo(AggregationInterval interval, DateTime date) {
        //Chop off everything below the minutes (seconds, millis)
        final DateTime instant = date.minuteOfHour().roundFloorCopy();

        final DateTime start, end;
        switch (interval) {
            case CALENDAR_QUARTER:
                {
                    final List<QuarterDetail> quartersDetails =
                            this.eventAggregationManagementDao.getQuartersDetails();
                    final QuarterDetail quarterDetail =
                            EventDateTimeUtils.findDateRangeSorted(instant, quartersDetails);
                    start = quarterDetail.getStartDateMidnight(date).toDateTime();
                    end = quarterDetail.getEndDateMidnight(date).toDateTime();
                    break;
                }
            case ACADEMIC_TERM:
                {
                    final List<AcademicTermDetail> academicTermDetails =
                            this.eventAggregationManagementDao.getAcademicTermDetails();
                    final AcademicTermDetail academicTermDetail =
                            EventDateTimeUtils.findDateRangeSorted(date, academicTermDetails);
                    if (academicTermDetail == null) {
                        return null;
                    }

                    start = academicTermDetail.getStart().toDateTime();
                    end = academicTermDetail.getEnd().toDateTime();

                    break;
                }
            default:
                {
                    start = interval.determineStart(instant);
                    end = interval.determineEnd(start);
                }
        }

        final LocalTime startTime = start.toLocalTime();
        final TimeDimension startTimeDimension =
                this.timeDimensionDao.getTimeDimensionByTime(startTime);

        final DateMidnight startDateMidnight = start.toDateMidnight();
        final DateDimension startDateDimension =
                this.dateDimensionDao.getDateDimensionByDate(startDateMidnight);

        return new AggregationIntervalInfo(
                interval, start, end, startDateDimension, startTimeDimension);
    }

    /**
     * Return a sorted list of AcademicTermDetail objects where the the first element of the list
     * where the first element is the first term that starts after the specified start DateTime.
     */
    protected List<AcademicTermDetail> getAcademicTermsAfter(DateTime start) {
        final List<AcademicTermDetail> terms =
                this.eventAggregationManagementDao.getAcademicTermDetails();
        final int index =
                Collections.binarySearch(
                        terms,
                        new AcademicTermDetailImpl(
                                start.toDateMidnight(), start.plusDays(1).toDateMidnight(), ""));
        if (index > 0) {
            return terms.subList(index, terms.size());
        } else if (index < 0) {
            return terms.subList(-(index + 1), terms.size());
        }
        return terms;
    }

    protected void verifyIntervalCount(
            DateTime start, DateTime end, int maxTimes, final int intervals) {
        if (maxTimes > 0 && intervals > maxTimes) {
            throw new IllegalArgumentException(
                    "There are "
                            + intervals
                            + " intervals between "
                            + start
                            + " and "
                            + end
                            + " which is more than the specified maximum of "
                            + maxTimes);
        }
    }
}
