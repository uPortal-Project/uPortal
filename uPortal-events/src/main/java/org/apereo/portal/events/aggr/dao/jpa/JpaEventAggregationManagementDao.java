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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import org.apereo.portal.events.aggr.AcademicTermDetail;
import org.apereo.portal.events.aggr.AggregatedGroupConfig;
import org.apereo.portal.events.aggr.AggregatedIntervalConfig;
import org.apereo.portal.events.aggr.EventDateTimeUtils;
import org.apereo.portal.events.aggr.IEventAggregatorStatus;
import org.apereo.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.apereo.portal.events.aggr.IPortalEventAggregator;
import org.apereo.portal.events.aggr.QuarterDetail;
import org.apereo.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;
import org.apereo.portal.jpa.OpenEntityManager;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

@Repository
public class JpaEventAggregationManagementDao extends BaseAggrEventsJpaDao
        implements IEventAggregationManagementDao {
    private static final Class<IPortalEventAggregator> DEFAULT_AGGREGATOR_TYPE =
            IPortalEventAggregator.class;

    private CriteriaQuery<AggregatedGroupConfigImpl> findAllGroupConfigsQuery;
    private CriteriaQuery<AggregatedGroupConfigImpl> findGroupConfigForAggregatorQuery;
    private CriteriaQuery<AggregatedIntervalConfigImpl> findAllIntervalConfigsQuery;
    private CriteriaQuery<AggregatedIntervalConfigImpl> findIntervalConfigForAggregatorQuery;
    private CriteriaQuery<QuarterDetailImpl> findAllQuarterDetailsQuery;
    private CriteriaQuery<AcademicTermDetailImpl> findAllAcademicTermDetailsQuery;

    private ParameterExpression<Class> aggregatorTypeParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.aggregatorTypeParameter =
                this.createParameterExpression(Class.class, "aggregatorType");

        this.findAllGroupConfigsQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<AggregatedGroupConfigImpl>>() {
                            @Override
                            public CriteriaQuery<AggregatedGroupConfigImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<AggregatedGroupConfigImpl> criteriaQuery =
                                        cb.createQuery(AggregatedGroupConfigImpl.class);
                                final Root<AggregatedGroupConfigImpl> entityRoot =
                                        criteriaQuery.from(AggregatedGroupConfigImpl.class);
                                criteriaQuery.select(entityRoot);
                                entityRoot.fetch(
                                        AggregatedGroupConfigImpl_.excludedGroups, JoinType.LEFT);
                                entityRoot.fetch(
                                        AggregatedGroupConfigImpl_.includedGroups, JoinType.LEFT);
                                return criteriaQuery;
                            }
                        });

        this.findGroupConfigForAggregatorQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<AggregatedGroupConfigImpl>>() {
                            @Override
                            public CriteriaQuery<AggregatedGroupConfigImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<AggregatedGroupConfigImpl> criteriaQuery =
                                        cb.createQuery(AggregatedGroupConfigImpl.class);
                                final Root<AggregatedGroupConfigImpl> entityRoot =
                                        criteriaQuery.from(AggregatedGroupConfigImpl.class);
                                criteriaQuery.select(entityRoot);
                                entityRoot.fetch(
                                        AggregatedGroupConfigImpl_.excludedGroups, JoinType.LEFT);
                                entityRoot.fetch(
                                        AggregatedGroupConfigImpl_.includedGroups, JoinType.LEFT);
                                criteriaQuery.where(
                                        cb.equal(
                                                entityRoot.get(
                                                        AggregatedGroupConfigImpl_.aggregatorType),
                                                aggregatorTypeParameter));

                                return criteriaQuery;
                            }
                        });

        this.findAllIntervalConfigsQuery =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder, CriteriaQuery<AggregatedIntervalConfigImpl>>() {
                            @Override
                            public CriteriaQuery<AggregatedIntervalConfigImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<AggregatedIntervalConfigImpl> criteriaQuery =
                                        cb.createQuery(AggregatedIntervalConfigImpl.class);
                                final Root<AggregatedIntervalConfigImpl> entityRoot =
                                        criteriaQuery.from(AggregatedIntervalConfigImpl.class);
                                criteriaQuery.select(entityRoot);
                                entityRoot.fetch(
                                        AggregatedIntervalConfigImpl_.excludedIntervals,
                                        JoinType.LEFT);
                                entityRoot.fetch(
                                        AggregatedIntervalConfigImpl_.includedIntervals,
                                        JoinType.LEFT);

                                return criteriaQuery;
                            }
                        });

        this.findIntervalConfigForAggregatorQuery =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder, CriteriaQuery<AggregatedIntervalConfigImpl>>() {
                            @Override
                            public CriteriaQuery<AggregatedIntervalConfigImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<AggregatedIntervalConfigImpl> criteriaQuery =
                                        cb.createQuery(AggregatedIntervalConfigImpl.class);
                                final Root<AggregatedIntervalConfigImpl> entityRoot =
                                        criteriaQuery.from(AggregatedIntervalConfigImpl.class);
                                criteriaQuery.select(entityRoot);
                                entityRoot.fetch(
                                        AggregatedIntervalConfigImpl_.excludedIntervals,
                                        JoinType.LEFT);
                                entityRoot.fetch(
                                        AggregatedIntervalConfigImpl_.includedIntervals,
                                        JoinType.LEFT);
                                criteriaQuery.where(
                                        cb.equal(
                                                entityRoot.get(
                                                        AggregatedIntervalConfigImpl_
                                                                .aggregatorType),
                                                aggregatorTypeParameter));

                                return criteriaQuery;
                            }
                        });

        this.findAllQuarterDetailsQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<QuarterDetailImpl>>() {
                            @Override
                            public CriteriaQuery<QuarterDetailImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<QuarterDetailImpl> criteriaQuery =
                                        cb.createQuery(QuarterDetailImpl.class);
                                final Root<QuarterDetailImpl> entityRoot =
                                        criteriaQuery.from(QuarterDetailImpl.class);
                                criteriaQuery.select(entityRoot);
                                criteriaQuery.orderBy(
                                        cb.asc(entityRoot.get(QuarterDetailImpl_.quarterId)));

                                return criteriaQuery;
                            }
                        });

        this.findAllAcademicTermDetailsQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<AcademicTermDetailImpl>>() {
                            @Override
                            public CriteriaQuery<AcademicTermDetailImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<AcademicTermDetailImpl> criteriaQuery =
                                        cb.createQuery(AcademicTermDetailImpl.class);
                                final Root<AcademicTermDetailImpl> entityRoot =
                                        criteriaQuery.from(AcademicTermDetailImpl.class);
                                criteriaQuery.select(entityRoot);
                                criteriaQuery.orderBy(
                                        cb.asc(entityRoot.get(AcademicTermDetailImpl_.start)));

                                return criteriaQuery;
                            }
                        });
    }

    @OpenEntityManager(unitName = BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    @Override
    public IEventAggregatorStatus getEventAggregatorStatus(
            final ProcessingType processingType, boolean create) {
        final NaturalIdQuery<EventAggregatorStatusImpl> query =
                this.createNaturalIdQuery(EventAggregatorStatusImpl.class);
        query.using(EventAggregatorStatusImpl_.processingType, processingType);
        EventAggregatorStatusImpl status = query.load();

        //Create the status object if it doesn't yet exist
        if (status == null && create) {
            status =
                    this.getTransactionOperations()
                            .execute(
                                    new TransactionCallback<EventAggregatorStatusImpl>() {
                                        @Override
                                        public EventAggregatorStatusImpl doInTransaction(
                                                TransactionStatus status) {
                                            final EventAggregatorStatusImpl eventAggregatorStatus =
                                                    new EventAggregatorStatusImpl(processingType);
                                            getEntityManager().persist(eventAggregatorStatus);
                                            return eventAggregatorStatus;
                                        }
                                    });
        }

        return status;
    }

    @Override
    @AggrEventsTransactional
    public void updateEventAggregatorStatus(IEventAggregatorStatus eventAggregatorStatus) {
        this.getEntityManager().persist(eventAggregatorStatus);
    }

    @Override
    public AggregatedGroupConfig getDefaultAggregatedGroupConfig() {
        AggregatedGroupConfig groupConfig = this.getAggregatedGroupConfig(DEFAULT_AGGREGATOR_TYPE);

        if (groupConfig == null) {
            groupConfig =
                    this.getTransactionOperations()
                            .execute(
                                    new TransactionCallback<AggregatedGroupConfig>() {
                                        @Override
                                        public AggregatedGroupConfig doInTransaction(
                                                TransactionStatus status) {
                                            return createAggregatedGroupConfig(
                                                    DEFAULT_AGGREGATOR_TYPE);
                                        }
                                    });
        }

        return groupConfig;
    }

    @Override
    public Set<AggregatedGroupConfig> getAggregatedGroupConfigs() {
        final TypedQuery<AggregatedGroupConfigImpl> query =
                this.createCachedQuery(this.findAllGroupConfigsQuery);
        final List<AggregatedGroupConfigImpl> results = query.getResultList();
        return new LinkedHashSet<AggregatedGroupConfig>(results);
    }

    @Override
    public AggregatedGroupConfig getAggregatedGroupConfig(
            Class<? extends IPortalEventAggregator> aggregatorType) {
        final TypedQuery<AggregatedGroupConfigImpl> query =
                this.createCachedQuery(this.findGroupConfigForAggregatorQuery);
        query.setParameter(this.aggregatorTypeParameter, aggregatorType);
        return DataAccessUtils.uniqueResult(query.getResultList());
    }

    @Override
    @AggrEventsTransactional
    public AggregatedGroupConfig createAggregatedGroupConfig(
            Class<? extends IPortalEventAggregator> aggregatorType) {
        final AggregatedGroupConfig aggregatedGroupConfig =
                new AggregatedGroupConfigImpl(aggregatorType);
        this.getEntityManager().persist(aggregatedGroupConfig);
        return aggregatedGroupConfig;
    }

    @Override
    @AggrEventsTransactional
    public void updateAggregatedGroupConfig(AggregatedGroupConfig aggregatedGroupConfig) {
        this.getEntityManager().persist(aggregatedGroupConfig);
    }

    @Override
    @AggrEventsTransactional
    public void deleteAggregatedGroupConfig(AggregatedGroupConfig aggregatedGroupConfig) {
        this.getEntityManager().remove(aggregatedGroupConfig);
    }

    @Override
    public Set<AggregatedIntervalConfig> getAggregatedIntervalConfigs() {
        final TypedQuery<AggregatedIntervalConfigImpl> query =
                this.createCachedQuery(this.findAllIntervalConfigsQuery);
        final List<AggregatedIntervalConfigImpl> results = query.getResultList();
        return new LinkedHashSet<AggregatedIntervalConfig>(results);
    }

    @Override
    public AggregatedIntervalConfig getDefaultAggregatedIntervalConfig() {
        AggregatedIntervalConfig intervalConfig =
                this.getAggregatedIntervalConfig(DEFAULT_AGGREGATOR_TYPE);

        if (intervalConfig == null) {
            intervalConfig =
                    this.getTransactionOperations()
                            .execute(
                                    new TransactionCallback<AggregatedIntervalConfig>() {
                                        @Override
                                        public AggregatedIntervalConfig doInTransaction(
                                                TransactionStatus status) {
                                            return createAggregatedIntervalConfig(
                                                    DEFAULT_AGGREGATOR_TYPE);
                                        }
                                    });
        }

        return intervalConfig;
    }

    @Override
    public AggregatedIntervalConfig getAggregatedIntervalConfig(
            Class<? extends IPortalEventAggregator> aggregatorType) {
        final TypedQuery<AggregatedIntervalConfigImpl> query =
                this.createCachedQuery(this.findIntervalConfigForAggregatorQuery);
        query.setParameter(this.aggregatorTypeParameter, aggregatorType);
        return DataAccessUtils.uniqueResult(query.getResultList());
    }

    @Override
    @AggrEventsTransactional
    public AggregatedIntervalConfig createAggregatedIntervalConfig(
            Class<? extends IPortalEventAggregator> aggregatorType) {
        final AggregatedIntervalConfig aggregatedIntervalConfig =
                new AggregatedIntervalConfigImpl(aggregatorType);
        this.getEntityManager().persist(aggregatedIntervalConfig);
        return aggregatedIntervalConfig;
    }

    @Override
    @AggrEventsTransactional
    public void updateAggregatedIntervalConfig(AggregatedIntervalConfig aggregatedIntervalConfig) {
        this.getEntityManager().persist(aggregatedIntervalConfig);
    }

    @Override
    @AggrEventsTransactional
    public void deleteAggregatedIntervalConfig(AggregatedIntervalConfig aggregatedIntervalConfig) {
        this.getEntityManager().remove(aggregatedIntervalConfig);
    }

    @Override
    public List<QuarterDetail> getQuartersDetails() {
        final TypedQuery<QuarterDetailImpl> query =
                this.createCachedQuery(this.findAllQuarterDetailsQuery);
        final List<QuarterDetailImpl> results = query.getResultList();
        if (results.size() == 4) {
            return new ArrayList<QuarterDetail>(results);
        }

        //No valid quarters config in db, populate the standard quarters
        return this.getTransactionOperations()
                .execute(
                        new TransactionCallback<List<QuarterDetail>>() {

                            @Override
                            public List<QuarterDetail> doInTransaction(TransactionStatus status) {
                                final List<QuarterDetail> standardQuarters =
                                        EventDateTimeUtils.createStandardQuarters();
                                setQuarterDetails(standardQuarters);
                                return standardQuarters;
                            }
                        });
    }

    @Override
    @AggrEventsTransactional
    public void setQuarterDetails(List<QuarterDetail> newQuarterDetails) {
        newQuarterDetails = EventDateTimeUtils.validateQuarters(newQuarterDetails);

        final TypedQuery<QuarterDetailImpl> query =
                this.createCachedQuery(this.findAllQuarterDetailsQuery);
        final Set<QuarterDetailImpl> existingQuarterDetails =
                new HashSet<QuarterDetailImpl>(query.getResultList());

        for (final Iterator<QuarterDetail> newQuarterDetailsItr = newQuarterDetails.iterator();
                newQuarterDetailsItr.hasNext();
                ) {
            final QuarterDetail quarterDetail = newQuarterDetailsItr.next();

            //If QD exists in both new and existing remove it from both
            if (existingQuarterDetails.remove(quarterDetail)) {
                newQuarterDetailsItr.remove();
            }
        }

        final EntityManager entityManager = this.getEntityManager();

        //Delete all existing QDs that were not in the new list
        for (final QuarterDetailImpl existingQuarterDetail : existingQuarterDetails) {
            entityManager.remove(existingQuarterDetail);
        }

        entityManager.flush();

        //Add all new QDs that were not in the existing set
        for (final QuarterDetail newQuarterDetail : newQuarterDetails) {
            entityManager.persist(newQuarterDetail);
        }
    }

    @Override
    public List<AcademicTermDetail> getAcademicTermDetails() {
        final TypedQuery<AcademicTermDetailImpl> query =
                this.createCachedQuery(this.findAllAcademicTermDetailsQuery);
        return new ArrayList<AcademicTermDetail>(query.getResultList());
    }

    @Override
    @AggrEventsTransactional
    public void setAcademicTermDetails(List<AcademicTermDetail> newAcademicTermDetails) {
        newAcademicTermDetails = EventDateTimeUtils.validateAcademicTerms(newAcademicTermDetails);

        final TypedQuery<AcademicTermDetailImpl> query =
                this.createCachedQuery(this.findAllAcademicTermDetailsQuery);
        final Set<AcademicTermDetailImpl> existingAcademicTermDetails =
                new HashSet<AcademicTermDetailImpl>(query.getResultList());

        for (final Iterator<AcademicTermDetail> newAcademicTermDetailsItr =
                        newAcademicTermDetails.iterator();
                newAcademicTermDetailsItr.hasNext();
                ) {
            final AcademicTermDetail academicTermDetail = newAcademicTermDetailsItr.next();

            //If ATD exists in both new and existing remove it from both
            if (existingAcademicTermDetails.remove(academicTermDetail)) {
                newAcademicTermDetailsItr.remove();
            }
        }

        final EntityManager entityManager = this.getEntityManager();

        //Delete all existing ATDs that were not in the new list
        for (final AcademicTermDetailImpl existingAcademicTermDetail :
                existingAcademicTermDetails) {
            entityManager.remove(existingAcademicTermDetail);
        }

        entityManager.flush();

        //Add all new ATDs that were not in the existing set
        for (final AcademicTermDetail newAcademicTermDetail : newAcademicTermDetails) {
            entityManager.persist(newAcademicTermDetail);
        }
    }
}
