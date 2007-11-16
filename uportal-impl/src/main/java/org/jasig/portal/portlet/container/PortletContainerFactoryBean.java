/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container;

import javax.servlet.ServletContext;

import org.apache.commons.lang.Validate;
import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.core.PortletContainerImpl;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.web.context.ServletContextAware;

/**
 * Factory bean for creating and initializing a {@link PortletContainer} instance. The requiredContainerServices
 * and optionalContaineServices properties are required. If desired the portletContainerName property can be set
 * to explicitly configure the container's name. By default the containers name is calculated in {@link #getContainerName()}
 * as {@link ServletContext#getServletContextName()} + "-PlutoPortletContainer".
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletContainerFactoryBean extends AbstractFactoryBean implements ServletContextAware, InitializingBean, DisposableBean {
    private PortletContainerImpl portletContainer;
    
    private ServletContext servletContext;
    private RequiredContainerServices requiredContainerServices;
    private OptionalContainerServices optionalContainerServices;
    private String portletContainerName = null;
    

    /**
     * @return the portletContainerName
     */
    public String getPortletContainerName() {
        return this.portletContainerName;
    }
    /**
     * @param portletContainerName the portletContainerName to set
     */
    public void setPortletContainerName(String portletContainerName) {
        this.portletContainerName = portletContainerName;
    }
    
    /**
     * @return the requiredContainerServices
     */
    public RequiredContainerServices getRequiredContainerServices() {
        return this.requiredContainerServices;
    }
    /**
     * @param requiredContainerServices the requiredContainerServices to set
     */
    @Required
    public void setRequiredContainerServices(RequiredContainerServices requiredContainerServices) {
        Validate.notNull(requiredContainerServices, "requiredContainerServices can not be null");
        this.requiredContainerServices = requiredContainerServices;
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
        Validate.notNull(optionalContainerServices, "optionalContainerServices can not be null");
        this.optionalContainerServices = optionalContainerServices;
    }

    /**
     * @return the servletContext
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }
    /* (non-Javadoc)
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance()
     */
    @Override
    protected Object createInstance() throws Exception {
        final String containerName = this.getContainerName();
        
        this.portletContainer = new PortletContainerImpl(containerName, this.requiredContainerServices, this.optionalContainerServices);
        this.portletContainer.init(this.servletContext);
        
        return this.portletContainer;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#destroyInstance(java.lang.Object)
     */
    @Override
    protected void destroyInstance(Object instance) throws Exception {
        ((PortletContainer)instance).destroy();
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType()
     */
    @Override
    public Class<PortletContainer> getObjectType() {
        return PortletContainer.class;
    }

    
    /**
     * @return The name to use for the pluto container.
     */
    protected String getContainerName() {
        if (this.portletContainerName != null) {
            return this.portletContainerName;
        }
        
        return this.servletContext.getServletContextName() + "-PlutoPortletContainer";
    }
}
