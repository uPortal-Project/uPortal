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
package org.apereo.portal.events.aggr.tabs;

import com.google.common.base.Function;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.jpa.OpenEntityManager;
import org.apereo.portal.jpa.cache.EntityManagerCache;
import org.apereo.portal.utils.Tuple;
import org.apereo.portal.utils.cache.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

/**
 * JPA dao to manage aggregated tab mappings
 *
 */
@Repository
public class JpaAggregatedTabLookupDao extends BaseAggrEventsJpaDao
        implements AggregatedTabLookupDao {
    private static final Pattern DLM_NODE = Pattern.compile("^u(\\d+)l(\\d+)s(\\d+)$");

    private CriteriaQuery<AggregatedTabMappingImpl> findAllTabMappingsQuery;

    private EntityManagerCache entityManagerCache;
    private JdbcOperations portalJdbcOperations;
    private Ehcache layoutNodeIdNameResolutionCache;

    @Autowired
    @Qualifier(
            "org.apereo.portal.events.aggr.tabrender.TabRenderAggregator.layoutNodeIdNameResolver")
    public void setLayoutNodeIdNameResolutionCache(Ehcache layoutNodeIdNameResolutionCache) {
        this.layoutNodeIdNameResolutionCache = layoutNodeIdNameResolutionCache;
    }

    @Autowired
    @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public void setPortalJdbcOperations(JdbcOperations portalJdbcOperations) {
        this.portalJdbcOperations = portalJdbcOperations;
    }

    @Autowired
    public void setEntityManagerCache(EntityManagerCache entityManagerCache) {
        this.entityManagerCache = entityManagerCache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllTabMappingsQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<AggregatedTabMappingImpl>>() {
                            @Override
                            public CriteriaQuery<AggregatedTabMappingImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<AggregatedTabMappingImpl> criteriaQuery =
                                        cb.createQuery(AggregatedTabMappingImpl.class);
                                criteriaQuery.from(AggregatedTabMappingImpl.class);
                                return criteriaQuery;
                            }
                        });
    }

    @OpenEntityManager(unitName = BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    @Override
    public AggregatedTabMapping getMappedTabForLayoutId(String layoutNodeId) {
        final Tuple<String, String> resolveTabName = this.resolveTabName(layoutNodeId);
        return getTabMapping(resolveTabName.first, resolveTabName.second);
    }

    @OpenEntityManager(unitName = BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    @Override
    public AggregatedTabMapping getTabMapping(final String fragmentName, final String tabName) {
        final CacheKey key = CacheKey.build(this.getClass().getName(), tabName);

        AggregatedTabMapping tabMapping = this.entityManagerCache.get(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key);
        if (tabMapping != null) {
            return tabMapping;
        }

        final NaturalIdQuery<AggregatedTabMappingImpl> query =
                this.createNaturalIdQuery(AggregatedTabMappingImpl.class);
        query.using(AggregatedTabMappingImpl_.fragmentName, fragmentName);
        query.using(AggregatedTabMappingImpl_.tabName, tabName);
        tabMapping = query.load();
        if (tabMapping != null) {
            this.entityManagerCache.put(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key, tabMapping);
            return tabMapping;
        }

        return this.getTransactionOperations()
                .execute(
                        new TransactionCallback<AggregatedTabMapping>() {
                            @Override
                            public AggregatedTabMapping doInTransaction(TransactionStatus status) {
                                final AggregatedTabMappingImpl aggregatedGroupMapping =
                                        new AggregatedTabMappingImpl(fragmentName, tabName);
                                getEntityManager().persist(aggregatedGroupMapping);

                                logger.debug("Created {}", aggregatedGroupMapping);
                                entityManagerCache.put(
                                        BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key, aggregatedGroupMapping);

                                return aggregatedGroupMapping;
                            }
                        });
    }

    @Override
    public Set<AggregatedTabMapping> getTabMappings() {
        final TypedQuery<AggregatedTabMappingImpl> cachedQuery =
                this.createCachedQuery(this.findAllTabMappingsQuery);
        cachedQuery.setFlushMode(FlushModeType.COMMIT);

        return new LinkedHashSet<AggregatedTabMapping>(cachedQuery.getResultList());
    }

    protected final Tuple<String, String> resolveTabName(final String targetedLayoutNodeId) {
        //Check the cache first
        final Element element = layoutNodeIdNameResolutionCache.get(targetedLayoutNodeId);
        if (element != null) {
            return (Tuple<String, String>) element.getObjectValue();
        }

        final String fragmentName;
        final String tabName;
        if (targetedLayoutNodeId == null) {
            //No layout node id, return null placeholder
            fragmentName = AggregatedTabMapping.MISSING_TAB_FRAGMENT_NAME;
            tabName = AggregatedTabMapping.MISSING_TAB_NAME;
        } else {
            final Matcher nodeIdMatcher = DLM_NODE.matcher(targetedLayoutNodeId);
            if (nodeIdMatcher.matches()) {
                final int userId = Integer.parseInt(nodeIdMatcher.group(1));
                final int layoutId = Integer.parseInt(nodeIdMatcher.group(2));
                final int nodeId = Integer.parseInt(nodeIdMatcher.group(3));

                final List<String> tabNameResult =
                        this.portalJdbcOperations.queryForList(
                                "SELECT NAME FROM UP_LAYOUT_STRUCT where USER_ID = ? AND LAYOUT_ID = ? AND STRUCT_ID = ?",
                                String.class,
                                userId,
                                layoutId,
                                nodeId);
                if (tabNameResult.isEmpty()) {
                    //No tab name found, fall back to using the bare layout node id
                    tabName = targetedLayoutNodeId;
                } else {
                    //Use the found tab name
                    tabName = tabNameResult.iterator().next();
                }

                final List<String> userNameResult =
                        this.portalJdbcOperations.queryForList(
                                "SELECT USER_NAME FROM UP_USER WHERE USER_ID=?",
                                String.class,
                                userId);
                if (userNameResult.isEmpty()) {
                    //No user name found, use the missing user placeholder
                    fragmentName = AggregatedTabMapping.MISSING_USER_FRAGMENT_NAME;
                } else {
                    //Use the found user name
                    fragmentName = userNameResult.iterator().next();
                }
            } else {
                //Node isn't from DLM return personal placeholder
                fragmentName = AggregatedTabMapping.PERSONAL_TAB_FRAGMENT_NAME;
                tabName = AggregatedTabMapping.PERSONAL_TAB_NAME;
            }
        }

        //cache the resolution
        final Tuple<String, String> tuple = new Tuple<String, String>(fragmentName, tabName);
        layoutNodeIdNameResolutionCache.put(new Element(targetedLayoutNodeId, tuple));
        return tuple;
    }

    @Override
    public AggregatedTabMapping getTabMapping(long tabMappingId) {
        return this.getEntityManager().find(AggregatedTabMappingImpl.class, tabMappingId);
    }
}
