/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.groups.pags.dao.jpa;

import com.google.common.base.Function;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.Validate;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinitionDao;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository("personAttributesGroupDefinitionDao")
public class JpaPersonAttributesGroupDefinitionDao extends BasePortalJpaDao
        implements IPersonAttributesGroupDefinitionDao {

    private CriteriaQuery<PersonAttributesGroupDefinitionImpl> findAllDefinitionsQuery;
    private CriteriaQuery<PersonAttributesGroupDefinitionImpl> groupDefinitionByNameQuery;
    private CriteriaQuery<PersonAttributesGroupDefinitionImpl> parentGroupDefinitionsQuery;
    private ParameterExpression<String> nameParameter;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void afterPropertiesSet() throws Exception {
        this.nameParameter = this.createParameterExpression(String.class, "name");

        this.findAllDefinitionsQuery =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder,
                                CriteriaQuery<PersonAttributesGroupDefinitionImpl>>() {
                            @Override
                            public CriteriaQuery<PersonAttributesGroupDefinitionImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<PersonAttributesGroupDefinitionImpl>
                                        criteriaQuery =
                                                cb.createQuery(
                                                        PersonAttributesGroupDefinitionImpl.class);
                                criteriaQuery.from(PersonAttributesGroupDefinitionImpl.class);
                                return criteriaQuery;
                            }
                        });

        this.groupDefinitionByNameQuery =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder,
                                CriteriaQuery<PersonAttributesGroupDefinitionImpl>>() {
                            @Override
                            public CriteriaQuery<PersonAttributesGroupDefinitionImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<PersonAttributesGroupDefinitionImpl>
                                        criteriaQuery =
                                                cb.createQuery(
                                                        PersonAttributesGroupDefinitionImpl.class);
                                Root<PersonAttributesGroupDefinitionImpl> root =
                                        criteriaQuery.from(
                                                PersonAttributesGroupDefinitionImpl.class);
                                criteriaQuery
                                        .select(root)
                                        .where(cb.equal(root.get("name"), nameParameter));
                                return criteriaQuery;
                            }
                        });

        this.parentGroupDefinitionsQuery =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder,
                                CriteriaQuery<PersonAttributesGroupDefinitionImpl>>() {
                            @Override
                            public CriteriaQuery<PersonAttributesGroupDefinitionImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<PersonAttributesGroupDefinitionImpl>
                                        criteriaQuery =
                                                cb.createQuery(
                                                        PersonAttributesGroupDefinitionImpl.class);
                                Root<PersonAttributesGroupDefinitionImpl> root =
                                        criteriaQuery.from(
                                                PersonAttributesGroupDefinitionImpl.class);
                                Join<
                                                PersonAttributesGroupDefinitionImpl,
                                                PersonAttributesGroupDefinitionImpl>
                                        members =
                                                root.join(
                                                        PersonAttributesGroupDefinitionImpl_
                                                                .members);
                                criteriaQuery.where(
                                        cb.equal(
                                                members.get(
                                                        PersonAttributesGroupDefinitionImpl_.name),
                                                nameParameter));
                                return criteriaQuery;
                            }
                        });
    }

    @PortalTransactional
    @Override
    public IPersonAttributesGroupDefinition updatePersonAttributesGroupDefinition(
            IPersonAttributesGroupDefinition personAttributesGroupDefinition) {
        Validate.notNull(
                personAttributesGroupDefinition, "personAttributesGroupDefinition can not be null");

        final IPersonAttributesGroupDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(personAttributesGroupDefinition)) {
            persistentDefinition = personAttributesGroupDefinition;
        } else {
            persistentDefinition = entityManager.merge(personAttributesGroupDefinition);
        }

        this.getEntityManager().persist(persistentDefinition);
        return persistentDefinition;
    }

    @PortalTransactional
    @Override
    public void deletePersonAttributesGroupDefinition(IPersonAttributesGroupDefinition definition) {
        Validate.notNull(definition, "definition can not be null");

        final IPersonAttributesGroupDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(definition)) {
            persistentDefinition = definition;
        } else {
            persistentDefinition = entityManager.merge(definition);
        }
        entityManager.remove(persistentDefinition);
    }

    @PortalTransactionalReadOnly
    @Override
    public Set<IPersonAttributesGroupDefinition> getPersonAttributesGroupDefinitionByName(
            String name) {
        TypedQuery<PersonAttributesGroupDefinitionImpl> query =
                this.createCachedQuery(groupDefinitionByNameQuery);
        query.setParameter(nameParameter, name);
        Set<IPersonAttributesGroupDefinition> groups =
                new HashSet<IPersonAttributesGroupDefinition>(query.getResultList());
        if (groups.size() > 1) {
            logger.error("More than one PAGS Group found for name: {}", name);
        }
        return groups;
    }

    @PortalTransactionalReadOnly
    @Override
    public Set<IPersonAttributesGroupDefinition> getParentPersonAttributesGroupDefinitions(
            IPersonAttributesGroupDefinition group) {
        TypedQuery<PersonAttributesGroupDefinitionImpl> query =
                this.createCachedQuery(parentGroupDefinitionsQuery);
        query.setParameter(nameParameter, group.getName());
        Set<IPersonAttributesGroupDefinition> result =
                new HashSet<IPersonAttributesGroupDefinition>(query.getResultList());
        return result;
    }

    @PortalTransactionalReadOnly
    @Override
    public Set<IPersonAttributesGroupDefinition> getPersonAttributesGroupDefinitions() {
        final TypedQuery<PersonAttributesGroupDefinitionImpl> query =
                this.createCachedQuery(this.findAllDefinitionsQuery);
        Set<IPersonAttributesGroupDefinition> groups =
                new HashSet<IPersonAttributesGroupDefinition>(query.getResultList());
        return groups;
    }

    @PortalTransactional
    @Override
    public IPersonAttributesGroupDefinition createPersonAttributesGroupDefinition(
            String name, String description) {
        final IPersonAttributesGroupDefinition personAttributesGroupDefinition =
                new PersonAttributesGroupDefinitionImpl(name, description);

        this.getEntityManager().persist(personAttributesGroupDefinition);
        return personAttributesGroupDefinition;
    }
}
