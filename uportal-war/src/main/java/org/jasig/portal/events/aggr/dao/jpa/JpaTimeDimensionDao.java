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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.jasig.portal.jpa.BaseJpaDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaTimeDimensionDao extends BaseJpaDao implements TimeDimensionDao {
    private static final String FIND_ALL_TIME_DIMENSIONS_CACHE_REGION = TimeDimensionImpl.class.getName() + ".query.FIND_ALL_TIME_DIMENSIONS";
    private static final String FIND_TIME_DIMENSION_BY_HOUR_MINUTE_CACHE_REGION = TimeDimensionImpl.class.getName() + ".query.FIND_TIME_DIMENSION_BY_HOUR_MINUTE";
    
    private CriteriaQuery<TimeDimensionImpl> findAllTimeDimensionsQuery;
    private CriteriaQuery<TimeDimensionImpl> findTimeDimensionByHourMinuteQuery;
    private ParameterExpression<Integer> hourParameter;
    private ParameterExpression<Integer> minuteParameter;
    
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
        this.hourParameter = cb.parameter(Integer.class, "hour");
        this.minuteParameter = cb.parameter(Integer.class, "minute");
        
        this.findAllTimeDimensionsQuery = this.buildFindAllTimeDimensions(cb);
        this.findTimeDimensionByHourMinuteQuery = this.buildFindTimeDimensionByHourMinuteQuery(cb);
    }
    
    protected CriteriaQuery<TimeDimensionImpl> buildFindAllTimeDimensions(final CriteriaBuilder cb) {
        final CriteriaQuery<TimeDimensionImpl> criteriaQuery = cb.createQuery(TimeDimensionImpl.class);
        final Root<TimeDimensionImpl> definitionRoot = criteriaQuery.from(TimeDimensionImpl.class);
        criteriaQuery.select(definitionRoot);
        
        return criteriaQuery;
    }
    
    protected CriteriaQuery<TimeDimensionImpl> buildFindTimeDimensionByHourMinuteQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<TimeDimensionImpl> criteriaQuery = cb.createQuery(TimeDimensionImpl.class);
        final Root<TimeDimensionImpl> definitionRoot = criteriaQuery.from(TimeDimensionImpl.class);
        criteriaQuery.select(definitionRoot);
        criteriaQuery.where(
            cb.and(
                    cb.equal(definitionRoot.get(TimeDimensionImpl_.hour), this.hourParameter),
                    cb.equal(definitionRoot.get(TimeDimensionImpl_.minute), this.minuteParameter)
            )
        );
        
        return criteriaQuery;
    }

    @Override
    @Transactional
    public TimeDimension createTimeDimension(int hour, int minute) {
        final TimeDimension timeDimension = new TimeDimensionImpl(hour, minute);
        
        this.entityManager.persist(timeDimension);
        
        return timeDimension;
    }

    @Override
    public List<TimeDimension> getTimeDimensions() {
        final TypedQuery<TimeDimensionImpl> query = this.createQuery(this.findAllTimeDimensionsQuery, FIND_ALL_TIME_DIMENSIONS_CACHE_REGION);
        
        final List<TimeDimensionImpl> portletDefinitions = query.getResultList();
        return new ArrayList<TimeDimension>(portletDefinitions);
    }
    
    @Override
    public TimeDimension getTimeDimensionById(long key) {
        final TimeDimension timeDimension = this.entityManager.find(TimeDimensionImpl.class, key);
        
        return timeDimension;
    }
    
    @Override
    public TimeDimension getTimeDimensionForDate(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return this.getTimeDimensionForCalendar(cal);
    }
    
    @Override
    public TimeDimension getTimeDimensionForTimeInMillis(long time) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return this.getTimeDimensionForCalendar(cal);
    }
    
    @Override
    public TimeDimension getTimeDimensionForCalendar(Calendar calendar) {
        final int hour = calendar.get(Calendar.HOUR);
        final int minute = calendar.get(Calendar.MINUTE);
        return this.getTimeDimensionByHourMinute(hour, minute);
    }
    
    @Override
    public TimeDimension getTimeDimensionByHourMinute(int hour, int minute) {
        final TypedQuery<TimeDimensionImpl> query = this.createQuery(this.findTimeDimensionByHourMinuteQuery, FIND_TIME_DIMENSION_BY_HOUR_MINUTE_CACHE_REGION);
        query.setParameter(this.hourParameter, hour);
        query.setParameter(this.minuteParameter, minute);
        query.setMaxResults(1);
        
        final List<TimeDimensionImpl> portletDefinitions = query.getResultList();
        return DataAccessUtils.uniqueResult(portletDefinitions);
    }
}
