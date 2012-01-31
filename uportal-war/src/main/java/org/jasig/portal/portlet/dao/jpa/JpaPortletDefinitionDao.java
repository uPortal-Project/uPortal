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

package org.jasig.portal.portlet.dao.jpa;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BaseJpaDao;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletType;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

/**
 * JPA implementation of the portlet definition DAO
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaPortletDefinitionDao extends BaseJpaDao implements IPortletDefinitionDao {
    private CriteriaQuery<PortletDefinitionImpl> findAllPortletDefinitions;
    private CriteriaQuery<PortletDefinitionImpl> findDefinitionByFnameQuery;
    private CriteriaQuery<PortletDefinitionImpl> findDefinitionByNameQuery;
    private CriteriaQuery<PortletDefinitionImpl> findDefinitionByNameOrTitleQuery;
    private CriteriaQuery<PortletDefinitionImpl> searchDefinitionByNameOrTitleQuery;
    private ParameterExpression<String> fnameParameter;
    private ParameterExpression<String> nameParameter;
    private ParameterExpression<String> titleParameter;
    private EntityManager entityManager;

    @PersistenceContext(unitName = "uPortalPersistence")
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.fnameParameter = this.createParameterExpression(String.class, "fname");
        this.nameParameter = this.createParameterExpression(String.class, "name");
        this.titleParameter = this.createParameterExpression(String.class, "title");
        
        this.findAllPortletDefinitions = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PortletDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PortletDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PortletDefinitionImpl> criteriaQuery = cb.createQuery(PortletDefinitionImpl.class);
                final Root<PortletDefinitionImpl> definitionRoot = criteriaQuery.from(PortletDefinitionImpl.class);
                criteriaQuery.select(definitionRoot);
                addFetches(definitionRoot);

                return criteriaQuery;
            }
        });

        
        this.findDefinitionByFnameQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PortletDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PortletDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PortletDefinitionImpl> criteriaQuery = cb.createQuery(PortletDefinitionImpl.class);
                final Root<PortletDefinitionImpl> definitionRoot = criteriaQuery.from(PortletDefinitionImpl.class);
                criteriaQuery.select(definitionRoot);
                addFetches(definitionRoot);
                criteriaQuery.where(
                    cb.equal(definitionRoot.get(PortletDefinitionImpl_.fname), fnameParameter)
                );
                
                return criteriaQuery;
            }
        });

        
        this.findDefinitionByNameQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PortletDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PortletDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PortletDefinitionImpl> criteriaQuery = cb.createQuery(PortletDefinitionImpl.class);
                final Root<PortletDefinitionImpl> definitionRoot = criteriaQuery.from(PortletDefinitionImpl.class);
                criteriaQuery.select(definitionRoot);
                addFetches(definitionRoot);
                criteriaQuery.where(
                    cb.equal(definitionRoot.get(PortletDefinitionImpl_.name), nameParameter)
                );
                
                return criteriaQuery;
            }
        });

        
        this.findDefinitionByNameOrTitleQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PortletDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PortletDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PortletDefinitionImpl> criteriaQuery = cb.createQuery(PortletDefinitionImpl.class);
                final Root<PortletDefinitionImpl> definitionRoot = criteriaQuery.from(PortletDefinitionImpl.class);
                criteriaQuery.select(definitionRoot);
                addFetches(definitionRoot);
                criteriaQuery.where(
                    cb.or(
                        cb.equal(definitionRoot.get(PortletDefinitionImpl_.name), nameParameter),
                        cb.equal(definitionRoot.get(PortletDefinitionImpl_.title), titleParameter)
                    )
                );
                
                return criteriaQuery;
            }
        });

        
        this.searchDefinitionByNameOrTitleQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PortletDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PortletDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PortletDefinitionImpl> criteriaQuery = cb.createQuery(PortletDefinitionImpl.class);
                final Root<PortletDefinitionImpl> definitionRoot = criteriaQuery.from(PortletDefinitionImpl.class);
                criteriaQuery.select(definitionRoot);
                addFetches(definitionRoot);
                criteriaQuery.where(
                    cb.or(
                        cb.like(definitionRoot.get(PortletDefinitionImpl_.name), nameParameter),
                        cb.like(definitionRoot.get(PortletDefinitionImpl_.title), titleParameter)
                    )
                );
                
                return criteriaQuery;
            }
        });
    }

    /**
     * Add all the fetches needed for completely loading the object graph
     */
    protected void addFetches(final Root<PortletDefinitionImpl> definitionRoot) {
        definitionRoot.fetch(PortletDefinitionImpl_.portletPreferences, JoinType.LEFT)
            .fetch(PortletPreferencesImpl_.portletPreferences, JoinType.LEFT)
            .fetch(PortletPreferenceImpl_.values, JoinType.LEFT);
        definitionRoot.fetch(PortletDefinitionImpl_.parameters, JoinType.LEFT);
        definitionRoot.fetch(PortletDefinitionImpl_.localizations, JoinType.LEFT);
    }
    
 
    @Override
    @Transactional(readOnly=true)
    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        
        final long internalPortletDefinitionId = getNativePortletDefinitionId(portletDefinitionId);
        final PortletDefinitionImpl portletDefinition = this.entityManager.find(PortletDefinitionImpl.class, internalPortletDefinitionId);
        
        return portletDefinition;
    }
    
    @Override
    @Transactional(readOnly=true)
    public IPortletDefinition getPortletDefinition(String portletDefinitionIdString) {
        Validate.notNull(portletDefinitionIdString, "portletDefinitionIdString can not be null");
        
        final long internalPortletDefinitionId = getNativePortletDefinitionId(portletDefinitionIdString);
        final PortletDefinitionImpl portletDefinition = this.entityManager.find(PortletDefinitionImpl.class, internalPortletDefinitionId);
        
        return portletDefinition;
    }

	@Override
    @Transactional(readOnly=true)
    public IPortletDefinition getPortletDefinitionByFname(String fname) {
	    final TypedQuery<PortletDefinitionImpl> query = this.createCachedQuery(this.findDefinitionByFnameQuery);
        query.setParameter(this.fnameParameter, fname);
        
        final List<PortletDefinitionImpl> portletDefinitions = query.getResultList();
        return DataAccessUtils.uniqueResult(portletDefinitions);
	}

    @Override
    @Transactional(readOnly=true)
    public IPortletDefinition getPortletDefinitionByName(String name) {
        final TypedQuery<PortletDefinitionImpl> query = this.createCachedQuery(this.findDefinitionByNameQuery);
        query.setParameter(this.nameParameter, name);
        
        final List<PortletDefinitionImpl> portletDefinitions = query.getResultList();
        return DataAccessUtils.uniqueResult(portletDefinitions);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<IPortletDefinition> searchForPortlets(String term, boolean allowPartial) {
        final CriteriaQuery<PortletDefinitionImpl> criteriaQuery;
        if (allowPartial) {
            criteriaQuery = this.searchDefinitionByNameOrTitleQuery;
            term = "%" + term.toUpperCase() + "%";
        }
        else {
            criteriaQuery = this.findDefinitionByNameOrTitleQuery;
        }
        
        final TypedQuery<PortletDefinitionImpl> query = this.createCachedQuery(criteriaQuery);
        query.setParameter("name", term);
        query.setParameter("title", term);
        
        final List<PortletDefinitionImpl> portletDefinitions = query.getResultList();
		return new ArrayList<IPortletDefinition>(portletDefinitions);
    }

    @Override
    @Transactional
	public void deletePortletDefinition(IPortletDefinition definition) {
        Validate.notNull(definition, "definition can not be null");
        
        final IPortletDefinition persistentPortletDefinition;
        if (this.entityManager.contains(definition)) {
            persistentPortletDefinition = definition;
        }
        else {
            persistentPortletDefinition = this.entityManager.merge(definition);
        }
        
        this.entityManager.remove(persistentPortletDefinition);
	}

	@Override
	@Transactional(readOnly=true)
    public List<IPortletDefinition> getPortletDefinitions() {
	    final TypedQuery<PortletDefinitionImpl> query = this.createCachedQuery(this.findAllPortletDefinitions);
        
        final List<PortletDefinitionImpl> portletDefinitions = query.getResultList();
        return new ArrayList<IPortletDefinition>(new LinkedHashSet<IPortletDefinition>(portletDefinitions));
	}
	
    @Override
    @Transactional
    public IPortletDefinition createPortletDefinition(IPortletType portletType, String fname, String name, String title, String applicationId, String portletName, boolean isFramework) {
        Validate.notNull(portletType, "portletType can not be null");
        Validate.notEmpty(fname, "fname can not be null");
        Validate.notEmpty(name, "name can not be null");
        Validate.notEmpty(title, "title can not be null");
        
        final PortletDefinitionImpl portletDefinition = new PortletDefinitionImpl(portletType, fname, name, title, applicationId, portletName, isFramework);
        
        this.entityManager.persist(portletDefinition);
        
        return portletDefinition;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletDefinitionDao#updatePortletDefinition(org.jasig.portal.om.portlet.IPortletDefinition)
     */
    @Override
    @Transactional
    public IPortletDefinition updatePortletDefinition(IPortletDefinition portletDefinition) {
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        
        this.entityManager.persist(portletDefinition);
        return portletDefinition;
    }

    protected long getNativePortletDefinitionId(IPortletDefinitionId portletDefinitionId) {
        return Long.parseLong(portletDefinitionId.getStringId());
    }
    protected long getNativePortletDefinitionId(String portletDefinitionId) {
        return Long.parseLong(portletDefinitionId);
    }
}
