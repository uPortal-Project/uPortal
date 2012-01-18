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

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.events.aggr.AcademicTermDetails;
import org.jasig.portal.events.aggr.AggregatedGroupConfig;
import org.jasig.portal.events.aggr.AggregatedIntervalConfig;
import org.jasig.portal.events.aggr.EventDateTimeUtils;
import org.jasig.portal.events.aggr.IEventAggregatorStatus;
import org.jasig.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.jasig.portal.events.aggr.IPortalEventAggregator;
import org.jasig.portal.events.aggr.QuarterDetails;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.jpa.BaseJpaDao;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaEventAggregationManagementDao extends BaseJpaDao implements IEventAggregationManagementDao {
    private static final Class<IPortalEventAggregator> DEFAULT_AGGREGATOR_TYPE = IPortalEventAggregator.class; 
    
    private static final String FIND_AGGR_STATUS_BY_PROC_TYPE_CACHE_REGION = EventAggregatorStatusImpl.class.getName() + ".query.FIND_AGGR_STATUS_BY_PROC_TYPE";
    
    private CriteriaQuery<EventAggregatorStatusImpl> findEventAggregatorStatusByProcessingTypeQuery;
    private CriteriaQuery<AggregatedGroupConfigImpl> findGroupConfigForAggregatorQuery;
    private CriteriaQuery<AggregatedIntervalConfigImpl> findIntervalConfigForAggregatorQuery;
    private CriteriaQuery<QuarterDetailsImpl> findAllQuarterDetailsQuery;
    private CriteriaQuery<AcademicTermDetailsImpl> findAllAcademicTermDetailsQuery;
    private CriteriaQuery<AcademicTermDetailsImpl> findAcademicTermDetailsForDateQuery;
    private String deleteAllQuarterDetailsQuery;

    private ParameterExpression<ProcessingType> processingTypeParameter;
    private ParameterExpression<Class> aggregatorTypeParameter;
    private ParameterExpression<DateTime> dateTimeTypeParameter;
    
    private TransactionOperations transactionOperations;
    private EntityManager entityManager;
    
    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Autowired
    public void setTransactionOperations(@Qualifier("aggrEvents") TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Override
    protected void buildCriteriaQueries(CriteriaBuilder cb) {
        this.processingTypeParameter = cb.parameter(ProcessingType.class, "processingType");
        this.aggregatorTypeParameter = cb.parameter(Class.class, "aggregatorType");
        this.dateTimeTypeParameter = cb.parameter(DateTime.class, "dateTime");
        
        this.findEventAggregatorStatusByProcessingTypeQuery = buildFindEventAggregatorStatusByProcessingTypeQuery(cb);
        this.findGroupConfigForAggregatorQuery = buildFindGroupConfigForAggregatorQuery(cb);
        this.findIntervalConfigForAggregatorQuery = buildFindIntervalConfigForAggregatorQuery(cb);
        this.findAllQuarterDetailsQuery = buildFindAllQuarterDetailsQuery(cb);
        this.findAllAcademicTermDetailsQuery = buildFindAllAcademicTermDetailsQuery(cb);
        this.findAcademicTermDetailsForDateQuery = this.buildFindAcademicTermDetailsForDateQuery(cb);
        
        this.deleteAllQuarterDetailsQuery = 
                "DELETE FROM " + QuarterDetailsImpl.class.getName() + " e ";
    }

    protected CriteriaQuery<EventAggregatorStatusImpl> buildFindEventAggregatorStatusByProcessingTypeQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<EventAggregatorStatusImpl> criteriaQuery = cb.createQuery(EventAggregatorStatusImpl.class);
        final Root<EventAggregatorStatusImpl> entityRoot = criteriaQuery.from(EventAggregatorStatusImpl.class);
        criteriaQuery.select(entityRoot);
        criteriaQuery.where(
            cb.equal(entityRoot.get(EventAggregatorStatusImpl_.processingType), this.processingTypeParameter)
        );
        
        return criteriaQuery;
    }

    protected CriteriaQuery<AggregatedGroupConfigImpl> buildFindGroupConfigForAggregatorQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<AggregatedGroupConfigImpl> criteriaQuery = cb.createQuery(AggregatedGroupConfigImpl.class);
        final Root<AggregatedGroupConfigImpl> entityRoot = criteriaQuery.from(AggregatedGroupConfigImpl.class);
        criteriaQuery.select(entityRoot);
        entityRoot.fetch(AggregatedGroupConfigImpl_.excludedGroups, JoinType.LEFT);
        entityRoot.fetch(AggregatedGroupConfigImpl_.includedGroups, JoinType.LEFT);
        criteriaQuery.where(
            cb.equal(entityRoot.get(AggregatedGroupConfigImpl_.aggregatorType), this.aggregatorTypeParameter)
        );
        
        return criteriaQuery;
    }

    protected CriteriaQuery<AggregatedIntervalConfigImpl> buildFindIntervalConfigForAggregatorQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<AggregatedIntervalConfigImpl> criteriaQuery = cb.createQuery(AggregatedIntervalConfigImpl.class);
        final Root<AggregatedIntervalConfigImpl> entityRoot = criteriaQuery.from(AggregatedIntervalConfigImpl.class);
        criteriaQuery.select(entityRoot);
        entityRoot.fetch(AggregatedIntervalConfigImpl_.excludedIntervals, JoinType.LEFT);
        entityRoot.fetch(AggregatedIntervalConfigImpl_.includedIntervals, JoinType.LEFT);
        criteriaQuery.where(
            cb.equal(entityRoot.get(AggregatedIntervalConfigImpl_.aggregatorType), this.aggregatorTypeParameter)
        );
        
        return criteriaQuery;
    }
    
    protected CriteriaQuery<QuarterDetailsImpl> buildFindAllQuarterDetailsQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<QuarterDetailsImpl> criteriaQuery = cb.createQuery(QuarterDetailsImpl.class);
        final Root<QuarterDetailsImpl> entityRoot = criteriaQuery.from(QuarterDetailsImpl.class);
        criteriaQuery.select(entityRoot);
        
        return criteriaQuery;
    }

    protected CriteriaQuery<AcademicTermDetailsImpl> buildFindAllAcademicTermDetailsQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<AcademicTermDetailsImpl> criteriaQuery = cb.createQuery(AcademicTermDetailsImpl.class);
        final Root<AcademicTermDetailsImpl> entityRoot = criteriaQuery.from(AcademicTermDetailsImpl.class);
        criteriaQuery.select(entityRoot);
        
        return criteriaQuery;
    }

    protected CriteriaQuery<AcademicTermDetailsImpl> buildFindAcademicTermDetailsForDateQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<AcademicTermDetailsImpl> criteriaQuery = cb.createQuery(AcademicTermDetailsImpl.class);
        final Root<AcademicTermDetailsImpl> entityRoot = criteriaQuery.from(AcademicTermDetailsImpl.class);
        criteriaQuery.select(entityRoot);
        criteriaQuery.where(
                cb.and(
                        cb.lessThanOrEqualTo(entityRoot.get(AcademicTermDetailsImpl_.start), dateTimeTypeParameter),
                        cb.greaterThan(entityRoot.get(AcademicTermDetailsImpl_.end), dateTimeTypeParameter)
                )
                //TODO need to queryby two datetimes
        );
        
        
        return criteriaQuery;
    }
    
    

    @Override
    public IEventAggregatorStatus getEventAggregatorStatus(final ProcessingType processingType) {
        final TypedQuery<EventAggregatorStatusImpl> query = this.createQuery(findEventAggregatorStatusByProcessingTypeQuery, FIND_AGGR_STATUS_BY_PROC_TYPE_CACHE_REGION);
        query.setParameter(this.processingTypeParameter, processingType);

        final List<EventAggregatorStatusImpl> resultList = query.getResultList();
        EventAggregatorStatusImpl status = DataAccessUtils.uniqueResult(resultList);
        
        //Create the status object if it doesn't yet exist
        if (status == null) {
            status = this.transactionOperations.execute(new TransactionCallback<EventAggregatorStatusImpl>() {
                @Override
                public EventAggregatorStatusImpl doInTransaction(TransactionStatus status) {
                    final EventAggregatorStatusImpl eventAggregatorStatus = new EventAggregatorStatusImpl(processingType);
                    entityManager.persist(eventAggregatorStatus);
                    return eventAggregatorStatus;
                }
            });
        }
        
        return status;
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public void updateEventAggregatorStatus(IEventAggregatorStatus eventAggregatorStatus) {
        this.entityManager.persist(eventAggregatorStatus);
    }

    @Override
    public AggregatedGroupConfig getDefaultAggregatedGroupConfig() {
        AggregatedGroupConfig groupConfig = this.getAggregatedGroupConfig(DEFAULT_AGGREGATOR_TYPE);
        
        if (groupConfig == null) {
            groupConfig = this.transactionOperations.execute(new TransactionCallback<AggregatedGroupConfig>() {
                @Override
                public AggregatedGroupConfig doInTransaction(TransactionStatus status) {
                    return createAggregatedGroupConfig(DEFAULT_AGGREGATOR_TYPE);
                }
            });
        }
        
        return groupConfig;
    }

    @Override
    public AggregatedGroupConfig getAggregatedGroupConfig(Class<? extends IPortalEventAggregator> aggregatorType) {
        final TypedQuery<AggregatedGroupConfigImpl> query = this.createQuery(this.findGroupConfigForAggregatorQuery, "GROUP_CONFIG_FOR_AGGREGATOR");
        query.setParameter(this.aggregatorTypeParameter, aggregatorType);
        return DataAccessUtils.uniqueResult(query.getResultList());
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public AggregatedGroupConfig createAggregatedGroupConfig(Class<? extends IPortalEventAggregator> aggregatorType) {
        final AggregatedGroupConfig aggregatedGroupConfig = new AggregatedGroupConfigImpl(aggregatorType);
        this.entityManager.persist(aggregatedGroupConfig);
        return aggregatedGroupConfig;
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public void updateAggregatedGroupConfig(AggregatedGroupConfig aggregatedGroupConfig) {
        this.entityManager.persist(aggregatedGroupConfig);
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public void deleteAggregatedGroupConfig(AggregatedGroupConfig aggregatedGroupConfig) {
        this.entityManager.remove(aggregatedGroupConfig);        
    }

    @Override
    public AggregatedIntervalConfig getDefaultAggregatedIntervalConfig() {
        AggregatedIntervalConfig intervalConfig = this.getAggregatedIntervalConfig(DEFAULT_AGGREGATOR_TYPE);
        
        if (intervalConfig == null) {
            intervalConfig = this.transactionOperations.execute(new TransactionCallback<AggregatedIntervalConfig>() {
                @Override
                public AggregatedIntervalConfig doInTransaction(TransactionStatus status) {
                    return createAggregatedIntervalConfig(DEFAULT_AGGREGATOR_TYPE);
                }
            });
        }
        
        return intervalConfig;
    }

    @Override
    public AggregatedIntervalConfig getAggregatedIntervalConfig(Class<? extends IPortalEventAggregator> aggregatorType) {
        final TypedQuery<AggregatedIntervalConfigImpl> query = this.createQuery(this.findIntervalConfigForAggregatorQuery, "INTERVAL_CONFIG_FOR_AGGREGATOR");
        query.setParameter(this.aggregatorTypeParameter, aggregatorType);
        return DataAccessUtils.uniqueResult(query.getResultList());
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public AggregatedIntervalConfig createAggregatedIntervalConfig(Class<? extends IPortalEventAggregator> aggregatorType) {
        final AggregatedIntervalConfig aggregatedIntervalConfig = new AggregatedIntervalConfigImpl(aggregatorType);
        this.entityManager.persist(aggregatedIntervalConfig);
        return aggregatedIntervalConfig;
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public void updateAggregatedIntervalConfig(AggregatedIntervalConfig aggregatedIntervalConfig) {
        this.entityManager.persist(aggregatedIntervalConfig);
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public void deleteAggregatedIntervalConfig(AggregatedIntervalConfig aggregatedIntervalConfig) {
        this.entityManager.remove(aggregatedIntervalConfig);        
    }
    

    @Override
    public SortedSet<QuarterDetails> getQuartersDetails() {
        final TypedQuery<QuarterDetailsImpl> query = this.createQuery(this.findAllQuarterDetailsQuery, "ALL");
        final List<QuarterDetailsImpl> results = query.getResultList();
        if (results.size() == 4) {
            return new TreeSet<QuarterDetails>(results);
        }
        
        //No valid quarters config in db, populate the standard quarters
        return this.transactionOperations.execute(new TransactionCallback<SortedSet<QuarterDetails>>() {

            @Override
            public SortedSet<QuarterDetails> doInTransaction(TransactionStatus status) {
                final SortedSet<QuarterDetails> standardQuarters = EventDateTimeUtils.createStandardQuarters();
                setQuarterDetails(standardQuarters);
                return standardQuarters;
            }
        });
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public void setQuarterDetails(Set<QuarterDetails> newQuarterDetails) {
        newQuarterDetails = EventDateTimeUtils.validateQuarters(newQuarterDetails);
        
        final Query deleteAllQuery = this.entityManager.createQuery(deleteAllQuarterDetailsQuery);
        deleteAllQuery.executeUpdate();

        for (final QuarterDetails quarterDetails : newQuarterDetails) {
            this.entityManager.persist(quarterDetails);
        }
    }

    @Override
    public SortedSet<AcademicTermDetails> getAcademicTermDetails() {
        final TypedQuery<AcademicTermDetailsImpl> query = this.createQuery(this.findAllAcademicTermDetailsQuery, "ALL");
        return new TreeSet<AcademicTermDetails>(query.getResultList());
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public void addAcademicTermDetails(DateMidnight start, DateMidnight end, String termName) {
        final TypedQuery<AcademicTermDetailsImpl> query = this.createQuery(this.findAcademicTermDetailsForDateQuery, "BY_CONTAINS_DATE");
        query.setParameter(this.dateTimeTypeParameter, start.toDateTime());
        
        //Check if term dates overlap and fail if they do
        
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public void updateAcademicTermDetails(AcademicTermDetails academicTermDetails) {
        this.entityManager.persist(academicTermDetails);
    }
}
