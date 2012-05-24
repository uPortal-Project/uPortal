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

package org.jasig.portal.layout.dao.jpa;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

/**
 * JPA DAO for stylesheet descriptor
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository("stylesheetDescriptorDao")
public class JpaStylesheetDescriptorDao extends BasePortalJpaDao implements IStylesheetDescriptorDao {
    private CriteriaQuery<StylesheetDescriptorImpl> findAllDescriptors;
    private CriteriaQuery<StylesheetDescriptorImpl> findDescriptorByNameQuery;
    private ParameterExpression<String> nameParameter;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.nameParameter = this.createParameterExpression(String.class, "name");
        
        this.findAllDescriptors = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<StylesheetDescriptorImpl>>() {
            @Override
            public CriteriaQuery<StylesheetDescriptorImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<StylesheetDescriptorImpl> criteriaQuery = cb.createQuery(StylesheetDescriptorImpl.class);
                criteriaQuery.from(StylesheetDescriptorImpl.class);
                return criteriaQuery;
            }
        });
        
        
        this.findDescriptorByNameQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<StylesheetDescriptorImpl>>() {
            @Override
            public CriteriaQuery<StylesheetDescriptorImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<StylesheetDescriptorImpl> criteriaQuery = cb.createQuery(StylesheetDescriptorImpl.class);
                final Root<StylesheetDescriptorImpl> descriptorRoot = criteriaQuery.from(StylesheetDescriptorImpl.class);
                criteriaQuery.select(descriptorRoot);
                descriptorRoot.fetch(StylesheetDescriptorImpl_.layoutAttributes, JoinType.LEFT);
                descriptorRoot.fetch(StylesheetDescriptorImpl_.outputProperties, JoinType.LEFT);
                descriptorRoot.fetch(StylesheetDescriptorImpl_.stylesheetParameters, JoinType.LEFT);
                criteriaQuery.where(
                    cb.equal(descriptorRoot.get(StylesheetDescriptorImpl_.name), nameParameter)
                );
                
                return criteriaQuery;
            }
        });
    }

    @PortalTransactional
    @Override
    public IStylesheetDescriptor createStylesheetDescriptor(String name, String stylesheetResource) {
        final StylesheetDescriptorImpl stylesheetDescriptor = new StylesheetDescriptorImpl(name, stylesheetResource);
        
        this.getEntityManager().persist(stylesheetDescriptor);
        
        return stylesheetDescriptor;
    }
    
    @Override
    public List<? extends IStylesheetDescriptor> getStylesheetDescriptors() {
        final TypedQuery<StylesheetDescriptorImpl> query = this.createCachedQuery(this.findAllDescriptors);
        final List<StylesheetDescriptorImpl> results = query.getResultList();
        return new ArrayList<IStylesheetDescriptor>(new LinkedHashSet<IStylesheetDescriptor>(results));
    }

    @Override
    public IStylesheetDescriptor getStylesheetDescriptor(long id) {
        final StylesheetDescriptorImpl stylesheetDescriptor = this.getEntityManager().find(StylesheetDescriptorImpl.class, id);
        return stylesheetDescriptor;
    }

    @Override
    public IStylesheetDescriptor getStylesheetDescriptorByName(String name) {
        final TypedQuery<StylesheetDescriptorImpl> query = this.createCachedQuery(this.findDescriptorByNameQuery);
        query.setParameter(this.nameParameter, name);
        
        final List<StylesheetDescriptorImpl> results = query.getResultList();
        return DataAccessUtils.uniqueResult(results);
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
        }
        else {
            persistentStylesheetDescriptor = entityManager.merge(stylesheetDescriptor);
        }
        
        entityManager.remove(persistentStylesheetDescriptor);
    }
}
