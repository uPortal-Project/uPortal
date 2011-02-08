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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletType;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of the portlet definition DAO
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaPortletDefinitionDao implements IPortletDefinitionDao {

    private static final String FIND_ALL_PORTLET_DEFS = "from PortletDefinitionImpl portlet";
    private static final String FIND_PORTLET_DEF_BY_FNAME = 
        "from PortletDefinitionImpl portlet where portlet.fname = :fname";
    private static final String FIND_PORTLET_DEF_BY_NAME = 
        "from PortletDefinitionImpl portlet where portlet.name = :name";
    private static final String SEARCH_PORTLETS_EXACT_MATCH = 
        "from PortletDefinitionImpl portlet where portlet.name = :name or portlet.title = :title";
    private static final String SEARCH_PORTLETS_PARTIAL_MATCH = 
        "from PortletDefinitionImpl portlet where portlet.name like :name or portlet.title like :title";

    private static final String FIND_ALL_PORTLET_DEFS_CACHE_REGION = PortletDefinitionImpl.class.getName() + ".query.FIND_ALL_PORTLET_DEFS";
    private static final String FIND_PORTLET_DEF_BY_FNAME_CACHE_REGION = PortletDefinitionImpl.class.getName() + ".query.FIND_PORTLET_DEF_BY_FNAME";
    private static final String FIND_PORTLET_DEF_BY_NAME_CACHE_REGION = PortletDefinitionImpl.class.getName() + ".query.FIND_PORTLET_DEF_BY_NAME";

    private EntityManager entityManager;
    
    /**
     * @return the entityManager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }
    /**
     * @param entityManager the entityManager to set
     */
    @PersistenceContext(unitName="uPortalPersistence")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    

    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        
        final long internalPortletDefinitionId = Long.parseLong(portletDefinitionId.getStringId());
        final PortletDefinitionImpl portletDefinition = this.entityManager.find(PortletDefinitionImpl.class, internalPortletDefinitionId);
        
        return portletDefinition;
    }
    
    public IPortletDefinition getPortletDefinition(String portletDefinitionIdString) {
        Validate.notNull(portletDefinitionIdString, "portletDefinitionIdString can not be null");
        
        final long internalPortletDefinitionId = Long.parseLong(portletDefinitionIdString);
        final PortletDefinitionImpl portletDefinition = this.entityManager.find(PortletDefinitionImpl.class, internalPortletDefinitionId);
        
        return portletDefinition;
    }

	public IPortletDefinition getPortletDefinitionByFname(String fname) {
        final Query query = this.entityManager.createQuery(FIND_PORTLET_DEF_BY_FNAME);
        query.setParameter("fname", fname);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", FIND_PORTLET_DEF_BY_FNAME_CACHE_REGION);
        query.setMaxResults(1);
        
        @SuppressWarnings("unchecked")
        final List<IPortletDefinition> portletDefinitions = query.getResultList();
        IPortletDefinition definition = (IPortletDefinition) DataAccessUtils.uniqueResult(portletDefinitions);
		return definition;
	}

    public IPortletDefinition getPortletDefinitionByName(String name) {
        final Query query = this.entityManager.createQuery(FIND_PORTLET_DEF_BY_NAME);
        query.setParameter("name", name);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", FIND_PORTLET_DEF_BY_NAME_CACHE_REGION);
        query.setMaxResults(1);
        
        @SuppressWarnings("unchecked")
        final List<IPortletDefinition> portletDefinitions = query.getResultList();
        IPortletDefinition definition = (IPortletDefinition) DataAccessUtils.uniqueResult(portletDefinitions);
		return definition;
    }
    
    public List<IPortletDefinition> searchForPortlets(String term, boolean allowPartial) {
    	String queryString = allowPartial ? SEARCH_PORTLETS_PARTIAL_MATCH : SEARCH_PORTLETS_EXACT_MATCH;
    	term = term.toUpperCase();
    	if (allowPartial) {
    		term = "%".concat(term).concat("%");
    	}
        final Query query = this.entityManager.createQuery(queryString);
        query.setParameter("name", term);
        query.setParameter("title", term);
//        query.setHint("org.hibernate.cacheable", true);
//        query.setHint("org.hibernate.cacheRegion", FIND_PORTLET_DEF_BY_NAME_CACHE_REGION);
        
        @SuppressWarnings("unchecked")
		final List<IPortletDefinition> portletDefinitions = query.getResultList();
		return portletDefinitions;
    }

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

	public List<IPortletDefinition> getPortletDefinitions() {
        final Query query = this.entityManager.createQuery(FIND_ALL_PORTLET_DEFS);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", FIND_ALL_PORTLET_DEFS_CACHE_REGION);
        
        @SuppressWarnings("unchecked")
        final List<IPortletDefinition> portletDefinitions = query.getResultList();
		return portletDefinitions;
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
}
