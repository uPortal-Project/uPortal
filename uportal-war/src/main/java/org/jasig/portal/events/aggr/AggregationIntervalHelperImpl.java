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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.AggregationStartComparator;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision: 18025 $
 */
@Service
public class AggregationIntervalHelperImpl implements AggregationIntervalHelper {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private TimeDimensionDao timeDimensionDao;
    private DateDimensionDao dateDimensionDao;
    private IEventAggregationManagementDao eventAggregationManagementDao;
    
    @Autowired
    public void setEventAggregationManagementDao(IEventAggregationManagementDao eventAggregationManagementDao) {
        this.eventAggregationManagementDao = eventAggregationManagementDao;
    }

    @Autowired
    public void setTimeDimensionDao(TimeDimensionDao timeDimensionDao) {
        this.timeDimensionDao = timeDimensionDao;
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.events.aggr.AggregationIntervalHelper#fillInBlanks(org.jasig.portal.events.aggr.AggregationInterval, org.joda.time.DateTime, org.joda.time.DateTime, java.util.List, com.google.common.base.Function)
     */
    public <T extends BaseAggregation> List<T> fillInBlanks(AggregationInterval interval, DateTime start, DateTime end, List<T> data,
            Function<AggregationIntervalInfo, T> missingDataCreator) {
        
        final List<T> fullData = new LinkedList<T>();
        
        // set the next interval to the first expected interval in the time period
        AggregationIntervalInfo nextInterval = this.getIntervalInfo(interval, start);

        AggregationIntervalInfo lastInterval = this.getIntervalInfo(interval, end.minusMillis(1));

        Collections.sort(data, new AggregationStartComparator());
        
        // iterate through the list of provided data points, adding zero-value
        // aggregations for any time period for which data is missing
        for (final T entry : data) {
            
            // Get the date/time dimension associated with the next data entry
            // in the list and convert it into an aggregation interval
            final DateDimension dateDimension = entry.getDateDimension();
            final TimeDimension timeDimension = entry.getTimeDimension();
            final DateTime entryDate = timeDimension.getTime().toDateTime(dateDimension.getDate());
            final AggregationIntervalInfo entryInterval = this.getIntervalInfo(interval, entryDate);
            
            // Fill in any missing data points before the start of this data
            // point
            while (nextInterval.getStart().isBefore(entryInterval.getStart())) {
                final T missingEntry = missingDataCreator.apply(nextInterval);
                fullData.add(missingEntry);
                
                final DateTime missingEntryDate = missingEntry.getTimeDimension().getTime().toDateTime(missingEntry.getDateDimension().getDate()).plusMinutes(missingEntry.getDuration());
                nextInterval = this.getIntervalInfo(interval, missingEntryDate);
            }
            
            fullData.add(entry);
            nextInterval = this.getIntervalInfo(interval, nextInterval.getEnd());
        }

        // Fill in any missing data points after the last time point for which
        // we have data
        while (!nextInterval.getEnd().isAfter(lastInterval.getEnd())) {
            final T missingEntry = missingDataCreator.apply(nextInterval);
            fullData.add(missingEntry);
            
			final DateTime missingEntryDate = missingEntry
					.getTimeDimension().getTime()
					.toDateTime(missingEntry.getDateDimension().getDate())
					.plusMinutes(missingEntry.getDuration());
            nextInterval = this.getIntervalInfo(interval, missingEntryDate);
        }

        return fullData;
    }

    @Autowired
    public void setDateDimensionDao(DateDimensionDao dateDimensionDao) {
        this.dateDimensionDao = dateDimensionDao;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.stats.IntervalHelper#getIntervalDates(org.jasig.portal.stats.Interval, java.util.Date)
     */
    @Override
    public AggregationIntervalInfo getIntervalInfo(AggregationInterval interval, DateTime date) {
        //Chop off everything below the minutes (seconds, millis)
        final DateTime instant = date.minuteOfHour().roundFloorCopy();
        
        final DateTime start, end;
        switch (interval) {
            case CALENDAR_QUARTER: {
                final List<QuarterDetail> quartersDetails = this.eventAggregationManagementDao.getQuartersDetails();
                final QuarterDetail quarterDetail = EventDateTimeUtils.findDateRangeSorted(instant, quartersDetails);
                start = quarterDetail.getStartDateMidnight(date).toDateTime();
                end = quarterDetail.getEndDateMidnight(date).toDateTime();
                break;
            }
            case ACADEMIC_TERM: {
                final List<AcademicTermDetail> academicTermDetails = this.eventAggregationManagementDao.getAcademicTermDetails();
                final AcademicTermDetail academicTermDetail = EventDateTimeUtils.findDateRangeSorted(date, academicTermDetails);
                if (academicTermDetail == null) {
                    return null;
                }
                
                start = academicTermDetail.getStart().toDateTime();
                end = academicTermDetail.getEnd().toDateTime();
                
                break;
            }
            default: {
                start = interval.determineStart(instant);
                end = interval.determineEnd(start);
            }
        }
        
        final LocalTime startTime = start.toLocalTime();
        final TimeDimension startTimeDimension = this.timeDimensionDao.getTimeDimensionByTime(startTime);

        final DateMidnight startDateMidnight = start.toDateMidnight();
        final DateDimension startDateDimension = this.dateDimensionDao.getDateDimensionByDate(startDateMidnight);
        
        return new AggregationIntervalInfo(interval, start, end, startDateDimension, startTimeDimension);
    }
}
