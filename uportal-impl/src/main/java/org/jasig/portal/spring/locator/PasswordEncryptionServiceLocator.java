package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IStringEncryptionService;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

public class PasswordEncryptionServiceLocator extends AbstractBeanLocator<IStringEncryptionService> {
    public static final String BEAN_NAME = "passwordEncryptionService";
    
    private static final Log LOG = LogFactory.getLog(PasswordEncryptionServiceLocator.class);
    private static AbstractBeanLocator<IStringEncryptionService> locatorInstance;

    public static IStringEncryptionService getPasswordEncryptionService() {
        AbstractBeanLocator<IStringEncryptionService> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(PasswordEncryptionServiceLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (IStringEncryptionService)applicationContext.getBean(BEAN_NAME, IStringEncryptionService.class);
            }
        }
        
        return locator.getInstance();
    }

    public PasswordEncryptionServiceLocator(IStringEncryptionService instance) {
        super(instance, IStringEncryptionService.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<IStringEncryptionService> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<IStringEncryptionService> locator) {
        locatorInstance = locator;
    }
}
