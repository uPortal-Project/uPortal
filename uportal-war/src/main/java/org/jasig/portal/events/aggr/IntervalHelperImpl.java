/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events.aggr;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Determines {@link IntervalInfo} for a specified {@link Interval} and {@link Date}
 * 
 * @author Eric Dalquist
 * @version $Revision: 18025 $
 */
@Service
public class IntervalHelperImpl implements IntervalHelper {
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

    @Autowired
    public void setDateDimensionDao(DateDimensionDao dateDimensionDao) {
        this.dateDimensionDao = dateDimensionDao;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.stats.IntervalHelper#getIntervalDates(org.jasig.portal.stats.Interval, java.util.Date)
     */
    @Override
    public IntervalInfo getIntervalInfo(Interval interval, DateTime date) {
        //Chop off everything below the minutes (seconds, millis)
        final DateTime instant = date.minuteOfHour().roundFloorCopy();
        
        //TODO cache this resolution ... best place would be in the current jpa session
        
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
                start = determineStart(interval, instant);
                end = determineEnd(interval, start);
                
            }
        }
        
        final LocalTime startTime = start.toLocalTime();
        final TimeDimension startTimeDimension = this.timeDimensionDao.getTimeDimensionByTime(startTime);

        final DateMidnight startDateMidnight = start.toDateMidnight();
        final DateDimension startDateDimension = this.dateDimensionDao.getDateDimensionByDate(startDateMidnight);
        
        return new IntervalInfo(start, end, startDateDimension, startTimeDimension);
    }

    protected DateTime determineStart(Interval interval, DateTime instant) {
        final DateTimeFieldType dateTimeFieldType = interval.getDateTimeFieldType();
        if (dateTimeFieldType != null) {
            return instant.property(dateTimeFieldType).roundFloorCopy();
        }
        
        if (interval == Interval.FIVE_MINUTE) {
            return instant.hourOfDay().roundFloorCopy().plusMinutes((instant.getMinuteOfHour() / 5) * 5);
        }
        
        throw new IllegalArgumentException("Unsupportd Interval: " + interval);
    }

    protected DateTime determineEnd(Interval interval, DateTime start) {
        final DateTimeFieldType dateTimeFieldType = interval.getDateTimeFieldType();
        if (dateTimeFieldType != null) {
            return start.property(dateTimeFieldType).addToCopy(1);
        }
        
        if (interval == Interval.FIVE_MINUTE) {
            return start.plusMinutes(5);
        }
        
        throw new IllegalArgumentException("Unsupportd Interval: " + interval);
    }
}
