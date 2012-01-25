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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.dao.jpa.DateDimensionImpl;
import org.jasig.portal.events.aggr.dao.jpa.DateDimensionImpl_;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.jpa.BaseJpaDao;
import org.joda.time.DateMidnight;
import org.joda.time.LocalDate;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaLoginAggregationDao extends BaseJpaDao implements LoginAggregationPrivateDao {

    private CriteriaQuery<LoginAggregationImpl> findLoginAggregationByDateTimeIntervalQuery;
    private CriteriaQuery<LoginAggregationImpl> findLoginAggregationByDateTimeIntervalGroupQuery;
    private CriteriaQuery<LoginAggregationImpl> findLoginAggregationsByDateRangeQuery;
    private ParameterExpression<TimeDimension> timeDimensionParameter;
    private ParameterExpression<DateDimension> dateDimensionParameter;
    private ParameterExpression<AggregationInterval> intervalParameter;
    private ParameterExpression<AggregatedGroupMapping> aggregatedGroupParameter;
    private ParameterExpression<Set> aggregatedGroupsParameter;
    private ParameterExpression<LocalDate> startDate;
    private ParameterExpression<LocalDate> endDate;
    
    private EntityManager entityManager;

    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    

    @Override
    public void afterPropertiesSet() throws Exception {
        this.timeDimensionParameter = this.createParameterExpression(TimeDimension.class, "timeDimension");
        this.dateDimensionParameter = this.createParameterExpression(DateDimension.class, "dateDimension");
        this.intervalParameter = this.createParameterExpression(AggregationInterval.class, "interval");
        this.aggregatedGroupParameter = this.createParameterExpression(AggregatedGroupMapping.class, "aggregatedGroup");
        this.aggregatedGroupsParameter = this.createParameterExpression(Set.class, "aggregatedGroups");
        this.startDate = this.createParameterExpression(LocalDate.class, "startDate");
        this.endDate = this.createParameterExpression(LocalDate.class, "endDate");
        
        this.findLoginAggregationByDateTimeIntervalQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<LoginAggregationImpl>>() {
            @Override
            public CriteriaQuery<LoginAggregationImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<LoginAggregationImpl> criteriaQuery = cb.createQuery(LoginAggregationImpl.class);
                final Root<LoginAggregationImpl> root = criteriaQuery.from(LoginAggregationImpl.class);
                criteriaQuery.select(root);
                root.fetch(LoginAggregationImpl_.uniqueUserNames, JoinType.LEFT);
                criteriaQuery.where(
                        cb.and(
                            cb.equal(root.get(LoginAggregationImpl_.dateDimension), dateDimensionParameter),
                            cb.equal(root.get(LoginAggregationImpl_.timeDimension), timeDimensionParameter),
                            cb.equal(root.get(LoginAggregationImpl_.interval), intervalParameter)
                        )
                    );
                
                return criteriaQuery;
            }
        });

        
        this.findLoginAggregationByDateTimeIntervalGroupQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<LoginAggregationImpl>>() {
            @Override
            public CriteriaQuery<LoginAggregationImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<LoginAggregationImpl> criteriaQuery = cb.createQuery(LoginAggregationImpl.class);
                final Root<LoginAggregationImpl> root = criteriaQuery.from(LoginAggregationImpl.class);
                criteriaQuery.select(root);
                root.fetch(LoginAggregationImpl_.uniqueUserNames, JoinType.LEFT);
                criteriaQuery.where(
                        cb.and(
                            cb.equal(root.get(LoginAggregationImpl_.dateDimension), dateDimensionParameter),
                            cb.equal(root.get(LoginAggregationImpl_.timeDimension), timeDimensionParameter),
                            cb.equal(root.get(LoginAggregationImpl_.interval), intervalParameter),
                            cb.equal(root.get(LoginAggregationImpl_.aggregatedGroup), aggregatedGroupParameter)
                        )
                    );
                
                return criteriaQuery;
            }
        });

        
        this.findLoginAggregationsByDateRangeQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<LoginAggregationImpl>>() {
            @Override
            public CriteriaQuery<LoginAggregationImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<LoginAggregationImpl> criteriaQuery = cb.createQuery(LoginAggregationImpl.class);
                
                final Root<DateDimensionImpl> root = criteriaQuery.from(DateDimensionImpl.class);
                final CollectionJoin<DateDimensionImpl, LoginAggregationImpl> loginAggrJoin = root.join(DateDimensionImpl_.loginAggregations, JoinType.LEFT);
                
                criteriaQuery.select(loginAggrJoin);
                criteriaQuery.where(
                        cb.and(
                                cb.between(root.get(DateDimensionImpl_.date), startDate, endDate),
                                cb.equal(loginAggrJoin.get(LoginAggregationImpl_.interval), intervalParameter),
                                loginAggrJoin.get(LoginAggregationImpl_.aggregatedGroup).in(aggregatedGroupsParameter)
                        )
                );
                criteriaQuery.orderBy(cb.desc(root.get(DateDimensionImpl_.date)));
                
                return criteriaQuery;
            }
        });
    }
    
    @Override
    public List<LoginAggregation> getLoginAggregations(DateMidnight start, DateMidnight end, AggregationInterval interval, AggregatedGroupMapping... aggregatedGroupMapping) {
        final TypedQuery<LoginAggregationImpl> query = this.createQuery(findLoginAggregationsByDateRangeQuery);
        query.setParameter(this.startDate, start.toLocalDate());
        query.setParameter(this.endDate, end.toLocalDate());
        query.setParameter(this.intervalParameter, interval);
        query.setParameter(this.aggregatedGroupsParameter, ImmutableSet.copyOf(aggregatedGroupMapping));
        
        return new ArrayList<LoginAggregation>(query.getResultList());
    }

    
    @Override
    public Set<LoginAggregationImpl> getLoginAggregationsForInterval(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval) {
        final TypedQuery<LoginAggregationImpl> query = this.createCachedQuery(this.findLoginAggregationByDateTimeIntervalQuery);
        query.setParameter(this.dateDimensionParameter, dateDimension);
        query.setParameter(this.timeDimensionParameter, timeDimension);
        query.setParameter(this.intervalParameter, interval);
        
        final List<LoginAggregationImpl> results = query.getResultList();
        return new LinkedHashSet<LoginAggregationImpl>(results);
    }

    @Override
    public LoginAggregationImpl getLoginAggregation(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        final TypedQuery<LoginAggregationImpl> query = this.createCachedQuery(this.findLoginAggregationByDateTimeIntervalGroupQuery);
        query.setParameter(this.dateDimensionParameter, dateDimension);
        query.setParameter(this.timeDimensionParameter, timeDimension);
        query.setParameter(this.intervalParameter, interval);
        query.setParameter(this.aggregatedGroupParameter, aggregatedGroup);
        
        final List<LoginAggregationImpl> results = query.getResultList();
        return DataAccessUtils.uniqueResult(results);
    }
    
    @Transactional("aggrEvents")
    @Override
    public LoginAggregationImpl createLoginAggregation(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        final LoginAggregationImpl loginAggregation = new LoginAggregationImpl(timeDimension, dateDimension, interval, aggregatedGroup);
        
        this.entityManager.persist(loginAggregation);
        
        return loginAggregation;
    }
    
    @Transactional("aggrEvents")
    @Override
    public void updateLoginAggregation(LoginAggregationImpl loginAggregation) {
        this.entityManager.persist(loginAggregation);
    }
        
}
