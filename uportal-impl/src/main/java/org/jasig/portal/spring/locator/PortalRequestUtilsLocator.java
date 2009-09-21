/**
 * 
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.context.ApplicationContext;

/**
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @deprecated code that needs an IPortalRequestUtils should use direct dependency injection where possible
 */
@Deprecated
public class PortalRequestUtilsLocator extends
		AbstractBeanLocator<IPortalRequestUtils> {

	public static final String BEAN_NAME = "portalRequestUtils";
	
	private static final Log LOG = LogFactory.getLog(PortalRequestUtilsLocator.class);
	private static AbstractBeanLocator<IPortalRequestUtils> locatorInstance;
	
	public static IPortalRequestUtils getPortalRequestUtils() {
		AbstractBeanLocator<IPortalRequestUtils> locator = locatorInstance;
    	if(locator == null) {
    		 final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
             applicationContext.getBean(PortalRequestUtilsLocator.class.getName());
             
             locator = locatorInstance;
             if (locator == null) {
                 LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                 return (IPortalRequestUtils)applicationContext.getBean(BEAN_NAME, IPortalRequestUtils.class);
             }
    	}
    	
    	return locator.getInstance();
	}
	
	/**
	 * 
	 * @param instance
	 */
	public PortalRequestUtilsLocator(IPortalRequestUtils instance) {
		super(instance, IPortalRequestUtils.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
	 */
	@Override
	protected AbstractBeanLocator<IPortalRequestUtils> getLocator() {
		return locatorInstance;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
	 */
	@Override
	protected void setLocator(AbstractBeanLocator<IPortalRequestUtils> locator) {
		locatorInstance = locator;
	}

}
