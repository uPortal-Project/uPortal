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
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.ParameterExpression;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.jpa.BaseJpaDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        
        this.selectQuery = 
                "SELECT e " +
                "FROM " + PersistentPortalEvent.class.getName() + " e " +
        		"WHERE e." + PersistentPortalEvent_.timestamp.getName() + " >= :" + this.startTimeParameter.getName() + " " +
                     "AND e." + PersistentPortalEvent_.timestamp.getName() + " <= :" + this.endTimeParameter.getName() + " " + 
        		"ORDER BY e." + PersistentPortalEvent_.timestamp.getName() + " ASC";
        
        this.deleteQuery = 
                "DELETE FROM " + PersistentPortalEvent.class.getName() + " e " +
        		"WHERE e." + PersistentPortalEvent_.timestamp.getName() + " >= :" + this.startTimeParameter.getName() + " " +  
        		     "AND e." + PersistentPortalEvent_.timestamp.getName() + " <= :" + this.endTimeParameter.getName();
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
    public void getPortalEvents(Date startTime, Date endTime, Function<PortalEvent, Object> handler) {
        final Session session = this.getEntityManager().unwrap(Session.class);
        final org.hibernate.Query query = session.createQuery(this.selectQuery);
        query.setParameter(this.startTimeParameter.getName(), startTime);
        query.setParameter(this.endTimeParameter.getName(), endTime);
        for (final ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY); results.next(); ) {
            final PersistentPortalEvent persistentPortalEvent = (PersistentPortalEvent)results.get(0);
            final PortalEvent portalEvent = this.toPortalEvent(persistentPortalEvent.getEventData(), persistentPortalEvent.getEventType());
            handler.apply(portalEvent);
            session.evict(persistentPortalEvent);
        }
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
