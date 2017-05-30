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

import org.apereo.portal.IPortalInfoProvider;
import org.apereo.portal.concurrency.locking.IClusterLockService;
import org.apereo.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.apereo.portal.events.handlers.db.IPortalEventDao;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao.AggrEventsTransactional;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PortalEventPurgerImpl implements PortalEventPurger {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IEventAggregationManagementDao eventAggregationManagementDao;
    private IPortalInfoProvider portalInfoProvider;
    private IPortalEventDao portalEventDao;
    private IClusterLockService clusterLockService;

    private ReadablePeriod purgeDelay = Period.days(1);

    @Autowired
    public void setClusterLockService(IClusterLockService clusterLockService) {
        this.clusterLockService = clusterLockService;
    }

    @Autowired
    public void setEventAggregationManagementDao(
            IEventAggregationManagementDao eventAggregationManagementDao) {
        this.eventAggregationManagementDao = eventAggregationManagementDao;
    }

    @Autowired
    public void setPortalInfoProvider(IPortalInfoProvider portalInfoProvider) {
        this.portalInfoProvider = portalInfoProvider;
    }

    @Autowired
    public void setPortalEventDao(IPortalEventDao portalEventDao) {
        this.portalEventDao = portalEventDao;
    }

    @Value("${org.apereo.portal.events.aggr.PortalEventPurgerImpl.purgeDelay:PT1H}")
    public void setPurgeDelay(ReadablePeriod purgeDelay) {
        this.purgeDelay = purgeDelay;
    }

    @AggrEventsTransactional
    public EventProcessingResult doPurgeRawEvents() {
        if (!this.clusterLockService.isLockOwner(PURGE_RAW_EVENTS_LOCK_NAME)) {
            throw new IllegalStateException(
                    "The cluster lock "
                            + PURGE_RAW_EVENTS_LOCK_NAME
                            + " must be owned by the current thread and server");
        }

        final IEventAggregatorStatus eventPurgerStatus =
                eventAggregationManagementDao.getEventAggregatorStatus(
                        IEventAggregatorStatus.ProcessingType.PURGING, true);

        //Update status with current server name
        final String serverName = this.portalInfoProvider.getUniqueServerName();
        eventPurgerStatus.setServerName(serverName);
        eventPurgerStatus.setLastStart(new DateTime());

        //Determine date of most recently aggregated data
        final IEventAggregatorStatus eventAggregatorStatus =
                eventAggregationManagementDao.getEventAggregatorStatus(
                        IEventAggregatorStatus.ProcessingType.AGGREGATION, false);
        if (eventAggregatorStatus == null || eventAggregatorStatus.getLastEventDate() == null) {
            //Nothing has been aggregated, skip purging

            eventPurgerStatus.setLastEnd(new DateTime());
            eventAggregationManagementDao.updateEventAggregatorStatus(eventPurgerStatus);

            return new EventProcessingResult(0, null, null, true);
        }
        boolean complete = true;

        //Calculate purge end date from most recent aggregation minus the purge delay
        final DateTime lastAggregated = eventAggregatorStatus.getLastEventDate();
        DateTime purgeEnd = lastAggregated.minus(this.purgeDelay);

        //Determine the DateTime of the oldest event
        DateTime oldestEventDate = eventPurgerStatus.getLastEventDate();
        if (oldestEventDate == null) {
            oldestEventDate = this.portalEventDao.getOldestPortalEventTimestamp();
        }

        //Make sure purgeEnd is no more than 1 hour after the oldest event date to limit delete scope
        final DateTime purgeEndLimit = oldestEventDate.plusHours(1);
        if (purgeEndLimit.isBefore(purgeEnd)) {
            purgeEnd = purgeEndLimit;
            complete = false;
        }

        final Thread currentThread = Thread.currentThread();
        final String currentName = currentThread.getName();
        final int events;
        try {
            currentThread.setName(currentName + "-" + purgeEnd);

            //Purge events
            logger.debug("Starting purge of events before {}", purgeEnd);
            events = portalEventDao.deletePortalEventsBefore(purgeEnd);
        } finally {
            currentThread.setName(currentName);
        }

        //Update the status object and store it
        purgeEnd =
                purgeEnd.minusMillis(
                        100); //decrement by 100ms since deletePortalEventsBefore uses lessThan and not lessThanEqualTo
        eventPurgerStatus.setLastEventDate(purgeEnd);
        eventPurgerStatus.setLastEnd(new DateTime());
        eventAggregationManagementDao.updateEventAggregatorStatus(eventPurgerStatus);

        return new EventProcessingResult(events, oldestEventDate, purgeEnd, complete);
    }
}
