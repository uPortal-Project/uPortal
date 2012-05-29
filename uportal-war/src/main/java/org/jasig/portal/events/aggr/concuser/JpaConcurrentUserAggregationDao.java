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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.ArrayUtils;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.dao.jpa.DateDimensionImpl;
import org.jasig.portal.events.aggr.dao.jpa.DateDimensionImpl_;
import org.jasig.portal.events.aggr.dao.jpa.TimeDimensionImpl;
import org.jasig.portal.events.aggr.dao.jpa.TimeDimensionImpl_;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaConcurrentUserAggregationDao extends BaseAggrEventsJpaDao implements ConcurrentUserAggregationPrivateDao {

    private CriteriaQuery<ConcurrentUserAggregationImpl> findConcurrentUserAggregationByDateTimeIntervalQuery;
    private CriteriaQuery<ConcurrentUserAggregationImpl> findConcurrentUserAggregationByDateTimeIntervalGroupQuery;
    private CriteriaQuery<ConcurrentUserAggregationImpl> findConcurrentUserAggregationsByDateRangeQuery;
    private CriteriaQuery<ConcurrentUserAggregationImpl> findUnclosedConcurrentUserAggregationsByDateRangeQuery;
    private ParameterExpression<TimeDimension> timeDimensionParameter;
    private ParameterExpression<DateDimension> dateDimensionParameter;
    private ParameterExpression<AggregationInterval> intervalParameter;
    private ParameterExpression<AggregatedGroupMapping> aggregatedGroupParameter;
    private ParameterExpression<Set> aggregatedGroupsParameter;
    private ParameterExpression<LocalDate> startDate;
    private ParameterExpression<LocalDate> endMinusOneDate;
    private ParameterExpression<LocalDate> endDate;
    private ParameterExpression<LocalTime> startTime;
    private ParameterExpression<LocalTime> endTime;
    

    @Override
    public void afterPropertiesSet() throws Exception {
        this.timeDimensionParameter = this.createParameterExpression(TimeDimension.class, "timeDimension");
        this.dateDimensionParameter = this.createParameterExpression(DateDimension.class, "dateDimension");
        this.intervalParameter = this.createParameterExpression(AggregationInterval.class, "interval");
        this.aggregatedGroupParameter = this.createParameterExpression(AggregatedGroupMapping.class, "aggregatedGroup");
        this.aggregatedGroupsParameter = this.createParameterExpression(Set.class, "aggregatedGroups");
        this.startDate = this.createParameterExpression(LocalDate.class, "startDate");
        this.endMinusOneDate = this.createParameterExpression(LocalDate.class, "endMinusOneDate");
        this.endDate = this.createParameterExpression(LocalDate.class, "endDate");
        this.startTime = this.createParameterExpression(LocalTime.class, "startTime");
        this.endTime = this.createParameterExpression(LocalTime.class, "endTime");
        
        this.findConcurrentUserAggregationByDateTimeIntervalQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<ConcurrentUserAggregationImpl>>() {
            @Override
            public CriteriaQuery<ConcurrentUserAggregationImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<ConcurrentUserAggregationImpl> criteriaQuery = cb.createQuery(ConcurrentUserAggregationImpl.class);
                
                final Root<ConcurrentUserAggregationImpl> cua = criteriaQuery.from(ConcurrentUserAggregationImpl.class);

                cua.fetch(ConcurrentUserAggregationImpl_.uniqueSessionIds, JoinType.LEFT);
                
                criteriaQuery.select(cua);
                criteriaQuery.where(
                        cb.and(
                            cb.equal(cua.get(ConcurrentUserAggregationImpl_.dateDimension), dateDimensionParameter),
                            cb.equal(cua.get(ConcurrentUserAggregationImpl_.timeDimension), timeDimensionParameter),
                            cb.equal(cua.get(ConcurrentUserAggregationImpl_.interval), intervalParameter)
                        )
                    );
                
                return criteriaQuery;
            }
        });

        
        this.findConcurrentUserAggregationByDateTimeIntervalGroupQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<ConcurrentUserAggregationImpl>>() {
            @Override
            public CriteriaQuery<ConcurrentUserAggregationImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<ConcurrentUserAggregationImpl> criteriaQuery = cb.createQuery(ConcurrentUserAggregationImpl.class);
                final Root<ConcurrentUserAggregationImpl> cua = criteriaQuery.from(ConcurrentUserAggregationImpl.class);
                
                cua.fetch(ConcurrentUserAggregationImpl_.uniqueSessionIds, JoinType.LEFT);

                criteriaQuery.select(cua);
                criteriaQuery.where(
                        cb.and(
                            cb.equal(cua.get(ConcurrentUserAggregationImpl_.dateDimension), dateDimensionParameter),
                            cb.equal(cua.get(ConcurrentUserAggregationImpl_.timeDimension), timeDimensionParameter),
                            cb.equal(cua.get(ConcurrentUserAggregationImpl_.interval), intervalParameter),
                            cb.equal(cua.get(ConcurrentUserAggregationImpl_.aggregatedGroup), aggregatedGroupParameter)
                        )
                    );
                
                return criteriaQuery;
            }
        });


        this.findConcurrentUserAggregationsByDateRangeQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<ConcurrentUserAggregationImpl>>() {
            @Override
            public CriteriaQuery<ConcurrentUserAggregationImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<ConcurrentUserAggregationImpl> criteriaQuery = cb.createQuery(ConcurrentUserAggregationImpl.class);
                
                final Root<ConcurrentUserAggregationImpl> cua = criteriaQuery.from(ConcurrentUserAggregationImpl.class);
                final Join<ConcurrentUserAggregationImpl, DateDimensionImpl> dd = cua.join(ConcurrentUserAggregationImpl_.dateDimension, JoinType.LEFT);
                final Join<ConcurrentUserAggregationImpl, TimeDimensionImpl> td = cua.join(ConcurrentUserAggregationImpl_.timeDimension, JoinType.LEFT);
                
                criteriaQuery.select(cua);
                criteriaQuery.where(
                    cb.and(
                        cb.and( //Restrict results by outer date range
                            cb.greaterThanOrEqualTo(dd.get(DateDimensionImpl_.date), startDate),
                            cb.lessThan(dd.get(DateDimensionImpl_.date), endDate)
                        ),
                        cb.or( //Restrict start of range by time as well
                            cb.greaterThan(dd.get(DateDimensionImpl_.date), startDate),
                            cb.greaterThanOrEqualTo(td.get(TimeDimensionImpl_.time), startTime)
                        ),
                        cb.or( //Restrict end of range by time as well
                            cb.lessThan(dd.get(DateDimensionImpl_.date), endMinusOneDate),
                            cb.lessThan(td.get(TimeDimensionImpl_.time), endTime)
                        ),
                        cb.equal(cua.get(ConcurrentUserAggregationImpl_.interval), intervalParameter),
                        cua.get(ConcurrentUserAggregationImpl_.aggregatedGroup).in(aggregatedGroupsParameter)
                    )
                );
                criteriaQuery.orderBy(cb.desc(dd.get(DateDimensionImpl_.date)), cb.desc(td.get(TimeDimensionImpl_.time)));
                
                return criteriaQuery;
            }
        });
        
        /*
         * Similar to the previous query but only returns aggregates that have entries in their uniqueUserNames set. This is
         * used for finding aggregates that missed having intervalComplete called due to interval boundary placement.
         */
        this.findUnclosedConcurrentUserAggregationsByDateRangeQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<ConcurrentUserAggregationImpl>>() {
            @Override
            public CriteriaQuery<ConcurrentUserAggregationImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<ConcurrentUserAggregationImpl> criteriaQuery = cb.createQuery(ConcurrentUserAggregationImpl.class);
                
                final Root<ConcurrentUserAggregationImpl> cua = criteriaQuery.from(ConcurrentUserAggregationImpl.class);
                final Join<ConcurrentUserAggregationImpl, DateDimensionImpl> dd = cua.join(ConcurrentUserAggregationImpl_.dateDimension, JoinType.LEFT);
                final Join<ConcurrentUserAggregationImpl, TimeDimensionImpl> td = cua.join(ConcurrentUserAggregationImpl_.timeDimension, JoinType.LEFT);
                
                cua.fetch(ConcurrentUserAggregationImpl_.uniqueSessionIds, JoinType.LEFT);

                criteriaQuery.select(cua);
                criteriaQuery.where(
                    cb.and(
                        cb.and( //Restrict results by outer date range
                            cb.greaterThanOrEqualTo(dd.get(DateDimensionImpl_.date), startDate),
                            cb.lessThan(dd.get(DateDimensionImpl_.date), endDate)
                        ),
                        cb.or( //Restrict start of range by time as well
                            cb.greaterThan(dd.get(DateDimensionImpl_.date), startDate),
                            cb.greaterThanOrEqualTo(td.get(TimeDimensionImpl_.time), startTime)
                        ),
                        cb.or( //Restrict end of range by time as well
                            cb.lessThan(dd.get(DateDimensionImpl_.date), endMinusOneDate),
                            cb.lessThan(td.get(TimeDimensionImpl_.time), endTime)
                        ),
                        cb.equal(cua.get(ConcurrentUserAggregationImpl_.interval), intervalParameter),
                        cb.notEqual(cb.size(cua.get(ConcurrentUserAggregationImpl_.uniqueSessionIds)), 0)
                    )
                );
                
                return criteriaQuery;
            }
        });
    }
    
    @Override
    public Set<ConcurrentUserAggregationImpl> getUnclosedConcurrentUserAggregations(DateTime start, DateTime end, AggregationInterval interval) {
        final TypedQuery<ConcurrentUserAggregationImpl> query = this.createQuery(findUnclosedConcurrentUserAggregationsByDateRangeQuery);
        
        query.setParameter(this.startDate, start.toLocalDate());
        query.setParameter(this.startTime, start.toLocalTime());
        
        query.setParameter(this.endDate, end.toLocalDate());
        query.setParameter(this.endTime, end.toLocalTime());
        query.setParameter(this.endMinusOneDate, end.minusDays(1).toLocalDate());
        
        query.setParameter(this.intervalParameter, interval);
        
        return new LinkedHashSet<ConcurrentUserAggregationImpl>(query.getResultList());
    }
    
    @Override
    public List<ConcurrentUserAggregationImpl> getConcurrentUserAggregations(DateTime start, DateTime end, AggregationInterval interval,
            AggregatedGroupMapping aggregatedGroupMapping, AggregatedGroupMapping... aggregatedGroupMappings) {
        
        final TypedQuery<ConcurrentUserAggregationImpl> query = this.createCachedQuery(findConcurrentUserAggregationsByDateRangeQuery);
        
        query.setParameter(this.startDate, start.toLocalDate());
        query.setParameter(this.startTime, start.toLocalTime());
        
        query.setParameter(this.endDate, end.toLocalDate());
        query.setParameter(this.endTime, end.toLocalTime());
        query.setParameter(this.endMinusOneDate, end.minusDays(1).toLocalDate());
        
        query.setParameter(this.intervalParameter, interval);

        aggregatedGroupMappings = (AggregatedGroupMapping[])ArrayUtils.add(aggregatedGroupMappings, aggregatedGroupMapping);
        query.setParameter(this.aggregatedGroupsParameter, ImmutableSet.copyOf(aggregatedGroupMappings));
        
        return new ArrayList<ConcurrentUserAggregationImpl>(query.getResultList());
    }

    
    @Override
    public Set<ConcurrentUserAggregationImpl> getConcurrentUserAggregationsForInterval(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval) {
        final TypedQuery<ConcurrentUserAggregationImpl> query = this.createQuery(this.findConcurrentUserAggregationByDateTimeIntervalQuery);
        query.setParameter(this.dateDimensionParameter, dateDimension);
        query.setParameter(this.timeDimensionParameter, timeDimension);
        query.setParameter(this.intervalParameter, interval);
        
        final List<ConcurrentUserAggregationImpl> results = query.getResultList();
        return new LinkedHashSet<ConcurrentUserAggregationImpl>(results);
    }

    @Override
    public ConcurrentUserAggregationImpl getConcurrentUserAggregation(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        final TypedQuery<ConcurrentUserAggregationImpl> query = this.createCachedQuery(this.findConcurrentUserAggregationByDateTimeIntervalGroupQuery);
        query.setParameter(this.dateDimensionParameter, dateDimension);
        query.setParameter(this.timeDimensionParameter, timeDimension);
        query.setParameter(this.intervalParameter, interval);
        query.setParameter(this.aggregatedGroupParameter, aggregatedGroup);
        
        final List<ConcurrentUserAggregationImpl> results = query.getResultList();
        return DataAccessUtils.uniqueResult(results);
    }
    
    @AggrEventsTransactional
    @Override
    public ConcurrentUserAggregationImpl createConcurrentUserAggregation(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        final ConcurrentUserAggregationImpl concurrentUserAggregation = new ConcurrentUserAggregationImpl(timeDimension, dateDimension, interval, aggregatedGroup);
        
        this.getEntityManager().persist(concurrentUserAggregation);
        
        return concurrentUserAggregation;
    }
    
    @AggrEventsTransactional
    @Override
    public void updateConcurrentUserAggregation(ConcurrentUserAggregationImpl concurrentUserAggregation) {
        this.getEntityManager().persist(concurrentUserAggregation);
    }
        
}
