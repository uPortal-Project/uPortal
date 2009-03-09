/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.spring.locator;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated code that needs an DataSource should use direct dependency injection where possible
 */
@Deprecated
public class PortalDbLocator extends AbstractBeanLocator<DataSource> {
    public static final String BEAN_NAME = RDBMServices.PORTAL_DB;
    
    private static final Log LOG = LogFactory.getLog(PortalDbLocator.class);
    private static AbstractBeanLocator<DataSource> locatorInstance;

    public static DataSource getPortalDb() {
        AbstractBeanLocator<DataSource> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(PortalDbLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (DataSource)applicationContext.getBean(BEAN_NAME, DataSource.class);
            }
        }
        
        return locator.getInstance();
    }

    public PortalDbLocator(DataSource instance) {
        super(instance, DataSource.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<DataSource> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<DataSource> locator) {
        locatorInstance = locator;
    }
}
