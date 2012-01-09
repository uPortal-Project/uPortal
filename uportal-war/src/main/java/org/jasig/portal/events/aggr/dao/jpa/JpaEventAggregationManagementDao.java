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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.events.aggr.IEventAggregatorStatus;
import org.jasig.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.jpa.BaseJpaDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaEventAggregationManagementDao extends BaseJpaDao implements IEventAggregationManagementDao {
    private static final String FIND_AGGR_STATUS_BY_PROC_TYPE_CACHE_REGION = EventAggregatorStatusImpl.class.getName() + ".query.FIND_AGGR_STATUS_BY_PROC_TYPE";
    
    private CriteriaQuery<EventAggregatorStatusImpl> findEventAggregatorStatusByProcessingTypeQuery;
    private ParameterExpression<ProcessingType> processingTypeParameter;
    
    private EntityManager entityManager;
    
    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Override
    protected void buildCriteriaQueries(CriteriaBuilder cb) {
        this.processingTypeParameter = cb.parameter(ProcessingType.class, "processingType");
        
        this.findEventAggregatorStatusByProcessingTypeQuery = buildFindEventAggregatorStatusByProcessingTypeQuery(cb);
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

    @Override
    public IEventAggregatorStatus getEventAggregatorStatus(ProcessingType processingType) {
        final TypedQuery<EventAggregatorStatusImpl> query = this.createQuery(findEventAggregatorStatusByProcessingTypeQuery, FIND_AGGR_STATUS_BY_PROC_TYPE_CACHE_REGION);
        query.setParameter(this.processingTypeParameter, processingType);

        final List<EventAggregatorStatusImpl> resultList = query.getResultList();
        return DataAccessUtils.singleResult(resultList);
    }
    
    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public IEventAggregatorStatus createEventAggregatorStatus(ProcessingType processingType) {
        final EventAggregatorStatusImpl eventAggregatorStatus = new EventAggregatorStatusImpl(processingType);
        this.entityManager.persist(eventAggregatorStatus);
        return eventAggregatorStatus;
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public void updateEventAggregatorStatus(IEventAggregatorStatus eventAggregatorStatus) {
        this.entityManager.persist(eventAggregatorStatus);
    }
}
