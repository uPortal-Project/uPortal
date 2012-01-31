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

package org.jasig.portal.events.aggr.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.jpa.BaseJpaDao;
import org.joda.time.DateMidnight;
import org.joda.time.LocalDate;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaDateDimensionDao extends BaseJpaDao implements DateDimensionDao {
    private CriteriaQuery<DateDimensionImpl> findAllDateDimensionsQuery;
    private CriteriaQuery<DateDimensionImpl> findAllDateDimensionsBetweenQuery;
    private CriteriaQuery<DateDimensionImpl> findDateDimensionByDateQuery;
    private CriteriaQuery<DateDimensionImpl> findNewestDateDimensionQuery;
    private CriteriaQuery<DateDimensionImpl> findOldestDateDimensionQuery;
    private ParameterExpression<LocalDate> dateTimeParameter;
    private ParameterExpression<LocalDate> endDateTimeParameter;
    
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
        this.dateTimeParameter = this.createParameterExpression(LocalDate.class, "dateTime");
        this.endDateTimeParameter = this.createParameterExpression(LocalDate.class, "endDateTime");
        
        this.findAllDateDimensionsQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<DateDimensionImpl>>() {
            @Override
            public CriteriaQuery<DateDimensionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<DateDimensionImpl> criteriaQuery = cb.createQuery(DateDimensionImpl.class);
                final Root<DateDimensionImpl> dimensionRoot = criteriaQuery.from(DateDimensionImpl.class);
                criteriaQuery.orderBy(cb.asc(dimensionRoot.get(DateDimensionImpl_.date)));
                return criteriaQuery;
            }
        });
        
        this.findAllDateDimensionsBetweenQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<DateDimensionImpl>>() {
            @Override
            public CriteriaQuery<DateDimensionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<DateDimensionImpl> criteriaQuery = cb.createQuery(DateDimensionImpl.class);
                final Root<DateDimensionImpl> dimensionRoot = criteriaQuery.from(DateDimensionImpl.class);
                criteriaQuery.select(dimensionRoot);
                criteriaQuery.where(
                        cb.and(
                                cb.greaterThanOrEqualTo(dimensionRoot.get(DateDimensionImpl_.date), dateTimeParameter),
                                cb.lessThan(dimensionRoot.get(DateDimensionImpl_.date), endDateTimeParameter)
                        )
                    );
                criteriaQuery.orderBy(cb.asc(dimensionRoot.get(DateDimensionImpl_.date)));
                
                return criteriaQuery;
            }
        });
        
        this.findDateDimensionByDateQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<DateDimensionImpl>>() {
            @Override
            public CriteriaQuery<DateDimensionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<DateDimensionImpl> criteriaQuery = cb.createQuery(DateDimensionImpl.class);
                final Root<DateDimensionImpl> dimensionRoot = criteriaQuery.from(DateDimensionImpl.class);
                criteriaQuery.select(dimensionRoot);
                criteriaQuery.where(
                    cb.equal(dimensionRoot.get(DateDimensionImpl_.date), dateTimeParameter)
                );
                
                return criteriaQuery;
            }
        });
        
        this.findNewestDateDimensionQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<DateDimensionImpl>>() {
            @Override
            public CriteriaQuery<DateDimensionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<DateDimensionImpl> criteriaQuery = cb.createQuery(DateDimensionImpl.class);
                final Root<DateDimensionImpl> dimensionRoot = criteriaQuery.from(DateDimensionImpl.class);
                
                //Build subquery for max date
                final Subquery<LocalDate> maxDateSub = criteriaQuery.subquery(LocalDate.class);
                final Root<DateDimensionImpl> maxDateDimensionSub = maxDateSub.from(DateDimensionImpl.class);
                maxDateSub
                    .select(cb.greatest(maxDateDimensionSub.get(DateDimensionImpl_.date)));
                
                //Get the date dimension
                criteriaQuery
                    .select(dimensionRoot)
                    .where(cb.equal(dimensionRoot.get(DateDimensionImpl_.date), maxDateSub));
                
                return criteriaQuery;
            }
        });
        
        this.findOldestDateDimensionQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<DateDimensionImpl>>() {
            @Override
            public CriteriaQuery<DateDimensionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<DateDimensionImpl> criteriaQuery = cb.createQuery(DateDimensionImpl.class);
                final Root<DateDimensionImpl> dimensionRoot = criteriaQuery.from(DateDimensionImpl.class);
                
                //Build subquery for max date
                final Subquery<LocalDate> maxDateSub = criteriaQuery.subquery(LocalDate.class);
                final Root<DateDimensionImpl> maxDateDimensionSub = maxDateSub.from(DateDimensionImpl.class);
                maxDateSub
                    .select(cb.least(maxDateDimensionSub.get(DateDimensionImpl_.date)));
                
                //Get the date dimension
                criteriaQuery
                    .select(dimensionRoot)
                    .where(cb.equal(dimensionRoot.get(DateDimensionImpl_.date), maxDateSub));
                
                return criteriaQuery;
            }
        });
    }
    
    @Override
    public DateDimension getNewestDateDimension() {
        final TypedQuery<DateDimensionImpl> query = this.createCachedQuery(this.findNewestDateDimensionQuery);
        final List<DateDimensionImpl> resultList = query.getResultList();
        return DataAccessUtils.uniqueResult(resultList);
    }
    
    @Override
    public DateDimension getOldestDateDimension() {
        final TypedQuery<DateDimensionImpl> query = this.createCachedQuery(this.findOldestDateDimensionQuery);
        final List<DateDimensionImpl> resultList = query.getResultList();
        return DataAccessUtils.uniqueResult(resultList);
    }
    
    @Override
    @Transactional("aggrEvents")
    public DateDimension createDateDimension(DateMidnight date, int quarter, String term) {
        final DateDimension dateDimension = new DateDimensionImpl(date, quarter, term);
        
        this.entityManager.persist(dateDimension);
        
        return dateDimension;
    }

    @Override
    public List<DateDimension> getDateDimensions() {
        final TypedQuery<DateDimensionImpl> query = this.createCachedQuery(this.findAllDateDimensionsQuery);
        
        final List<DateDimensionImpl> portletDefinitions = query.getResultList();
        return new ArrayList<DateDimension>(portletDefinitions);
    }

    @Override
    public List<DateDimension> getDateDimensionsBetween(DateMidnight start, DateMidnight end) {
        final TypedQuery<DateDimensionImpl> query = this.createCachedQuery(this.findAllDateDimensionsBetweenQuery);
        query.setParameter(this.dateTimeParameter, start.toLocalDate());
        query.setParameter(this.endDateTimeParameter, end.toLocalDate());
        
        final List<DateDimensionImpl> portletDefinitions = query.getResultList();
        return new ArrayList<DateDimension>(portletDefinitions);
    }
    
    @Override
    public DateDimension getDateDimensionById(long key) {
        final DateDimension dateDimension = this.entityManager.find(DateDimensionImpl.class, key);
        
        return dateDimension;
    }

    @Override
    public DateDimension getDateDimensionByDate(DateMidnight date) {
        final TypedQuery<DateDimensionImpl> query = this.createCachedQuery(this.findDateDimensionByDateQuery);
        query.setParameter(this.dateTimeParameter, date.toLocalDate());
        
        final List<DateDimensionImpl> portletDefinitions = query.getResultList();
        return DataAccessUtils.uniqueResult(portletDefinitions);
    }
}
