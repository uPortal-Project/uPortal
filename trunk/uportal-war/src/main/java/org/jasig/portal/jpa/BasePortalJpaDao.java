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

package org.jasig.portal.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for JPA DAOs in the portal that contains common functions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BasePortalJpaDao implements InitializingBean {
    protected EntityManager entityManager;

    @PersistenceContext(unitName = "uPortalPersistence")
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    public final void afterPropertiesSet() throws Exception {
        final CriteriaBuilder criteriaBuilder = this.getCriteriaBuilder();
        this.buildCriteriaQueries(criteriaBuilder);
    }

    /**
     * Subclasses can implement this method to generate {@link CriteriaQuery} objects.
     * Called by {@link #afterPropertiesSet()}
     */
    protected void buildCriteriaQueries(CriteriaBuilder criteriaBuilder) {
    }


    /**
     * @return Get the {@link CriteriaBuilder} to use to generate {@link CriteriaQuery}
     */
    protected final CriteriaBuilder getCriteriaBuilder() {
        final EntityManagerFactory entityManagerFactory = this.entityManager.getEntityManagerFactory();
        return entityManagerFactory.getCriteriaBuilder();
    }

    /**
     * Common logic for creating and configuring JPA queries
     * @param criteriaQuery The criteria to create the query from
     * @param cacheRegion The hibernate query cache region to use for the query
     */
    protected final <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery, String cacheRegion) {
        final TypedQuery<T> query = this.entityManager.createQuery(criteriaQuery);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", cacheRegion);
        return query;
    }
}