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
package org.apereo.portal.events.aggr.portlets;

import com.google.common.base.Function;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;
import org.apereo.portal.jpa.OpenEntityManager;
import org.apereo.portal.jpa.cache.EntityManagerCache;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.utils.cache.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

/**
 * JPA dao to manage aggregated tab mappings
 *
 */
@Repository
public class JpaAggregatedPortletLookupDao extends BaseAggrEventsJpaDao
        implements AggregatedPortletLookupDao {
    private CriteriaQuery<AggregatedPortletMappingImpl> findAllPortletMappingsQuery;

    private EntityManagerCache entityManagerCache;
    private IPortletDefinitionDao portletDefinitionDao;

    @Autowired
    public void setPortletDefinitionDao(IPortletDefinitionDao portletDefinitionDao) {
        this.portletDefinitionDao = portletDefinitionDao;
    }

    @Autowired
    public void setEntityManagerCache(EntityManagerCache entityManagerCache) {
        this.entityManagerCache = entityManagerCache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllPortletMappingsQuery =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder, CriteriaQuery<AggregatedPortletMappingImpl>>() {
                            @Override
                            public CriteriaQuery<AggregatedPortletMappingImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<AggregatedPortletMappingImpl> criteriaQuery =
                                        cb.createQuery(AggregatedPortletMappingImpl.class);
                                criteriaQuery.from(AggregatedPortletMappingImpl.class);
                                return criteriaQuery;
                            }
                        });
    }

    @OpenEntityManager(unitName = BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    @Override
    public AggregatedPortletMapping getMappedPortletForFname(final String fname) {
        final CacheKey key = CacheKey.build(this.getClass().getName(), fname);

        AggregatedPortletMapping portletMapping =
                this.entityManagerCache.get(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key);
        if (portletMapping != null) {
            return portletMapping;
        }

        final NaturalIdQuery<AggregatedPortletMappingImpl> query =
                this.createNaturalIdQuery(AggregatedPortletMappingImpl.class);
        query.using(AggregatedPortletMappingImpl_.fname, fname);
        portletMapping = query.load();
        if (portletMapping != null) {
            this.entityManagerCache.put(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key, portletMapping);
            return portletMapping;
        }

        return this.getTransactionOperations()
                .execute(
                        new TransactionCallback<AggregatedPortletMapping>() {
                            @Override
                            public AggregatedPortletMapping doInTransaction(
                                    TransactionStatus status) {
                                final IPortletDefinition portletDefinition =
                                        portletDefinitionDao.getPortletDefinitionByFname(fname);
                                final String name;
                                if (portletDefinition != null) {
                                    name = portletDefinition.getName();
                                } else {
                                    name = fname;
                                }

                                final AggregatedPortletMappingImpl aggregatedGroupMapping =
                                        new AggregatedPortletMappingImpl(name, fname);
                                getEntityManager().persist(aggregatedGroupMapping);

                                logger.debug("Created {}", aggregatedGroupMapping);
                                entityManagerCache.put(
                                        BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key, aggregatedGroupMapping);

                                return aggregatedGroupMapping;
                            }
                        });
    }

    @Override
    public Set<AggregatedPortletMapping> getPortletMappings() {
        final TypedQuery<AggregatedPortletMappingImpl> cachedQuery =
                this.createCachedQuery(this.findAllPortletMappingsQuery);
        cachedQuery.setFlushMode(FlushModeType.COMMIT);

        return new LinkedHashSet<AggregatedPortletMapping>(cachedQuery.getResultList());
    }
}
