/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.dao.IPortletEntityDao;
import org.jasig.portal.portlet.om.AbstractObjectId;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provides access to IPortletEntity objects and convenience methods for creating
 * and converting them and related objects.
 * 
 * The portlet adaptor channel will be responsible for listenting to unsubscribe events and cleaning up entity objects
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletEntityRegistryImpl implements IPortletEntityRegistry {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortletEntityDao portletEntityDao;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    
    
    /**
     * @return the portletEntityDao
     */
    public IPortletEntityDao getPortletEntityDao() {
        return portletEntityDao;
    }
    /**
     * @param portletEntityDao the portletEntityDao to set
     */
    @Required
    public void setPortletEntityDao(IPortletEntityDao portletEntityDao) {
        this.portletEntityDao = portletEntityDao;
    }

    /**
     * @return the portletDefinitionRegistry
     */
    public IPortletDefinitionRegistry getPortletDefinitionRegistry() {
        return portletDefinitionRegistry;
    }
    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
    @Required
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#createPortletEntity(org.jasig.portal.portlet.om.IPortletDefinitionId, java.lang.String, int)
     */
    public IPortletEntity createPortletEntity(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, int userId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        Validate.notNull(channelSubscribeId, "channelSubscribeId can not be null");
        
        return new InterimPortletEntityImpl(portletDefinitionId, channelSubscribeId, userId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletEntity getPortletEntity(IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        IPortletEntity portletEntity = null;

        if (InterimPortletEntityImpl.isInterimPortletEntityId(portletEntityId)) {
            portletEntity = new InterimPortletEntityImpl(portletEntityId);
        } else {
            portletEntity = this.portletEntityDao.getPortletEntity(portletEntityId);
        }

        return portletEntity;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(java.lang.String)
     */
    public IPortletEntity getPortletEntity(String portletEntityIdString) {
        Validate.notNull(portletEntityIdString, "portletEntityId can not be null");
        
        IPortletEntity portletEntity = null;

        if (InterimPortletEntityImpl.isInterimPortletEntityId(portletEntityIdString)) {
            portletEntity = new InterimPortletEntityImpl(portletEntityIdString);
        } else {
            final long portletEntityIdLong;
            try {
                portletEntityIdLong = Long.parseLong(portletEntityIdString);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(
                        "PortletEntityId must parsable as a long", nfe);
            }

            final PortletEntityIdImpl portletEntityId = new PortletEntityIdImpl(portletEntityIdLong);
            portletEntity = this.portletEntityDao.getPortletEntity(portletEntityId);
        }
        
        return portletEntity;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(java.lang.String, int)
     */
    public IPortletEntity getPortletEntity(String channelSubscribeId, int userId) {
        Validate.notNull(channelSubscribeId, "channelSubscribeId can not be null");
        
        return this.portletEntityDao.getPortletEntity(channelSubscribeId, userId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntitiesForUser(int)
     */
    public Set<IPortletEntity> getPortletEntitiesForUser(int userId) {
        return this.portletEntityDao.getPortletEntitiesForUser(userId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getOrCreatePortletEntity(org.jasig.portal.portlet.om.IPortletDefinitionId, java.lang.String, int)
     */
    public IPortletEntity getOrCreatePortletEntity(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, int userId) {
        final IPortletEntity portletEntity = this.getPortletEntity(channelSubscribeId, userId);
        if (portletEntity != null) {
            if (!portletDefinitionId.equals(portletEntity.getPortletDefinitionId())) {
                this.logger.warn("Found portlet entity '" + portletEntity + "' is not the correct entity for portlet definition id: " + portletDefinitionId + ". The entity will be deleted and a new one created.");
                this.deletePortletEntity(portletEntity);
            }
            else {
                return portletEntity;
            }
        }
        
        return this.createPortletEntity(portletDefinitionId, channelSubscribeId, userId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#storePortletEntity(org.jasig.portal.portlet.om.IPortletEntity)
     */
    public void storePortletEntity(IPortletEntity portletEntity) {
        Validate.notNull(portletEntity, "portletEntity can not be null");
        
        // only persist IF preferences exist.
        if (portletEntity.getPortletPreferences().getPortletPreferences()
                .size() > 0) {
            if (InterimPortletEntityImpl.isInterimPortletEntityId(portletEntity.getPortletEntityId())) {
                IPortletEntity persistantEntity = this.portletEntityDao
                        .createPortletEntity(portletEntity
                                .getPortletDefinitionId(), portletEntity
                                .getChannelSubscribeId(), portletEntity
                                .getUserId());

                persistantEntity.setPortletPreferences(portletEntity
                        .getPortletPreferences());
                this.portletEntityDao.updatePortletEntity(persistantEntity);
            } else {
                this.portletEntityDao.updatePortletEntity(portletEntity);
            }
        }
        
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#deletePortletEntity(org.jasig.portal.portlet.om.IPortletEntity)
     */
    public void deletePortletEntity(IPortletEntity portletEntity) {
        Validate.notNull(portletEntity, "portletEntity can not be null");
        
        if ((portletEntity != null) && (!InterimPortletEntityImpl
                .isInterimPortletEntityId(portletEntity.getPortletEntityId()))) {
            this.portletEntityDao.deletePortletEntity(portletEntity);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getParentPortletDefinition(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletDefinition getParentPortletDefinition(IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        IPortletDefinitionId portletDefinitionId = null;
        if (InterimPortletEntityImpl.isInterimPortletEntityId(portletEntityId)) {
            IPortletEntity portletEntity = new InterimPortletEntityImpl(portletEntityId);
            portletDefinitionId = portletEntity.getPortletDefinitionId();
        } else {
            final IPortletEntity portletEntity = this.getPortletEntity(portletEntityId);
            portletDefinitionId = portletEntity.getPortletDefinitionId();
        }

        return this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
    }
}

class PortletEntityIdImpl extends AbstractObjectId implements IPortletEntityId {
    private static final long serialVersionUID = 1L;

    public PortletEntityIdImpl(long portletEntityId) {
        super(Long.toString(portletEntityId));
    }
}

