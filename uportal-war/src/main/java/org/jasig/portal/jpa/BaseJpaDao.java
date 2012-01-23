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

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for JPA DAOs in the portal that contains common functions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseJpaDao implements InitializingBean {
    private static final String QUERY_AFFIX = ".query.";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected abstract EntityManager getEntityManager();
    
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
        final EntityManagerFactory entityManagerFactory = this.getEntityManager().getEntityManagerFactory();
        return entityManagerFactory.getCriteriaBuilder();
    }
    
    /**
     * Common logic for creating and configuring JPA queries
     * 
     * @param criteriaQuery The criteria to create the query from
     */
    protected final <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return this.createQuery(criteriaQuery, null);
    }

    /**
     * Common logic for creating and configuring JPA queries
     * 
     * @param criteriaQuery The criteria to create the query from
     * @param queryName A name for the query, used with the query root class name to generate the cache region name. For example
     *                  a query with root class "org.jasig.portal.events.aggr.session.EventSessionImpl" and queryName "FIND_BY_EVENT_SESSION_ID"
     *                  will result in a region named "org.jasig.portal.events.aggr.session.EventSessionImpl.query.FIND_BY_EVENT_SESSION_ID"
     */
    protected final <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery, String queryName) {
        final TypedQuery<T> query = this.getEntityManager().createQuery(criteriaQuery);
        if (queryName != null) {
            final String cacheRegion = getCacheRegionName(criteriaQuery, queryName);
            
            query.setHint("org.hibernate.cacheable", true);
            query.setHint("org.hibernate.cacheRegion", cacheRegion);
        }
        return query;
    }

    /**
     * Creates the cache region name for the named query
     * 
     * @param criteriaQuery
     * @param queryName
     * @return
     */
    protected <T> String getCacheRegionName(CriteriaQuery<T> criteriaQuery, String queryName) {
        final Set<Root<?>> roots = criteriaQuery.getRoots();
        final Class<?> cacheRegionType = roots.iterator().next().getJavaType();
        final String cacheRegion = cacheRegionType.getName() + QUERY_AFFIX + queryName;
        
        if (roots.size() > 1) {
            logger.warn("Query " + queryName + " has " + roots.size() + " roots. The first will be used to generated the cache region name: " + cacheRegion);
        }
        return cacheRegion;
    }
}