package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.fragment.subscribe.dao.IUserFragmentSubscriptionDao;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.utils.cache.CacheFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@Deprecated
public class UserFragmentSubscriptionDaoLocator  extends
        AbstractBeanLocator<IUserFragmentSubscriptionDao> {

    public static final String BEAN_NAME = "jpaUserFragmentSubscriptionDaoImpl";

    private static final Log LOG = LogFactory.getLog(UserFragmentSubscriptionDaoLocator.class);
    private static AbstractBeanLocator<IUserFragmentSubscriptionDao> locatorInstance;
    
    public static IUserFragmentSubscriptionDao getUserIdentityStore() {
        AbstractBeanLocator<IUserFragmentSubscriptionDao> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(UserFragmentSubscriptionDaoLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (IUserFragmentSubscriptionDao)applicationContext.getBean(BEAN_NAME, CacheFactory.class);
            }
        }
        
        return locator.getInstance();
    }
    @Autowired(required=true)
    public UserFragmentSubscriptionDaoLocator(IUserFragmentSubscriptionDao instance) {
        super(instance, IUserFragmentSubscriptionDao.class);
    }
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<IUserFragmentSubscriptionDao> getLocator() {
        return locatorInstance;
    }
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<IUserFragmentSubscriptionDao> locator) {
        locatorInstance = locator;
    }


}
