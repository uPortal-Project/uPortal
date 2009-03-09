/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.layout.dlm.ConfigurationLoader;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated code that needs an ConfigurationLoader should use direct dependency injection where possible
 */
@Deprecated
public class ConfigurationLoaderLocator extends AbstractBeanLocator<ConfigurationLoader> {
    public static final String BEAN_NAME = "dlmConfigurationLoader";
    
    private static final Log LOG = LogFactory.getLog(ConfigurationLoaderLocator.class);
    private static AbstractBeanLocator<ConfigurationLoader> locatorInstance;

    public static ConfigurationLoader getConfigurationLoader() {
        AbstractBeanLocator<ConfigurationLoader> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(ConfigurationLoaderLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (ConfigurationLoader)applicationContext.getBean(BEAN_NAME, ConfigurationLoader.class);
            }
        }
        
        return locator.getInstance();
    }

    public ConfigurationLoaderLocator(ConfigurationLoader instance) {
        super(instance, ConfigurationLoader.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<ConfigurationLoader> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<ConfigurationLoader> locator) {
        locatorInstance = locator;
    }
}
