/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.JpaInterceptor;

/**
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated code that needs an JpaInterceptor should use direct dependency injection where possible
 */
@Deprecated
public class JpaInterceptorLocator extends AbstractBeanLocator<JpaInterceptor> {
    public static final String BEAN_NAME = "jpaInterceptor";
    
    private static final Log LOG = LogFactory.getLog(JpaInterceptorLocator.class);
    private static AbstractBeanLocator<JpaInterceptor> locatorInstance;

    public static JpaInterceptor getJpaInterceptor() {
        AbstractBeanLocator<JpaInterceptor> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(JpaInterceptorLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (JpaInterceptor)applicationContext.getBean(BEAN_NAME, JpaInterceptor.class);
            }
        }
        
        return locator.getInstance();
    }

    public JpaInterceptorLocator(JpaInterceptor instance) {
        super(instance, JpaInterceptor.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<JpaInterceptor> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<JpaInterceptor> locator) {
        locatorInstance = locator;
    }
}
