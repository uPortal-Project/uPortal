/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of the definition registry, pulls together the related parts of the framework for creation and access
 * of {@link IPortletDefinition}s.
 * 
 * TODO this needs to listen for channel deletion events and remove the corresponding portlet definition, this would likley need a hook in ChannelRegistryManager.removeChannel
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletDefinitionRegistryImpl implements IPortletDefinitionRegistry {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IChannelRegistryStore channelRegistryStore;
    private IPortletDefinitionDao portletDefinitionDao;
    private OptionalContainerServices optionalContainerServices;
    
    /**
     * @return the portletDefinitionDao
     */
    public IPortletDefinitionDao getPortletDefinitionDao() {
        return this.portletDefinitionDao;
    }
    /**
     * @param portletDefinitionDao the portletDefinitionDao to set
     */
    @Required
    public void setPortletDefinitionDao(IPortletDefinitionDao portletDefinitionDao) {
        Validate.notNull(portletDefinitionDao);
        this.portletDefinitionDao = portletDefinitionDao;
    }
    
    /**
     * @return the optionalContainerServices
     */
    public OptionalContainerServices getOptionalContainerServices() {
        return this.optionalContainerServices;
    }
    /**
     * @param optionalContainerServices the optionalContainerServices to set
     */
    @Required
    public void setOptionalContainerServices(OptionalContainerServices optionalContainerServices) {
        Validate.notNull(optionalContainerServices);
        this.optionalContainerServices = optionalContainerServices;
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
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#createPortletDefinition(int)
     */
    public IPortletDefinition createPortletDefinition(int channelPublishId) {
        final ChannelDefinition channelDefinition = this.getChannelDefinition(channelPublishId);
        
        //Grab the parameters that describe the pluto descriptor objects
        final ChannelParameter portletApplicationIdParam = channelDefinition.getParameter(IPortletAdaptor.CHANNEL_PARAM__PORTLET_APPLICATION_ID);
        if (portletApplicationIdParam == null) {
            throw new IllegalArgumentException("No portletApplicationId available under ChannelParameter '" + IPortletAdaptor.CHANNEL_PARAM__PORTLET_APPLICATION_ID + "' for channelId:" + channelPublishId);
        }
        final String portletApplicationId = portletApplicationIdParam.getValue();
        
        final ChannelParameter portletNameParam = channelDefinition.getParameter(IPortletAdaptor.CHANNEL_PARAM__PORTLET_NAME);
        if (portletNameParam == null) {
            throw new IllegalArgumentException("No portletName available under ChannelParameter '" + IPortletAdaptor.CHANNEL_PARAM__PORTLET_NAME + "' for channelId:" + channelPublishId);
        }
        final String portletName = portletNameParam.getValue();

        return this.createPortletDefinition(channelPublishId, portletApplicationId, portletName);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#createPortletDefinition(int, java.lang.String, java.lang.String)
     */
    public IPortletDefinition createPortletDefinition(int channelPublishId, String portletApplicationId, String portletName) {
        Validate.notNull(portletApplicationId, "portletApplicationId can not be null");
        Validate.notNull(portletName, "portletName can not be null");
        
        //Create and return the defintion
        return this.portletDefinitionDao.createPortletDefinition(channelPublishId, portletApplicationId, portletName);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#getPortletDefinition(int)
     */
    public IPortletDefinition getPortletDefinition(int channelPublishId) {
        return this.portletDefinitionDao.getPortletDefinition(channelPublishId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#getOrCreatePortletDefinition(int)
     */
    public IPortletDefinition getOrCreatePortletDefinition(int channelPublishId) {
        final IPortletDefinition portletDefinition = this.getPortletDefinition(channelPublishId);
        if (portletDefinition != null) {
            return portletDefinition;
        }
        
        return this.createPortletDefinition(channelPublishId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#getOrCreatePortletDefinition(int, java.lang.String, java.lang.String)
     */
    public IPortletDefinition getOrCreatePortletDefinition(int channelPublishId, String portletApplicationId, String portletName) {
        final IPortletDefinition portletDefinition = this.getPortletDefinition(channelPublishId);
        if (portletDefinition != null) {
            return portletDefinition;
        }
        
        return this.createPortletDefinition(channelPublishId, portletApplicationId, portletName);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#getPortletDefinition(org.jasig.portal.portlet.om.IPortletDefinitionId)
     */
    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        
        return this.portletDefinitionDao.getPortletDefinition(portletDefinitionId);
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#updatePortletDefinition(org.jasig.portal.portlet.om.IPortletDefinition)
     */
    public void updatePortletDefinition(IPortletDefinition portletDefinition) {
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        
        this.portletDefinitionDao.updatePortletDefinition(portletDefinition);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#getParentPortletApplicationDescriptor(org.jasig.portal.portlet.om.IPortletDefinitionId)
     */
    public PortletAppDD getParentPortletApplicationDescriptor(IPortletDefinitionId portletDefinitionId) throws PortletContainerException {
        final IPortletDefinition portletDefinition = this.getPortletDefinition(portletDefinitionId);
        if (portletDefinition == null) {
            return null;
        }
        
        final PortletRegistryService portletRegistryService = this.optionalContainerServices.getPortletRegistryService();
        final String portletApplicationId = portletDefinition.getPortletApplicationId();
        
        return portletRegistryService.getPortletApplicationDescriptor(portletApplicationId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#getParentPortletDescriptor(org.jasig.portal.portlet.om.IPortletDefinitionId)
     */
    public PortletDD getParentPortletDescriptor(IPortletDefinitionId portletDefinitionId) throws PortletContainerException {
        final IPortletDefinition portletDefinition = this.getPortletDefinition(portletDefinitionId);
        if (portletDefinition == null) {
            return null;
        }
        
        final PortletRegistryService portletRegistryService = this.optionalContainerServices.getPortletRegistryService();
        final String portletApplicationId = portletDefinition.getPortletApplicationId();
        final String portletName = portletDefinition.getPortletName();
        
        return portletRegistryService.getPortletDescriptor(portletApplicationId, portletName);
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
