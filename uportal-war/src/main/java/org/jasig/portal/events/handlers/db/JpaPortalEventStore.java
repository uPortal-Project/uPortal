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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.jasig.portal.events.PortalEvent;
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

    private final ObjectMapper mapper;
    private String deleteQuery;
    private CriteriaQuery<PersistentPortalEvent> findPortalEventsForTimeRangeQuery;
    private ParameterExpression<Date> startTimeParameter;
    private ParameterExpression<Date> endTimeParameter;

    private EntityManager entityManager;
    
    public JpaPortalEventStore() {
        mapper = new ObjectMapper();
        final AnnotationIntrospector pair = new AnnotationIntrospector.Pair(new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector());
        mapper.getDeserializationConfig().withAnnotationIntrospector(pair);
        mapper.getSerializationConfig().withAnnotationIntrospector(pair);
    }

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
        this.startTimeParameter = cb.parameter(Date.class, "startTime");
        this.endTimeParameter = cb.parameter(Date.class, "endTime");
        
        this.findPortalEventsForTimeRangeQuery = this.buildFindPortalEventsForTimeRangeQuery(cb);
        
        this.deleteQuery = 
                "DELETE FROM " + PersistentPortalEvent.class.getName() + " e " +
        		"WHERE e." + PersistentPortalEvent_.timestamp.getName() + " >= :" + this.startTimeParameter.getName() + 
        		     " AND e." + PersistentPortalEvent_.timestamp.getName() + " <= :" + this.endTimeParameter.getName();
    }

    protected CriteriaQuery<PersistentPortalEvent> buildFindPortalEventsForTimeRangeQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<PersistentPortalEvent> criteriaQuery = cb.createQuery(PersistentPortalEvent.class);
        final Root<PersistentPortalEvent> entityRoot = criteriaQuery.from(PersistentPortalEvent.class);
        criteriaQuery.select(entityRoot);
        criteriaQuery.where(
            cb.and(
                cb.greaterThan(entityRoot.get(PersistentPortalEvent_.timestamp), this.startTimeParameter),
                cb.lessThan(entityRoot.get(PersistentPortalEvent_.timestamp), this.endTimeParameter)
            )
        );
        criteriaQuery.orderBy(cb.asc(entityRoot.get(PersistentPortalEvent_.timestamp)));
        
        return criteriaQuery;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventDao#storePortalEvent(org.jasig.portal.events.PortalEvent)
     */
    @Override
    @Transactional(value="statsTransactionManager")
    public void storePortalEvent(PortalEvent portalEvent) {
        final PersistentPortalEvent persistentPortalEvent = this.wrapPortalEvent(portalEvent);
        this.entityManager.persist(persistentPortalEvent);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventDao#storePortalEvents(org.jasig.portal.events.PortalEvent[])
     */
    @Override
    @Transactional(value="statsTransactionManager")
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
    @Transactional(value="statsTransactionManager")
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
    public List<PortalEvent> getPortalEvents(Date startTime, Date endTime) {
        final TypedQuery<PersistentPortalEvent> query = this.getEntityManager().createQuery(this.findPortalEventsForTimeRangeQuery);
        query.setParameter(this.startTimeParameter, startTime);
        query.setParameter(this.endTimeParameter, endTime);
        
        final List<PersistentPortalEvent> resultList = query.getResultList();
        final List<PortalEvent> events = new ArrayList<PortalEvent>(resultList.size());
        
        for (final PersistentPortalEvent persistentPortalEvent : resultList) {
            final String eventData = persistentPortalEvent.getEventData();
            final PortalEvent event = toPortalEvent(eventData);
            events.add(event);
        }
        
        return events;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventDao#deletePortalEventsBefore(java.util.Date)
     */
    @Override
    @Transactional(value="statsTransactionManager")
    public void deletePortalEvents(Date startTime, Date endTime) {
        final Query query = this.entityManager.createQuery(this.deleteQuery);
        query.setParameter(this.startTimeParameter.getName(), startTime);
        query.setParameter(this.endTimeParameter.getName(), endTime);
        final int deleted = query.executeUpdate();
        this.logger.debug("Purged {} events between {} and {}", new Object[] { deleted, startTime, endTime });
    }
    
    protected PersistentPortalEvent wrapPortalEvent(PortalEvent event) {
        final String portalEventData = this.toString(event);
        return new PersistentPortalEvent(event, portalEventData);
    }

    protected PortalEvent toPortalEvent(final String eventData) {
        try {
            return mapper.readValue(eventData, PortalEvent.class);
        }
        catch (JsonParseException e) {
            throw new RuntimeException("Failed to deserialize PortalEvent data", e);
        }
        catch (JsonMappingException e) {
            throw new RuntimeException("Failed to deserialize PortalEvent data", e);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to deserialize PortalEvent data", e);
        }
    }
    
    protected String toString(PortalEvent event) {
        try {
            return mapper.writeValueAsString(event);
        }
        catch (JsonParseException e) {
            throw new RuntimeException("Failed to serialize PortalEvent data", e);
        }
        catch (JsonMappingException e) {
            throw new RuntimeException("Failed to serialize PortalEvent data", e);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to serialize PortalEvent data", e);
        }
    }
}
