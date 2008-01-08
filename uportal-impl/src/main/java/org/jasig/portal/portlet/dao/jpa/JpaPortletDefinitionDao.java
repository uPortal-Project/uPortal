/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao.jpa;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.Validate;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.springframework.beans.factory.annotation.Required;
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

    private IChannelRegistryStore channelRegistryStore;
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
    
    /**
     * @return the channelRegistryStore
     */
    public IChannelRegistryStore getChannelRegistryStore() {
        return channelRegistryStore;
    }
    /**
     * @param channelRegistryStore the channelRegistryStore to set
     */
    @Required
    public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
        Validate.notNull(channelRegistryStore);
        this.channelRegistryStore = channelRegistryStore;
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletDefinitionDao#createPortletDefinition(int, java.lang.String, java.lang.String)
     */
    @Transactional
    public IPortletDefinition createPortletDefinition(int channelDefinitionId, String portletApplicaitonId, String portletName) {
        PortletDefinitionImpl portletDefinition = new PortletDefinitionImpl(channelDefinitionId);
        
        portletDefinition = this.entityManager.merge(portletDefinition);

        portletDefinition.setPortletApplicaitonId(portletApplicaitonId);
        portletDefinition.setPortletName(portletName);
        
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
        
        this.updatePortletDescriptorKeys(portletDefinition);
        
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
        
        final List<PortletDefinitionImpl> portletDefinitions = query.getResultList();
        final PortletDefinitionImpl portletDefinition = (PortletDefinitionImpl)DataAccessUtils.uniqueResult(portletDefinitions);
        
        this.updatePortletDescriptorKeys(portletDefinition);
        
        return portletDefinition;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletDefinitionDao#updatePortletDefinition(org.jasig.portal.om.portlet.IPortletDefinition)
     */
    @Transactional
    public void updatePortletDefinition(IPortletDefinition portletDefinition) {
        portletDefinition = this.entityManager.merge(portletDefinition);
        
        this.updatePortletDescriptorKeys((PortletDefinitionImpl)portletDefinition);
        
        this.entityManager.persist(portletDefinition);
    }

    /**
     * @see #updatePortletDescriptorKeys(PortletDefinitionImpl)
     */
    protected void updatePortletDescriptorKeys(Collection<PortletDefinitionImpl> portletDefinitions) {
        for (final PortletDefinitionImpl portletDefinition : portletDefinitions) {
            this.updatePortletDescriptorKeys(portletDefinition);
        }
    }

    /**
     * Looks up the ChannelDefinition that backs the PortletDefinitionImpl and populates the
     * portletApplicaitonId and portletName properties from the ChannelDefinition parameters.
     */
    protected void updatePortletDescriptorKeys(PortletDefinitionImpl portletDefinition) {
        if (portletDefinition == null) {
            return;
        }

        final int channelDefinitionId = portletDefinition.getChannelDefinitionId();
        final ChannelDefinition channelDefinition = this.getChannelDefinition(channelDefinitionId);
        if (channelDefinition == null) {
            throw new IllegalArgumentException("No ChannelDefinition exists for the specified channelDefinitionId=" + channelDefinitionId);
        }

        final ChannelParameter portletApplicaitonIdParam = channelDefinition.getParameter(IPortletAdaptor.CHANNEL_PARAM__PORTLET_APPLICATION_ID);
        if (portletApplicaitonIdParam == null) {
            throw new IllegalArgumentException("The specified ChannelDefinition does not provide the needed channel parameter '" + IPortletAdaptor.CHANNEL_PARAM__PORTLET_APPLICATION_ID + "'. ChannelDefinition=" + channelDefinition);
        }

        portletDefinition.setPortletApplicaitonId(portletApplicaitonIdParam.getValue());
        
        final ChannelParameter portletNameParam = channelDefinition.getParameter(IPortletAdaptor.CHANNEL_PARAM__PORTLET_NAME);
        if (portletNameParam == null) {
            throw new IllegalArgumentException("The specified ChannelDefinition does not provide the needed channel parameter '" + IPortletAdaptor.CHANNEL_PARAM__PORTLET_NAME + "'. ChannelDefinition=" + channelDefinition);
        }

        
        portletDefinition.setPortletName(portletNameParam.getValue());
    }
    
    /**
     * Get the ChannelDefinition for the specified channelPublishId
     */
    protected ChannelDefinition getChannelDefinition(int channelPublishId) {
        //Lookup the ChannelDefinition
        final ChannelDefinition channelDefinition;
        try {
            channelDefinition = this.channelRegistryStore.getChannelDefinition(channelPublishId);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Failed to retrieve required ChannelDefinition for channelPublishId: " + channelPublishId, e);
        }
        return channelDefinition;
    }

}
