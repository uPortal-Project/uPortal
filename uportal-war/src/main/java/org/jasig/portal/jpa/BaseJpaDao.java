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

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Attribute;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jasig.portal.utils.PrimitiveUtils;
import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for JPA DAOs in the portal that contains common functions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseJpaDao implements InitializingBean {
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
     * @param criteriaQuery The criteria to create the query from
     * @param cacheRegion The hibernate query cache region to use for the query
     */
    protected final <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery, String cacheRegion) {
        final TypedQuery<T> query = this.getEntityManager().createQuery(criteriaQuery);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", cacheRegion);
        return query;
    }
    
    protected final <T> T executeNaturalIdQuery(Class<T> type, Map<? extends Attribute<T, ?>, ?> params, String cacheRegion) {
        final Session session = getEntityManager().unwrap(Session.class);
        
        final NaturalIdentifier naturalIdRestriction = Restrictions.naturalId();
        for (Map.Entry<? extends Attribute<T, ?>, ?> paramEntry : params.entrySet()) {
            final Attribute<T, ?> attribute = paramEntry.getKey();
            final Class<?> paramType = attribute.getJavaType();
            final Object value = paramEntry.getValue();
            naturalIdRestriction.set(attribute.getName(), paramType.cast(value));
        }
        
        final Criteria criteria = session.createCriteria(type);
        criteria.add(naturalIdRestriction)
            .setCacheable(true)
            .setCacheRegion(cacheRegion);
        
        return type.cast(criteria.uniqueResult()); 
    }
    
    public final <T> NaturalIdQueryBuilder<T> createNaturalIdQuery(Class<T> entityType, String cacheRegion) {
        final Session session = getEntityManager().unwrap(Session.class);
        return new NaturalIdQueryBuilder<T>(session, entityType, cacheRegion);
    }

    public static class NaturalIdQueryBuilder<T> {
        private final Class<T> entityType;
        private final Criteria criteria;
        private final NaturalIdentifier naturalIdRestriction;
        
        private NaturalIdQueryBuilder(Session session, Class<T> entityType, String cacheRegion) {
            this.entityType = entityType;
            this.criteria = session.createCriteria(this.entityType);
            this.criteria.setCacheable(true);
            this.criteria.setCacheRegion(cacheRegion);
            
            this.naturalIdRestriction = Restrictions.naturalId();
        }
        
        public <V> NaturalIdQueryBuilder<T> setNaturalIdParam(Attribute<T, V> attribute, V value) {
            final Class<V> valueType = PrimitiveUtils.toReferenceClass(attribute.getJavaType());
            this.naturalIdRestriction.set(attribute.getName(), valueType.cast(value));
            return this;
        }
        
        public T execute() {
            this.criteria.add(this.naturalIdRestriction);
            return this.entityType.cast(criteria.uniqueResult());
        }
    }
}