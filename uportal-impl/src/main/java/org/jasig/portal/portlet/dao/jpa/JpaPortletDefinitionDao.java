/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
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
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    

    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        
        final long internalPortletDefinitionId = Long.parseLong(portletDefinitionId.getStringId());
        final PortletDefinitionImpl portletDefinition = this.entityManager.find(PortletDefinitionImpl.class, internalPortletDefinitionId);
        
        return portletDefinition;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletDefinitionDao#updatePortletDefinition(org.jasig.portal.om.portlet.IPortletDefinition)
     */
    @Transactional
    public void updatePortletDefinition(IPortletDefinition portletDefinition) {
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        
        this.entityManager.persist(portletDefinition);
    }
}
