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

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.JpaBaseAggregationDao;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMapping;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMappingImpl;
import org.jasig.portal.jpa.BaseJpaDao.NaturalIdQuery;
import org.springframework.stereotype.Repository;

/**
 * DAO for Tab Render Aggregations
 * 
 * @author Eric Dalquist
 */
@Repository
public class JpaTabRenderAggregationDao extends
        JpaBaseAggregationDao<TabRenderAggregationImpl, TabRenderAggregationKey> implements
        TabRenderAggregationPrivateDao {
    
    private ParameterExpression<AggregatedTabMappingImpl> tabMappingParameter;

    public JpaTabRenderAggregationDao() {
        super(TabRenderAggregationImpl.class);
    }
    

    @Override
    protected void createParameterExpressions() {
        this.tabMappingParameter = this.createParameterExpression(AggregatedTabMappingImpl.class, "tabMapping");
    }

    @Override
    protected void addFetches(Root<TabRenderAggregationImpl> root) {
        root.fetch(TabRenderAggregationImpl_.statisticalSummary, JoinType.LEFT);
    }

    @Override
    protected void addUnclosedPredicate(CriteriaBuilder cb, Root<TabRenderAggregationImpl> root,
            List<Predicate> keyPredicates) {
        keyPredicates.add(cb.isFalse(root.get(TabRenderAggregationImpl_.complete)));
    }

    @Override
    protected void addAggregationSpecificKeyPredicate(CriteriaBuilder cb, Root<TabRenderAggregationImpl> root,
            List<Predicate> keyPredicates) {
        keyPredicates.add(cb.equal(root.get(TabRenderAggregationImpl_.aggregatedTab), tabMappingParameter));
    }

    @Override
    protected void bindAggregationSpecificKeyParameters(TypedQuery<TabRenderAggregationImpl> query,
            TabRenderAggregationKey key) {
        query.setParameter(this.tabMappingParameter, (AggregatedTabMappingImpl)key.getTabMapping());
    }
    
    @Override
    protected void bindAggregationSpecificKeyParameters(NaturalIdQuery<TabRenderAggregationImpl> query,
            TabRenderAggregationKey key) {
        query.using(TabRenderAggregationImpl_.aggregatedTab, (AggregatedTabMappingImpl)key.getTabMapping());
    }

    @Override
    protected TabRenderAggregationImpl createAggregationInstance(TabRenderAggregationKey key) {
        final TimeDimension timeDimension = key.getTimeDimension();
        final DateDimension dateDimension = key.getDateDimension();
        final AggregationInterval interval = key.getInterval();
        final AggregatedGroupMapping aggregatedGroup = key.getAggregatedGroup();
        final AggregatedTabMapping tabMapping = key.getTabMapping();
        return new TabRenderAggregationImpl(timeDimension, dateDimension, interval, aggregatedGroup, tabMapping);
    }

    @Override
    protected TabRenderAggregationKey getAggregationKey(TabRenderAggregationImpl instance) {
        return instance.getAggregationKey();
    }
}
