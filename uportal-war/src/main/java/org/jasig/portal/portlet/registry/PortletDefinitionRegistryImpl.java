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

package org.jasig.portal.portlet.registry;

import javax.servlet.ServletContext;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.driver.PortalDriverContainerServices;
import org.apache.pluto.container.driver.PortletRegistryService;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.utils.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

/**
 * Implementation of the definition registry, pulls together the related parts of the framework for creation and access
 * of {@link IPortletDefinition}s.
 * 
 * TODO this needs to listen for channel deletion events and remove the corresponding portlet definition, this would likley need a hook in ChannelRegistryManager.removeChannel
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portletDefinitionRegistry")
public class PortletDefinitionRegistryImpl implements IPortletDefinitionRegistry, ServletContextAware {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IChannelRegistryStore channelRegistryStore;
    private IPortletDefinitionDao portletDefinitionDao;
    private PortalDriverContainerServices portalDriverContainerServices;
    private ServletContext servletContext;
    
    /**
     * @return the portletDefinitionDao
     */
    public IPortletDefinitionDao getPortletDefinitionDao() {
        return this.portletDefinitionDao;
    }
    /**
     * @param portletDefinitionDao the portletDefinitionDao to set
     */
    @Autowired
    public void setPortletDefinitionDao(IPortletDefinitionDao portletDefinitionDao) {
        this.portletDefinitionDao = portletDefinitionDao;
    }
    /**
     * 
     * @return
     */
    public PortalDriverContainerServices getPortalDriverContainerServices() {
		return portalDriverContainerServices;
	}
    /**
     * 
     * @param portalDriverContainerServices
     */
    @Autowired
	public void setPortalDriverContainerServices(
			PortalDriverContainerServices portalDriverContainerServices) {
		this.portalDriverContainerServices = portalDriverContainerServices;
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
    @Autowired
    public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
        this.channelRegistryStore = channelRegistryStore;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#createPortletDefinition(int)
     */
    @Deprecated
    public IPortletDefinition createPortletDefinition(int channelPublishId) {
        final IChannelDefinition channelDefinition = this.channelRegistryStore.getChannelDefinition(channelPublishId);
        return channelDefinition.getPortletDefinition();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#getPortletDefinition(int)
     */
    @Deprecated
    public IPortletDefinition getPortletDefinition(int channelPublishId) {
        final IChannelDefinition channelDefinition = this.channelRegistryStore.getChannelDefinition(channelPublishId);
        return channelDefinition.getPortletDefinition();
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
    public PortletApplicationDefinition getParentPortletApplicationDescriptor(IPortletDefinitionId portletDefinitionId) {
        final IPortletDefinition portletDefinition = this.getPortletDefinition(portletDefinitionId);
        if (portletDefinition == null) {
            return null;
        }
        
        final Tuple<String, String> portletDescriptorKeys = this.getPortletDescriptorKeys(portletDefinition);
        
        final PortletRegistryService portletRegistryService = this.portalDriverContainerServices.getPortletRegistryService();
        try {
            return portletRegistryService.getPortletApplication(portletDescriptorKeys.first);
        }
        catch (PortletContainerException e) {
            throw new DataRetrievalFailureException("No portlet application descriptor could be found for the portlet definition: " + portletDefinition, e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#getParentPortletDescriptor(org.jasig.portal.portlet.om.IPortletDefinitionId)
     */
    public PortletDefinition getParentPortletDescriptor(IPortletDefinitionId portletDefinitionId) {
        final IPortletDefinition portletDefinition = this.getPortletDefinition(portletDefinitionId);
        if (portletDefinition == null) {
            return null;
        }
        
        final Tuple<String, String> portletDescriptorKeys = this.getPortletDescriptorKeys(portletDefinition);
        
        final PortletRegistryService portletRegistryService = this.portalDriverContainerServices.getPortletRegistryService();
        try {
            return portletRegistryService.getPortlet(portletDescriptorKeys.first, portletDescriptorKeys.second);
        }
        catch (PortletContainerException e) {
            throw new DataRetrievalFailureException("No portlet descriptor could be found for the portlet definition: " + portletDefinition, e);
        }
    }
    
    /**
     * Get the portletApplicationId and portletName for the specified channel definition id. The portletApplicationId
     * will be {@link Tuple#first} and the portletName will be {@link Tuple#second}
     */
    public Tuple<String, String> getPortletDescriptorKeys(IPortletDefinition portletDefinition) {
        final IChannelDefinition channelDefinition = portletDefinition.getChannelDefinition();
        
        final String portletApplicationId;
        final IChannelParameter isFrameworkPortletParam = channelDefinition.getParameter(IPortletRenderer.CHANNEL_PARAM__IS_FRAMEWORK_PORTLET);
        if (isFrameworkPortletParam != null && Boolean.valueOf(isFrameworkPortletParam.getValue())) {
            portletApplicationId = this.servletContext.getContextPath();
        }
        else {
            final IChannelParameter portletApplicaitonIdParam = channelDefinition.getParameter(IPortletRenderer.CHANNEL_PARAM__PORTLET_APPLICATION_ID);
            if (portletApplicaitonIdParam == null) {
                throw new NotAPortletException("The specified ChannelDefinition does not provide the needed channel parameter '" + IPortletRenderer.CHANNEL_PARAM__PORTLET_APPLICATION_ID + "'. ChannelDefinition=" + channelDefinition);
            }
            
            portletApplicationId = portletApplicaitonIdParam.getValue();
        }
        
        final IChannelParameter portletNameParam = channelDefinition.getParameter(IPortletRenderer.CHANNEL_PARAM__PORTLET_NAME);
        if (portletNameParam == null) {
            throw new NotAPortletException("The specified ChannelDefinition does not provide the needed channel parameter '" + IPortletRenderer.CHANNEL_PARAM__PORTLET_NAME + "'. ChannelDefinition=" + channelDefinition);
        }
        final String portletName = portletNameParam.getValue();
        
        return new Tuple<String, String>(portletApplicationId, portletName);
    }
}
