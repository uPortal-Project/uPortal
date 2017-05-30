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
package org.apereo.portal.events.aggr.dao.jpa;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.dao.DateDimensionDao;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;
import org.apereo.portal.jpa.OpenEntityManager;
import org.joda.time.DateMidnight;
import org.joda.time.LocalDate;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

/**
 */
@Repository
public class JpaDateDimensionDao extends BaseAggrEventsJpaDao implements DateDimensionDao {
    private CriteriaQuery<DateDimensionImpl> findAllDateDimensionsQuery;
    private CriteriaQuery<DateDimensionImpl> findAllDateDimensionsBetweenQuery;
    private CriteriaQuery<DateDimensionImpl> findAllDateDimensionsWithoutTermQuery;
    private CriteriaQuery<DateDimensionImpl> findNewestDateDimensionQuery;
    private CriteriaQuery<DateDimensionImpl> findOldestDateDimensionQuery;
    private ParameterExpression<LocalDate> dateTimeParameter;
    private ParameterExpression<LocalDate> endDateTimeParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.dateTimeParameter = this.createParameterExpression(LocalDate.class, "dateTime");
        this.endDateTimeParameter = this.createParameterExpression(LocalDate.class, "endDateTime");

        this.findAllDateDimensionsQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<DateDimensionImpl>>() {
                            @Override
                            public CriteriaQuery<DateDimensionImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<DateDimensionImpl> criteriaQuery =
                                        cb.createQuery(DateDimensionImpl.class);
                                final Root<DateDimensionImpl> dimensionRoot =
                                        criteriaQuery.from(DateDimensionImpl.class);
                                criteriaQuery.orderBy(
                                        cb.asc(dimensionRoot.get(DateDimensionImpl_.date)));
                                return criteriaQuery;
                            }
                        });

        this.findAllDateDimensionsBetweenQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<DateDimensionImpl>>() {
                            @Override
                            public CriteriaQuery<DateDimensionImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<DateDimensionImpl> criteriaQuery =
                                        cb.createQuery(DateDimensionImpl.class);
                                final Root<DateDimensionImpl> dimensionRoot =
                                        criteriaQuery.from(DateDimensionImpl.class);
                                criteriaQuery.select(dimensionRoot);
                                criteriaQuery.where(
                                        cb.and(
                                                cb.greaterThanOrEqualTo(
                                                        dimensionRoot.get(DateDimensionImpl_.date),
                                                        dateTimeParameter),
                                                cb.lessThan(
                                                        dimensionRoot.get(DateDimensionImpl_.date),
                                                        endDateTimeParameter)));
                                criteriaQuery.orderBy(
                                        cb.asc(dimensionRoot.get(DateDimensionImpl_.date)));

                                return criteriaQuery;
                            }
                        });

        this.findAllDateDimensionsWithoutTermQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<DateDimensionImpl>>() {
                            @Override
                            public CriteriaQuery<DateDimensionImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<DateDimensionImpl> criteriaQuery =
                                        cb.createQuery(DateDimensionImpl.class);
                                final Root<DateDimensionImpl> dimensionRoot =
                                        criteriaQuery.from(DateDimensionImpl.class);
                                criteriaQuery.select(dimensionRoot);
                                criteriaQuery.where(
                                        cb.isNull(dimensionRoot.get(DateDimensionImpl_.term)));

                                return criteriaQuery;
                            }
                        });

        this.findNewestDateDimensionQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<DateDimensionImpl>>() {
                            @Override
                            public CriteriaQuery<DateDimensionImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<DateDimensionImpl> criteriaQuery =
                                        cb.createQuery(DateDimensionImpl.class);
                                final Root<DateDimensionImpl> dimensionRoot =
                                        criteriaQuery.from(DateDimensionImpl.class);

                                //Build subquery for max date
                                final Subquery<LocalDate> maxDateSub =
                                        criteriaQuery.subquery(LocalDate.class);
                                final Root<DateDimensionImpl> maxDateDimensionSub =
                                        maxDateSub.from(DateDimensionImpl.class);
                                maxDateSub.select(
                                        cb.greatest(
                                                maxDateDimensionSub.get(DateDimensionImpl_.date)));

                                //Get the date dimension
                                criteriaQuery
                                        .select(dimensionRoot)
                                        .where(
                                                cb.equal(
                                                        dimensionRoot.get(DateDimensionImpl_.date),
                                                        maxDateSub));

                                return criteriaQuery;
                            }
                        });

        this.findOldestDateDimensionQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<DateDimensionImpl>>() {
                            @Override
                            public CriteriaQuery<DateDimensionImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<DateDimensionImpl> criteriaQuery =
                                        cb.createQuery(DateDimensionImpl.class);
                                final Root<DateDimensionImpl> dimensionRoot =
                                        criteriaQuery.from(DateDimensionImpl.class);

                                //Build subquery for max date
                                final Subquery<LocalDate> maxDateSub =
                                        criteriaQuery.subquery(LocalDate.class);
                                final Root<DateDimensionImpl> maxDateDimensionSub =
                                        maxDateSub.from(DateDimensionImpl.class);
                                maxDateSub.select(
                                        cb.least(maxDateDimensionSub.get(DateDimensionImpl_.date)));

                                //Get the date dimension
                                criteriaQuery
                                        .select(dimensionRoot)
                                        .where(
                                                cb.equal(
                                                        dimensionRoot.get(DateDimensionImpl_.date),
                                                        maxDateSub));

                                return criteriaQuery;
                            }
                        });
    }

    @Override
    public DateDimension getNewestDateDimension() {
        final TypedQuery<DateDimensionImpl> query =
                this.createCachedQuery(this.findNewestDateDimensionQuery);
        final List<DateDimensionImpl> resultList = query.getResultList();
        return DataAccessUtils.uniqueResult(resultList);
    }

    @Override
    public DateDimension getOldestDateDimension() {
        final TypedQuery<DateDimensionImpl> query =
                this.createCachedQuery(this.findOldestDateDimensionQuery);
        final List<DateDimensionImpl> resultList = query.getResultList();
        return DataAccessUtils.uniqueResult(resultList);
    }

    @Override
    @AggrEventsTransactional
    public DateDimension createDateDimension(DateMidnight date, int quarter, String term) {
        final DateDimension dateDimension = new DateDimensionImpl(date, quarter, term);

        this.getEntityManager().persist(dateDimension);

        return dateDimension;
    }

    @Override
    @AggrEventsTransactional
    public void updateDateDimension(DateDimension dateDimension) {
        this.getEntityManager().persist(dateDimension);
    }

    @Override
    public List<DateDimension> getDateDimensions() {
        final TypedQuery<DateDimensionImpl> query =
                this.createCachedQuery(this.findAllDateDimensionsQuery);
        query.setFlushMode(FlushModeType.COMMIT);

        final List<DateDimensionImpl> portletDefinitions = query.getResultList();
        return new ArrayList<DateDimension>(portletDefinitions);
    }

    @Override
    public List<DateDimension> getDateDimensionsBetween(DateMidnight start, DateMidnight end) {
        final TypedQuery<DateDimensionImpl> query =
                this.createCachedQuery(this.findAllDateDimensionsBetweenQuery);
        query.setFlushMode(FlushModeType.COMMIT);
        query.setParameter(this.dateTimeParameter, start.toLocalDate());
        query.setParameter(this.endDateTimeParameter, end.toLocalDate());

        final List<DateDimensionImpl> portletDefinitions = query.getResultList();
        return new ArrayList<DateDimension>(portletDefinitions);
    }

    @Override
    public List<DateDimension> getDateDimensionsWithoutTerm() {
        final TypedQuery<DateDimensionImpl> query =
                this.createQuery(this.findAllDateDimensionsWithoutTermQuery);
        query.setFlushMode(FlushModeType.COMMIT);

        final List<DateDimensionImpl> portletDefinitions = query.getResultList();
        return new ArrayList<DateDimension>(portletDefinitions);
    }

    @Override
    public DateDimension getDateDimensionById(long key) {
        final EntityManager entityManager = this.getEntityManager();
        final FlushModeType flushMode = entityManager.getFlushMode();
        try {
            entityManager.setFlushMode(FlushModeType.COMMIT);
            return entityManager.find(DateDimensionImpl.class, key);
        } finally {
            entityManager.setFlushMode(flushMode);
        }
    }

    @OpenEntityManager(unitName = BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    @Override
    public DateDimension getDateDimensionByDate(DateMidnight date) {
        final NaturalIdQuery<DateDimensionImpl> query =
                this.createNaturalIdQuery(DateDimensionImpl.class);
        query.using(DateDimensionImpl_.date, date.toLocalDate());
        return query.load();
    }
}
