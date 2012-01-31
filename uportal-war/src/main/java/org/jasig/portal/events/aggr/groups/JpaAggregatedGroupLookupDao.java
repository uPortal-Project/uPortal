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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.jpa.BaseJpaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.base.Function;

/**
 * JPA dao to manage aggregated group mappings
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaAggregatedGroupLookupDao extends BaseJpaDao implements AggregatedGroupLookupDao {
    private CriteriaQuery<AggregatedGroupMappingImpl> findGroupMappingByServiceAndNameQuery;
    private ParameterExpression<String> groupServiceParameter;
    private ParameterExpression<String> groupNameParameter;
    
    private EntityManager entityManager;
    private TransactionOperations transactionOperations;
    private ICompositeGroupService compositeGroupService;
    
    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Autowired
    public void setTransactionOperations(@Qualifier("aggrEvents") TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    @Autowired
    public void setCompositeGroupService(ICompositeGroupService compositeGroupService) {
        this.compositeGroupService = compositeGroupService;
    }

    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.groupServiceParameter = this.createParameterExpression(String.class, "groupService");
        this.groupNameParameter = this.createParameterExpression(String.class, "groupName");
        
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
        final TypedQuery<AggregatedGroupMappingImpl> query = this.createCachedQuery(this.findGroupMappingByServiceAndNameQuery);
        query.setParameter(this.groupServiceParameter, groupService);
        query.setParameter(this.groupNameParameter, groupName);
        
        final List<AggregatedGroupMappingImpl> resultList = query.getResultList();
        if (!resultList.isEmpty()) { 
            return DataAccessUtils.uniqueResult(resultList);
        }
        
        return this.transactionOperations.execute(new TransactionCallback<AggregatedGroupMapping>() {
            @Override
            public AggregatedGroupMapping doInTransaction(TransactionStatus status) {
                final AggregatedGroupMappingImpl aggregatedGroupMapping = new AggregatedGroupMappingImpl(groupService, groupName);
                entityManager.persist(aggregatedGroupMapping);
                
                logger.debug("Created {}", aggregatedGroupMapping);
                
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
}
