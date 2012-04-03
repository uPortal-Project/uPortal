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

import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.driver.PortalDriverContainerServices;
import org.apache.pluto.container.driver.PortletRegistryService;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.concurrency.caching.RequestCache;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.utils.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

/**
 * Implementation of the definition registry, pulls together the related parts of the framework for creation and access
 * of {@link IPortletDefinition}s.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portletDefinitionRegistry")
public class PortletDefinitionRegistryImpl implements IPortletDefinitionRegistry, ServletContextAware {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
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
    
    /* (non-Javadoc)
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#getPortletDefinition(org.jasig.portal.portlet.om.IPortletDefinitionId)
     */
    @Override
    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        
        return this.portletDefinitionDao.getPortletDefinition(portletDefinitionId);
    }
    
    @Override
    @RequestCache
    public IPortletDefinition getPortletDefinition(String portletDefinitionIdString) {
        Validate.notNull(portletDefinitionIdString, "portletDefinitionId can not be null");
        
        return this.portletDefinitionDao.getPortletDefinition(portletDefinitionIdString);
	}
    
    @Override
    @RequestCache
    public IPortletDefinition getPortletDefinitionByFname(String fname) {
        Validate.notNull(fname, "portletFname can not be null");
        
        return this.portletDefinitionDao.getPortletDefinitionByFname(fname);
	}
    
    @Override
    @RequestCache
    public IPortletDefinition getPortletDefinitionByName(String name) {
        Validate.notNull(name, "portletFname can not be null");
        
        return this.portletDefinitionDao.getPortletDefinitionByName(name);
	}

    @Override
    public List<IPortletDefinition> searchForPortlets(String term, boolean allowPartial) {
    	return this.portletDefinitionDao.searchForPortlets(term, allowPartial);
    }

    @Override
    public List<IPortletDefinition> getAllPortletDefinitions() {
    	return this.portletDefinitionDao.getPortletDefinitions();
    }

    @Override
    public IPortletDefinition createPortletDefinition(IPortletType portletType, String fname, String name, String title, String applicationId, String portletName, boolean isFramework) {
        return this.portletDefinitionDao.createPortletDefinition(portletType, fname, name, title, applicationId, portletName, isFramework);
    }

	/* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#savePortletDefinition(org.jasig.portal.portlet.om.IPortletDefinition)
     */
    @Override
    public IPortletDefinition updatePortletDefinition(IPortletDefinition portletDefinition) {
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        return this.portletDefinitionDao.updatePortletDefinition(portletDefinition);
    }
    
    @Override
    public void deletePortletDefinition(IPortletDefinition portletDefinition) {
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        this.portletDefinitionDao.deletePortletDefinition(portletDefinition);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#getParentPortletApplicationDescriptor(org.jasig.portal.portlet.om.IPortletDefinitionId)
     */
    @Override
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
            this.logger.warn("No portlet application descriptor could be found likely not deplotyed. For portlet definition: " + portletDefinition, e);
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletDefinitionRegistry#getParentPortletDescriptor(org.jasig.portal.portlet.om.IPortletDefinitionId)
     */
    @Override
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
            if (this.logger.isDebugEnabled()) {
                this.logger.warn("No portlet descriptor could be found for the portlet definition, null will be returned: " + portletDefinition, e);
            }
            else {
                this.logger.warn("No portlet descriptor could be found for the portlet definition, null will be returned: " + portletDefinition + " Enable DEBUG for stack trace.");
            }
            return null;
        }
    }
    
    /**
     * Get the portletApplicationId and portletName for the specified channel definition id. The portletApplicationId
     * will be {@link Tuple#first} and the portletName will be {@link Tuple#second}
     */
    @Override
    public Tuple<String, String> getPortletDescriptorKeys(IPortletDefinition portletDefinition) {
        
        final String portletApplicationId;
        if (portletDefinition.getPortletDescriptorKey().isFrameworkPortlet()) {
            portletApplicationId = this.servletContext.getContextPath();
        }
        else {
            portletApplicationId = portletDefinition.getPortletDescriptorKey().getWebAppName();
        }
        
        final String portletName = portletDefinition.getPortletDescriptorKey().getPortletName();
        
        return new Tuple<String, String>(portletApplicationId, portletName);
    }
    
}
