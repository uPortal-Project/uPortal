/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;


/**
 * Static bean factory locator service for the uPortal framework.
 * <br>
 * This class reads the 'properties/beanRefFactory.xml' file to use for
 * retrieving an application context's {@link BeanFactory}. This allows
 * the factories to be used by any component of the uPortal framework in
 * a singleton manner. 
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public final class PortalApplicationContextFacade {
    /**
     * Location this class uses to initialize the {@link SingletonBeanFactoryLocator}
     * used to marshall application contexts from.
     */
    public static String BEAN_REF_FACTORY_PATH = "properties/beanRefFactory.xml";
    
    /**
     * Name of the default portal application context.
     */
    public static String PORTAL_CONTEXT_NAME = "org.jasig.portal";
    
    /**
     * Calls {@link #getApplicationContext(String)} with
     * {@link PortalApplicationContextFacade#PORTAL_CONTEXT_NAME} as the
     * argument.
     * 
     * @see #getApplicationContext(String)
     */
    public static BeanFactory getPortalApplicationContext() {
        return getApplicationContext(PORTAL_CONTEXT_NAME);
    }
    
    /**
     * Gets the {@link BeanFactory} for the specified application context.
     * 
     * @param contextName The name of the application context {@link BeanFactory} to return.
     * @return The (singleton) {@link BeanFactory} for the application context. 
     */
    public static BeanFactory getApplicationContext(final String contextName) {
        final BeanFactoryLocator locator = SingletonBeanFactoryLocator.getInstance(BEAN_REF_FACTORY_PATH);
        final BeanFactoryReference reference = locator.useBeanFactory(contextName);
        return reference.getFactory();
    }
    
    /** ensure this class can't be instantiated */
    private PortalApplicationContextFacade() { }
}
