/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Provides base functionality for a static reference bean locator. Used by legacy code that
 * is not managed within spring, avoids direct use of PortalApplicationContextLocator by
 * client code and in the case of an already created ApplicationConext uses the bean refernce
 * injected by the context.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractBeanLocator<T> implements DisposableBean, InitializingBean {
    protected final Log logger = LogFactory.getLog(AbstractBeanLocator.class);
    
    private final T instance;
    
    public AbstractBeanLocator(T instance, Class<T> type) {
        Assert.notNull(instance, "instance must not be null");
        Assert.notNull(type, "type must not be null");
        Assert.isInstanceOf(type, instance, instance + " must implement " + type);

        this.instance = instance;
    }
    
    protected abstract void setLocator(AbstractBeanLocator<T> locator);
    
    protected abstract AbstractBeanLocator<T> getLocator();

    public final T getInstance() {
        return this.instance;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public final void afterPropertiesSet() throws Exception {
        if (this.getLocator() != null) {
            logger.warn("Static " + this.getClass().getName() + " reference has already been set and setInstance is being called");
        }
        this.setLocator(this);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public final void destroy() throws Exception {
        if (this.getLocator() == null) {
            logger.warn("Static " + this.getClass().getName() + " reference is already null and destroy is being called");
        }
        this.setLocator(null);
    }
}
