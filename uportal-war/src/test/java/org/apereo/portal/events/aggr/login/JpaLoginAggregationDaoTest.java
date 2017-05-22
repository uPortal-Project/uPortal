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
package org.apereo.portal.events.aggr.login;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang.RandomStringUtils;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.AggregationIntervalInfo;
import org.apereo.portal.events.aggr.BaseAggregationPrivateDao;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.JpaBaseAggregationDaoTest;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class JpaLoginAggregationDaoTest
        extends JpaBaseAggregationDaoTest<
                LoginAggregationImpl, LoginAggregationKey, LoginAggregationDiscriminator> {
    @Autowired private LoginAggregationPrivateDao loginAggregationDao;

    @Override
    protected BaseAggregationPrivateDao<LoginAggregationImpl, LoginAggregationKey>
            getAggregationDao() {
        return this.loginAggregationDao;
    }

    @Override
    protected void updateAggregation(
            AggregationIntervalInfo intervalInfo, LoginAggregationImpl aggregation, Random r) {
        aggregation.countUser(RandomStringUtils.random(8, 0, 0, true, true, null, r));
    }

    @Override
    protected LoginAggregationKey createAggregationKey(
            AggregationIntervalInfo intervalInfo, AggregatedGroupMapping aggregatedGroup) {
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        return new LoginAggregationKeyImpl(
                dateDimension, timeDimension, aggregationInterval, aggregatedGroup);
    }

    @Override
    protected LoginAggregationKey createAggregationKey(
            AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        return new LoginAggregationKeyImpl(interval, aggregatedGroup);
    }

    @Override
    protected Map<LoginAggregationKey, LoginAggregationImpl> createAggregations(
            AggregationIntervalInfo intervalInfo, AggregatedGroupMapping aggregatedGroup) {

        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        final LoginAggregationKeyImpl key =
                new LoginAggregationKeyImpl(
                        dateDimension, timeDimension, aggregationInterval, aggregatedGroup);
        final LoginAggregationImpl aggr = loginAggregationDao.createAggregation(key);
        return Collections.<LoginAggregationKey, LoginAggregationImpl>singletonMap(key, aggr);
    }
}
