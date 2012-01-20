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
import org.joda.time.DateTime;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaDateDimensionDao extends BaseJpaDao implements DateDimensionDao {
    private static final String FIND_ALL_DATE_DIMENSIONS_CACHE_REGION = DateDimensionImpl.class.getName() + ".query.FIND_ALL_DATE_DIMENSIONS";
    private static final String FIND_ALL_DATE_DIMENSIONS_BETWEEN_CACHE_REGION = DateDimensionImpl.class.getName() + ".query.FIND_ALL_DATE_DIMENSIONS_BETWEEN";
    private static final String FIND_DATE_DIMENSION_BY_DATE_CACHE_REGION = DateDimensionImpl.class.getName() + ".query.FIND_DATE_DIMENSION_BY_DATE";
    private static final String FIND_NEWEST_DATE_DIMENSION_CACHE_REGION = DateDimensionImpl.class.getName() + ".query.FIND_NEWEST_DATE_DIMENSION";
    private static final String FIND_OLDEST_DATE_DIMENSION_CACHE_REGION = DateDimensionImpl.class.getName() + ".query.FIND_OLDEST_DATE_DIMENSION";
    
    private CriteriaQuery<DateDimensionImpl> findAllDateDimensionsQuery;
    private CriteriaQuery<DateDimensionImpl> findAllDateDimensionsBetweenQuery;
    private CriteriaQuery<DateDimensionImpl> findDateDimensionByYearMonthDayQuery;
    private CriteriaQuery<DateDimensionImpl> findNewestDateDimensionQuery;
    private CriteriaQuery<DateDimensionImpl> findOldestDateDimensionQuery;
    private ParameterExpression<DateTime> dateTimeParameter;
    private ParameterExpression<DateTime> endDateTimeParameter;
    
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
        this.dateTimeParameter = cb.parameter(DateTime.class, "dateTime");
        this.endDateTimeParameter = cb.parameter(DateTime.class, "endDateTime");
        
        this.findAllDateDimensionsQuery = this.buildFindAllDateDimensions(cb);
        this.findAllDateDimensionsBetweenQuery = this.buildFindDateDimensionsBetween(cb);
        this.findDateDimensionByYearMonthDayQuery = this.buildFindDateDimensionByYearMonthDayQuery(cb);
        this.findNewestDateDimensionQuery = this.buildFindNewestDateDimension(cb);
        this.findOldestDateDimensionQuery = this.buildFindOldestDateDimension(cb);
    }
    
    protected CriteriaQuery<DateDimensionImpl> buildFindAllDateDimensions(final CriteriaBuilder cb) {
        final CriteriaQuery<DateDimensionImpl> criteriaQuery = cb.createQuery(DateDimensionImpl.class);
        final Root<DateDimensionImpl> dimensionRoot = criteriaQuery.from(DateDimensionImpl.class);
        criteriaQuery.select(dimensionRoot);
        criteriaQuery.orderBy(cb.asc(dimensionRoot.get(DateDimensionImpl_.fullDate)));
        
        return criteriaQuery;
    }
    
    protected CriteriaQuery<DateDimensionImpl> buildFindDateDimensionsBetween(final CriteriaBuilder cb) {
        final CriteriaQuery<DateDimensionImpl> criteriaQuery = cb.createQuery(DateDimensionImpl.class);
        final Root<DateDimensionImpl> dimensionRoot = criteriaQuery.from(DateDimensionImpl.class);
        criteriaQuery.select(dimensionRoot);
        criteriaQuery.where(
                cb.and(
                        cb.greaterThanOrEqualTo(dimensionRoot.get(DateDimensionImpl_.fullDate), this.dateTimeParameter),
                        cb.lessThan(dimensionRoot.get(DateDimensionImpl_.fullDate), this.endDateTimeParameter)
                )
            );
        criteriaQuery.orderBy(cb.asc(dimensionRoot.get(DateDimensionImpl_.fullDate)));
        
        return criteriaQuery;
    }
    
    protected CriteriaQuery<DateDimensionImpl> buildFindNewestDateDimension(final CriteriaBuilder cb) {
        final CriteriaQuery<DateDimensionImpl> criteriaQuery = cb.createQuery(DateDimensionImpl.class);
        final Root<DateDimensionImpl> dimensionRoot = criteriaQuery.from(DateDimensionImpl.class);
        
        //Build subquery for max date
        final Subquery<DateTime> maxDateSub = criteriaQuery.subquery(DateTime.class);
        final Root<DateDimensionImpl> maxDateDimensionSub = maxDateSub.from(DateDimensionImpl.class);
        maxDateSub
            .select(cb.greatest(maxDateDimensionSub.get(DateDimensionImpl_.fullDate)));
        
        //Get the date dimension
        criteriaQuery
            .select(dimensionRoot)
            .where(cb.equal(dimensionRoot.get(DateDimensionImpl_.fullDate), maxDateSub));
        
        return criteriaQuery;
    }
    
    protected CriteriaQuery<DateDimensionImpl> buildFindOldestDateDimension(final CriteriaBuilder cb) {
        final CriteriaQuery<DateDimensionImpl> criteriaQuery = cb.createQuery(DateDimensionImpl.class);
        final Root<DateDimensionImpl> dimensionRoot = criteriaQuery.from(DateDimensionImpl.class);
        
        //Build subquery for max date
        final Subquery<DateTime> maxDateSub = criteriaQuery.subquery(DateTime.class);
        final Root<DateDimensionImpl> maxDateDimensionSub = maxDateSub.from(DateDimensionImpl.class);
        maxDateSub
            .select(cb.least(maxDateDimensionSub.get(DateDimensionImpl_.fullDate)));
        
        //Get the date dimension
        criteriaQuery
            .select(dimensionRoot)
            .where(cb.equal(dimensionRoot.get(DateDimensionImpl_.fullDate), maxDateSub));
        
        return criteriaQuery;
    }
    
    protected CriteriaQuery<DateDimensionImpl> buildFindDateDimensionByYearMonthDayQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<DateDimensionImpl> criteriaQuery = cb.createQuery(DateDimensionImpl.class);
        final Root<DateDimensionImpl> dimensionRoot = criteriaQuery.from(DateDimensionImpl.class);
        criteriaQuery.select(dimensionRoot);
        criteriaQuery.where(
            cb.equal(dimensionRoot.get(DateDimensionImpl_.fullDate), this.dateTimeParameter)
        );
        
        return criteriaQuery;
    }
    
    @Override
    public DateDimension getNewestDateDimension() {
        final TypedQuery<DateDimensionImpl> query = this.createQuery(this.findNewestDateDimensionQuery, FIND_NEWEST_DATE_DIMENSION_CACHE_REGION);
        query.setMaxResults(1);
        final List<DateDimensionImpl> resultList = query.getResultList();
        return DataAccessUtils.singleResult(resultList);
    }
    
    @Override
    public DateDimension getOldestDateDimension() {
        final TypedQuery<DateDimensionImpl> query = this.createQuery(this.findOldestDateDimensionQuery, FIND_OLDEST_DATE_DIMENSION_CACHE_REGION);
        query.setMaxResults(1);
        final List<DateDimensionImpl> resultList = query.getResultList();
        return DataAccessUtils.singleResult(resultList);
    }
    
    @Override
    @Transactional("aggrEvents")
    public DateDimension createDateDimension(DateMidnight cal, int quarter, String term) {
        final DateDimension dateDimension = new DateDimensionImpl(cal.getYear(), cal.getMonthOfYear(), cal.getDayOfMonth(), quarter, term);
        
        this.entityManager.persist(dateDimension);
        
        return dateDimension;
    }

    @Override
    public List<DateDimension> getDateDimensions() {
        final TypedQuery<DateDimensionImpl> query = this.createQuery(this.findAllDateDimensionsQuery, FIND_ALL_DATE_DIMENSIONS_CACHE_REGION);
        
        final List<DateDimensionImpl> portletDefinitions = query.getResultList();
        return new ArrayList<DateDimension>(portletDefinitions);
    }

    @Override
    public List<DateDimension> getDateDimensionsBetween(DateMidnight start, DateMidnight end) {
        final TypedQuery<DateDimensionImpl> query = this.createQuery(this.findAllDateDimensionsBetweenQuery, FIND_ALL_DATE_DIMENSIONS_BETWEEN_CACHE_REGION);
        query.setParameter(this.dateTimeParameter, start.toDateTime());
        query.setParameter(this.endDateTimeParameter, end.toDateTime());
        
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
        final TypedQuery<DateDimensionImpl> query = this.createQuery(this.findDateDimensionByYearMonthDayQuery, FIND_DATE_DIMENSION_BY_DATE_CACHE_REGION);
        query.setParameter(this.dateTimeParameter, date.toDateTime());
        query.setMaxResults(1);
        
        final List<DateDimensionImpl> portletDefinitions = query.getResultList();
        return DataAccessUtils.uniqueResult(portletDefinitions);
    }
}
