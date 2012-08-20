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

package org.jasig.portal.events.aggr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.ArrayUtils;
import org.jasig.portal.events.aggr.dao.jpa.DateDimensionImpl;
import org.jasig.portal.events.aggr.dao.jpa.DateDimensionImpl_;
import org.jasig.portal.events.aggr.dao.jpa.TimeDimensionImpl;
import org.jasig.portal.events.aggr.dao.jpa.TimeDimensionImpl_;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMappingImpl;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao;
import org.jasig.portal.jpa.OpenEntityManager;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

/**
 * Base for JPA DAOs that handle {@link BaseAggregationImpl} subclasses. Provides impls of the standard methods defined by 
 * {@link BaseAggregationPrivateDao}. Note that subclasses MUST call {@link #afterPropertiesSet()} if they override it or this
 * class WILL NOT WORK
 * 
 * @author Eric Dalquist
 * @param <T> The entity type being aggregated
 * @param <K> The entity primary key
 */
public abstract class JpaBaseAggregationDao<
            T extends BaseAggregationImpl<K>, 
            K extends BaseAggregationKey> 
        extends BaseAggrEventsJpaDao 
        implements BaseAggregationPrivateDao<T, K> {
    
    
    private final Class<T> aggregationEntityType;
    private HibernateCacheEvictor hibernateCacheEvictor;
    
    protected CriteriaQuery<T> findAggregationByDateTimeIntervalQuery;
    protected CriteriaQuery<T> findAggregationByDateTimeIntervalGroupQuery;
    protected CriteriaQuery<T> findAggregationsByDateRangeQuery;
    protected CriteriaQuery<T> findUnclosedAggregationsByDateRangeQuery;
    protected ParameterExpression<TimeDimension> timeDimensionParameter;
    protected ParameterExpression<DateDimension> dateDimensionParameter;
    protected ParameterExpression<AggregationInterval> intervalParameter;
    protected ParameterExpression<AggregatedGroupMapping> aggregatedGroupParameter;
    protected ParameterExpression<Set> aggregatedGroupsParameter;
    protected ParameterExpression<LocalDate> startDate;
    protected ParameterExpression<LocalDate> endMinusOneDate;
    protected ParameterExpression<LocalDate> endDate;
    protected ParameterExpression<LocalTime> startTime;
    protected ParameterExpression<LocalTime> endTime;
    
    public JpaBaseAggregationDao(Class<T> aggregationEntityType) {
        this.aggregationEntityType = aggregationEntityType;
    }
    
    @Autowired
    public void setHibernateCacheEvictor(HibernateCacheEvictor hibernateCacheEvictor) {
        this.hibernateCacheEvictor = hibernateCacheEvictor;
    }

    /**
     * Add any fetches needed for the following queries:
     *  findAggregationByDateTimeIntervalQuery
     *  findUnclosedAggregationsByDateRangeQuery
     */
    protected abstract void addFetches(Root<T> root);
    
    /**
     * Add the additional predicate needed to find the unclosed aggregates 
     */
    protected abstract void addUnclosedPredicate(CriteriaBuilder cb, Root<T> root, List<Predicate> keyPredicates);
    
    /**
     * Add the additional predicate needed if using an extension of {@link BaseAggregationKey}
     */
    protected void addAggregationSpecificKeyPredicate(CriteriaBuilder cb, Root<T> root, List<Predicate> keyPredicates) {
    }
    
    /**
     * Bind the non-standard key parameters from the extension of {@link BaseAggregationKey} for standard queries
     */
    protected void bindAggregationSpecificKeyParameters(TypedQuery<T> query, K key) {
    }
    
    /**
     * Bind the non-standard key parameters from the extension of {@link BaseAggregationKey} for natual id queries
     */
    protected void bindAggregationSpecificKeyParameters(NaturalIdQuery<T> query, K key) {
    }
    
    /**
     * For subclasses to use to create additional {@link ParameterExpression}s
     */
    protected void createParameterExpressions() {
    }
    
    /**
     * For subclasses to use to create additional {@link CriteriaQuery}s
     */
    protected void createCriteriaQueries() {
    }
    
    /**
     * Create a new aggregation instance
     */
    protected abstract T createAggregationInstance(K key);
    
    /**
     * Get the aggregation key for this instance
     */
    protected abstract K getAggregationKey(T instance);

    @Override
    public final void afterPropertiesSet() throws Exception {
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
        this.createParameterExpressions();
        
        this.findAggregationByDateTimeIntervalQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<T>>() {
            @Override
            public CriteriaQuery<T> apply(CriteriaBuilder cb) {
                final CriteriaQuery<T> criteriaQuery = cb.createQuery(aggregationEntityType);
                
                final Root<T> ba = criteriaQuery.from(aggregationEntityType);

                addFetches(ba);
                
                criteriaQuery.select(ba);
                criteriaQuery.where(
                        cb.equal(ba.get(BaseAggregationImpl_.dateDimension), dateDimensionParameter),
                        cb.equal(ba.get(BaseAggregationImpl_.timeDimension), timeDimensionParameter),
                        cb.equal(ba.get(BaseAggregationImpl_.interval), intervalParameter)
                );
                
                return criteriaQuery;
            }
        });

        
        this.findAggregationByDateTimeIntervalGroupQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<T>>() {
            @Override
            public CriteriaQuery<T> apply(CriteriaBuilder cb) {
                final CriteriaQuery<T> criteriaQuery = cb.createQuery(aggregationEntityType);
                final Root<T> ba = criteriaQuery.from(aggregationEntityType);
                

                final List<Predicate> keyPredicates = new ArrayList<Predicate>();
                keyPredicates.add(cb.equal(ba.get(BaseAggregationImpl_.dateDimension), dateDimensionParameter));
                keyPredicates.add(cb.equal(ba.get(BaseAggregationImpl_.timeDimension), timeDimensionParameter));
                keyPredicates.add(cb.equal(ba.get(BaseAggregationImpl_.interval), intervalParameter));
                keyPredicates.add(cb.equal(ba.get(BaseAggregationImpl_.aggregatedGroup), aggregatedGroupParameter));
                addAggregationSpecificKeyPredicate(cb, ba, keyPredicates);
                
                criteriaQuery.select(ba);
                criteriaQuery.where(keyPredicates.toArray(new Predicate[keyPredicates.size()]));
                
                return criteriaQuery;
            }
        });


        this.findAggregationsByDateRangeQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<T>>() {
            @Override
            public CriteriaQuery<T> apply(CriteriaBuilder cb) {
                final CriteriaQuery<T> criteriaQuery = cb.createQuery(aggregationEntityType);
                
                final Root<T> ba = criteriaQuery.from(aggregationEntityType);
                final Join<T, DateDimensionImpl> dd = ba.join(BaseAggregationImpl_.dateDimension, JoinType.LEFT);
                final Join<T, TimeDimensionImpl> td = ba.join(BaseAggregationImpl_.timeDimension, JoinType.LEFT);
                

                final List<Predicate> keyPredicates = new ArrayList<Predicate>();
                keyPredicates.add(cb.and( //Restrict results by outer date range
                        cb.greaterThanOrEqualTo(dd.get(DateDimensionImpl_.date), startDate),
                        cb.lessThan(dd.get(DateDimensionImpl_.date), endDate)
                    ));
                keyPredicates.add(cb.or( //Restrict start of range by time as well
                        cb.greaterThan(dd.get(DateDimensionImpl_.date), startDate),
                        cb.greaterThanOrEqualTo(td.get(TimeDimensionImpl_.time), startTime)
                    ));
                keyPredicates.add(cb.or( //Restrict end of range by time as well
                        cb.lessThan(dd.get(DateDimensionImpl_.date), endMinusOneDate),
                        cb.lessThan(td.get(TimeDimensionImpl_.time), endTime)
                    ));
                keyPredicates.add(cb.equal(ba.get(BaseAggregationImpl_.interval), intervalParameter));
                keyPredicates.add(ba.get(BaseAggregationImpl_.aggregatedGroup).in(aggregatedGroupsParameter));
                addAggregationSpecificKeyPredicate(cb, ba, keyPredicates);
                
                criteriaQuery.select(ba);
                criteriaQuery.where(keyPredicates.toArray(new Predicate[keyPredicates.size()]));
                criteriaQuery.orderBy(cb.desc(dd.get(DateDimensionImpl_.date)), cb.desc(td.get(TimeDimensionImpl_.time)));
                
                return criteriaQuery;
            }
        });
        
        /*
         * Similar to the previous query but only returns aggregates that also match the unclosed predicate generated
         * by the subclass. This is used for finding aggregates that missed having intervalComplete called due to
         * interval boundary placement.
         */
        this.findUnclosedAggregationsByDateRangeQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<T>>() {
            @Override
            public CriteriaQuery<T> apply(CriteriaBuilder cb) {
                final CriteriaQuery<T> criteriaQuery = cb.createQuery(aggregationEntityType);
                
                final Root<T> ba = criteriaQuery.from(aggregationEntityType);
                final Join<T, DateDimensionImpl> dd = ba.join(BaseAggregationImpl_.dateDimension, JoinType.LEFT);
                final Join<T, TimeDimensionImpl> td = ba.join(BaseAggregationImpl_.timeDimension, JoinType.LEFT);
                
                addFetches(ba);
                
                final List<Predicate> keyPredicates = new ArrayList<Predicate>();
                keyPredicates.add(cb.and( //Restrict results by outer date range
                        cb.greaterThanOrEqualTo(dd.get(DateDimensionImpl_.date), startDate),
                        cb.lessThan(dd.get(DateDimensionImpl_.date), endDate)
                    ));
                keyPredicates.add(cb.or( //Restrict start of range by time as well
                        cb.greaterThan(dd.get(DateDimensionImpl_.date), startDate),
                        cb.greaterThanOrEqualTo(td.get(TimeDimensionImpl_.time), startTime)
                    ));
                keyPredicates.add(cb.or( //Restrict end of range by time as well
                        cb.lessThan(dd.get(DateDimensionImpl_.date), endMinusOneDate),
                        cb.lessThan(td.get(TimeDimensionImpl_.time), endTime)
                    ));
                keyPredicates.add(cb.equal(ba.get(BaseAggregationImpl_.interval), intervalParameter));
                //No aggregation specific key bits here, we only have start/end/interval parameters to work with
                addUnclosedPredicate(cb, ba, keyPredicates);

                criteriaQuery.select(ba);
                criteriaQuery.where(keyPredicates.toArray(new Predicate[keyPredicates.size()]));
                
                return criteriaQuery;
            }
        });
        
        this.createCriteriaQueries();
    }
    
    @Override
    public final List<T> getAggregations(DateTime start, DateTime end, K key,
            AggregatedGroupMapping... aggregatedGroupMappings) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start must be before End: " + start + " - " + end);
        }
        final LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        if (startDate.equals(endDate)) {
            endDate = endDate.plusDays(1);
        }

        final TypedQuery<T> query = this.createCachedQuery(findAggregationsByDateRangeQuery);
        
        query.setParameter(this.startDate, start.toLocalDate());
        query.setParameter(this.startTime, start.toLocalTime());
        
        query.setParameter(this.endDate, end.toLocalDate());
        query.setParameter(this.endTime, end.toLocalTime());
        query.setParameter(this.endMinusOneDate, end.minusDays(1).toLocalDate());
        
        query.setParameter(this.intervalParameter, key.getInterval());
        
        this.bindAggregationSpecificKeyParameters(query, key);

        aggregatedGroupMappings = (AggregatedGroupMapping[])ArrayUtils.add(aggregatedGroupMappings, key.getAggregatedGroup());
        query.setParameter(this.aggregatedGroupsParameter, ImmutableSet.copyOf(aggregatedGroupMappings));
        
        return query.getResultList();
    }

    @Override
    public final Map<K, T> getAggregationsForInterval(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval) {
        final TypedQuery<T> query = this.createQuery(this.findAggregationByDateTimeIntervalQuery);
        query.setParameter(this.dateDimensionParameter, dateDimension);
        query.setParameter(this.timeDimensionParameter, timeDimension);
        query.setParameter(this.intervalParameter, interval);
        
        final List<T> results = query.getResultList();
        final Map<K, T> resultMap = new HashMap<K, T>();
        for (final T result : results) {
            final K key = this.getAggregationKey(result);
            resultMap.put(key, result);
        }
        
        return resultMap;
    }

    @Override
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public final T getAggregation(K key) {
        final NaturalIdQuery<T> query = this.createNaturalIdQuery(this.aggregationEntityType);
        query.using(BaseAggregationImpl_.dateDimension, (DateDimensionImpl)key.getDateDimension());
        query.using(BaseAggregationImpl_.timeDimension, (TimeDimensionImpl)key.getTimeDimension());
        query.using(BaseAggregationImpl_.interval, key.getInterval());
        query.using(BaseAggregationImpl_.aggregatedGroup, (AggregatedGroupMappingImpl)key.getAggregatedGroup());
        
        this.bindAggregationSpecificKeyParameters(query, key);
        
        return query.load();
    }

    @Override
    public Collection<T> getUnclosedAggregations(DateTime start, DateTime end, AggregationInterval interval) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start must be before End: " + start + " - " + end);
        }
        final LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        if (startDate.equals(endDate)) {
            endDate = endDate.plusDays(1);
        }

        final TypedQuery<T> query = this.createQuery(findUnclosedAggregationsByDateRangeQuery);
        
        query.setParameter(this.startDate, startDate);
        query.setParameter(this.startTime, start.toLocalTime());
        
        query.setParameter(this.endDate, endDate);
        query.setParameter(this.endTime, end.toLocalTime());
        query.setParameter(this.endMinusOneDate, end.minusDays(1).toLocalDate());
        
        query.setParameter(this.intervalParameter, interval);
        
        //Need set to handle duplicate results from join
        return new LinkedHashSet<T>(query.getResultList());
    }

    @AggrEventsTransactional
    @Override
    public final T createAggregation(K key) {
        final T aggregation = createAggregationInstance(key);
        
        this.getEntityManager().persist(aggregation);
        
        return aggregation;
    }

    @AggrEventsTransactional
    @Override
    public final void updateAggregation(T aggregation) {
        this.getEntityManager().persist(aggregation);
    }

    @AggrEventsTransactional
    @Override
    public final void updateAggregations(Iterable<T> aggregations, boolean removeFromCache) {
        final EntityManager entityManager = this.getEntityManager();
        
        for (final T aggregation : aggregations) {
            entityManager.persist(aggregation);
            
            if (removeFromCache) {
                this.hibernateCacheEvictor.evictEntity(aggregation.getClass(), aggregation.getId());
            }
        }
    }
}
