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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.jpa.OpenEntityManager;
import org.jasig.portal.pags.dao.IPersonAttributeGroupStoreDefinitionDao;
import org.jasig.portal.pags.om.IPersonAttributeGroupStoreDefinition;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
@Repository("personAttributeGroupStoreDefinitionDao")
public class JpaPersonAttributeGroupStoreDefinitionDao extends BasePortalJpaDao implements IPersonAttributeGroupStoreDefinitionDao {
    private CriteriaQuery<PersonAttributeGroupStoreDefinitionImpl> findAllDefinitions;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllDefinitions = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PersonAttributeGroupStoreDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PersonAttributeGroupStoreDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PersonAttributeGroupStoreDefinitionImpl> criteriaQuery = cb.createQuery(PersonAttributeGroupStoreDefinitionImpl.class);
                criteriaQuery.from(PersonAttributeGroupStoreDefinitionImpl.class);
                return criteriaQuery;
            }
        });
    }

    @PortalTransactional
    @Override
    public IPersonAttributeGroupStoreDefinition updatePersonAttributeGroupStoreDefinition(IPersonAttributeGroupStoreDefinition personAttributeGroupStoreDefinition) {
        Validate.notNull(personAttributeGroupStoreDefinition, "personAttributeGroupStoreDefinition can not be null");
        
        this.getEntityManager().persist(personAttributeGroupStoreDefinition);
        return personAttributeGroupStoreDefinition;
    }

    @PortalTransactional
    @Override
    public void deletePersonAttributeGroupStoreDefinition(IPersonAttributeGroupStoreDefinition definition) {
        Validate.notNull(definition, "definition can not be null");

        final IPersonAttributeGroupStoreDefinition persistentDefinition;
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
    public List<PersonAttributeGroupStoreDefinitionImpl> getPersonAttributeGroupStoreDefinitionByName(String name) {
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<PersonAttributeGroupStoreDefinitionImpl> criteriaQuery = 
                criteriaBuilder.createQuery(PersonAttributeGroupStoreDefinitionImpl.class);
        Root<PersonAttributeGroupStoreDefinitionImpl> root = criteriaQuery.from(PersonAttributeGroupStoreDefinitionImpl.class);
        ParameterExpression<String> nameParameter = criteriaBuilder.parameter(String.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), nameParameter));
        TypedQuery<PersonAttributeGroupStoreDefinitionImpl> query = this.getEntityManager().createQuery(criteriaQuery);
        query.setParameter(nameParameter, name);
        return query.getResultList();
    }

    @Override
    public List<PersonAttributeGroupStoreDefinitionImpl> getPersonAttributeGroupStoreDefinitions() {
        final TypedQuery<PersonAttributeGroupStoreDefinitionImpl> query = this.createCachedQuery(this.findAllDefinitions);
        return query.getResultList();
    }

    @PortalTransactional
    @Override
    public IPersonAttributeGroupStoreDefinition createPersonAttributeGroupStoreDefinition(String name, String description) {
        final IPersonAttributeGroupStoreDefinition personAttributeGroupStoreDefinition = new PersonAttributeGroupStoreDefinitionImpl(name, description);
        this.getEntityManager().persist(personAttributeGroupStoreDefinition);
        return personAttributeGroupStoreDefinition;
    }

}
