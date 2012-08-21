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

package org.jasig.portal.events.aggr.tabrender;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalInfo;
import org.jasig.portal.events.aggr.BaseAggregationPrivateDao;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.JpaBaseAggregationDaoTest;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.tabs.AggregatedTabLookupDao;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMapping;
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
public class JpaTabRenderAggregationDaoTest extends JpaBaseAggregationDaoTest<TabRenderAggregationImpl, TabRenderAggregationKey> {
    @Autowired
    private TabRenderAggregationPrivateDao renderAggregationDao;
    @Autowired
    private AggregatedTabLookupDao aggregatedTabLookupDao;

    @Override
    protected BaseAggregationPrivateDao<TabRenderAggregationImpl, TabRenderAggregationKey> getAggregationDao() {
        return this.renderAggregationDao;
    }

    @Override
    protected void updateAggregation(AggregationIntervalInfo intervalInfo, TabRenderAggregationImpl aggregation,
            Random r) {
        aggregation.addValue(r.nextInt((int)TimeUnit.SECONDS.toNanos(10)));
    }

    @Override
    protected TabRenderAggregationKey createAggregationKey(AggregationIntervalInfo intervalInfo,
            AggregatedGroupMapping aggregatedGroup) {
        
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        final AggregatedTabMapping mappedTab = this.aggregatedTabLookupDao.getMappedTabForLayoutId("u1l1n1");
        return new TabRenderAggregationKeyImpl(dateDimension, timeDimension, aggregationInterval, aggregatedGroup, mappedTab);
    }

    @Override
    protected TabRenderAggregationKey createAggregationKey(AggregationInterval interval,
            AggregatedGroupMapping aggregatedGroup) {
        final AggregatedTabMapping mappedTab = this.aggregatedTabLookupDao.getMappedTabForLayoutId("u1l1n1");
        return new TabRenderAggregationKeyImpl(interval, aggregatedGroup, mappedTab);
    }

    @Override
    protected Map<TabRenderAggregationKey, TabRenderAggregationImpl> createAggregations(
            AggregationIntervalInfo intervalInfo, AggregatedGroupMapping aggregatedGroup) {
        
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        final AggregatedTabMapping mappedTab = this.aggregatedTabLookupDao.getMappedTabForLayoutId("u1l1n1");
        final TabRenderAggregationKeyImpl key = new TabRenderAggregationKeyImpl(
                dateDimension, timeDimension, aggregationInterval, aggregatedGroup, mappedTab);
        final TabRenderAggregationImpl aggr = renderAggregationDao.createAggregation(key);
        return Collections.<TabRenderAggregationKey, TabRenderAggregationImpl>singletonMap(key, aggr);
    }
    
}
