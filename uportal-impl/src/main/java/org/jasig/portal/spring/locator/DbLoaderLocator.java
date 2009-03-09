/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.tools.dbloader.IDbLoader;
import org.springframework.context.ApplicationContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated code that needs an IDbLoader should use direct dependency injection where possible
 */
@Deprecated
public class DbLoaderLocator extends AbstractBeanLocator<IDbLoader> {
    public static final String BEAN_NAME = "dbLoader";
    
    private static final Log LOG = LogFactory.getLog(DbLoaderLocator.class);
    private static AbstractBeanLocator<IDbLoader> locatorInstance;

    public static IDbLoader getDbLoader() {
        AbstractBeanLocator<IDbLoader> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(DbLoaderLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (IDbLoader)applicationContext.getBean(BEAN_NAME, IDbLoader.class);
            }
        }
        
        return locator.getInstance();
    }

    public DbLoaderLocator(IDbLoader instance) {
        super(instance, IDbLoader.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<IDbLoader> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<IDbLoader> locator) {
        locatorInstance = locator;
    }
}
