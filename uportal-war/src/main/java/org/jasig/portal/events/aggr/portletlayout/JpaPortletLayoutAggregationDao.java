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
package org.jasig.portal.events.aggr.portletlayout;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.JpaBaseAggregationDao;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMappingImpl;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO for Portlet Layout Aggregations
 *
 * @author Chris Waymire <cwaymire@unicon.net>
 */
@Repository
public class JpaPortletLayoutAggregationDao extends
        JpaBaseAggregationDao<PortletLayoutAggregationImpl, PortletLayoutAggregationKey> implements
        PortletLayoutAggregationPrivateDao {

    private ParameterExpression<Set> portletMappingParameter;

    public JpaPortletLayoutAggregationDao() {
        super(PortletLayoutAggregationImpl.class);
    }


    @Override
    protected void createParameterExpressions() {
        this.portletMappingParameter = this.createParameterExpression(Set.class, "portletMapping");
    }

    @Override
    protected void addFetches(Root<PortletLayoutAggregationImpl> root) {
        //root.fetch(PortletAddAggregationImpl_.uniqueStrings, JoinType.LEFT);
    }

    @Override
    protected void addUnclosedPredicate(CriteriaBuilder cb, Root<PortletLayoutAggregationImpl> root,
                                        List<Predicate> keyPredicates) {
        keyPredicates.add(cb.isFalse(root.get(PortletLayoutAggregationImpl_.complete)));
    }

    @Override
    protected void addAggregationSpecificKeyPredicate(CriteriaBuilder cb, Root<PortletLayoutAggregationImpl> root,
                                                      List<Predicate> keyPredicates) {
        keyPredicates.add(root.get(PortletLayoutAggregationImpl_.aggregatedPortlet).in(portletMappingParameter));
    }

    @Override
    protected void bindAggregationSpecificKeyParameters(TypedQuery<PortletLayoutAggregationImpl> query,
                                                        Set<PortletLayoutAggregationKey> keys) {
        query.setParameter(this.portletMappingParameter, extractAggregatePortletMappings(keys));
    }

    private Set<AggregatedPortletMapping> extractAggregatePortletMappings(Set<PortletLayoutAggregationKey> keys) {
        Set<AggregatedPortletMapping> portletMappings = new HashSet<AggregatedPortletMapping>();
        for (PortletLayoutAggregationKey key : keys) {
            portletMappings.add(key.getPortletMapping());
        }
        return portletMappings;
    }

    @Override
    protected void bindAggregationSpecificKeyParameters(NaturalIdQuery<PortletLayoutAggregationImpl> query,
                                                        PortletLayoutAggregationKey key) {
        query.using(PortletLayoutAggregationImpl_.aggregatedPortlet, (AggregatedPortletMappingImpl)key.getPortletMapping());
    }

    @Override
    protected PortletLayoutAggregationImpl createAggregationInstance(PortletLayoutAggregationKey key) {
        final TimeDimension timeDimension = key.getTimeDimension();
        final DateDimension dateDimension = key.getDateDimension();
        final AggregationInterval interval = key.getInterval();
        final AggregatedGroupMapping aggregatedGroup = key.getAggregatedGroup();
        final AggregatedPortletMapping portletMapping = key.getPortletMapping();
        return new PortletLayoutAggregationImpl(timeDimension, dateDimension, interval, aggregatedGroup,portletMapping);
    }

    @Override
    protected PortletLayoutAggregationKey getAggregationKey(PortletLayoutAggregationImpl instance) {
        return instance.getAggregationKey();
    }}
