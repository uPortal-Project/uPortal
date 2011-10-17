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

package org.jasig.portal.events.handlers.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.PortalEvent_;
import org.jasig.portal.jpa.BaseJpaDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores portal events using JPA/Hibenate no internal batch segmentation is done to the passed list
 * of {@link PortalEvent}s. If a {@link PortalEvent} is not mapped as a persistent entity a message is logged
 * at the WARN level and the event is ignored.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaPortalEventStore extends BaseJpaDao implements IPortalEventDao {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private CriteriaQuery<PortalEvent> findPortalEventsForTimeRangeQuery;
    private ParameterExpression<Long> startTimeParameter;
    private ParameterExpression<Long> endTimeParameter;


    private EntityManager entityManager;

    /**
     * @param entityManager the entityManager to set
     */
    @PersistenceContext(unitName = "uPortalStatsPersistence")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.jpa.BaseJpaDao#getEntityManager()
     */
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Override
    protected void buildCriteriaQueries(CriteriaBuilder cb) {
        this.startTimeParameter = cb.parameter(Long.class, "startTime");
        this.endTimeParameter = cb.parameter(Long.class, "endTime");
        
        this.findPortalEventsForTimeRangeQuery = this.buildFindPortalEventsForTimeRangeQuery(cb);
    }

    protected CriteriaQuery<PortalEvent> buildFindPortalEventsForTimeRangeQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<PortalEvent> criteriaQuery = cb.createQuery(PortalEvent.class);
        final Root<PortalEvent> entityRoot = criteriaQuery.from(PortalEvent.class);
        criteriaQuery.select(entityRoot);
        criteriaQuery.where(
            cb.and(
                cb.ge(entityRoot.get(PortalEvent_.timestamp), this.startTimeParameter),
                cb.lessThan(entityRoot.get(PortalEvent_.timestamp), this.endTimeParameter)
            )
        );
        criteriaQuery.orderBy(cb.asc(entityRoot.get(PortalEvent_.timestamp)));
        
        return criteriaQuery;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.BatchedApplicationListener#onApplicationEvent(java.util.Collection)
     */
    @Override
    @Transactional
    public void onApplicationEvent(Collection<PortalEvent> events) {
        this.storePortalEvents(events);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventDao#storePortalEvent(org.jasig.portal.events.PortalEvent)
     */
    @Override
    @Transactional
    public void storePortalEvent(PortalEvent portalEvent) {
        this.entityManager.persist(portalEvent);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventDao#storePortalEvents(org.jasig.portal.events.PortalEvent[])
     */
    @Override
    @Transactional
    public void storePortalEvents(PortalEvent... portalEvents) {
        for (final PortalEvent portalEvent : portalEvents) {
            try {
                storePortalEvent(portalEvent);
            }
            catch (IllegalArgumentException iae) {
                this.logger.warn(portalEvent.getClass().getName() + " is not mapped as a persistent entity and will not be stored. " + portalEvent + " Exception=" + iae.getMessage());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventDao#storePortalEvents(java.lang.Iterable)
     */
    @Override
    @Transactional
    public void storePortalEvents(Iterable<PortalEvent> portalEvents) {
        for (final PortalEvent portalEvent : portalEvents) {
            try {
                storePortalEvent(portalEvent);
            }
            catch (IllegalArgumentException iae) {
                this.logger.warn(portalEvent.getClass().getName() + " is not mapped as a persistent entity and will not be stored. " + portalEvent + " Exception=" + iae.getMessage());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventDao#getPortalEvents(long, long)
     */
    @Override
    public List<PortalEvent> getPortalEvents(long startTime, long endTime) {
        final TypedQuery<PortalEvent> query = this.getEntityManager().createQuery(this.findPortalEventsForTimeRangeQuery);
        query.setParameter(this.startTimeParameter, startTime);
        query.setParameter(this.endTimeParameter, endTime);
        
        return new ArrayList<PortalEvent>(query.getResultList());
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventDao#deletePortalEvents(java.util.Collection)
     */
    @Override
    @Transactional
    public void deletePortalEvents(Collection<PortalEvent> events) {
        for (final PortalEvent event : events) {
            this.entityManager.remove(event);
        }
    }
    
    
}
