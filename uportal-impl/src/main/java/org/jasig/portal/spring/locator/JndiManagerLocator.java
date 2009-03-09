/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.jndi.IJndiManager;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated code that needs an IJndiManager should use direct dependency injection where possible
 */
@Deprecated
public class JndiManagerLocator extends AbstractBeanLocator<IJndiManager> {
    public static final String BEAN_NAME = "jndiManager";
    
    private static final Log LOG = LogFactory.getLog(JndiManagerLocator.class);
    private static AbstractBeanLocator<IJndiManager> locatorInstance;

    public static IJndiManager getJndiManager() {
        AbstractBeanLocator<IJndiManager> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(JndiManagerLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (IJndiManager)applicationContext.getBean(BEAN_NAME, IJndiManager.class);
            }
        }
        
        return locator.getInstance();
    }

    public JndiManagerLocator(IJndiManager instance) {
        super(instance, IJndiManager.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<IJndiManager> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<IJndiManager> locator) {
        locatorInstance = locator;
    }
}
