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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao;
import org.jasig.portal.jpa.cache.EntityManagerCache;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.cache.CacheKey;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository("eventSessionDao")
public class JpaEventSessionDao extends BaseAggrEventsJpaDao implements EventSessionDao {
    private final static String EVENT_SESSION_CACHE_SOURCE = JpaEventSessionDao.class.getName() + "_EVENT_SESSION";

    private String deleteByEventSessionIdQuery;
    private CriteriaQuery<EventSessionImpl> findExpiredEventSessionsQuery;
    private ParameterExpression<String> eventSessionIdParameter;
    private ParameterExpression<DateTime> dateTimeParameter;
    
    private AggregatedGroupLookupDao aggregatedGroupLookupDao;
    private ICompositeGroupService compositeGroupService;
    private EntityManagerCache entityManagerCache;
    
    @Autowired
    public void setEntityManagerCache(EntityManagerCache entityManagerCache) {
        this.entityManagerCache = entityManagerCache;
    }

    @Autowired
    public void setCompositeGroupService(ICompositeGroupService compositeGroupService) {
        this.compositeGroupService = compositeGroupService;
    }

    @Autowired
    public void setAggregatedGroupLookupDao(AggregatedGroupLookupDao aggregatedGroupLookupDao) {
        this.aggregatedGroupLookupDao = aggregatedGroupLookupDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.eventSessionIdParameter = this.createParameterExpression(String.class, "eventSessionId");
        this.dateTimeParameter = this.createParameterExpression(DateTime.class, "dateTime");
        
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
    
    @AggrEventsTransactional
    @Override
    public void storeEventSession(EventSession eventSession) {
        this.getEntityManager().persist(eventSession);
    }
    
    @AggrEventsTransactional
    @Override
    public EventSession getEventSession(PortalEvent event) {
        final String eventSessionId = event.getEventSessionId();
        
        final CacheKey key = CacheKey.build(EVENT_SESSION_CACHE_SOURCE, eventSessionId);
        EventSessionImpl eventSession = this.entityManagerCache.get(PERSISTENCE_UNIT_NAME, key);
        if (eventSession != null) {
            return eventSession;
        }
        
        final NaturalIdQuery<EventSessionImpl> naturalIdQuery = this.createNaturalIdQuery(EventSessionImpl.class);
        naturalIdQuery.using(EventSessionImpl_.eventSessionId, eventSessionId);

        eventSession = naturalIdQuery.load();
        if (eventSession == null) {
            //No event session, somehow we missed the login event. Look at the groups the user is currently a member of
            final Set<AggregatedGroupMapping> groupMappings = this.getGroupsForEvent(event);
            
            final DateTime eventDate = event.getTimestampAsDate();
            eventSession = new EventSessionImpl(eventSessionId, eventDate, groupMappings);
            
            this.getEntityManager().persist(eventSession);
            this.entityManagerCache.put(PERSISTENCE_UNIT_NAME, key, eventSession);
        }
                
        return eventSession;
    }

    @AggrEventsTransactional
    @Override
    public void deleteEventSession(String eventSessionId) {
        final Query query = this.getEntityManager().createQuery(this.deleteByEventSessionIdQuery);
        query.setParameter(this.eventSessionIdParameter.getName(), eventSessionId);
        query.executeUpdate();
    }

    @AggrEventsTransactional
    @Override
    public int purgeEventSessionsBefore(DateTime lastAggregatedEventDate) {
        final TypedQuery<EventSessionImpl> query = this.createQuery(this.findExpiredEventSessionsQuery);
        query.setParameter(this.dateTimeParameter, lastAggregatedEventDate);
        final List<EventSessionImpl> resultList = query.getResultList();
        for (final EventSessionImpl eventSession : resultList) {
            this.getEntityManager().remove(eventSession);
        }
        
        return resultList.size();
    }
    
    /**
     * Get groups for the event
     */
    protected Set<AggregatedGroupMapping> getGroupsForEvent(PortalEvent event) {
        final Set<AggregatedGroupMapping> groupMappings = new LinkedHashSet<AggregatedGroupMapping>();
        
        if (event instanceof LoginEvent) {
            for (final String groupKey : ((LoginEvent) event).getGroups()) {
                final AggregatedGroupMapping groupMapping = this.aggregatedGroupLookupDao.getGroupMapping(groupKey);
                if (groupMapping != null) {
                    groupMappings.add(groupMapping);
                }
            }
        }
        else {
            final String userName = event.getUserName();
            final IGroupMember groupMember = this.compositeGroupService.getGroupMember(userName, IPerson.class);
            for (@SuppressWarnings("unchecked")
            final Iterator<IEntityGroup> containingGroups = this.compositeGroupService.findContainingGroups(groupMember); containingGroups.hasNext(); ) {
                final IEntityGroup group = containingGroups.next();
                final AggregatedGroupMapping groupMapping = this.aggregatedGroupLookupDao.getGroupMapping(group.getServiceName().toString(), group.getName());
                groupMappings.add(groupMapping);
            }
        }
        
        return groupMappings;
    }
}
