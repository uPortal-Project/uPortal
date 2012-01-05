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

import java.util.List;

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

    private CriteriaQuery<LoginAggregationImpl> findLoginAggregationByDateAndTimeQuery;
    private ParameterExpression<TimeDimension> timeDimensionParameter;
    private ParameterExpression<DateDimension> dateDimensionParameter;
    
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
        
        this.findLoginAggregationByDateAndTimeQuery = this.buildFindLoginAggregationByDateAndTimeQuery(cb);
    }
    
    protected CriteriaQuery<LoginAggregationImpl> buildFindLoginAggregationByDateAndTimeQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<LoginAggregationImpl> criteriaQuery = cb.createQuery(LoginAggregationImpl.class);
        final Root<LoginAggregationImpl> root = criteriaQuery.from(LoginAggregationImpl.class);
        criteriaQuery.select(root);
        criteriaQuery.where(cb.and(
                cb.equal(root.get(LoginAggregationImpl_.dateDimension), this.dateDimensionParameter),
                cb.equal(root.get(LoginAggregationImpl_.timeDimension), this.timeDimensionParameter)));
        
        return criteriaQuery;
    }

    
    @Override
    public LoginAggregationImpl getLoginAggregation(DateDimension dateDimension, TimeDimension timeDimension) {
        final TypedQuery<LoginAggregationImpl> query = this.createQuery(this.findLoginAggregationByDateAndTimeQuery, "FIND_BY_DATE_AND_TIME");
        query.setMaxResults(1);
        query.setParameter(this.dateDimensionParameter, dateDimension);
        query.setParameter(this.timeDimensionParameter, timeDimension);
        
        final List<LoginAggregationImpl> results = query.getResultList();
        return DataAccessUtils.singleResult(results);
    }
    
    @Transactional("aggrEvents")
    @Override
    public LoginAggregationImpl createLoginAggregation(DateDimension dateDimension, TimeDimension timeDimension, Interval interval, String groupName) {
        final LoginAggregationImpl loginAggregation = new LoginAggregationImpl(timeDimension, dateDimension, interval, groupName);
        
        this.entityManager.persist(loginAggregation);
        
        return loginAggregation;
    }
    
    @Transactional("aggrEvents")
    @Override
    public void updateLoginAggregation(LoginAggregation loginAggregation) {
        this.entityManager.persist(loginAggregation);
    }
        
}
