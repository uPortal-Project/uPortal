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

import org.apereo.portal.concurrency.locking.IClusterLockService;
import org.apereo.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.apereo.portal.events.aggr.session.EventSessionDao;
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
public class PortalEventSessionPurgerImpl implements PortalEventSessionPurger {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IEventAggregationManagementDao eventAggregationManagementDao;
    private EventSessionDao eventSessionDao;
    private IClusterLockService clusterLockService;

    private ReadablePeriod eventSessionDuration = Period.days(1);

    @Autowired
    public void setEventSessionDao(EventSessionDao eventSessionDao) {
        this.eventSessionDao = eventSessionDao;
    }

    @Autowired
    public void setClusterLockService(IClusterLockService clusterLockService) {
        this.clusterLockService = clusterLockService;
    }

    @Autowired
    public void setEventAggregationManagementDao(
            IEventAggregationManagementDao eventAggregationManagementDao) {
        this.eventAggregationManagementDao = eventAggregationManagementDao;
    }

    @Value(
            "${org.apereo.portal.events.aggr.session.PortalEventSessionPurgerImpl.eventSessionDuration:P1D}")
    public void setEventSessionDuration(ReadablePeriod eventSessionDuration) {
        this.eventSessionDuration = eventSessionDuration;
    }

    @Override
    @AggrEventsTransactional
    public EventProcessingResult doPurgeEventSessions() {
        if (!this.clusterLockService.isLockOwner(PURGE_EVENT_SESSION_LOCK_NAME)) {
            throw new IllegalStateException(
                    "The cluster lock "
                            + PURGE_EVENT_SESSION_LOCK_NAME
                            + " must be owned by the current thread and server");
        }

        final IEventAggregatorStatus eventAggregatorStatus =
                eventAggregationManagementDao.getEventAggregatorStatus(
                        IEventAggregatorStatus.ProcessingType.AGGREGATION, false);
        if (eventAggregatorStatus == null || eventAggregatorStatus.getLastEventDate() == null) {
            return new EventProcessingResult(0, null, null, true);
        }

        final DateTime lastEventDate = eventAggregatorStatus.getLastEventDate();
        final DateTime sessionPurgeDate = lastEventDate.minus(eventSessionDuration);
        final int purgeCount = eventSessionDao.purgeEventSessionsBefore(sessionPurgeDate);

        return new EventProcessingResult(purgeCount, null, sessionPurgeDate, true);
    }
}
