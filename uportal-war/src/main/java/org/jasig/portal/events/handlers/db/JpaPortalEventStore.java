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
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.jasig.portal.concurrency.FunctionWithoutResult;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.jpa.BaseJpaDao;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

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
    private String selectQuery;
    private String selectUnaggregatedQuery;
    private int flushPeriod = 1000;
    private CriteriaQuery<DateTime> findNewestPersistentPortalEventTimestampQuery;
    private CriteriaQuery<DateTime> findOldestPersistentPortalEventTimestampQuery;
    private ParameterExpression<DateTime> startTimeParameter;
    private ParameterExpression<DateTime> endTimeParameter;

    private EntityManager entityManager;
    
    public JpaPortalEventStore() {
        mapper = new ObjectMapper();
        final AnnotationIntrospector pair = new AnnotationIntrospector.Pair(new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector());
        mapper.getDeserializationConfig().withAnnotationIntrospector(pair);
        mapper.getSerializationConfig().withAnnotationIntrospector(pair);
    }
    
    /**
     * Frequency that updated events should be flushed during a call to {@link #aggregatePortalEvents(DateTime, DateTime, int, FunctionWithoutResult)}, defaults to 1000.
     */
    public void setAggregationFlushPeriod(int flushPeriod) {
        this.flushPeriod = flushPeriod;
    }


    /**
     * @param entityManager the entityManager to set
     */
    @PersistenceContext(unitName = "uPortalRawEventsPersistence")
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
    public void afterPropertiesSet() throws Exception {
        this.startTimeParameter = this.createParameterExpression(DateTime.class, "startTime");
        this.endTimeParameter = this.createParameterExpression(DateTime.class, "endTime");
        
        this.selectQuery = 
                "SELECT e " +
                "FROM " + PersistentPortalEvent.class.getName() + " e " +
        		"WHERE e." + PersistentPortalEvent_.timestamp.getName() + " >= :" + this.startTimeParameter.getName() + " " +
                     "AND e." + PersistentPortalEvent_.timestamp.getName() + " < :" + this.endTimeParameter.getName() + " " + 
        		"ORDER BY e." + PersistentPortalEvent_.timestamp.getName() + " ASC";
        
        this.selectUnaggregatedQuery = 
                "SELECT e " +
                "FROM " + PersistentPortalEvent.class.getName() + " e " +
                "WHERE e." + PersistentPortalEvent_.timestamp.getName() + " >= :" + this.startTimeParameter.getName() + " " +
                     "AND e." + PersistentPortalEvent_.timestamp.getName() + " < :" + this.endTimeParameter.getName() + " " +
                     "AND (e." + PersistentPortalEvent_.aggregated.getName() + " is null OR e." + PersistentPortalEvent_.aggregated.getName() + " = false) " +
                "ORDER BY e." + PersistentPortalEvent_.timestamp.getName() + " ASC";
        
        this.deleteQuery = 
                "DELETE FROM " + PersistentPortalEvent.class.getName() + " e " +
        		"WHERE e." + PersistentPortalEvent_.timestamp.getName() + " < :" + this.endTimeParameter.getName();
        
        this.findNewestPersistentPortalEventTimestampQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<DateTime>>() {
            @Override
            public CriteriaQuery<DateTime> apply(CriteriaBuilder cb) {
                final CriteriaQuery<DateTime> criteriaQuery = cb.createQuery(DateTime.class);
                final Root<PersistentPortalEvent> eventRoot = criteriaQuery.from(PersistentPortalEvent.class);
                
                //Get the largest event timestamp
                criteriaQuery
                    .select(cb.greatest(eventRoot.get(PersistentPortalEvent_.timestamp)));
                
                return criteriaQuery;
            }
        });
        
        
        this.findOldestPersistentPortalEventTimestampQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<DateTime>>() {
            @Override
            public CriteriaQuery<DateTime> apply(CriteriaBuilder cb) {
                final CriteriaQuery<DateTime> criteriaQuery = cb.createQuery(DateTime.class);
                final Root<PersistentPortalEvent> eventRoot = criteriaQuery.from(PersistentPortalEvent.class);
                
                //Get the smallest event timestamp
                criteriaQuery
                    .select(cb.least(eventRoot.get(PersistentPortalEvent_.timestamp)));
                
                return criteriaQuery;
            }
        });
    }
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventDao#storePortalEvent(org.jasig.portal.events.PortalEvent)
     */
    @Override
    @Transactional(value="rawEvents")
    public void storePortalEvent(PortalEvent portalEvent) {
        final PersistentPortalEvent persistentPortalEvent = this.wrapPortalEvent(portalEvent);
        this.entityManager.persist(persistentPortalEvent);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventDao#storePortalEvents(org.jasig.portal.events.PortalEvent[])
     */
    @Override
    @Transactional(value="rawEvents")
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
    @Transactional(value="rawEvents")
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
    
    @Override
    public DateTime getOldestPortalEventTimestamp() {
        final TypedQuery<DateTime> query = this.createQuery(this.findOldestPersistentPortalEventTimestampQuery);
        final List<DateTime> results = query.getResultList();
        return DataAccessUtils.uniqueResult(results);
    }
    
    @Override
    public DateTime getNewestPortalEventTimestamp() {
        final TypedQuery<DateTime> query = this.createQuery(this.findNewestPersistentPortalEventTimestampQuery);
        final List<DateTime> results = query.getResultList();
        return DataAccessUtils.uniqueResult(results);
    }

    @Override
    @Transactional(value="rawEvents")
    public void aggregatePortalEvents(DateTime startTime, DateTime endTime, int maxEvents, FunctionWithoutResult<PortalEvent> handler) {
        final Session session = this.getEntityManager().unwrap(Session.class);
        final org.hibernate.Query query = session.createQuery(this.selectUnaggregatedQuery);
        query.setParameter(this.startTimeParameter.getName(), startTime);
        query.setParameter(this.endTimeParameter.getName(), endTime);
        if (maxEvents > 0) {
            query.setMaxResults(maxEvents);
        }

        int resultCount = 0;
        for (final ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY); results.next(); ) {
            final PersistentPortalEvent persistentPortalEvent = (PersistentPortalEvent)results.get(0);
            final PortalEvent portalEvent = this.toPortalEvent(persistentPortalEvent.getEventData(), persistentPortalEvent.getEventType());
            handler.apply(portalEvent);
            persistentPortalEvent.setAggregated(true);
            session.persist(persistentPortalEvent);
            
            //periodic flush and clear of session to manage memory demands
            if (++resultCount % this.flushPeriod == 0) {
                this.logger.debug("Aggregated {} events, flush and clear session.", resultCount);
                session.flush();
                session.clear();
            }
        }
    }
    
    @Override
    public void getPortalEvents(DateTime startTime, DateTime endTime, FunctionWithoutResult<PortalEvent> handler) {
        this.getPortalEvents(startTime, endTime, -1, handler);
    }
    
    @Override
    public void getPortalEvents(DateTime startTime, DateTime endTime, int maxEvents, FunctionWithoutResult<PortalEvent> handler) {
        final Session session = this.getEntityManager().unwrap(Session.class);
        final org.hibernate.Query query = session.createQuery(this.selectQuery);
        query.setParameter(this.startTimeParameter.getName(), startTime);
        query.setParameter(this.endTimeParameter.getName(), endTime);
        if (maxEvents > 0) {
            query.setMaxResults(maxEvents);
        }

        for (final ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY); results.next(); ) {
            final PersistentPortalEvent persistentPortalEvent = (PersistentPortalEvent)results.get(0);
            final PortalEvent portalEvent = this.toPortalEvent(persistentPortalEvent.getEventData(), persistentPortalEvent.getEventType());
            handler.apply(portalEvent);
            persistentPortalEvent.setAggregated(true);
            session.evict(persistentPortalEvent);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventDao#deletePortalEventsBefore(java.util.Date)
     */
    @Override
    @Transactional(value="rawEvents")
    public int deletePortalEventsBefore(DateTime time) {
        final Query query = this.entityManager.createQuery(this.deleteQuery);
        query.setParameter(this.endTimeParameter.getName(), time);
        return query.executeUpdate();
    }
    
    protected PersistentPortalEvent wrapPortalEvent(PortalEvent event) {
        final String portalEventData = this.toString(event);
        return new PersistentPortalEvent(event, portalEventData);
    }

    protected <E extends PortalEvent> E toPortalEvent(final String eventData, Class<E> eventType) {
        try {
            return mapper.readValue(eventData, eventType);
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
