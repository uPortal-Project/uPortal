/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.car.CarResources;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated code that needs an CarResources should use direct dependency injection where possible
 */
@Deprecated
public class CarResourcesLocator extends AbstractBeanLocator<CarResources> {
    public static final String BEAN_NAME = "carResources";
    
    private static final Log LOG = LogFactory.getLog(CarResourcesLocator.class);
    private static AbstractBeanLocator<CarResources> locatorInstance;

    public static CarResources getCarResources() {
        AbstractBeanLocator<CarResources> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(CarResourcesLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (CarResources)applicationContext.getBean(BEAN_NAME, CarResources.class);
            }
        }
        
        return locator.getInstance();
    }

    public CarResourcesLocator(CarResources instance) {
        super(instance, CarResources.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<CarResources> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<CarResources> locator) {
        locatorInstance = locator;
    }
}
