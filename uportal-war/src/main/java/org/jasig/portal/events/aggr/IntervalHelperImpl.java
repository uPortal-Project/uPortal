/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events.aggr;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
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
    public IntervalInfo getIntervalDates(Interval interval, Date date) {
        final Calendar instant = Calendar.getInstance();
        instant.setLenient(false);
        instant.clear();
        instant.setTime(date);
        //minute is our smallest time value, nuke the rest
        instant.clear(Calendar.MILLISECOND);
        instant.clear(Calendar.SECOND);
        
        final Calendar start = determineStart(interval, instant);
        final Calendar end = determineEnd(interval, start);
        
        TimeDimension startTimeDimension = this.timeDimensionDao.getTimeDimensionForCalendar(start);
        if (startTimeDimension == null) {
            startTimeDimension = this.timeDimensionDao.createTimeDimension(start);
        }
        
        DateDimension startDateDimension = this.dateDimensionDao.getDateDimensionForCalendar(start);
        if (startDateDimension == null) {
            startDateDimension = this.dateDimensionDao.createDateDimension(start);
        }
        
        return new IntervalInfo(start.getTime(), end.getTime(), startDateDimension, startTimeDimension);
    }

    protected Calendar determineStart(Interval interval, final Calendar instant) {
        final Calendar start = Calendar.getInstance();
        start.setLenient(false);
        start.clear();

        //TODO qtr, term
        switch (interval) {
            case MINUTE: {
                start.set(Calendar.MINUTE, instant.get(Calendar.MINUTE));
            }
            case FIVE_MINUTE: {
                //Kinda gross but really we want either MINUTE or FIVE_MINUTE logic to run, not both
                if (interval != Interval.MINUTE) {
                    start.set(Calendar.MINUTE, (instant.get(Calendar.MINUTE) / 5) * 5);
                }
            }
            case HOUR: {
                start.set(Calendar.HOUR_OF_DAY, instant.get(Calendar.HOUR_OF_DAY));
            }
            case DAY: {
                start.set(Calendar.DAY_OF_MONTH, instant.get(Calendar.DAY_OF_MONTH));
            }
            case WEEK: {
                start.set(Calendar.WEEK_OF_YEAR, instant.get(Calendar.WEEK_OF_YEAR));
            }
            case MONTH: {
                start.set(Calendar.MONTH, instant.get(Calendar.MONTH));
            }
            case YEAR: {
                start.set(Calendar.YEAR, instant.get(Calendar.YEAR));
                break;
            }
            case CALENDAR_QUARTER: {
                final DateDimension instantDateDimension = this.dateDimensionDao.getDateDimensionForCalendar(instant);
                final int quarter = instantDateDimension.getQuarter();
//                this.dateDimensionDao.getFirstDateDimension(Interval.CALENDAR_QUARTER, quarter);
                
                //TODO
                break;
            }
            case ACADEMIC_TERM: {
                
                //TODO
                break;
            }
        }
        return start;
    }

    protected Calendar determineEnd(Interval interval, final Calendar start) {
        final Calendar end = (Calendar)start.clone();
        switch (interval) {
            case MINUTE: {
                end.add(Calendar.MINUTE, 1);
                break;
            }
            case FIVE_MINUTE: {
                end.add(Calendar.MINUTE, 5);
                break;
            }
            case HOUR: {
                end.add(Calendar.HOUR_OF_DAY, 1);
                break;
            }
            case DAY: {
                end.add(Calendar.DAY_OF_MONTH, 1);
                break;
            }
            case WEEK: {
                end.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            }
            case MONTH: {
                end.add(Calendar.MONTH, 1);
                break;
            }
            case YEAR: {
                end.add(Calendar.YEAR, 1);
                break;
            }
            case CALENDAR_QUARTER: {
                //TODO
                break;
            }
            case ACADEMIC_TERM: {
                //TODO
                break;
            }
        }
        return end;
    }
}
