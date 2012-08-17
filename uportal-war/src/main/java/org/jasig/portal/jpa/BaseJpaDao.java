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
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;

import org.hibernate.LockOptions;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.base.Function;

/**
 * Base class for JPA DAOs in the portal that contains common functions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseJpaDao implements InitializingBean, ApplicationContextAware {
    private static final String QUERY_SUFFIX = ".Query";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private ApplicationContext applicationContext;
    
    protected abstract EntityManager getEntityManager();
    
    protected abstract TransactionOperations getTransactionOperations();
    
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
    
    protected final <T> ParameterExpression<T> createParameterExpression(Class<T> paramClass) {
        final EntityManager entityManager = this.getEntityManager();
        final EntityManagerFactory entityManagerFactory = entityManager.getEntityManagerFactory();
        final CriteriaBuilder criteriaBuilder = entityManagerFactory.getCriteriaBuilder();
        
        return criteriaBuilder.parameter(paramClass);
    }
    
    protected final <T> ParameterExpression<T> createParameterExpression(Class<T> paramClass, String name) {
        final EntityManager entityManager = this.getEntityManager();
        final EntityManagerFactory entityManagerFactory = entityManager.getEntityManagerFactory();
        final CriteriaBuilder criteriaBuilder = entityManagerFactory.getCriteriaBuilder();
        
        return criteriaBuilder.parameter(paramClass, name);
    }
    
    protected final <T> CriteriaQuery<T> createCriteriaQuery(Function<CriteriaBuilder, CriteriaQuery<T>> builder) {
        final EntityManager entityManager = this.getEntityManager();
        final EntityManagerFactory entityManagerFactory = entityManager.getEntityManagerFactory();
        final CriteriaBuilder criteriaBuilder = entityManagerFactory.getCriteriaBuilder();
        
        final CriteriaQuery<T> criteriaQuery = builder.apply(criteriaBuilder);
        
        //Do in TX so the EM gets closed correctly
        final TransactionOperations transactionOperations = this.getTransactionOperations();
        transactionOperations.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                entityManager.createQuery(criteriaQuery); //pre-compile critera query to avoid race conditions when setting aliases
            }
        });
        
        return criteriaQuery;
    }
    
    /**
     * Common logic for creating and configuring JPA queries
     * 
     * @param criteriaQuery The criteria to create the query from
     */
    protected final <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return this.getEntityManager().createQuery(criteriaQuery);
    }

    /**
     * Common logic for creating and configuring JPA queries
     * 
     * @param criteriaQuery The criteria to create the query from
     */
    protected final <T> TypedQuery<T> createCachedQuery(CriteriaQuery<T> criteriaQuery) {
        final TypedQuery<T> query = this.getEntityManager().createQuery(criteriaQuery);
        final String cacheRegion = getCacheRegionName(criteriaQuery);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", cacheRegion);
        return query;
    }
    
    /**
     * Utility for creating queries based on naturalId. The caller MUST be annotated with {@link OpenEntityManager} or
     * {@link Transactional} so that the Hibernate specific extensions are available.
     */
    protected final <T> NaturalIdQuery<T> createNaturalIdQuery(Class<T> entityType) {
        final EntityManager entityManager = this.getEntityManager();
        final Session session;
        try {
            session = entityManager.unwrap(Session.class);
        }
        catch (IllegalStateException e) {
            throw new IllegalStateException("The DAO Method that calls createNaturalIdQuery must be annotated with @OpenEntityManager or @Transactional", e);
        }
        final NaturalIdLoadAccess naturalIdLoadAccess = session.byNaturalId(entityType);
        return new NaturalIdQuery<T>(entityType, naturalIdLoadAccess);
    }

    /**
     * Creates the cache region name for the criteria query
     * 
     * @param criteriaQuery The criteria to create the cache name for
     */
    protected final <T> String getCacheRegionName(CriteriaQuery<T> criteriaQuery) {
        final Set<Root<?>> roots = criteriaQuery.getRoots();
        final Class<?> cacheRegionType = roots.iterator().next().getJavaType();
        final String cacheRegion = cacheRegionType.getName() + QUERY_SUFFIX;
        
        if (roots.size() > 1) {
            logger.warn("Query " + criteriaQuery + " in " + this.getClass() + " has " + roots.size() + " roots. The first will be used to generated the cache region name: " + cacheRegion);
        }
        return cacheRegion;
    }
    
    /**
     * Build a query for an entity using its naturalId 
     * @param <T> The entity type to return
     */
    public static final class NaturalIdQuery<T> {
        private final Class<T> type;
        private final NaturalIdLoadAccess naturalIdLoadAccess;

        public NaturalIdQuery(Class<T> type, NaturalIdLoadAccess naturalIdLoadAccess) {
            this.type = type;
            this.naturalIdLoadAccess = naturalIdLoadAccess;
        }

        /**
         * @see NaturalIdLoadAccess#with(LockOptions)
         */
        public NaturalIdQuery<T> with(LockOptions lockOptions) {
            naturalIdLoadAccess.with(lockOptions);
            return this;
        }

        /**
         * Set a naturalId parameter using the JPA2 MetaModel API
         * @see NaturalIdLoadAccess#using(String, Object)
         */
        public <P> NaturalIdQuery<T> using(Attribute<? super T, P> attribute, P value) {
            naturalIdLoadAccess.using(attribute.getName(), value);
            return this;
        }

        /**
         * @see NaturalIdLoadAccess#getReference()
         */
        public T getReference() {
            return type.cast(naturalIdLoadAccess.getReference());
        }

        /**
         * @see NaturalIdLoadAccess#load()
         */
        public T load() {
            return type.cast(naturalIdLoadAccess.load());
        }
    }
}