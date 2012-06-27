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
import org.jasig.portal.events.aggr.portletexec.PortletExecutionAggregationKey.ExecutionType;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMappingImpl;
import org.springframework.stereotype.Repository;

/**
 * DAO for Tab Render Aggregations
 * 
 * @author Eric Dalquist
 */
@Repository
public class JpaPortletExecutionAggregationDao extends
        JpaBaseAggregationDao<PortletExecutionAggregationImpl, PortletExecutionAggregationKey> implements
        PortletExecutionAggregationPrivateDao {
    
    private ParameterExpression<AggregatedPortletMappingImpl> portletMappingParameter;
    private ParameterExpression<ExecutionType> executionTypeParameter;

    public JpaPortletExecutionAggregationDao() {
        super(PortletExecutionAggregationImpl.class);
    }
    

    @Override
    protected void createParameterExpressions() {
        this.portletMappingParameter = this.createParameterExpression(AggregatedPortletMappingImpl.class, "portletMapping");
        this.executionTypeParameter = this.createParameterExpression(ExecutionType.class, "executionType");
    }

    @Override
    protected void addFetches(Root<PortletExecutionAggregationImpl> root) {
        root.fetch(PortletExecutionAggregationImpl_.statisticalSummary, JoinType.LEFT);
    }

    @Override
    protected void addUnclosedPredicate(CriteriaBuilder cb, Root<PortletExecutionAggregationImpl> root,
            List<Predicate> keyPredicates) {
        keyPredicates.add(cb.isFalse(root.get(PortletExecutionAggregationImpl_.complete)));
    }

    @Override
    protected void addAggregationSpecificKeyPredicate(CriteriaBuilder cb, Root<PortletExecutionAggregationImpl> root,
            List<Predicate> keyPredicates) {
        keyPredicates.add(cb.equal(root.get(PortletExecutionAggregationImpl_.aggregatedPortlet), portletMappingParameter));
        keyPredicates.add(cb.equal(root.get(PortletExecutionAggregationImpl_.executionType), executionTypeParameter));
    }

    @Override
    protected void bindAggregationSpecificKeyParameters(TypedQuery<PortletExecutionAggregationImpl> query,
            PortletExecutionAggregationKey key) {
        query.setParameter(this.portletMappingParameter, (AggregatedPortletMappingImpl)key.getPortletMapping());
        query.setParameter(this.executionTypeParameter, key.getExecutionType());
    }
    
    @Override
    protected void bindAggregationSpecificKeyParameters(NaturalIdQuery<PortletExecutionAggregationImpl> query,
            PortletExecutionAggregationKey key) {
        query.using(PortletExecutionAggregationImpl_.aggregatedPortlet, (AggregatedPortletMappingImpl)key.getPortletMapping());
        query.using(PortletExecutionAggregationImpl_.executionType, key.getExecutionType());
    }


    @Override
    protected PortletExecutionAggregationImpl createAggregationInstance(PortletExecutionAggregationKey key) {
        final TimeDimension timeDimension = key.getTimeDimension();
        final DateDimension dateDimension = key.getDateDimension();
        final AggregationInterval interval = key.getInterval();
        final AggregatedGroupMapping aggregatedGroup = key.getAggregatedGroup();
        final AggregatedPortletMapping portletMapping = key.getPortletMapping();
        final ExecutionType executionType = key.getExecutionType();
        return new PortletExecutionAggregationImpl(timeDimension, dateDimension, interval, aggregatedGroup, portletMapping, executionType);
    }


    @Override
    protected PortletExecutionAggregationKey getAggregationKey(PortletExecutionAggregationImpl instance) {
        return instance.getAggregationKey();
    }
}
