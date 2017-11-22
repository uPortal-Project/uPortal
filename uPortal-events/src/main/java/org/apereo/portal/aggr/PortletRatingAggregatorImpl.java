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
package org.apereo.portal.aggr;

import org.apereo.portal.concurrency.FunctionWithoutResult;
import org.apereo.portal.concurrency.locking.ClusterMutex;
import org.apereo.portal.concurrency.locking.IClusterLockService;
import org.apereo.portal.concurrency.locking.IClusterLockService.LockStatus;
import org.apereo.portal.concurrency.locking.IClusterLockService.TryLockFunctionResult;
import org.apereo.portal.concurrency.locking.LockOptions;
import org.apereo.portal.portlet.dao.IMarketplaceRatingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("portletRatingAggregator")
public class PortletRatingAggregatorImpl implements PortletRatingAggregator, DisposableBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String PORTLET_RATING_AGGREGATOR_LOCK_NAME =
            PortletRatingAggregatorImpl.class.getName() + ".PURGE_LOCK";

    private IClusterLockService clusterLockService;
    private IMarketplaceRatingDao marketplaceRatingDao;
    private volatile boolean shutdown = false;

    @Override
    public void destroy() throws Exception {
        this.shutdown = true;
    }

    @Autowired
    public void setClusterLockService(IClusterLockService clusterLockService) {
        this.clusterLockService = clusterLockService;
    }

    @Autowired
    public void setMarketplaceRatingDao(IMarketplaceRatingDao dao) {
        this.marketplaceRatingDao = dao;
    }

    @Override
    public boolean aggregatePortletRatings() {

        if (shutdown) {
            logger.warn("aggregatePortletRatings called after shutdown, ignoring call");
            return false;
        }

        try {
            final TryLockFunctionResult<Object> result =
                    this.clusterLockService.doInTryLock(
                            PORTLET_RATING_AGGREGATOR_LOCK_NAME,
                            LockOptions.builder().lastRunDelay(0),
                            new FunctionWithoutResult<ClusterMutex>() {
                                @Override
                                protected void applyWithoutResult(ClusterMutex input) {
                                    marketplaceRatingDao.aggregateMarketplaceRating();
                                }
                            });
            return result.getLockStatus() == LockStatus.EXECUTED;
        } catch (InterruptedException e) {
            logger.warn("Interrupted while aggregating portlet ratings", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
