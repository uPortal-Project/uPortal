/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.groups.pags.dao.jpa;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinitionDao;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;
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
    public Set<IPersonAttributesGroupTestDefinition> getPersonAttributesGroupTestDefinitions() {
        final TypedQuery<PersonAttributesGroupTestDefinitionImpl> query = this.createCachedQuery(this.findAllDefinitions);
        Set<IPersonAttributesGroupTestDefinition> tests = new HashSet<IPersonAttributesGroupTestDefinition>();
        for(IPersonAttributesGroupTestDefinition test : query.getResultList()) {
            tests.add(test);
        }
        return tests;
    }
    
    @PortalTransactional
    @Override
    public IPersonAttributesGroupTestDefinition createPersonAttributesGroupTestDefinition(IPersonAttributesGroupTestGroupDefinition testGroup, String attributeName, String testerClass, String testValue) {
        final IPersonAttributesGroupTestDefinition personAttributesGroupTestDefinition = new PersonAttributesGroupTestDefinitionImpl((PersonAttributesGroupTestGroupDefinitionImpl)testGroup, attributeName, testerClass, testValue);
        this.getEntityManager().persist(personAttributesGroupTestDefinition);
        return personAttributesGroupTestDefinition;
    }
}
