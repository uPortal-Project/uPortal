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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.Interval;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.jpa.BaseJpaDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaLoginAggregationDao extends BaseJpaDao implements LoginAggregationPrivateDao {

    private CriteriaQuery<LoginAggregationImpl> findLoginAggregationByDateTimeIntervalQuery;
    private CriteriaQuery<LoginAggregationImpl> findLoginAggregationByDateTimeIntervalGroupQuery;
    private ParameterExpression<TimeDimension> timeDimensionParameter;
    private ParameterExpression<DateDimension> dateDimensionParameter;
    private ParameterExpression<Interval> intervalParameter;
    private ParameterExpression<AggregatedGroupMapping> aggregatedGroupParameter;
    
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
    protected void buildCriteriaQueries(CriteriaBuilder cb) {
        this.timeDimensionParameter = cb.parameter(TimeDimension.class, "timeDimension");
        this.dateDimensionParameter = cb.parameter(DateDimension.class, "dateDimension");
        this.intervalParameter = cb.parameter(Interval.class, "interval");
        this.aggregatedGroupParameter = cb.parameter(AggregatedGroupMapping.class, "aggregatedGroup");
        
        this.findLoginAggregationByDateTimeIntervalQuery = this.buildFindLoginAggregationByDateTimeIntervalQuery(cb);
        this.findLoginAggregationByDateTimeIntervalGroupQuery = this.buildFindLoginAggregationByDateTimeIntervalGroupQuery(cb);
    }
    
    protected CriteriaQuery<LoginAggregationImpl> buildFindLoginAggregationByDateTimeIntervalQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<LoginAggregationImpl> criteriaQuery = cb.createQuery(LoginAggregationImpl.class);
        final Root<LoginAggregationImpl> root = criteriaQuery.from(LoginAggregationImpl.class);
        criteriaQuery.select(root);
        criteriaQuery.where(
                cb.and(
                    cb.equal(root.get(LoginAggregationImpl_.dateDimension), this.dateDimensionParameter),
                    cb.equal(root.get(LoginAggregationImpl_.timeDimension), this.timeDimensionParameter),
                    cb.equal(root.get(LoginAggregationImpl_.interval), this.intervalParameter)
                )
            );
        
        return criteriaQuery;
    }
    
    protected CriteriaQuery<LoginAggregationImpl> buildFindLoginAggregationByDateTimeIntervalGroupQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<LoginAggregationImpl> criteriaQuery = cb.createQuery(LoginAggregationImpl.class);
        final Root<LoginAggregationImpl> root = criteriaQuery.from(LoginAggregationImpl.class);
        criteriaQuery.select(root);
        criteriaQuery.where(
                cb.and(
                    cb.equal(root.get(LoginAggregationImpl_.dateDimension), this.dateDimensionParameter),
                    cb.equal(root.get(LoginAggregationImpl_.timeDimension), this.timeDimensionParameter),
                    cb.equal(root.get(LoginAggregationImpl_.interval), this.intervalParameter),
                    cb.equal(root.get(LoginAggregationImpl_.aggregatedGroup), this.aggregatedGroupParameter)
                )
            );
        
        return criteriaQuery;
    }

    
    @Override
    public Set<LoginAggregationImpl> getLoginAggregationsForInterval(DateDimension dateDimension, TimeDimension timeDimension, Interval interval) {
        final TypedQuery<LoginAggregationImpl> query = this.createQuery(this.findLoginAggregationByDateTimeIntervalQuery, "FIND_BY_DATE_TIME_INTERVAL");
        query.setParameter(this.dateDimensionParameter, dateDimension);
        query.setParameter(this.timeDimensionParameter, timeDimension);
        query.setParameter(this.intervalParameter, interval);
        
        final List<LoginAggregationImpl> results = query.getResultList();
        return new LinkedHashSet<LoginAggregationImpl>(results);
    }

    @Override
    public LoginAggregationImpl getLoginAggregation(DateDimension dateDimension, TimeDimension timeDimension, Interval interval, AggregatedGroupMapping aggregatedGroup) {
        final TypedQuery<LoginAggregationImpl> query = this.createQuery(this.findLoginAggregationByDateTimeIntervalGroupQuery, "FIND_BY_DATE_TIME_INTERVAL_GROUP");
        query.setMaxResults(1);
        query.setParameter(this.dateDimensionParameter, dateDimension);
        query.setParameter(this.timeDimensionParameter, timeDimension);
        query.setParameter(this.intervalParameter, interval);
        query.setParameter(this.aggregatedGroupParameter, aggregatedGroup);
        
        final List<LoginAggregationImpl> results = query.getResultList();
        return DataAccessUtils.singleResult(results);
    }
    
    @Transactional("aggrEvents")
    @Override
    public LoginAggregationImpl createLoginAggregation(DateDimension dateDimension, TimeDimension timeDimension, Interval interval, AggregatedGroupMapping aggregatedGroup) {
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
