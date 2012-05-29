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
public class JpaLoginAggregationDao extends BaseAggrEventsJpaDao implements LoginAggregationPrivateDao {

    private CriteriaQuery<LoginAggregationImpl> findLoginAggregationByDateTimeIntervalQuery;
    private CriteriaQuery<LoginAggregationImpl> findLoginAggregationByDateTimeIntervalGroupQuery;
    private CriteriaQuery<LoginAggregationImpl> findLoginAggregationsByDateRangeQuery;
    private CriteriaQuery<LoginAggregationImpl> findUnclosedLoginAggregationsByDateRangeQuery;
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
        
        this.findLoginAggregationByDateTimeIntervalQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<LoginAggregationImpl>>() {
            @Override
            public CriteriaQuery<LoginAggregationImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<LoginAggregationImpl> criteriaQuery = cb.createQuery(LoginAggregationImpl.class);
                
                final Root<LoginAggregationImpl> lea = criteriaQuery.from(LoginAggregationImpl.class);

                lea.fetch(LoginAggregationImpl_.uniqueUserNames, JoinType.LEFT);
                
                criteriaQuery.select(lea);
                criteriaQuery.where(
                        cb.and(
                            cb.equal(lea.get(LoginAggregationImpl_.dateDimension), dateDimensionParameter),
                            cb.equal(lea.get(LoginAggregationImpl_.timeDimension), timeDimensionParameter),
                            cb.equal(lea.get(LoginAggregationImpl_.interval), intervalParameter)
                        )
                    );
                
                return criteriaQuery;
            }
        });

        
        this.findLoginAggregationByDateTimeIntervalGroupQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<LoginAggregationImpl>>() {
            @Override
            public CriteriaQuery<LoginAggregationImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<LoginAggregationImpl> criteriaQuery = cb.createQuery(LoginAggregationImpl.class);
                final Root<LoginAggregationImpl> lea = criteriaQuery.from(LoginAggregationImpl.class);
                
                lea.fetch(LoginAggregationImpl_.uniqueUserNames, JoinType.LEFT);

                criteriaQuery.select(lea);
                criteriaQuery.where(
                        cb.and(
                            cb.equal(lea.get(LoginAggregationImpl_.dateDimension), dateDimensionParameter),
                            cb.equal(lea.get(LoginAggregationImpl_.timeDimension), timeDimensionParameter),
                            cb.equal(lea.get(LoginAggregationImpl_.interval), intervalParameter),
                            cb.equal(lea.get(LoginAggregationImpl_.aggregatedGroup), aggregatedGroupParameter)
                        )
                    );
                
                return criteriaQuery;
            }
        });


        /*
         * SQL (oracle syntax) this critera query is based on
         * 
         *   SELECT DD.DD_YEAR, DD.DD_MONTH, DD.DD_DAY, TD.TD_HOUR, TD.TD_MINUTE, LEA.LOGIN_COUNT, LEA.UNIQUE_LOGIN_COUNT
         *   FROM UP_LOGIN_EVENT_AGGREGATE LEA
         *       LEFT JOIN UP_DATE_DIMENSION DD on LEA.DATE_DIMENSION_ID = DD.DATE_ID
         *       LEFT JOIN UP_TIME_DIMENSION TD on LEA.TIME_DIMENSION_ID = TD.TIME_ID
         *   WHERE ( DD.DD_DATE >= To_date('2012/04/16', 'yyyy/mm/dd') AND DD.DD_DATE < To_date('2012/04/17', 'yyyy/mm/dd') ) AND
         *          ( DD.DD_DATE > To_date('2012/04/16', 'yyyy/mm/dd') OR TD.TD_TIME >= To_date('1970/01/01 07:21', 'yyyy/mm/dd HH24:MI') ) AND
         *          ( DD.DD_DATE < To_date('2012/04/16', 'yyyy/mm/dd') OR TD.TD_TIME < To_date('1970/01/01 09:20', 'yyyy/mm/dd HH24:MI') ) AND
         *          LEA.AGGR_INTERVAL='FIVE_MINUTE' and LEA.AGGREGATED_GROUP_ID=791
         */
        this.findLoginAggregationsByDateRangeQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<LoginAggregationImpl>>() {
            @Override
            public CriteriaQuery<LoginAggregationImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<LoginAggregationImpl> criteriaQuery = cb.createQuery(LoginAggregationImpl.class);
                
                final Root<LoginAggregationImpl> lea = criteriaQuery.from(LoginAggregationImpl.class);
                final Join<LoginAggregationImpl, DateDimensionImpl> dd = lea.join(LoginAggregationImpl_.dateDimension, JoinType.LEFT);
                final Join<LoginAggregationImpl, TimeDimensionImpl> td = lea.join(LoginAggregationImpl_.timeDimension, JoinType.LEFT);
                
//                lea.fetch(LoginAggregationImpl_.uniqueUserNames, JoinType.LEFT);

                criteriaQuery.select(lea);
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
                        cb.equal(lea.get(LoginAggregationImpl_.interval), intervalParameter),
                        lea.get(LoginAggregationImpl_.aggregatedGroup).in(aggregatedGroupsParameter)
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
        this.findUnclosedLoginAggregationsByDateRangeQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<LoginAggregationImpl>>() {
            @Override
            public CriteriaQuery<LoginAggregationImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<LoginAggregationImpl> criteriaQuery = cb.createQuery(LoginAggregationImpl.class);
                
                final Root<LoginAggregationImpl> lea = criteriaQuery.from(LoginAggregationImpl.class);
                final Join<LoginAggregationImpl, DateDimensionImpl> dd = lea.join(LoginAggregationImpl_.dateDimension, JoinType.LEFT);
                final Join<LoginAggregationImpl, TimeDimensionImpl> td = lea.join(LoginAggregationImpl_.timeDimension, JoinType.LEFT);
                
                lea.fetch(LoginAggregationImpl_.uniqueUserNames, JoinType.LEFT);

                criteriaQuery.select(lea);
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
                        cb.equal(lea.get(LoginAggregationImpl_.interval), intervalParameter),
                        cb.notEqual(cb.size(lea.get(LoginAggregationImpl_.uniqueUserNames)), 0)
                    )
                );
                
                return criteriaQuery;
            }
        });
    }
    
    @Override
    public Set<LoginAggregationImpl> getUnclosedLoginAggregations(DateTime start, DateTime end, AggregationInterval interval) {
        final TypedQuery<LoginAggregationImpl> query = this.createQuery(findUnclosedLoginAggregationsByDateRangeQuery);
        
        query.setParameter(this.startDate, start.toLocalDate());
        query.setParameter(this.startTime, start.toLocalTime());
        
        query.setParameter(this.endDate, end.toLocalDate());
        query.setParameter(this.endTime, end.toLocalTime());
        query.setParameter(this.endMinusOneDate, end.minusDays(1).toLocalDate());
        
        query.setParameter(this.intervalParameter, interval);
        
        return new LinkedHashSet<LoginAggregationImpl>(query.getResultList());
    }
    
    @Override
    public List<LoginAggregationImpl> getLoginAggregations(DateTime start, DateTime end, AggregationInterval interval,
            AggregatedGroupMapping aggregatedGroupMapping, AggregatedGroupMapping... aggregatedGroupMappings) {
        
        final TypedQuery<LoginAggregationImpl> query = this.createCachedQuery(findLoginAggregationsByDateRangeQuery);
        
        query.setParameter(this.startDate, start.toLocalDate());
        query.setParameter(this.startTime, start.toLocalTime());
        
        query.setParameter(this.endDate, end.toLocalDate());
        query.setParameter(this.endTime, end.toLocalTime());
        query.setParameter(this.endMinusOneDate, end.minusDays(1).toLocalDate());
        
        query.setParameter(this.intervalParameter, interval);

        aggregatedGroupMappings = (AggregatedGroupMapping[])ArrayUtils.add(aggregatedGroupMappings, aggregatedGroupMapping);
        query.setParameter(this.aggregatedGroupsParameter, ImmutableSet.copyOf(aggregatedGroupMappings));
        
        return new ArrayList<LoginAggregationImpl>(query.getResultList());
    }

    
    @Override
    public Set<LoginAggregationImpl> getLoginAggregationsForInterval(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval) {
        final TypedQuery<LoginAggregationImpl> query = this.createQuery(this.findLoginAggregationByDateTimeIntervalQuery);
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
    
    @AggrEventsTransactional
    @Override
    public LoginAggregationImpl createLoginAggregation(DateDimension dateDimension, TimeDimension timeDimension, AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        final LoginAggregationImpl loginAggregation = new LoginAggregationImpl(timeDimension, dateDimension, interval, aggregatedGroup);
        
        this.getEntityManager().persist(loginAggregation);
        
        return loginAggregation;
    }
    
    @AggrEventsTransactional
    @Override
    public void updateLoginAggregation(LoginAggregationImpl loginAggregation) {
        this.getEntityManager().persist(loginAggregation);
    }
        
}
