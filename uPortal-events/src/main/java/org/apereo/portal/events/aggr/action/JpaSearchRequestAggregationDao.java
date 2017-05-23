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
package org.apereo.portal.events.aggr.action;

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
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSearchRequestAggregationDao
        extends JpaBaseAggregationDao<SearchRequestAggregationImpl, SearchRequestAggregationKey>
        implements SearchRequestAggregationPrivateDao {

    private ParameterExpression<Set> searchTermParameter;

    protected CriteriaQuery<SearchRequestAggregationImpl>
            findAllSearchRequestAggregationsByDateRangeQuery;

    public JpaSearchRequestAggregationDao() {
        super(SearchRequestAggregationImpl.class);
    }

    @Override
    protected void createCriteriaQueries() {
        this.findAllSearchRequestAggregationsByDateRangeQuery =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder, CriteriaQuery<SearchRequestAggregationImpl>>() {
                            @Override
                            public CriteriaQuery<SearchRequestAggregationImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<SearchRequestAggregationImpl> criteriaQuery =
                                        cb.createQuery(SearchRequestAggregationImpl.class);

                                final Root<SearchRequestAggregationImpl> ba =
                                        criteriaQuery.from(SearchRequestAggregationImpl.class);
                                final Join<SearchRequestAggregationImpl, DateDimensionImpl> dd =
                                        ba.join(BaseAggregationImpl_.dateDimension, JoinType.LEFT);
                                final Join<SearchRequestAggregationImpl, TimeDimensionImpl> td =
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
        this.searchTermParameter = this.createParameterExpression(Set.class, "searchTerm");
    }

    @Override
    protected void addFetches(Root<SearchRequestAggregationImpl> root) {}

    @Override
    protected void addUnclosedPredicate(
            CriteriaBuilder cb,
            Root<SearchRequestAggregationImpl> root,
            List<Predicate> keyPredicates) {
        keyPredicates.add(cb.isFalse(root.get(SearchRequestAggregationImpl_.complete)));
    }

    @Override
    protected SearchRequestAggregationImpl createAggregationInstance(
            SearchRequestAggregationKey key) {
        final TimeDimension timeDimension = key.getTimeDimension();
        final DateDimension dateDimension = key.getDateDimension();
        final AggregationInterval interval = key.getInterval();
        final AggregatedGroupMapping aggregatedGroup = key.getAggregatedGroup();
        final String searchTerm = key.getSearchTerm();
        return new SearchRequestAggregationImpl(
                timeDimension, dateDimension, interval, aggregatedGroup, searchTerm);
    }

    @Override
    protected SearchRequestAggregationKey getAggregationKey(SearchRequestAggregationImpl instance) {
        return instance.getAggregationKey();
    }

    @Override
    protected void bindAggregationSpecificKeyParameters(
            TypedQuery<SearchRequestAggregationImpl> query, Set<SearchRequestAggregationKey> keys) {
        query.setParameter(this.searchTermParameter, extractSearchTerms(keys));
    }

    @Override
    protected void bindAggregationSpecificKeyParameters(
            NaturalIdQuery<SearchRequestAggregationImpl> query, SearchRequestAggregationKey key) {
        query.using(SearchRequestAggregationImpl_.searchTerm, key.getSearchTerm());
    }

    @Override
    public final List<SearchRequestAggregationImpl> getAggregations(
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

        final TypedQuery<SearchRequestAggregationImpl> query =
                this.createQuery(this.findAllSearchRequestAggregationsByDateRangeQuery);

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

    private Set<String> extractSearchTerms(Set<SearchRequestAggregationKey> keys) {
        Set<String> searchTerms = new HashSet<String>();
        for (SearchRequestAggregationKey key : keys) {
            searchTerms.add(key.getSearchTerm());
        }
        return searchTerms;
    }
}
