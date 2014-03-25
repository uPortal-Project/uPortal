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

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.pags.dao.IPersonAttributeGroupTestDefinitionDao;
import org.jasig.portal.pags.om.IPersonAttributeGroupTestDefinition;
import org.jasig.portal.pags.om.IPersonAttributeGroupTestGroupDefinition;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
@Repository("personAttributeGroupTestDefinitionDao")
public class JpaPersonAttributeGroupTestDefinitionDao extends BasePortalJpaDao implements IPersonAttributeGroupTestDefinitionDao {
    private CriteriaQuery<PersonAttributeGroupTestDefinitionImpl> findAllDefinitions;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllDefinitions = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PersonAttributeGroupTestDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PersonAttributeGroupTestDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PersonAttributeGroupTestDefinitionImpl> criteriaQuery = cb.createQuery(PersonAttributeGroupTestDefinitionImpl.class);
                criteriaQuery.from(PersonAttributeGroupTestDefinitionImpl.class);
                return criteriaQuery;
            }
        });
    }

    @PortalTransactional
    @Override
    public IPersonAttributeGroupTestDefinition updatePersonAttributeGroupTestDefinition(IPersonAttributeGroupTestDefinition personAttributeGroupTestDefinition) {
        Validate.notNull(personAttributeGroupTestDefinition, "personAttributeGroupTestDefinition can not be null");
        
        this.getEntityManager().persist(personAttributeGroupTestDefinition);
        return personAttributeGroupTestDefinition;
    }

    @PortalTransactional
    @Override
    public void deletePersonAttributeGroupTestDefinition(IPersonAttributeGroupTestDefinition definition) {
        Validate.notNull(definition, "definition can not be null");
        
        final IPersonAttributeGroupTestDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(definition)) {
            persistentDefinition = definition;
        } else {
            persistentDefinition = entityManager.merge(definition);
        }
        entityManager.remove(persistentDefinition);
    }

    @Override
    public List<IPersonAttributeGroupTestDefinition> getPersonAttributeGroupTestDefinitions() {
        final TypedQuery<PersonAttributeGroupTestDefinitionImpl> query = this.createCachedQuery(this.findAllDefinitions);
        List<IPersonAttributeGroupTestDefinition> tests = new ArrayList<IPersonAttributeGroupTestDefinition>();
        for(IPersonAttributeGroupTestDefinition test : query.getResultList()) {
            tests.add(test);
        }
        return tests;
    }
    
    @PortalTransactional
    @Override
    public IPersonAttributeGroupTestDefinition createPersonAttributeGroupTestDefinition(IPersonAttributeGroupTestGroupDefinition testGroup, String attributeName, String testerClass, String testValue) {
        final IPersonAttributeGroupTestDefinition personAttributeGroupTestDefinition = new PersonAttributeGroupTestDefinitionImpl((PersonAttributeGroupTestGroupDefinitionImpl)testGroup, attributeName, testerClass, testValue);
        this.getEntityManager().persist(personAttributeGroupTestDefinition);
        return personAttributeGroupTestDefinition;
    }

}
