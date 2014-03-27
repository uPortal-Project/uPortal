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
import org.jasig.portal.pags.dao.IPersonAttributesGroupTestDefinitionDao;
import org.jasig.portal.pags.om.IPersonAttributesGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupTestGroupDefinition;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
@Repository("personAttributesGroupTestDefinitionDao")
public class JpaPersonAttributesGroupTestDefinitionDao extends BasePortalJpaDao implements IPersonAttributesGroupTestDefinitionDao {
    private CriteriaQuery<PersonAttributesGroupTestDefinitionImpl> findAllDefinitions;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllDefinitions = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PersonAttributesGroupTestDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PersonAttributesGroupTestDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PersonAttributesGroupTestDefinitionImpl> criteriaQuery = cb.createQuery(PersonAttributesGroupTestDefinitionImpl.class);
                criteriaQuery.from(PersonAttributesGroupTestDefinitionImpl.class);
                return criteriaQuery;
            }
        });
    }

    @PortalTransactional
    @Override
    public IPersonAttributesGroupTestDefinition updatePersonAttributesGroupTestDefinition(IPersonAttributesGroupTestDefinition personAttributesGroupTestDefinition) {
        Validate.notNull(personAttributesGroupTestDefinition, "personAttributesGroupTestDefinition can not be null");
        
        final IPersonAttributesGroupTestDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(personAttributesGroupTestDefinition)) {
            persistentDefinition = personAttributesGroupTestDefinition;
        } else {
            persistentDefinition = entityManager.merge(personAttributesGroupTestDefinition);
        }
        
        this.getEntityManager().persist(persistentDefinition);
        return persistentDefinition;
    }

    @PortalTransactional
    @Override
    public void deletePersonAttributesGroupTestDefinition(IPersonAttributesGroupTestDefinition definition) {
        Validate.notNull(definition, "definition can not be null");
        
        final IPersonAttributesGroupTestDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(definition)) {
            persistentDefinition = definition;
        } else {
            persistentDefinition = entityManager.merge(definition);
        }
        entityManager.remove(persistentDefinition);
    }

    @Override
    public List<IPersonAttributesGroupTestDefinition> getPersonAttributesGroupTestDefinitions() {
        final TypedQuery<PersonAttributesGroupTestDefinitionImpl> query = this.createCachedQuery(this.findAllDefinitions);
        List<IPersonAttributesGroupTestDefinition> tests = new ArrayList<IPersonAttributesGroupTestDefinition>();
        for(IPersonAttributesGroupTestDefinition test : query.getResultList()) {
            tests.add(test);
        }
        return tests;
    }
    
    @PortalTransactional
    @Override
    public IPersonAttributesGroupTestDefinition createPersonAttributesGroupTestDefinition(IPersonAttributesGroupTestGroupDefinition testGroup, String name, String description, String attributeName, String testerClass, String testValue) {
        final IPersonAttributesGroupTestDefinition personAttributesGroupTestDefinition = new PersonAttributesGroupTestDefinitionImpl((PersonAttributesGroupTestGroupDefinitionImpl)testGroup, name, description, attributeName, testerClass, testValue);
        this.getEntityManager().persist(personAttributesGroupTestDefinition);
        return personAttributesGroupTestDefinition;
    }

    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    @Override
    public List<IPersonAttributesGroupTestDefinition> getPersonAttributesGroupTestDefinitionByName(String name) {
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<PersonAttributesGroupTestDefinitionImpl> criteriaQuery = 
                criteriaBuilder.createQuery(PersonAttributesGroupTestDefinitionImpl.class);
        Root<PersonAttributesGroupTestDefinitionImpl> root = criteriaQuery.from(PersonAttributesGroupTestDefinitionImpl.class);
        ParameterExpression<String> nameParameter = criteriaBuilder.parameter(String.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), nameParameter));
        TypedQuery<PersonAttributesGroupTestDefinitionImpl> query = this.getEntityManager().createQuery(criteriaQuery);
        query.setParameter(nameParameter, name);
        List<IPersonAttributesGroupTestDefinition> testGroups = new ArrayList<IPersonAttributesGroupTestDefinition>();
        for (IPersonAttributesGroupTestDefinition testGroup : query.getResultList()) {
            testGroups.add(testGroup);
        }
        return testGroups;
    }
}
