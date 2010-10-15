package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPortalPasswordService;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

@Deprecated
public class PortalPasswordServiceLocator extends AbstractBeanLocator<IPortalPasswordService> {
    public static final String BEAN_NAME = "localAccountDao";
    
    private static final Log LOG = LogFactory.getLog(PortalPasswordServiceLocator.class);
    private static AbstractBeanLocator<IPortalPasswordService> locatorInstance;

    public static IPortalPasswordService getPortalPasswordService() {
        AbstractBeanLocator<IPortalPasswordService> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(PortalPasswordServiceLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (IPortalPasswordService)applicationContext.getBean(BEAN_NAME, IPortalPasswordService.class);
            }
        }
        
        return locator.getInstance();
    }

    public PortalPasswordServiceLocator(IPortalPasswordService instance) {
        super(instance, IPortalPasswordService.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<IPortalPasswordService> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<IPortalPasswordService> locator) {
        locatorInstance = locator;
    }
}
