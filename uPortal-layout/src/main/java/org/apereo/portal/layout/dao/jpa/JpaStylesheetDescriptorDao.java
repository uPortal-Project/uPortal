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
package org.apereo.portal.layout.dao.jpa;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.apache.commons.lang.Validate;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.jpa.OpenEntityManager;
import org.apereo.portal.layout.dao.IStylesheetDescriptorDao;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.springframework.stereotype.Repository;

/**
 * JPA DAO for stylesheet descriptor
 *
 */
@Repository("stylesheetDescriptorDao")
public class JpaStylesheetDescriptorDao extends BasePortalJpaDao
        implements IStylesheetDescriptorDao {
    private CriteriaQuery<StylesheetDescriptorImpl> findAllDescriptors;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllDescriptors =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<StylesheetDescriptorImpl>>() {
                            @Override
                            public CriteriaQuery<StylesheetDescriptorImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<StylesheetDescriptorImpl> criteriaQuery =
                                        cb.createQuery(StylesheetDescriptorImpl.class);
                                criteriaQuery.from(StylesheetDescriptorImpl.class);
                                return criteriaQuery;
                            }
                        });
    }

    @PortalTransactional
    @Override
    public IStylesheetDescriptor createStylesheetDescriptor(
            String name, String stylesheetResource) {
        final StylesheetDescriptorImpl stylesheetDescriptor =
                new StylesheetDescriptorImpl(name, stylesheetResource);

        this.getEntityManager().persist(stylesheetDescriptor);

        return stylesheetDescriptor;
    }

    @Override
    public List<? extends IStylesheetDescriptor> getStylesheetDescriptors() {
        final TypedQuery<StylesheetDescriptorImpl> query =
                this.createCachedQuery(this.findAllDescriptors);
        final List<StylesheetDescriptorImpl> results = query.getResultList();
        return new ArrayList<IStylesheetDescriptor>(
                new LinkedHashSet<IStylesheetDescriptor>(results));
    }

    @Override
    public IStylesheetDescriptor getStylesheetDescriptor(long id) {
        final StylesheetDescriptorImpl stylesheetDescriptor =
                this.getEntityManager().find(StylesheetDescriptorImpl.class, id);
        return stylesheetDescriptor;
    }

    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    @Override
    public IStylesheetDescriptor getStylesheetDescriptorByName(String name) {
        final NaturalIdQuery<StylesheetDescriptorImpl> query =
                this.createNaturalIdQuery(StylesheetDescriptorImpl.class);
        query.using(StylesheetDescriptorImpl_.name, name);
        return query.load();
    }

    @PortalTransactional
    @Override
    public void updateStylesheetDescriptor(IStylesheetDescriptor stylesheetDescriptor) {
        Validate.notNull(stylesheetDescriptor, "stylesheetDescriptor can not be null");

        this.getEntityManager().persist(stylesheetDescriptor);
    }

    @PortalTransactional
    @Override
    public void deleteStylesheetDescriptor(IStylesheetDescriptor stylesheetDescriptor) {
        Validate.notNull(stylesheetDescriptor, "definition can not be null");

        final IStylesheetDescriptor persistentStylesheetDescriptor;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(stylesheetDescriptor)) {
            persistentStylesheetDescriptor = stylesheetDescriptor;
        } else {
            persistentStylesheetDescriptor = entityManager.merge(stylesheetDescriptor);
        }

        entityManager.remove(persistentStylesheetDescriptor);
    }
}
