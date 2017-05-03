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
package org.apereo.portal.events.aggr.portletexec;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.JpaBaseAggregationDao;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.portletexec.PortletExecutionAggregationKey.ExecutionType;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMappingImpl;
import org.springframework.stereotype.Repository;

/**
 * DAO for Portlet Execution Aggregations
 *
 */
@Repository
public class JpaPortletExecutionAggregationDao
        extends JpaBaseAggregationDao<
                PortletExecutionAggregationImpl, PortletExecutionAggregationKey>
        implements PortletExecutionAggregationPrivateDao {

    private ParameterExpression<Set> portletMappingParameter;
    private ParameterExpression<Set> executionTypeParameter;

    public JpaPortletExecutionAggregationDao() {
        super(PortletExecutionAggregationImpl.class);
    }

    @Override
    protected void createParameterExpressions() {
        this.portletMappingParameter = this.createParameterExpression(Set.class, "portletMapping");
        this.executionTypeParameter = this.createParameterExpression(Set.class, "executionType");
    }

    @Override
    protected void addFetches(Root<PortletExecutionAggregationImpl> root) {
        root.fetch(PortletExecutionAggregationImpl_.statisticalSummary, JoinType.LEFT);
    }

    @Override
    protected void addUnclosedPredicate(
            CriteriaBuilder cb,
            Root<PortletExecutionAggregationImpl> root,
            List<Predicate> keyPredicates) {
        keyPredicates.add(cb.isFalse(root.get(PortletExecutionAggregationImpl_.complete)));
    }

    @Override
    protected void addAggregationSpecificKeyPredicate(
            CriteriaBuilder cb,
            Root<PortletExecutionAggregationImpl> root,
            List<Predicate> keyPredicates) {
        keyPredicates.add(
                root.get(PortletExecutionAggregationImpl_.aggregatedPortlet)
                        .in(portletMappingParameter));
        keyPredicates.add(
                root.get(PortletExecutionAggregationImpl_.executionType)
                        .in(executionTypeParameter));
    }

    // The execution type is obtained from the first PortletExecutionAggregationKey.
    @Override
    protected void bindAggregationSpecificKeyParameters(
            TypedQuery<PortletExecutionAggregationImpl> query,
            Set<PortletExecutionAggregationKey> keys) {
        query.setParameter(this.portletMappingParameter, extractAggregatePortletMappings(keys));
        query.setParameter(this.executionTypeParameter, extractExecutionTypes(keys));
    }

    private Set<AggregatedPortletMapping> extractAggregatePortletMappings(
            Set<PortletExecutionAggregationKey> keys) {
        Set<AggregatedPortletMapping> portletMappings = new HashSet<AggregatedPortletMapping>();
        for (PortletExecutionAggregationKey key : keys) {
            portletMappings.add(key.getPortletMapping());
        }
        return portletMappings;
    }

    private Set<ExecutionType> extractExecutionTypes(Set<PortletExecutionAggregationKey> keys) {
        Set<ExecutionType> executionTypes = EnumSet.noneOf(ExecutionType.class);
        for (PortletExecutionAggregationKey key : keys) {
            executionTypes.add(key.getExecutionType());
        }
        return executionTypes;
    }

    @Override
    protected void bindAggregationSpecificKeyParameters(
            NaturalIdQuery<PortletExecutionAggregationImpl> query,
            PortletExecutionAggregationKey key) {
        query.using(
                PortletExecutionAggregationImpl_.aggregatedPortlet,
                (AggregatedPortletMappingImpl) key.getPortletMapping());
        query.using(PortletExecutionAggregationImpl_.executionType, key.getExecutionType());
    }

    @Override
    protected PortletExecutionAggregationImpl createAggregationInstance(
            PortletExecutionAggregationKey key) {
        final TimeDimension timeDimension = key.getTimeDimension();
        final DateDimension dateDimension = key.getDateDimension();
        final AggregationInterval interval = key.getInterval();
        final AggregatedGroupMapping aggregatedGroup = key.getAggregatedGroup();
        final AggregatedPortletMapping portletMapping = key.getPortletMapping();
        final ExecutionType executionType = key.getExecutionType();
        return new PortletExecutionAggregationImpl(
                timeDimension,
                dateDimension,
                interval,
                aggregatedGroup,
                portletMapping,
                executionType);
    }

    @Override
    protected PortletExecutionAggregationKey getAggregationKey(
            PortletExecutionAggregationImpl instance) {
        return instance.getAggregationKey();
    }
}
