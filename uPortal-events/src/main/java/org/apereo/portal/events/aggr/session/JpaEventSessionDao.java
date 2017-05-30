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
package org.apereo.portal.events.aggr.session;

import com.google.common.base.Function;
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
import org.apereo.portal.events.LoginEvent;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.groups.ICompositeGroupService;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;
import org.apereo.portal.jpa.cache.EntityManagerCache;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.utils.cache.CacheKey;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 */
@Repository("eventSessionDao")
public class JpaEventSessionDao extends BaseAggrEventsJpaDao implements EventSessionDao {
    private static final String EVENT_SESSION_CACHE_SOURCE =
            JpaEventSessionDao.class.getName() + "_EVENT_SESSION";
    private int maxPurgeBatchSize;

    private String deleteByEventSessionIdQuery;
    private CriteriaQuery<EventSessionImpl> findExpiredEventSessionsQuery;
    private CriteriaQuery<Long> countExpiredEventSessionsQuery;
    private ParameterExpression<String> eventSessionIdParameter;
    private ParameterExpression<DateTime> dateTimeParameter;

    private AggregatedGroupLookupDao aggregatedGroupLookupDao;
    private ICompositeGroupService compositeGroupService;
    private EntityManagerCache entityManagerCache;

    @Autowired
    @Value("${org.apereo.portal.events.aggr.session.JpaEventSessionDao.maxPurgeBatchSize:100000}")
    public void setMaxPurgeBatchSize(int maxPurgeBatchSize) {
        this.maxPurgeBatchSize = maxPurgeBatchSize;
    }

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
        this.eventSessionIdParameter =
                this.createParameterExpression(String.class, "eventSessionId");
        this.dateTimeParameter = this.createParameterExpression(DateTime.class, "dateTime");

        this.findExpiredEventSessionsQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<EventSessionImpl>>() {
                            @Override
                            public CriteriaQuery<EventSessionImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<EventSessionImpl> criteriaQuery =
                                        cb.createQuery(EventSessionImpl.class);
                                final Root<EventSessionImpl> root =
                                        criteriaQuery.from(EventSessionImpl.class);
                                criteriaQuery.select(root);
                                criteriaQuery.where(
                                        cb.lessThanOrEqualTo(
                                                root.get(EventSessionImpl_.lastAccessed),
                                                dateTimeParameter));

                                return criteriaQuery;
                            }
                        });

        this.countExpiredEventSessionsQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<Long>>() {
                            @Override
                            public CriteriaQuery<Long> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<Long> criteriaQuery =
                                        cb.createQuery(Long.class);
                                final Root<EventSessionImpl> root =
                                        criteriaQuery.from(EventSessionImpl.class);
                                criteriaQuery.select(cb.count(root));
                                criteriaQuery.where(
                                        cb.lessThanOrEqualTo(
                                                root.get(EventSessionImpl_.lastAccessed),
                                                dateTimeParameter));

                                return criteriaQuery;
                            }
                        });

        this.deleteByEventSessionIdQuery =
                "DELETE FROM "
                        + EventSessionImpl.class.getName()
                        + " e "
                        + "WHERE e."
                        + EventSessionImpl_.eventSessionId.getName()
                        + " = :"
                        + this.eventSessionIdParameter.getName();
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
        EventSessionImpl eventSession = this.entityManagerCache.get(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key);
        if (eventSession != null) {
            return eventSession;
        }

        final NaturalIdQuery<EventSessionImpl> naturalIdQuery =
                this.createNaturalIdQuery(EventSessionImpl.class);
        naturalIdQuery.using(EventSessionImpl_.eventSessionId, eventSessionId);

        eventSession = naturalIdQuery.load();
        if (eventSession == null) {
            //No event session, somehow we missed the login event. Look at the groups the user is currently a member of
            final Set<AggregatedGroupMapping> groupMappings = this.getGroupsForEvent(event);

            final DateTime eventDate = event.getTimestampAsDate();
            eventSession = new EventSessionImpl(eventSessionId, eventDate, groupMappings);

            this.getEntityManager().persist(eventSession);
            this.entityManagerCache.put(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key, eventSession);
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

    private void purgeEventList(int batchSize, DateTime lastAggregatedEventDate) {
        final TypedQuery<EventSessionImpl> query =
                this.createQuery(this.findExpiredEventSessionsQuery);
        query.setParameter(this.dateTimeParameter, lastAggregatedEventDate);
        query.setMaxResults(batchSize);
        final List<EventSessionImpl> resultList = query.getResultList();
        for (final EventSessionImpl eventSession : resultList) {
            this.getEntityManager().remove(eventSession);
        }
    }

    @AggrEventsTransactional
    @Override
    public int purgeEventSessionsBefore(DateTime lastAggregatedEventDate) {
        final TypedQuery<Long> countQuery = this.createQuery(this.countExpiredEventSessionsQuery);
        countQuery.setParameter(this.dateTimeParameter, lastAggregatedEventDate);
        final int totalRows = countQuery.getSingleResult().intValue();

        if (totalRows > 0) {
            final int numberBatches = totalRows / maxPurgeBatchSize;
            for (int i = 0; i <= numberBatches; i++) {
                purgeEventList(maxPurgeBatchSize, lastAggregatedEventDate);
            }
        }

        return totalRows;
    }

    /** Get groups for the event */
    protected Set<AggregatedGroupMapping> getGroupsForEvent(PortalEvent event) {
        final Set<AggregatedGroupMapping> groupMappings =
                new LinkedHashSet<AggregatedGroupMapping>();

        if (event instanceof LoginEvent) {
            for (final String groupKey : ((LoginEvent) event).getGroups()) {
                final AggregatedGroupMapping groupMapping =
                        this.aggregatedGroupLookupDao.getGroupMapping(groupKey);
                if (groupMapping != null) {
                    groupMappings.add(groupMapping);
                }
            }
        } else {
            final String userName = event.getUserName();
            final IGroupMember groupMember =
                    this.compositeGroupService.getGroupMember(userName, IPerson.class);
            for (@SuppressWarnings("unchecked")
                    final Iterator<IEntityGroup> containingGroups =
                            this.compositeGroupService.findParentGroups(groupMember);
                    containingGroups.hasNext();
                    ) {
                final IEntityGroup group = containingGroups.next();
                final AggregatedGroupMapping groupMapping =
                        this.aggregatedGroupLookupDao.getGroupMapping(
                                group.getServiceName().toString(), group.getName());
                groupMappings.add(groupMapping);
            }
        }

        return groupMappings;
    }
}
