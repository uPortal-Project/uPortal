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

package org.jasig.portal.events.aggr.portletexec;

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
import org.jasig.portal.events.aggr.portletexec.PortletExecutionAggregationKey.ExecutionType;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletLookupDao;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;
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
public class JpaPortletExecutionAggregationDaoTest extends JpaBaseAggregationDaoTest<PortletExecutionAggregationImpl, PortletExecutionAggregationKey> {
    @Autowired
    private PortletExecutionAggregationPrivateDao portletExecutionAggregationDao;
    @Autowired
    private AggregatedPortletLookupDao aggregatedPortletLookupDao;

    @Override
    protected BaseAggregationPrivateDao<PortletExecutionAggregationImpl, PortletExecutionAggregationKey> getAggregationDao() {
        return this.portletExecutionAggregationDao;
    }

    @Override
    protected void updateAggregation(AggregationIntervalInfo intervalInfo, PortletExecutionAggregationImpl aggregation,
            Random r) {
        aggregation.addValue(r.nextInt((int)TimeUnit.SECONDS.toNanos(10)));
    }

    @Override
    protected PortletExecutionAggregationKey createAggregationKey(AggregationIntervalInfo intervalInfo,
            AggregatedGroupMapping aggregatedGroup) {
        
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        
        final AggregatedPortletMapping mappedPortlet = aggregatedPortletLookupDao.getMappedPortletForFname("Foo");
        
        return new PortletExecutionAggregationKeyImpl(dateDimension, timeDimension, aggregationInterval, aggregatedGroup, mappedPortlet, ExecutionType.ALL);
    }

    @Override
    protected PortletExecutionAggregationKey createAggregationKey(AggregationInterval interval,
            AggregatedGroupMapping aggregatedGroup) {
        
        final AggregatedPortletMapping mappedPortlet = aggregatedPortletLookupDao.getMappedPortletForFname("Foo");
        return new PortletExecutionAggregationKeyImpl(interval, aggregatedGroup, mappedPortlet, ExecutionType.ALL);
    }

    @Override
    protected Map<PortletExecutionAggregationKey, PortletExecutionAggregationImpl> createAggregations(
            AggregationIntervalInfo intervalInfo, AggregatedGroupMapping aggregatedGroup) {
        
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        final AggregatedPortletMapping mappedPortlet = aggregatedPortletLookupDao.getMappedPortletForFname("Foo");
        final PortletExecutionAggregationKeyImpl key = new PortletExecutionAggregationKeyImpl(
                dateDimension, timeDimension, aggregationInterval, aggregatedGroup, mappedPortlet, ExecutionType.ALL);
        final PortletExecutionAggregationImpl aggr = portletExecutionAggregationDao.createAggregation(key);
        return Collections.<PortletExecutionAggregationKey, PortletExecutionAggregationImpl>singletonMap(key, aggr);
    }
    
}
