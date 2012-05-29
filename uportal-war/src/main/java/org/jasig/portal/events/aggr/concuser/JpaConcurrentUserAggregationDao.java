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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.JpaBaseAggregationDao;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.springframework.stereotype.Repository;

/**
 * DAO for Concurrent User Aggregations
 * 
 * @author Eric Dalquist
 */
@Repository
public class JpaConcurrentUserAggregationDao extends JpaBaseAggregationDao<ConcurrentUserAggregationImpl> implements ConcurrentUserAggregationPrivateDao {

    public JpaConcurrentUserAggregationDao() {
        super(ConcurrentUserAggregationImpl.class);
    }
    
    @Override
    protected void addFetches(Root<ConcurrentUserAggregationImpl> root) {
        root.fetch(ConcurrentUserAggregationImpl_.uniqueSessionIds, JoinType.LEFT);        
    }

    @Override
    protected Predicate createUnclosedPredicate(CriteriaBuilder cb, Root<ConcurrentUserAggregationImpl> root) {
        return cb.notEqual(cb.size(root.get(ConcurrentUserAggregationImpl_.uniqueSessionIds)), 0);
    }

    @Override
    protected ConcurrentUserAggregationImpl createAggregationInstance(DateDimension dateDimension,
            TimeDimension timeDimension, AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        return new ConcurrentUserAggregationImpl(timeDimension, dateDimension, interval, aggregatedGroup);
    }
}
