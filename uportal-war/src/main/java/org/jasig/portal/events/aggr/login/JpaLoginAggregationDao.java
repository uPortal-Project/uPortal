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

package org.jasig.portal.events.aggr.login;

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
 * DAO For Login Aggregations
 * 
 * @author Eric Dalquist
 */
@Repository
public class JpaLoginAggregationDao extends JpaBaseAggregationDao<LoginAggregationImpl> implements LoginAggregationPrivateDao {

    public JpaLoginAggregationDao() {
        super(LoginAggregationImpl.class);
    }
    
    @Override
    protected void addFetches(Root<LoginAggregationImpl> root) {
        root.fetch(LoginAggregationImpl_.uniqueUserNames, JoinType.LEFT);        
    }

    @Override
    protected Predicate createUnclosedPredicate(CriteriaBuilder cb, Root<LoginAggregationImpl> root) {
        return cb.notEqual(cb.size(root.get(LoginAggregationImpl_.uniqueUserNames)), 0);
    }

    @Override
    protected LoginAggregationImpl createAggregationInstance(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        return new LoginAggregationImpl(timeDimension, dateDimension, interval, aggregatedGroup);
    }
}
