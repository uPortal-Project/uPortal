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

import java.util.ArrayList;
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
import org.jasig.portal.pags.dao.IPersonAttributeGroupTestGroupDefinitionDao;
import org.jasig.portal.pags.om.IPersonAttributeGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributeGroupTestGroupDefinition;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
@Repository("personAttributeGroupTestGroupDefinitionDao")
public class JpaPersonAttributeGroupTestGroupDefinitionDao extends BasePortalJpaDao implements IPersonAttributeGroupTestGroupDefinitionDao {
    private CriteriaQuery<PersonAttributeGroupTestGroupDefinitionImpl> findAllDefinitions;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllDefinitions = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PersonAttributeGroupTestGroupDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PersonAttributeGroupTestGroupDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PersonAttributeGroupTestGroupDefinitionImpl> criteriaQuery = cb.createQuery(PersonAttributeGroupTestGroupDefinitionImpl.class);
                criteriaQuery.from(PersonAttributeGroupTestGroupDefinitionImpl.class);
                return criteriaQuery;
            }
        });
    }

    @PortalTransactional
    @Override
    public IPersonAttributeGroupTestGroupDefinition updatePersonAttributeGroupTestGroupDefinition(IPersonAttributeGroupTestGroupDefinition personAttributeGroupTestGroupDefinition) {
        Validate.notNull(personAttributeGroupTestGroupDefinition, "personAttributeGroupTestGroupDefinition can not be null");
        
        this.getEntityManager().persist(personAttributeGroupTestGroupDefinition);
        return personAttributeGroupTestGroupDefinition;
    }

    @PortalTransactional
    @Override
    public void deletePersonAttributeGroupTestGroupDefinition(IPersonAttributeGroupTestGroupDefinition definition) {
        Validate.notNull(definition, "definition can not be null");
        
        final IPersonAttributeGroupTestGroupDefinition persistentDefinition;
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
    public List<IPersonAttributeGroupTestGroupDefinition> getPersonAttributeGroupTestGroupDefinitionByName(String name) {
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<PersonAttributeGroupTestGroupDefinitionImpl> criteriaQuery = 
                criteriaBuilder.createQuery(PersonAttributeGroupTestGroupDefinitionImpl.class);
        Root<PersonAttributeGroupTestGroupDefinitionImpl> root = criteriaQuery.from(PersonAttributeGroupTestGroupDefinitionImpl.class);
        ParameterExpression<String> nameParameter = criteriaBuilder.parameter(String.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), nameParameter));
        TypedQuery<PersonAttributeGroupTestGroupDefinitionImpl> query = this.getEntityManager().createQuery(criteriaQuery);
        query.setParameter(nameParameter, name);
        List<IPersonAttributeGroupTestGroupDefinition> testGroups = new ArrayList<IPersonAttributeGroupTestGroupDefinition>();
        for (IPersonAttributeGroupTestGroupDefinition testGroup : query.getResultList()) {
            testGroups.add(testGroup);
        }
        return testGroups;
    }

    @Override
    public List<IPersonAttributeGroupTestGroupDefinition> getPersonAttributeGroupTestGroupDefinitions() {
        final TypedQuery<PersonAttributeGroupTestGroupDefinitionImpl> query = this.createCachedQuery(this.findAllDefinitions);
        List<IPersonAttributeGroupTestGroupDefinition> testGroups = new ArrayList<IPersonAttributeGroupTestGroupDefinition>();
        for (IPersonAttributeGroupTestGroupDefinition testGroup : query.getResultList()) {
            testGroups.add(testGroup);
        }
        return testGroups;
    }
    
    @PortalTransactional
    @Override
    public IPersonAttributeGroupTestGroupDefinition createPersonAttributeGroupTestGroupDefinition(IPersonAttributeGroupDefinition group, String name, String description) {
        final IPersonAttributeGroupTestGroupDefinition personAttributeGroupTestGroupDefinition = new PersonAttributeGroupTestGroupDefinitionImpl((PersonAttributeGroupDefinitionImpl)group, name, description);
        this.getEntityManager().persist(personAttributeGroupTestGroupDefinition);
        return personAttributeGroupTestGroupDefinition;
    }

}
