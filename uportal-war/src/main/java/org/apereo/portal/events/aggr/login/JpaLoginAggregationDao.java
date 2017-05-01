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

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.JpaBaseAggregationDao;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.springframework.stereotype.Repository;

/**
 * DAO For Login Aggregations
 *
 */
@Repository
public class JpaLoginAggregationDao
        extends JpaBaseAggregationDao<LoginAggregationImpl, LoginAggregationKey>
        implements LoginAggregationPrivateDao {
    public JpaLoginAggregationDao() {
        super(LoginAggregationImpl.class);
    }

    @Override
    protected void addFetches(Root<LoginAggregationImpl> root) {
        root.fetch(LoginAggregationImpl_.uniqueStrings, JoinType.LEFT);
    }

    @Override
    protected void addUnclosedPredicate(
            CriteriaBuilder cb, Root<LoginAggregationImpl> root, List<Predicate> keyPredicates) {
        keyPredicates.add(cb.isNotNull(root.get(LoginAggregationImpl_.uniqueStrings)));
    }

    @Override
    protected LoginAggregationImpl createAggregationInstance(LoginAggregationKey key) {
        final TimeDimension timeDimension = key.getTimeDimension();
        final DateDimension dateDimension = key.getDateDimension();
        final AggregationInterval interval = key.getInterval();
        final AggregatedGroupMapping aggregatedGroup = key.getAggregatedGroup();
        return new LoginAggregationImpl(timeDimension, dateDimension, interval, aggregatedGroup);
    }

    @Override
    protected LoginAggregationKey getAggregationKey(LoginAggregationImpl instance) {
        return instance.getAggregationKey();
    }
}
