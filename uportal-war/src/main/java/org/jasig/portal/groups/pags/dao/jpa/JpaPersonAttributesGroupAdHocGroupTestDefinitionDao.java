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

import com.google.common.base.Function;
import org.apache.commons.lang.Validate;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupAdHocGroupTestDefinition;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupAdHocGroupTestDefinitionDao;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Benito J. Gonzalez <bgonzalez@unicon.net>
 */
@Repository("personAttributesGroupAdHocGroupTestDefinitionDao")
public class JpaPersonAttributesGroupAdHocGroupTestDefinitionDao extends BasePortalJpaDao
        implements IPersonAttributesGroupAdHocGroupTestDefinitionDao {
    private CriteriaQuery<PersonAttributesGroupAdHocGroupTestDefinitionImpl> findAllDefinitions;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllDefinitions = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PersonAttributesGroupAdHocGroupTestDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PersonAttributesGroupAdHocGroupTestDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PersonAttributesGroupAdHocGroupTestDefinitionImpl> criteriaQuery
                        = cb.createQuery(PersonAttributesGroupAdHocGroupTestDefinitionImpl.class);
                criteriaQuery.from(PersonAttributesGroupAdHocGroupTestDefinitionImpl.class);
                return criteriaQuery;
            }
        });
    }

    @PortalTransactional
    @Override
    public IPersonAttributesGroupAdHocGroupTestDefinition updatePersonAttributesGroupAdHocGroupTestDefinition(IPersonAttributesGroupAdHocGroupTestDefinition definition) {
        Validate.notNull(definition, "definition can not be null");
        
        final IPersonAttributesGroupAdHocGroupTestDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(definition)) {
            persistentDefinition = definition;
        } else {
            persistentDefinition = entityManager.merge(definition);
        }
        
        this.getEntityManager().persist(persistentDefinition);
        return persistentDefinition;
    }

    @PortalTransactional
    @Override
    public void deletePersonAttributesGroupAdHocGroupTestDefinition(IPersonAttributesGroupAdHocGroupTestDefinition definition) {
        Validate.notNull(definition, "definition can not be null");
        
        final IPersonAttributesGroupAdHocGroupTestDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(definition)) {
            persistentDefinition = definition;
        } else {
            persistentDefinition = entityManager.merge(definition);
        }
        entityManager.remove(persistentDefinition);
    }

    @Override
    public Set<IPersonAttributesGroupAdHocGroupTestDefinition> getPersonAttributesGroupAdHocGroupTestDefinitions() {
        final TypedQuery<PersonAttributesGroupAdHocGroupTestDefinitionImpl> query = this.createCachedQuery(this.findAllDefinitions);
        Set<IPersonAttributesGroupAdHocGroupTestDefinition> adhocGroupTests = new HashSet<>();
        for(IPersonAttributesGroupAdHocGroupTestDefinition test : query.getResultList()) {
            adhocGroupTests.add(test);
        }
        return adhocGroupTests;
    }
    
    @PortalTransactional
    @Override
    public IPersonAttributesGroupAdHocGroupTestDefinition createPersonAttributesGroupAdHocGroupTestDefinition(
            IPersonAttributesGroupTestGroupDefinition testGroup, String groupName, Boolean isExclude) {
        Validate.notNull(testGroup, "testGroup can not be null");
        Validate.notEmpty(groupName, "groupName can not be empty");
        Validate.notNull(isExclude, "isExclude can not be null");
        final IPersonAttributesGroupAdHocGroupTestDefinition personAttributesGroupAdHocGroupTestDefinition
                = new PersonAttributesGroupAdHocGroupTestDefinitionImpl(testGroup, groupName, isExclude);
        this.getEntityManager().persist(personAttributesGroupAdHocGroupTestDefinition);
        return personAttributesGroupAdHocGroupTestDefinition;
    }
}
