/**
 * 
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.utils.cache.CacheFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 * @deprecated code that needs an {@link IUserIdentityStore} should use direct dependency injection where possible
 */
@Deprecated
public class UserIdentityStoreLocator extends
		AbstractBeanLocator<IUserIdentityStore> {

	public static final String BEAN_NAME = "userIdentityStore";

    private static final Log LOG = LogFactory.getLog(UserIdentityStoreLocator.class);
    private static AbstractBeanLocator<IUserIdentityStore> locatorInstance;
    
    public static IUserIdentityStore getUserIdentityStore() {
    	AbstractBeanLocator<IUserIdentityStore> locator = locatorInstance;
    	if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(UserIdentityStoreLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (IUserIdentityStore)applicationContext.getBean(BEAN_NAME, CacheFactory.class);
            }
        }
        
        return locator.getInstance();
    }
    
	public UserIdentityStoreLocator(IUserIdentityStore instance) {
		super(instance, IUserIdentityStore.class);
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
	 */
	@Override
	protected AbstractBeanLocator<IUserIdentityStore> getLocator() {
		return locatorInstance;
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
	 */
	@Override
	protected void setLocator(AbstractBeanLocator<IUserIdentityStore> locator) {
		locatorInstance = locator;
	}

}
