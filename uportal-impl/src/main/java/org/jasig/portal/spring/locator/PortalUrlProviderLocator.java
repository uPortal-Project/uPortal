/**
 * 
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.url.IPortalUrlProvider;
import org.springframework.context.ApplicationContext;

/**
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @deprecated code that needs an IPortalUrlProvider should use direct dependency injection where possible
 */
@Deprecated
public class PortalUrlProviderLocator extends AbstractBeanLocator<IPortalUrlProvider> {

	public static final String BEAN_NAME = "portalUrlProvider";
    
    private static final Log LOG = LogFactory.getLog(PortalUrlProviderLocator.class);
    private static AbstractBeanLocator<IPortalUrlProvider> locatorInstance;
    
    public static IPortalUrlProvider getPortalUrlProvider() {
    	AbstractBeanLocator<IPortalUrlProvider> locator = locatorInstance;
    	if(locator == null) {
    		 final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
             applicationContext.getBean(PortalUrlProviderLocator.class.getName());
             
             locator = locatorInstance;
             if (locator == null) {
                 LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                 return (IPortalUrlProvider)applicationContext.getBean(BEAN_NAME, IPortalUrlProvider.class);
             }
    	}
    	
    	return locator.getInstance();
    }
	public PortalUrlProviderLocator(IPortalUrlProvider instance) {
		super(instance, IPortalUrlProvider.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
	 */
	@Override
	protected AbstractBeanLocator<IPortalUrlProvider> getLocator() {
		return locatorInstance;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
	 */
	@Override
	protected void setLocator(AbstractBeanLocator<IPortalUrlProvider> locator) {
		locatorInstance = locator;
	}

}
