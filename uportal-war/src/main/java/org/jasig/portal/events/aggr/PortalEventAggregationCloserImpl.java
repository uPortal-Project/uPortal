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
import java.util.Set;

import org.jasig.portal.IPortalInfoProvider;
import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao.AggrEventsTransactional;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PortalEventAggregationCloserImpl implements PortalEventAggregationCloser, DisposableBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IClusterLockService clusterLockService;
    private IEventAggregationManagementDao eventAggregationManagementDao;
    private IPortalInfoProvider portalInfoProvider;
    private AggregationIntervalHelper intervalHelper;
    private Set<IPortalEventAggregator<PortalEvent>> portalEventAggregators = Collections.emptySet();
    
    private volatile boolean shutdown = false;
    
    @Autowired
    public void setClusterLockService(IClusterLockService clusterLockService) {
        this.clusterLockService = clusterLockService;
    }

    @Autowired
    public void setEventAggregationManagementDao(IEventAggregationManagementDao eventAggregationManagementDao) {
        this.eventAggregationManagementDao = eventAggregationManagementDao;
    }

    @Autowired
    public void setPortalInfoProvider(IPortalInfoProvider portalInfoProvider) {
        this.portalInfoProvider = portalInfoProvider;
    }

    @Autowired
    public void setIntervalHelper(AggregationIntervalHelper intervalHelper) {
        this.intervalHelper = intervalHelper;
    }

    @Autowired
    public void setPortalEventAggregators(Set<IPortalEventAggregator<PortalEvent>> portalEventAggregators) {
        this.portalEventAggregators = portalEventAggregators;
    }

    @Override
    public void destroy() throws Exception {
        this.shutdown = true;
    }

    private void checkShutdown() {
        if (shutdown) {
            //Mark ourselves as interupted and throw an exception
            Thread.currentThread().interrupt();
            throw new RuntimeException("uPortal is shutting down, throwing an exeption to stop processing");
        }
    }
    
    @Override
    @AggrEventsTransactional
    public EventProcessingResult doCloseAggregations() {
        if (!this.clusterLockService.isLockOwner(AGGREGATION_CLOSER_LOCK_NAME)) {
            throw new IllegalStateException("The cluster lock " + AGGREGATION_CLOSER_LOCK_NAME + " must be owned by the current thread and server");
        }
        
        final IEventAggregatorStatus cleanUnclosedStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.CLEAN_UNCLOSED, true);
        
        //Update status with current server name
        final String serverName = this.portalInfoProvider.getUniqueServerName();
        cleanUnclosedStatus.setServerName(serverName);
        cleanUnclosedStatus.setLastStart(new DateTime());
        
        //Determine date of most recently aggregated data
        final IEventAggregatorStatus eventAggregatorStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION, false);
        if (eventAggregatorStatus == null || eventAggregatorStatus.getLastEventDate() == null) {
            //Nothing has been aggregated, skip unclosed cleanup
            
            cleanUnclosedStatus.setLastEnd(new DateTime());
            eventAggregationManagementDao.updateEventAggregatorStatus(cleanUnclosedStatus);
            
            return new EventProcessingResult(0, null, null, true);
        }
        
        //Calculate clean end date from most recent aggregation minus the purge delay
        final DateTime lastAggregatedDate = eventAggregatorStatus.getLastEventDate();
        final DateTime lastCleanUnclosedDate = cleanUnclosedStatus.getLastEventDate();
        
        if (lastAggregatedDate.equals(lastCleanUnclosedDate)) {
            logger.debug("No events aggregated since last unclosed aggregation cleaning, skipping clean: {}", lastAggregatedDate);
            return new EventProcessingResult(0, null, null, true);
        }
        
        int closedAggregations = 0;
        final Thread currentThread = Thread.currentThread();
        final String currentName = currentThread.getName();
        try {
            currentThread.setName(currentName + "-" + lastAggregatedDate);
            
            //For each interval the aggregator supports, cleanup the unclosed aggregations
            for (final AggregationInterval interval : AggregationInterval.values()) {
                //Determine the current interval
                final AggregationIntervalInfo currentInterval = intervalHelper.getIntervalInfo(interval, lastAggregatedDate);
                if (currentInterval != null) {
                    //Use the start of the current interval as the end of the current cleanup range
                    final DateTime cleanBeforeDate = currentInterval.getStart();
                    
                    //Determine the end of the cleanup to use as the start of the cleanup range
                    final DateTime cleanAfterDate;
                    if (lastCleanUnclosedDate == null) {
                        cleanAfterDate = new DateTime(0);
                    }
                    else {
                        final AggregationIntervalInfo previousInterval = intervalHelper.getIntervalInfo(interval, lastCleanUnclosedDate);
                        if (previousInterval == null) {
                            cleanAfterDate = new DateTime(0);
                        }
                        else {
                            cleanAfterDate = previousInterval.getStart();
                        }
                    }
                    
                    logger.debug("Cleaning unclosed {} aggregations between {} and {}",  new Object[] { interval, cleanAfterDate, cleanBeforeDate});

                    for (final IPortalEventAggregator<PortalEvent> portalEventAggregator : portalEventAggregators) {
                        checkShutdown();
                        
                        final Class<? extends IPortalEventAggregator<?>> aggregatorType = getClass(portalEventAggregator);
                        
                        //Get aggregator specific interval info config
                        AggregatedIntervalConfig aggregatorIntervalConfig = eventAggregationManagementDao.getAggregatedIntervalConfig(aggregatorType);
                        if (aggregatorIntervalConfig == null) {
                            aggregatorIntervalConfig = eventAggregationManagementDao.getDefaultAggregatedIntervalConfig();
                        }
                        
                        //If the aggregator is being used for the specified interval call cleanUnclosedAggregations
                        if (aggregatorIntervalConfig.isIncluded(interval)) {
                            closedAggregations += portalEventAggregator.cleanUnclosedAggregations(cleanAfterDate, cleanBeforeDate, interval);
                        }
                    }
                }
            }
        }
        finally {
            currentThread.setName(currentName);
        }
        
        //Update the status object and store it
        cleanUnclosedStatus.setLastEventDate(lastAggregatedDate); 
        cleanUnclosedStatus.setLastEnd(new DateTime());
        eventAggregationManagementDao.updateEventAggregatorStatus(cleanUnclosedStatus);
        
        return new EventProcessingResult(closedAggregations, lastCleanUnclosedDate, lastAggregatedDate, true);
    }
    
    @SuppressWarnings("unchecked")
    protected final <T> Class<T> getClass(T object) {
        return (Class<T>)AopProxyUtils.ultimateTargetClass(object);
    }
}
