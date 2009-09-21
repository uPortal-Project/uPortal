/**
 * 
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.url.IUrlGenerator;
import org.springframework.context.ApplicationContext;

/**
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @deprecated code that needs an IPortalUrlProvider should use direct dependency injection where possible
 */
@Deprecated
public class UrlGeneratorLocator extends AbstractBeanLocator<IUrlGenerator> {

	/**
	 * This locator shares the same value for BEAN_NAME with PortalUrlProviderLocator as the implementation
	 * we are currently using implements both IPortalUrlProvider and IUrlGenerator.
	 */
	public static final String BEAN_NAME = "portalUrlProvider";

	private static final Log LOG = LogFactory.getLog(UrlGeneratorLocator.class);
	private static AbstractBeanLocator<IUrlGenerator> locatorInstance;

	public static IUrlGenerator getUrlGenerator() {
		AbstractBeanLocator<IUrlGenerator> locator = locatorInstance;
    	if(locator == null) {
    		 final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
             applicationContext.getBean(UrlGeneratorLocator.class.getName());
             
             locator = locatorInstance;
             if (locator == null) {
                 LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                 return (IUrlGenerator)applicationContext.getBean(BEAN_NAME, IUrlGenerator.class);
             }
    	}
    	
    	return locator.getInstance();
	}
	
	public UrlGeneratorLocator(IUrlGenerator instance) {
		super(instance, IUrlGenerator.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
	 */
	@Override
	protected AbstractBeanLocator<IUrlGenerator> getLocator() {
		return locatorInstance;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
	 */
	@Override
	protected void setLocator(AbstractBeanLocator<IUrlGenerator> locator) {
		locatorInstance = locator;
	}



}
