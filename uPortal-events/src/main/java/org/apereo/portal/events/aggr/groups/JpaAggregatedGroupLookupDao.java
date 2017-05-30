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
package org.apereo.portal.events.aggr.groups;

import com.google.common.base.Function;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.apereo.portal.groups.CompositeEntityIdentifier;
import org.apereo.portal.groups.ICompositeGroupService;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;
import org.apereo.portal.jpa.OpenEntityManager;
import org.apereo.portal.jpa.cache.EntityManagerCache;
import org.apereo.portal.utils.cache.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

/**
 * JPA dao to manage aggregated group mappings
 *
 */
@Repository
public class JpaAggregatedGroupLookupDao extends BaseAggrEventsJpaDao
        implements AggregatedGroupLookupDao {
    private CriteriaQuery<AggregatedGroupMappingImpl> findAllGroupMappingsQuery;

    private EntityManagerCache entityManagerCache;
    private ICompositeGroupService compositeGroupService;

    @Autowired
    public void setEntityManagerCache(EntityManagerCache entityManagerCache) {
        this.entityManagerCache = entityManagerCache;
    }

    @Autowired
    public void setCompositeGroupService(ICompositeGroupService compositeGroupService) {
        this.compositeGroupService = compositeGroupService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllGroupMappingsQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<AggregatedGroupMappingImpl>>() {
                            @Override
                            public CriteriaQuery<AggregatedGroupMappingImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<AggregatedGroupMappingImpl> criteriaQuery =
                                        cb.createQuery(AggregatedGroupMappingImpl.class);
                                criteriaQuery.from(AggregatedGroupMappingImpl.class);
                                return criteriaQuery;
                            }
                        });
    }

    @OpenEntityManager(unitName = BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    @Override
    public AggregatedGroupMapping getGroupMapping(
            final String groupService, final String groupName) {
        final CacheKey key = CacheKey.build(this.getClass().getName(), groupService, groupName);

        AggregatedGroupMapping groupMapping =
                this.entityManagerCache.get(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key);
        if (groupMapping != null) {
            return groupMapping;
        }

        final NaturalIdQuery<AggregatedGroupMappingImpl> query =
                this.createNaturalIdQuery(AggregatedGroupMappingImpl.class);
        query.using(AggregatedGroupMappingImpl_.groupService, groupService);
        query.using(AggregatedGroupMappingImpl_.groupName, groupName);

        groupMapping = query.load();
        if (groupMapping != null) {
            this.entityManagerCache.put(
                    BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key, groupMapping);
            return groupMapping;
        }

        return this.getTransactionOperations()
                .execute(
                        new TransactionCallback<AggregatedGroupMapping>() {
                            @Override
                            public AggregatedGroupMapping doInTransaction(
                                    TransactionStatus status) {
                                final AggregatedGroupMappingImpl aggregatedGroupMapping =
                                        new AggregatedGroupMappingImpl(groupService, groupName);
                                getEntityManager().persist(aggregatedGroupMapping);

                                logger.debug("Created {}", aggregatedGroupMapping);
                                entityManagerCache.put(
                                        BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME,
                                        key,
                                        aggregatedGroupMapping);

                                return aggregatedGroupMapping;
                            }
                        });
    }

    private final Set<String> warnedGroupKeys =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @OpenEntityManager(unitName = BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    @Override
    public AggregatedGroupMapping getGroupMapping(final String portalGroupKey) {
        final IEntityGroup group = compositeGroupService.findGroup(portalGroupKey);
        if (group == null) {
            if (warnedGroupKeys.add(portalGroupKey)) {
                logger.warn(
                        "No group found for key {}, no aggregate group mapping will be done and the group key will be ignored.",
                        portalGroupKey);
            }

            final CompositeEntityIdentifier compositeEntityIdentifier =
                    new CompositeEntityIdentifier(portalGroupKey, IEntityGroup.class);
            final String serviceName = compositeEntityIdentifier.getServiceName().toString();
            final String groupKey = compositeEntityIdentifier.getLocalKey();
            return this.getGroupMapping(serviceName, groupKey);
        }

        final String groupService = group.getServiceName().toString();
        final String groupName = group.getName();

        return this.getGroupMapping(groupService, groupName);
    }

    @Override
    public Set<AggregatedGroupMapping> getGroupMappings() {
        final TypedQuery<AggregatedGroupMappingImpl> cachedQuery =
                this.createCachedQuery(this.findAllGroupMappingsQuery);
        cachedQuery.setFlushMode(FlushModeType.COMMIT);

        return new LinkedHashSet<AggregatedGroupMapping>(cachedQuery.getResultList());
    }

    @Override
    public AggregatedGroupMapping getGroupMapping(long groupMappingId) {
        return this.getEntityManager().find(AggregatedGroupMappingImpl.class, groupMappingId);
    }
}
