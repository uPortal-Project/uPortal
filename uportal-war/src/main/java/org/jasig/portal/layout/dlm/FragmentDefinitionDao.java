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

package org.jasig.portal.layout.dlm;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BaseJpaDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

@Repository
public class FragmentDefinitionDao extends BaseJpaDao implements IFragmentDefinitionDao {
    private CriteriaQuery<FragmentDefinition> findAllFragmentsQuery;
    private CriteriaQuery<FragmentDefinition> findFragmentByNameQuery;
    private CriteriaQuery<FragmentDefinition> findFragmentByOwnerQuery;
    private ParameterExpression<String> nameParameter;
    private ParameterExpression<String> ownerParameter;
    private EntityManager entityManager;

    @PersistenceContext(unitName = "uPortalPersistence")
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.nameParameter = this.createParameterExpression(String.class, "name");
        this.ownerParameter = this.createParameterExpression(String.class, "ownerId");
        
        this.findAllFragmentsQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<FragmentDefinition>>() {
            @Override
            public CriteriaQuery<FragmentDefinition> apply(CriteriaBuilder cb) {
                final CriteriaQuery<FragmentDefinition> criteriaQuery = cb.createQuery(FragmentDefinition.class);
                criteriaQuery.from(FragmentDefinition.class);
                return criteriaQuery;
            }
        });
        
        this.findFragmentByNameQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<FragmentDefinition>>() {
            @Override
            public CriteriaQuery<FragmentDefinition> apply(CriteriaBuilder cb) {
                final CriteriaQuery<FragmentDefinition> criteriaQuery = cb.createQuery(FragmentDefinition.class);
                final Root<FragmentDefinition> fragDefRoot = criteriaQuery.from(FragmentDefinition.class);
                criteriaQuery.select(fragDefRoot);
                criteriaQuery.where(
                    cb.equal(fragDefRoot.get(FragmentDefinition_.name), nameParameter)
                );
                
                return criteriaQuery;
            }
        });

        this.findFragmentByOwnerQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<FragmentDefinition>>() {
            @Override
            public CriteriaQuery<FragmentDefinition> apply(CriteriaBuilder cb) {
                final CriteriaQuery<FragmentDefinition> criteriaQuery = cb.createQuery(FragmentDefinition.class);
                final Root<FragmentDefinition> fragDefRoot = criteriaQuery.from(FragmentDefinition.class);
                criteriaQuery.select(fragDefRoot);
                criteriaQuery.where(
                    cb.equal(fragDefRoot.get(FragmentDefinition_.ownerID), ownerParameter)
                );
                
                return criteriaQuery;
            }
        });
    }

    @Override
    public List<FragmentDefinition> getAllFragments() {
        final TypedQuery<FragmentDefinition> query = this.createCachedQuery(this.findAllFragmentsQuery);
        final List<FragmentDefinition> rslt = query.getResultList();
        return rslt;
        
    }

    @Override
    public FragmentDefinition getFragmentDefinition(String name) {
        final TypedQuery<FragmentDefinition> query = this.createCachedQuery(this.findFragmentByNameQuery);
        query.setParameter(this.nameParameter, name);
        
        final List<FragmentDefinition> list = query.getResultList();
        final FragmentDefinition rslt = DataAccessUtils.uniqueResult(list);
        return rslt;
        
    }

    @Override
    public FragmentDefinition getFragmentDefinitionByOwner(String ownerId) {
        final TypedQuery<FragmentDefinition> query = this.createCachedQuery(this.findFragmentByOwnerQuery);
        query.setParameter(this.ownerParameter, ownerId);
        
        final List<FragmentDefinition> list = query.getResultList();
        final FragmentDefinition rslt = DataAccessUtils.uniqueResult(list);
        return rslt;
    }

    @Override
    @Transactional
    public void updateFragmentDefinition(FragmentDefinition fd) {
        
        Validate.notNull(fd, "FragmentDefinition can not be null");
        this.entityManager.persist(fd);
        
    }

    @Override
    @Transactional
    public void removeFragmentDefinition(FragmentDefinition fd) {
        
        Validate.notNull(fd, "FragmentDefinition can not be null");
        final FragmentDefinition persistentFragmentDefinition;
        if (this.entityManager.contains(fd)) {
            persistentFragmentDefinition = fd;
        }
        else {
            persistentFragmentDefinition = this.entityManager.merge(fd);
        }
        
        this.entityManager.remove(persistentFragmentDefinition);
    }

}
