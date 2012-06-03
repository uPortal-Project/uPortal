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

package org.jasig.portal.events.aggr.concuser;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalInfo;
import org.jasig.portal.events.aggr.BaseAggregationPrivateDao;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.JpaBaseAggregationDaoTest;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class JpaConcurrentUserAggregationDaoTest extends JpaBaseAggregationDaoTest<ConcurrentUserAggregationImpl, ConcurrentUserAggregationKey> {
    @Autowired
    private ConcurrentUserAggregationPrivateDao concurrentUserAggregationDao;
    
    @Override
    protected BaseAggregationPrivateDao<ConcurrentUserAggregationImpl, ConcurrentUserAggregationKey> getAggregationDao() {
        return this.concurrentUserAggregationDao;
    }

    @Override
    protected void updateAggregation(AggregationIntervalInfo intervalInfo, ConcurrentUserAggregationImpl aggregation, Random r) {
        aggregation.countSession(RandomStringUtils.random(8, 0, 0, true, true, null, r));
    }

    @Override
    protected ConcurrentUserAggregationKey createAggregationKey(AggregationIntervalInfo intervalInfo, AggregatedGroupMapping aggregatedGroup) {
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        return new ConcurrentUserAggregationKeyImpl(dateDimension, timeDimension, aggregationInterval, aggregatedGroup);
    }

    @Override
    protected ConcurrentUserAggregationKey createAggregationKey(AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        return new ConcurrentUserAggregationKeyImpl(interval, aggregatedGroup);
    }

    @Override
    protected Map<ConcurrentUserAggregationKey, ConcurrentUserAggregationImpl> createAggregations(
            AggregationIntervalInfo intervalInfo, AggregatedGroupMapping aggregatedGroup) {
        
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        final ConcurrentUserAggregationKeyImpl key = new ConcurrentUserAggregationKeyImpl(dateDimension, timeDimension, aggregationInterval, aggregatedGroup);
        final ConcurrentUserAggregationImpl aggr = concurrentUserAggregationDao.createAggregation(key);
        return Collections.<ConcurrentUserAggregationKey, ConcurrentUserAggregationImpl>singletonMap(key, aggr);
    }
}
