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
package org.apereo.portal.events.aggr.portletlayout;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.BaseAggregationImpl_;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.JpaBaseAggregationDao;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.dao.jpa.DateDimensionImpl;
import org.apereo.portal.events.aggr.dao.jpa.DateDimensionImpl_;
import org.apereo.portal.events.aggr.dao.jpa.TimeDimensionImpl;
import org.apereo.portal.events.aggr.dao.jpa.TimeDimensionImpl_;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMappingImpl;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;

/**
 * DAO for Portlet Layout Aggregations
 *
 */
@Repository
public class JpaPortletLayoutAggregationDao
        extends JpaBaseAggregationDao<PortletLayoutAggregationImpl, PortletLayoutAggregationKey>
        implements PortletLayoutAggregationPrivateDao {

    private ParameterExpression<Set> portletMappingParameter;

    public JpaPortletLayoutAggregationDao() {
        super(PortletLayoutAggregationImpl.class);
    }

    protected CriteriaQuery<PortletLayoutAggregationImpl>
            findAllPortletAggregationsByDateRangeQuery;

    @Override
    protected void createCriteriaQueries() {
        this.findAllPortletAggregationsByDateRangeQuery =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder, CriteriaQuery<PortletLayoutAggregationImpl>>() {
                            @Override
                            public CriteriaQuery<PortletLayoutAggregationImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<PortletLayoutAggregationImpl> criteriaQuery =
                                        cb.createQuery(PortletLayoutAggregationImpl.class);

                                final Root<PortletLayoutAggregationImpl> ba =
                                        criteriaQuery.from(PortletLayoutAggregationImpl.class);
                                final Join<PortletLayoutAggregationImpl, DateDimensionImpl> dd =
                                        ba.join(BaseAggregationImpl_.dateDimension, JoinType.LEFT);
                                final Join<PortletLayoutAggregationImpl, TimeDimensionImpl> td =
                                        ba.join(BaseAggregationImpl_.timeDimension, JoinType.LEFT);

                                final List<Predicate> keyPredicates = new ArrayList<Predicate>();
                                keyPredicates.add(
                                        cb.and( //Restrict results by outer date range
                                                cb.greaterThanOrEqualTo(
                                                        dd.get(DateDimensionImpl_.date), startDate),
                                                cb.lessThan(
                                                        dd.get(DateDimensionImpl_.date),
                                                        endPlusOneDate)));
                                keyPredicates.add(
                                        cb.or( //Restrict start of range by time as well
                                                cb.greaterThan(
                                                        dd.get(DateDimensionImpl_.date), startDate),
                                                cb.greaterThanOrEqualTo(
                                                        td.get(TimeDimensionImpl_.time),
                                                        startTime)));
                                keyPredicates.add(
                                        cb.or( //Restrict end of range by time as well
                                                cb.lessThan(
                                                        dd.get(DateDimensionImpl_.date), endDate),
                                                cb.lessThan(
                                                        td.get(TimeDimensionImpl_.time), endTime)));
                                keyPredicates.add(
                                        cb.equal(
                                                ba.get(BaseAggregationImpl_.interval),
                                                intervalParameter));
                                keyPredicates.add(
                                        ba.get(BaseAggregationImpl_.aggregatedGroup)
                                                .in(aggregatedGroupsParameter));

                                criteriaQuery.select(ba);
                                criteriaQuery.where(
                                        keyPredicates.toArray(new Predicate[keyPredicates.size()]));
                                criteriaQuery.orderBy(
                                        cb.desc(dd.get(DateDimensionImpl_.date)),
                                        cb.desc(td.get(TimeDimensionImpl_.time)));

                                return criteriaQuery;
                            }
                        });
    }

    @Override
    protected void createParameterExpressions() {
        this.portletMappingParameter = this.createParameterExpression(Set.class, "portletMapping");
    }

    @Override
    protected void addFetches(Root<PortletLayoutAggregationImpl> root) {}

    @Override
    protected void addUnclosedPredicate(
            CriteriaBuilder cb,
            Root<PortletLayoutAggregationImpl> root,
            List<Predicate> keyPredicates) {
        keyPredicates.add(cb.isFalse(root.get(PortletLayoutAggregationImpl_.complete)));
    }

    @Override
    protected void addAggregationSpecificKeyPredicate(
            CriteriaBuilder cb,
            Root<PortletLayoutAggregationImpl> root,
            List<Predicate> keyPredicates) {
        keyPredicates.add(
                root.get(PortletLayoutAggregationImpl_.aggregatedPortlet)
                        .in(portletMappingParameter));
    }

    public final List<PortletLayoutAggregationImpl> getAggregationsForAllPortlets(
            DateTime start,
            DateTime end,
            AggregationInterval interval,
            AggregatedGroupMapping aggregatedGroupMapping,
            AggregatedGroupMapping... aggregatedGroupMappings) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start must be before End: " + start + " - " + end);
        }
        final LocalDate startDate = start.toLocalDate();
        final LocalDate endDate = end.toLocalDate();

        final TypedQuery<PortletLayoutAggregationImpl> query =
                this.createQuery(this.findAllPortletAggregationsByDateRangeQuery);

        query.setParameter(this.startDate, startDate);
        query.setParameter(this.startTime, start.toLocalTime());

        query.setParameter(this.endDate, endDate);
        query.setParameter(this.endTime, end.toLocalTime());
        query.setParameter(this.endPlusOneDate, endDate.plusDays(1));

        query.setParameter(this.intervalParameter, interval);

        final Set<AggregatedGroupMapping> groups =
                ImmutableSet.<AggregatedGroupMapping>builder()
                        .add(aggregatedGroupMapping)
                        .add(aggregatedGroupMappings)
                        .build();
        query.setParameter(this.aggregatedGroupsParameter, groups);

        return query.getResultList();
    }

    @Override
    protected void bindAggregationSpecificKeyParameters(
            TypedQuery<PortletLayoutAggregationImpl> query, Set<PortletLayoutAggregationKey> keys) {
        query.setParameter(this.portletMappingParameter, extractAggregatePortletMappings(keys));
    }

    private Set<AggregatedPortletMapping> extractAggregatePortletMappings(
            Set<PortletLayoutAggregationKey> keys) {
        Set<AggregatedPortletMapping> portletMappings = new HashSet<AggregatedPortletMapping>();
        for (PortletLayoutAggregationKey key : keys) {
            portletMappings.add(key.getPortletMapping());
        }
        return portletMappings;
    }

    @Override
    protected void bindAggregationSpecificKeyParameters(
            NaturalIdQuery<PortletLayoutAggregationImpl> query, PortletLayoutAggregationKey key) {
        query.using(
                PortletLayoutAggregationImpl_.aggregatedPortlet,
                (AggregatedPortletMappingImpl) key.getPortletMapping());
    }

    @Override
    protected PortletLayoutAggregationImpl createAggregationInstance(
            PortletLayoutAggregationKey key) {
        final TimeDimension timeDimension = key.getTimeDimension();
        final DateDimension dateDimension = key.getDateDimension();
        final AggregationInterval interval = key.getInterval();
        final AggregatedGroupMapping aggregatedGroup = key.getAggregatedGroup();
        final AggregatedPortletMapping portletMapping = key.getPortletMapping();
        return new PortletLayoutAggregationImpl(
                timeDimension, dateDimension, interval, aggregatedGroup, portletMapping);
    }

    @Override
    protected PortletLayoutAggregationKey getAggregationKey(PortletLayoutAggregationImpl instance) {
        return instance.getAggregationKey();
    }
}
