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

package org.jasig.portal.events.aggr.session;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.jpa.BaseJpaDao;
import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository("eventSessionDao")
public class JpaEventSessionDao extends BaseJpaDao implements EventSessionDao {
    private String deleteByEventSessionIdQuery;
    private CriteriaQuery<EventSessionImpl> findExpiredEventSessionsQuery;
    private CriteriaQuery<EventSessionImpl> findByEventSessionIdQuery;
    private ParameterExpression<String> eventSessionIdParameter;
    private ParameterExpression<DateTime> dateTimeParameter;
    
    private EntityManager entityManager;
    private AggregatedGroupLookupDao aggregatedGroupLookupDao;
    private ReadablePeriod eventSessionDuration;
    
    @Value("${org.jasig.portal.events.aggr.session.JpaEventSessionDao.eventSessionDuration:P1D}")
    public void setEventSessionDuration(ReadablePeriod eventSessionDuration) {
        this.eventSessionDuration = eventSessionDuration;
    }

    @Autowired
    public void setAggregatedGroupLookupDao(AggregatedGroupLookupDao aggregatedGroupLookupDao) {
        this.aggregatedGroupLookupDao = aggregatedGroupLookupDao;
    }

    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.eventSessionIdParameter = this.createParameterExpression(String.class, "eventSessionId");
        this.dateTimeParameter = this.createParameterExpression(DateTime.class, "dateTime");
        
        this.findByEventSessionIdQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<EventSessionImpl>>() {
            @Override
            public CriteriaQuery<EventSessionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<EventSessionImpl> criteriaQuery = cb.createQuery(EventSessionImpl.class);
                final Root<EventSessionImpl> root = criteriaQuery.from(EventSessionImpl.class);
                criteriaQuery.select(root);
                criteriaQuery.where(
                        cb.equal(root.get(EventSessionImpl_.eventSessionId), eventSessionIdParameter)
                    );
                
                return criteriaQuery;
            }
        });
        
        
        this.findExpiredEventSessionsQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<EventSessionImpl>>() {
            @Override
            public CriteriaQuery<EventSessionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<EventSessionImpl> criteriaQuery = cb.createQuery(EventSessionImpl.class);
                final Root<EventSessionImpl> root = criteriaQuery.from(EventSessionImpl.class);
                criteriaQuery.select(root);
                criteriaQuery.where(
                        cb.lessThanOrEqualTo(root.get(EventSessionImpl_.lastAccessed), dateTimeParameter)
                    );
                
                return criteriaQuery;
            }
        });
        
        
        this.deleteByEventSessionIdQuery = 
                "DELETE FROM " + EventSessionImpl.class.getName() + " e " +
                "WHERE e." + EventSessionImpl_.eventSessionId.getName() + " = :" + this.eventSessionIdParameter.getName();
    }
    

    @Transactional("aggrEvents")
    @Override
    public EventSession createEventSession(LoginEvent loginEvent) {
        final Set<AggregatedGroupMapping> groupMappings = new LinkedHashSet<AggregatedGroupMapping>();
        for (final String groupKey : loginEvent.getGroups()) {
            final AggregatedGroupMapping groupMapping = this.aggregatedGroupLookupDao.getGroupMapping(groupKey);
            groupMappings.add(groupMapping);
        }
        
        final EventSessionImpl eventSession = new EventSessionImpl(loginEvent.getEventSessionId(), groupMappings);
        
        this.entityManager.persist(eventSession);
        
        return eventSession;
    }

    @Transactional("aggrEvents")
    @Override
    public EventSession getEventSession(String eventSessionId) {
        final TypedQuery<EventSessionImpl> query = this.createCachedQuery(this.findByEventSessionIdQuery);
        query.setParameter(this.eventSessionIdParameter, eventSessionId);
        
        final List<EventSessionImpl> results = query.getResultList();
        
        final EventSessionImpl eventSession = DataAccessUtils.uniqueResult(results);
        if (eventSession == null) {
            return null;
        }
        
        eventSession.recordAccess();
        this.entityManager.persist(eventSession);
        
        return eventSession;
    }

    @Transactional("aggrEvents")
    @Override
    public void deleteEventSession(String eventSessionId) {
        final Query query = this.entityManager.createQuery(this.deleteByEventSessionIdQuery);
        query.setParameter(this.eventSessionIdParameter.getName(), eventSessionId);
        query.executeUpdate();
    }

    @Transactional("aggrEvents")
    @Override
    public void purgeExpiredEventSessions() {
        final TypedQuery<EventSessionImpl> query = this.createQuery(this.findExpiredEventSessionsQuery);
        query.setParameter(this.dateTimeParameter, DateTime.now().minus(eventSessionDuration));
        for (final EventSessionImpl eventSession : query.getResultList()) {
            this.entityManager.remove(eventSession);
        }
    }
}
