/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao.jpa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class JpaPortletDefinitionDao  implements IPortletDefinitionDao {
    private static final String FIND_PORTLET_DEF_BY_CHAN_DEF = 
        "from PortletDefinitionImpl portDef " +
        "where portDef.channelDefinitionId = :channelDefinitionId";

    private static final String FIND_PORTLET_DEFS_BY_PORTLET_APP = 
        "from PortletDefinitionImpl portDef " +
        "where portDef.portletApplicaitonId = :portletApplicaitonId and portDef.portletName = :portletName";

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

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletDefinitionDao#createPortletDefinition(int, java.lang.String, java.lang.String)
     */
    @Transactional
    public IPortletDefinition createPortletDefinition(int channelDefinitionId, String portletApplicaitonId, String portletName) {
        IPortletDefinition portletDefinition = new PortletDefinitionImpl(channelDefinitionId, portletApplicaitonId, portletName);
        
        portletDefinition = this.entityManager.merge(portletDefinition);
        this.entityManager.persist(portletDefinition);
        
        return portletDefinition;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletDefinitionDao#deletePortletDefinition(org.jasig.portal.om.portlet.IPortletDefinition)
     */
    @Transactional
    public void deletePortletDefinition(IPortletDefinition portletDefinition) {
        final IPortletDefinition persistentPortletDefinition;
        if (this.entityManager.contains(portletDefinition)) {
            persistentPortletDefinition = portletDefinition;
        }
        else {
            persistentPortletDefinition = this.entityManager.merge(portletDefinition);
        }
        
        this.entityManager.remove(persistentPortletDefinition);
    }

    @Transactional(readOnly = true)
    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId) {
        final long internalPortletDefinitionId = Long.parseLong(portletDefinitionId.getStringId());
        final PortletDefinitionImpl portletDefinition = this.entityManager.find(PortletDefinitionImpl.class, internalPortletDefinitionId);
        return portletDefinition;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletDefinitionDao#getPortletDefinition(int)
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public IPortletDefinition getPortletDefinition(int channelDefinitionId) {
        final Query query = this.entityManager.createQuery(FIND_PORTLET_DEF_BY_CHAN_DEF);
        query.setParameter("channelDefinitionId", channelDefinitionId);
        query.setMaxResults(1);
        
        final List<IPortletDefinition> portletDefinitions = query.getResultList();
        final IPortletDefinition portletDefinition = (IPortletDefinition)DataAccessUtils.uniqueResult(portletDefinitions);
        return portletDefinition;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletDefinitionDao#getPortletDefinitions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Set<IPortletDefinition> getPortletDefinitions(String portletApplicaitonId, String portletName) {
        final Query query = this.entityManager.createQuery(FIND_PORTLET_DEFS_BY_PORTLET_APP);
        query.setParameter("portletApplicaitonId", portletApplicaitonId);
        query.setParameter("portletName", portletName);
        
        final List<IPortletDefinition> portletDefinitions = query.getResultList();
        return new HashSet<IPortletDefinition>(portletDefinitions);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletDefinitionDao#updatePortletDefinition(org.jasig.portal.om.portlet.IPortletDefinition)
     */
    @Transactional
    public void updatePortletDefinition(IPortletDefinition portletDefinition) {
        portletDefinition = this.entityManager.merge(portletDefinition);
        this.entityManager.persist(portletDefinition);
    }

}
