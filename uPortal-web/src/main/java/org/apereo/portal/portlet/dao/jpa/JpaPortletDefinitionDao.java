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
package org.apereo.portal.portlet.dao.jpa;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.Validate;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.jpa.OpenEntityManager;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.spring.tx.DialectAwareTransactional;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

/** JPA implementation of the portlet definition DAO */
@Repository
public class JpaPortletDefinitionDao extends BasePortalJpaDao implements IPortletDefinitionDao {

    private CriteriaQuery<PortletDefinitionImpl> findAllPortletDefinitions;
    private CriteriaQuery<PortletDefinitionImpl> findDefinitionByNameQuery;
    private CriteriaQuery<PortletDefinitionImpl> findDefinitionByNameOrTitleQuery;
    private CriteriaQuery<PortletDefinitionImpl> searchDefinitionByNameOrTitleQuery;
    private ParameterExpression<String> nameParameter;
    private ParameterExpression<String> titleParameter;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void afterPropertiesSet() {
        this.nameParameter = this.createParameterExpression(String.class, "name");
        this.titleParameter = this.createParameterExpression(String.class, "title");

        this.findAllPortletDefinitions =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<PortletDefinitionImpl>>() {
                            @Override
                            public CriteriaQuery<PortletDefinitionImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<PortletDefinitionImpl> criteriaQuery =
                                        cb.createQuery(PortletDefinitionImpl.class);
                                final Root<PortletDefinitionImpl> definitionRoot =
                                        criteriaQuery.from(PortletDefinitionImpl.class);
                                criteriaQuery.select(definitionRoot);

                                return criteriaQuery;
                            }
                        });

        this.findDefinitionByNameQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<PortletDefinitionImpl>>() {
                            @Override
                            public CriteriaQuery<PortletDefinitionImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<PortletDefinitionImpl> criteriaQuery =
                                        cb.createQuery(PortletDefinitionImpl.class);
                                final Root<PortletDefinitionImpl> definitionRoot =
                                        criteriaQuery.from(PortletDefinitionImpl.class);
                                criteriaQuery.select(definitionRoot);
                                criteriaQuery.where(
                                        cb.equal(
                                                definitionRoot.get(PortletDefinitionImpl_.name),
                                                nameParameter));

                                return criteriaQuery;
                            }
                        });

        this.findDefinitionByNameOrTitleQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<PortletDefinitionImpl>>() {
                            @Override
                            public CriteriaQuery<PortletDefinitionImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<PortletDefinitionImpl> criteriaQuery =
                                        cb.createQuery(PortletDefinitionImpl.class);
                                final Root<PortletDefinitionImpl> definitionRoot =
                                        criteriaQuery.from(PortletDefinitionImpl.class);
                                criteriaQuery.select(definitionRoot);
                                criteriaQuery.where(
                                        cb.or(
                                                cb.equal(
                                                        definitionRoot.get(
                                                                PortletDefinitionImpl_.name),
                                                        nameParameter),
                                                cb.equal(
                                                        definitionRoot.get(
                                                                PortletDefinitionImpl_.title),
                                                        titleParameter)));

                                return criteriaQuery;
                            }
                        });

        this.searchDefinitionByNameOrTitleQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<PortletDefinitionImpl>>() {
                            @Override
                            public CriteriaQuery<PortletDefinitionImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<PortletDefinitionImpl> criteriaQuery =
                                        cb.createQuery(PortletDefinitionImpl.class);
                                final Root<PortletDefinitionImpl> definitionRoot =
                                        criteriaQuery.from(PortletDefinitionImpl.class);
                                criteriaQuery.select(definitionRoot);
                                criteriaQuery.where(
                                        cb.or(
                                                cb.like(
                                                        definitionRoot.get(
                                                                PortletDefinitionImpl_.name),
                                                        nameParameter),
                                                cb.like(
                                                        definitionRoot.get(
                                                                PortletDefinitionImpl_.title),
                                                        titleParameter)));

                                return criteriaQuery;
                            }
                        });
    }

    @Override
    @DialectAwareTransactional(value = PostgreSQL81Dialect.class, exclude = false)
    @PortalTransactionalReadOnly
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");

        final long internalPortletDefinitionId = getNativePortletDefinitionId(portletDefinitionId);

        return this.getEntityManager()
                .find(PortletDefinitionImpl.class, internalPortletDefinitionId);
    }

    @Override
    @DialectAwareTransactional(value = PostgreSQL81Dialect.class, exclude = false)
    @PortalTransactionalReadOnly
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public IPortletDefinition getPortletDefinition(String portletDefinitionIdString) {
        Validate.notNull(portletDefinitionIdString, "portletDefinitionIdString can not be null");

        PortletDefinitionImpl result = null; // default

        final Long internalPortletDefinitionId =
                getNativePortletDefinitionId(portletDefinitionIdString);
        if (internalPortletDefinitionId != null) {
            result =
                    this.getEntityManager()
                            .find(PortletDefinitionImpl.class, internalPortletDefinitionId);
        }

        return result;
    }

    @Override
    @DialectAwareTransactional(value = PostgreSQL81Dialect.class, exclude = false)
    @PortalTransactionalReadOnly
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public IPortletDefinition getPortletDefinitionByFname(String fname) {
        final NaturalIdQuery<PortletDefinitionImpl> query =
                this.createNaturalIdQuery(PortletDefinitionImpl.class);
        query.using(PortletDefinitionImpl_.fname, fname);
        return query.load();
    }

    @Override
    @DialectAwareTransactional(value = PostgreSQL81Dialect.class, exclude = false)
    @PortalTransactionalReadOnly
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public IPortletDefinition getPortletDefinitionByName(String name) {
        final TypedQuery<PortletDefinitionImpl> query =
                this.createCachedQuery(this.findDefinitionByNameQuery);
        query.setParameter(this.nameParameter, name);

        final List<PortletDefinitionImpl> portletDefinitions = query.getResultList();
        return DataAccessUtils.uniqueResult(portletDefinitions);
    }

    @Override
    @DialectAwareTransactional(value = PostgreSQL81Dialect.class, exclude = false)
    @PortalTransactionalReadOnly
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public List<IPortletDefinition> searchForPortlets(String term, boolean allowPartial) {
        final CriteriaQuery<PortletDefinitionImpl> criteriaQuery;
        if (allowPartial) {
            criteriaQuery = this.searchDefinitionByNameOrTitleQuery;
            term = "%" + term.toUpperCase() + "%";
        } else {
            criteriaQuery = this.findDefinitionByNameOrTitleQuery;
        }

        final TypedQuery<PortletDefinitionImpl> query = this.createCachedQuery(criteriaQuery);
        query.setParameter("name", term);
        query.setParameter("title", term);

        final List<PortletDefinitionImpl> portletDefinitions = query.getResultList();
        return new ArrayList<>(portletDefinitions);
    }

    @Override
    @PortalTransactional
    public void deletePortletDefinition(IPortletDefinition definition) {
        Validate.notNull(definition, "definition can not be null");

        final IPortletDefinition persistentPortletDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(definition)) {
            persistentPortletDefinition = definition;
        } else {
            persistentPortletDefinition = entityManager.merge(definition);
        }

        entityManager.remove(persistentPortletDefinition);
    }

    @Override
    @DialectAwareTransactional(value = PostgreSQL81Dialect.class, exclude = false)
    @PortalTransactionalReadOnly
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public List<IPortletDefinition> getPortletDefinitions() {
        final TypedQuery<PortletDefinitionImpl> query =
                this.createCachedQuery(this.findAllPortletDefinitions);

        final List<PortletDefinitionImpl> portletDefinitions = query.getResultList();
        return new ArrayList<>(new LinkedHashSet<IPortletDefinition>(portletDefinitions));
    }

    @Override
    @PortalTransactional
    public IPortletDefinition savePortletDefinition(IPortletDefinition portletDefinition) {
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        Validate.notNull(
                portletDefinition.getType(), "portletDefinition portlet type can not be null");
        Validate.notEmpty(portletDefinition.getFName(), "portletDefinition fname can not be null");
        Validate.notEmpty(portletDefinition.getName(), "portletDefinition name can not be null");
        Validate.notEmpty(portletDefinition.getTitle(), "portletDefinition title can not be null");
        this.getEntityManager().persist(portletDefinition);
        return portletDefinition;
    }

    @Override
    @PortalTransactional
    public IPortletDefinition mergePortletDefinition(IPortletDefinition portletDefinition) {
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        Validate.notNull(
                portletDefinition.getType(), "portletDefinition portlet type can not be null");
        Validate.notEmpty(portletDefinition.getFName(), "portletDefinition fname can not be null");
        Validate.notEmpty(portletDefinition.getName(), "portletDefinition name can not be null");
        Validate.notEmpty(portletDefinition.getTitle(), "portletDefinition title can not be null");
        this.getEntityManager().merge(portletDefinition);
        return portletDefinition;
    }

    private long getNativePortletDefinitionId(IPortletDefinitionId portletDefinitionId) {
        return Long.parseLong(portletDefinitionId.getStringId());
    }

    private Long getNativePortletDefinitionId(String portletDefinitionId) {
        Long result = null; // default
        try {
            result = Long.parseLong(portletDefinitionId);
        } catch (NumberFormatException nfe) {
            logger.warn(
                    "The portletDefinitionId '{}' is not parsable to a valid portletId (long);  null will be returned",
                    portletDefinitionId);
        }
        return result;
    }
}
