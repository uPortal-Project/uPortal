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

package org.jasig.portal.pags.dao.jpa;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.jpa.OpenEntityManager;
import org.jasig.portal.pags.dao.IPersonAttributesGroupStoreDefinitionDao;
import org.jasig.portal.pags.om.IPersonAttributesGroupStoreDefinition;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
@Repository("personAttributesGroupStoreDefinitionDao")
public class JpaPersonAttributesGroupStoreDefinitionDao extends BasePortalJpaDao implements IPersonAttributesGroupStoreDefinitionDao {
    private CriteriaQuery<PersonAttributesGroupStoreDefinitionImpl> findAllDefinitions;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllDefinitions = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PersonAttributesGroupStoreDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PersonAttributesGroupStoreDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PersonAttributesGroupStoreDefinitionImpl> criteriaQuery = cb.createQuery(PersonAttributesGroupStoreDefinitionImpl.class);
                criteriaQuery.from(PersonAttributesGroupStoreDefinitionImpl.class);
                return criteriaQuery;
            }
        });
    }

    @PortalTransactional
    @Override
    public IPersonAttributesGroupStoreDefinition updatePersonAttributesGroupStoreDefinition(IPersonAttributesGroupStoreDefinition personAttributesGroupStoreDefinition) {
        Validate.notNull(personAttributesGroupStoreDefinition, "personAttributesGroupStoreDefinition can not be null");
        
        final IPersonAttributesGroupStoreDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(personAttributesGroupStoreDefinition)) {
            persistentDefinition = personAttributesGroupStoreDefinition;
        } else {
            persistentDefinition = entityManager.merge(personAttributesGroupStoreDefinition);
        }
        
        this.getEntityManager().persist(persistentDefinition);
        return persistentDefinition;
    }

    @PortalTransactional
    @Override
    public void deletePersonAttributesGroupStoreDefinition(IPersonAttributesGroupStoreDefinition definition) {
        Validate.notNull(definition, "definition can not be null");

        final IPersonAttributesGroupStoreDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(definition)) {
            persistentDefinition = definition;
        } else {
            persistentDefinition = entityManager.merge(definition);
        }
        entityManager.remove(persistentDefinition);
    }

    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    @Override
    public Set<IPersonAttributesGroupStoreDefinition> getPersonAttributesGroupStoreDefinitionByName(String name) {
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<PersonAttributesGroupStoreDefinitionImpl> criteriaQuery = 
                criteriaBuilder.createQuery(PersonAttributesGroupStoreDefinitionImpl.class);
        Root<PersonAttributesGroupStoreDefinitionImpl> root = criteriaQuery.from(PersonAttributesGroupStoreDefinitionImpl.class);
        ParameterExpression<String> nameParameter = criteriaBuilder.parameter(String.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), nameParameter));
        TypedQuery<PersonAttributesGroupStoreDefinitionImpl> query = this.getEntityManager().createQuery(criteriaQuery);
        query.setParameter(nameParameter, name);
        Set<IPersonAttributesGroupStoreDefinition> stores = new HashSet<IPersonAttributesGroupStoreDefinition>();
        for (IPersonAttributesGroupStoreDefinition store : query.getResultList()) {
            stores.add(store);
        }
        if(stores.size() > 1) {
            logger.error("More than one PAGS Store found for name: {}", name);
        }
        return stores;
    }

    @Override
    public Set<IPersonAttributesGroupStoreDefinition> getPersonAttributesGroupStoreDefinitions() {
        final TypedQuery<PersonAttributesGroupStoreDefinitionImpl> query = this.createCachedQuery(this.findAllDefinitions);
        Set<IPersonAttributesGroupStoreDefinition> stores = new HashSet<IPersonAttributesGroupStoreDefinition>();
        for (IPersonAttributesGroupStoreDefinition store : query.getResultList()) {
            stores.add(store);
        }
        if(stores.size() > 1) {
            logger.error("More than one PAGS Store found");
        }
        return stores;
    }

    @PortalTransactional
    @Override
    public IPersonAttributesGroupStoreDefinition createPersonAttributesGroupStoreDefinition(String name, String description) {
        final IPersonAttributesGroupStoreDefinition personAttributesGroupStoreDefinition = new PersonAttributesGroupStoreDefinitionImpl(name, description);
        this.getEntityManager().persist(personAttributesGroupStoreDefinition);
        return personAttributesGroupStoreDefinition;
    }

}
