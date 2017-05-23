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
import org.apereo.portal.concurrency.locking.IClusterLockService;
import org.apereo.portal.events.aggr.dao.DateDimensionDao;
import org.apereo.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.apereo.portal.events.aggr.dao.TimeDimensionDao;
import org.apereo.portal.events.handlers.db.IPortalEventDao;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PortalEventDimensionPopulatorImpl extends BaseAggrEventsJpaDao
        implements DisposableBean, PortalEventDimensionPopulator {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private TimeDimensionDao timeDimensionDao;
    private DateDimensionDao dateDimensionDao;
    private AggregationIntervalHelper intervalHelper;
    private IPortalEventDao portalEventDao;
    private IEventAggregationManagementDao eventAggregationManagementDao;
    private IClusterLockService clusterLockService;

    private ReadablePeriod dimensionBuffer = Period.days(30);

    private volatile boolean checkedDimensions = false;
    private volatile boolean shutdown = false;

    @Autowired
    public void setClusterLockService(IClusterLockService clusterLockService) {
        this.clusterLockService = clusterLockService;
    }

    @Autowired
    public void setTimeDimensionDao(TimeDimensionDao timeDimensionDao) {
        this.timeDimensionDao = timeDimensionDao;
    }

    @Autowired
    public void setDateDimensionDao(DateDimensionDao dateDimensionDao) {
        this.dateDimensionDao = dateDimensionDao;
    }

    @Autowired
    public void setIntervalHelper(AggregationIntervalHelper intervalHelper) {
        this.intervalHelper = intervalHelper;
    }

    @Autowired
    public void setPortalEventDao(IPortalEventDao portalEventDao) {
        this.portalEventDao = portalEventDao;
    }

    @Autowired
    public void setEventAggregationManagementDao(
            IEventAggregationManagementDao eventAggregationManagementDao) {
        this.eventAggregationManagementDao = eventAggregationManagementDao;
    }

    @Value(
            "${org.apereo.portal.events.aggr.PortalEventDimensionPopulatorImpl.dimensionBuffer:P30D}")
    public void setDimensionBuffer(ReadablePeriod dimensionBuffer) {
        if (new Period(dimensionBuffer).toStandardDays().getDays() < 1) {
            throw new IllegalArgumentException(
                    "dimensionBuffer must be at least 1 day. Is: "
                            + new Period(dimensionBuffer).toStandardDays().getDays());
        }
        this.dimensionBuffer = dimensionBuffer;
    }

    @Override
    public void destroy() throws Exception {
        this.shutdown = true;
    }

    @Override
    public boolean isCheckedDimensions() {
        return checkedDimensions;
    }

    @Override
    @AggrEventsTransactional
    public void doPopulateDimensions() {
        if (shutdown) {
            logger.warn("populateDimensions called after shutdown, ignoring call");
        }

        if (!this.clusterLockService.isLockOwner(DIMENSION_LOCK_NAME)) {
            throw new IllegalStateException(
                    "The cluster lock "
                            + DIMENSION_LOCK_NAME
                            + " must be owned by the current thread and server");
        }

        doPopulateTimeDimensions();
        doPopulateDateDimensions();
        doUpdateDateDimensions();

        //Immediately flush all date/time dimension changes to the database
        this.getEntityManager().flush();

        this.checkedDimensions = true;
    }

    private void checkShutdown() {
        if (shutdown) {
            //Mark ourselves as interupted and throw an exception
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "uPortal is shutting down, throwing an exeption to stop processing");
        }
    }

    /** Populate the time dimensions */
    final void doPopulateTimeDimensions() {
        final List<TimeDimension> timeDimensions = this.timeDimensionDao.getTimeDimensions();
        if (timeDimensions.isEmpty()) {
            logger.info("No TimeDimensions exist, creating them");
        } else if (timeDimensions.size() != (24 * 60)) {
            this.logger.info(
                    "There are only "
                            + timeDimensions.size()
                            + " time dimensions in the database, there should be "
                            + (24 * 60)
                            + " creating missing dimensions");
        } else {
            this.logger.debug("Found expected " + timeDimensions.size() + " time dimensions");
            return;
        }

        LocalTime nextTime = new LocalTime(0, 0);
        final LocalTime lastTime = new LocalTime(23, 59);

        for (final TimeDimension timeDimension : timeDimensions) {
            LocalTime dimensionTime = timeDimension.getTime();
            if (nextTime.isBefore(dimensionTime)) {
                do {
                    checkShutdown();
                    this.timeDimensionDao.createTimeDimension(nextTime);
                    nextTime = nextTime.plusMinutes(1);
                } while (nextTime.isBefore(dimensionTime));
            } else if (nextTime.isAfter(dimensionTime)) {
                do {
                    checkShutdown();
                    this.timeDimensionDao.createTimeDimension(dimensionTime);
                    dimensionTime = dimensionTime.plusMinutes(1);
                } while (nextTime.isAfter(dimensionTime));
            }

            nextTime = dimensionTime.plusMinutes(1);
        }

        //Add any missing times from the tail
        while (nextTime.isBefore(lastTime) || nextTime.equals(lastTime)) {
            checkShutdown();
            this.timeDimensionDao.createTimeDimension(nextTime);
            if (nextTime.equals(lastTime)) {
                break;
            }
            nextTime = nextTime.plusMinutes(1);
        }
    }

    final void doPopulateDateDimensions() {
        final DateTime now = getNow();

        final AggregationIntervalInfo startIntervalInfo;
        final DateTime oldestPortalEventTimestamp =
                this.portalEventDao.getOldestPortalEventTimestamp();
        if (oldestPortalEventTimestamp == null || now.isBefore(oldestPortalEventTimestamp)) {
            startIntervalInfo =
                    this.intervalHelper.getIntervalInfo(
                            AggregationInterval.YEAR, now.minus(this.dimensionBuffer));
        } else {
            startIntervalInfo =
                    this.intervalHelper.getIntervalInfo(
                            AggregationInterval.YEAR,
                            oldestPortalEventTimestamp.minus(this.dimensionBuffer));
        }

        final AggregationIntervalInfo endIntervalInfo;
        final DateTime newestPortalEventTimestamp =
                this.portalEventDao.getNewestPortalEventTimestamp();
        if (newestPortalEventTimestamp == null || now.isAfter(newestPortalEventTimestamp)) {
            endIntervalInfo =
                    this.intervalHelper.getIntervalInfo(
                            AggregationInterval.YEAR, now.plus(this.dimensionBuffer));
        } else {
            endIntervalInfo =
                    this.intervalHelper.getIntervalInfo(
                            AggregationInterval.YEAR,
                            newestPortalEventTimestamp.plus(this.dimensionBuffer));
        }

        final DateMidnight start = startIntervalInfo.getStart().toDateMidnight();
        final DateMidnight end = endIntervalInfo.getEnd().toDateMidnight();

        doPopulateDateDimensions(start, end);
    }

    final void doPopulateDateDimensions(final DateMidnight start, final DateMidnight end) {
        logger.info("Populating DateDimensions between {} and {}", start, end);

        final List<QuarterDetail> quartersDetails =
                this.eventAggregationManagementDao.getQuartersDetails();
        final List<AcademicTermDetail> academicTermDetails =
                this.eventAggregationManagementDao.getAcademicTermDetails();

        final List<DateDimension> dateDimensions =
                this.dateDimensionDao.getDateDimensionsBetween(start, end);

        DateMidnight nextDate = start;
        for (final DateDimension dateDimension : dateDimensions) {
            DateMidnight dimensionDate = dateDimension.getDate();
            if (nextDate.isBefore(dimensionDate)) {
                do {
                    checkShutdown();
                    createDateDimension(quartersDetails, academicTermDetails, nextDate);
                    nextDate = nextDate.plusDays(1);
                } while (nextDate.isBefore(dimensionDate));
            } else if (nextDate.isAfter(dimensionDate)) {
                do {
                    checkShutdown();
                    createDateDimension(quartersDetails, academicTermDetails, dimensionDate);
                    dimensionDate = dimensionDate.plusDays(1);
                } while (nextDate.isAfter(dimensionDate));
            }

            nextDate = dimensionDate.plusDays(1);
        }

        //Add any missing dates from the tail
        while (nextDate.isBefore(end)) {
            checkShutdown();
            createDateDimension(quartersDetails, academicTermDetails, nextDate);
            nextDate = nextDate.plusDays(1);
        }
    }

    /** Populate the term/quarter data for dimensions that are missing the data */
    final void doUpdateDateDimensions() {
        final List<DateDimension> dateDimensions =
                this.dateDimensionDao.getDateDimensionsWithoutTerm();
        final List<AcademicTermDetail> academicTermDetails =
                this.eventAggregationManagementDao.getAcademicTermDetails();

        for (final DateDimension dateDimension : dateDimensions) {
            final DateMidnight date = dateDimension.getDate();
            final AcademicTermDetail termDetail =
                    EventDateTimeUtils.findDateRangeSorted(date, academicTermDetails);
            if (termDetail != null) {
                dateDimension.setTerm(termDetail.getTermName());
                this.dateDimensionDao.updateDateDimension(dateDimension);
            }
        }
    }

    /** Exists to make this class testable */
    DateTime getNow() {
        return DateTime.now();
    }

    /** Creates a date dimension, handling the quarter and term lookup logic */
    protected void createDateDimension(
            List<QuarterDetail> quartersDetails,
            List<AcademicTermDetail> academicTermDetails,
            DateMidnight date) {

        final QuarterDetail quarterDetail =
                EventDateTimeUtils.findDateRangeSorted(date, quartersDetails);
        final AcademicTermDetail termDetail =
                EventDateTimeUtils.findDateRangeSorted(date, academicTermDetails);
        this.dateDimensionDao.createDateDimension(
                date,
                quarterDetail.getQuarterId(),
                termDetail != null ? termDetail.getTermName() : null);
    }
}
