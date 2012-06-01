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

package org.jasig.portal.events.aggr.groups;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao;
import org.jasig.portal.jpa.cache.EntityManagerCache;
import org.jasig.portal.utils.cache.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.google.common.base.Function;

/**
 * JPA dao to manage aggregated group mappings
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaAggregatedGroupLookupDao extends BaseAggrEventsJpaDao implements AggregatedGroupLookupDao {
    private CriteriaQuery<AggregatedGroupMappingImpl> findGroupMappingByServiceAndNameQuery;
    private CriteriaQuery<AggregatedGroupMappingImpl> findAllGroupMappingsQuery;
    private ParameterExpression<String> groupServiceParameter;
    private ParameterExpression<String> groupNameParameter;
    

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
        this.groupServiceParameter = this.createParameterExpression(String.class, "groupService");
        this.groupNameParameter = this.createParameterExpression(String.class, "groupName");
        
        this.findAllGroupMappingsQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<AggregatedGroupMappingImpl>>() {
            @Override
            public CriteriaQuery<AggregatedGroupMappingImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<AggregatedGroupMappingImpl> criteriaQuery = cb.createQuery(AggregatedGroupMappingImpl.class);
                criteriaQuery.from(AggregatedGroupMappingImpl.class);
                return criteriaQuery;
            }
        });
        
        this.findGroupMappingByServiceAndNameQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<AggregatedGroupMappingImpl>>() {
            @Override
            public CriteriaQuery<AggregatedGroupMappingImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<AggregatedGroupMappingImpl> criteriaQuery = cb.createQuery(AggregatedGroupMappingImpl.class);
                final Root<AggregatedGroupMappingImpl> root = criteriaQuery.from(AggregatedGroupMappingImpl.class);
                criteriaQuery.select(root);
                criteriaQuery.where(
                        cb.and(
                            cb.equal(root.get(AggregatedGroupMappingImpl_.groupService), groupServiceParameter),
                            cb.equal(root.get(AggregatedGroupMappingImpl_.groupName), groupNameParameter)
                        )
                    );
                
                return criteriaQuery;
            }
        });
    }
    
    @Override
    public AggregatedGroupMapping getGroupMapping(final String groupService, final String groupName) {
        final CacheKey key = CacheKey.build(this.getClass().getName(), groupService, groupName);
        
        AggregatedGroupMapping groupMapping = this.entityManagerCache.get(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key);
        if (groupMapping != null) {
            return groupMapping;
        }
        
        final TypedQuery<AggregatedGroupMappingImpl> query = this.createCachedQuery(this.findGroupMappingByServiceAndNameQuery);
        query.setParameter(this.groupServiceParameter, groupService);
        query.setParameter(this.groupNameParameter, groupName);
        
        final List<AggregatedGroupMappingImpl> resultList = query.getResultList();
        groupMapping = DataAccessUtils.uniqueResult(resultList);
        if (groupMapping != null) {
            this.entityManagerCache.put(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key, groupMapping);
            return groupMapping;
        }
        
        return this.getTransactionOperations().execute(new TransactionCallback<AggregatedGroupMapping>() {
            @Override
            public AggregatedGroupMapping doInTransaction(TransactionStatus status) {
                final AggregatedGroupMappingImpl aggregatedGroupMapping = new AggregatedGroupMappingImpl(groupService, groupName);
                getEntityManager().persist(aggregatedGroupMapping);
                
                logger.debug("Created {}", aggregatedGroupMapping);
                entityManagerCache.put(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME, key, aggregatedGroupMapping);
                
                return aggregatedGroupMapping;
            }
        });
    }

    @Override
    public AggregatedGroupMapping getGroupMapping(final String portalGroupKey) {
        final IEntityGroup group = compositeGroupService.findGroup(portalGroupKey);
        if (group == null) {
            logger.info("No group found for key {}, no aggregate group mapping will be done and the group key will be ignored.", portalGroupKey);
            return null;
        }
        
        final String groupService = group.getServiceName().toString();
        final String groupName = group.getName();
        
        return this.getGroupMapping(groupService, groupName);
    }

    @Override
    public Set<AggregatedGroupMapping> getGroupMappings() {
        final TypedQuery<AggregatedGroupMappingImpl> cachedQuery = this.createCachedQuery(this.findAllGroupMappingsQuery);
        
        return new LinkedHashSet<AggregatedGroupMapping>(cachedQuery.getResultList());
    }
    
}
