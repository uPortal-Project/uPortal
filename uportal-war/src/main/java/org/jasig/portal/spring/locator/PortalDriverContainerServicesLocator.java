/**
 * 
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.driver.OptionalContainerServices;
import org.apache.pluto.container.driver.PortalDriverContainerServices;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * @author nblair
 * @deprecated code that needs an OptionalContainerServices should use direct dependency injection where possible
 */
@Deprecated
public class PortalDriverContainerServicesLocator extends
		AbstractBeanLocator<PortalDriverContainerServices> {

	public static final String BEAN_NAME = "portalDriverContainerServices";
    
    private static final Log LOG = LogFactory.getLog(PortalDriverContainerServicesLocator.class);
    private static AbstractBeanLocator<PortalDriverContainerServices> locatorInstance;

    public static PortalDriverContainerServices getPortalDriverContainerServices() {
        AbstractBeanLocator<PortalDriverContainerServices> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(PortalDriverContainerServicesLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (PortalDriverContainerServices)applicationContext.getBean(BEAN_NAME, OptionalContainerServices.class);
            }
        }
        
        return locator.getInstance();
    }

    public PortalDriverContainerServicesLocator(PortalDriverContainerServices instance) {
        super(instance, PortalDriverContainerServices.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<PortalDriverContainerServices> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<PortalDriverContainerServices> locator) {
        locatorInstance = locator;
    }
}
