/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.services;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.springframework.context.ApplicationContext;

/**
 * PersonDirectory is a static lookup mechanism for a singleton instance of 
 * IPersonAttributeDao.  It is configurable via a
 * Spring beans.dtd compliant configuration file in the properties directory
 * called personDirectory.xml (as referenced by applicationContext.xml -
 * that is, you could choose to declare the underlying IPersonAttributesDao
 * backing your PersonDirectory directly in applicationContext.xml, 
 * or elsewhere. PersonDirectory looks for an IPersonAttributeDao instance 
 * named 'personAttributeDao'.
 * 
 * This class serves as the lookup mechanism for clients to obtain a reference
 * to the singleton IPersonAttributeDao instance.  Via legacy methods, 
 * PersonDirectory also serves as the interface by which client
 * code accesses user attributes.  These deprecated legacy methods are a facade
 * to the PersonAttributeDao.
 * 
 * The default configuration of that file implements the legacy behavior of using
 * the PersonDirs.xml file for configuration.  It is expected that PersonDirs.xml
 * offers the flexibility necessary to support most uPortal installations.
 * 
 * @author Howard Gilbert
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 * @deprecated If possible classes that need access to the {@link IPersonAttributeDao} should be Spring managed beans
 * themselves and just have the dependency injected directly. This class should only be used by non Spring managed code.
 */
@Deprecated
public class PersonDirectory {

    private static final String PADAO_BEAN_NAME = "personAttributeDao";

    /**
     * Static lookup for a the configured {@link IPersonAttributeDao} implementation available from PortalApplicationContextFacade.
     * <br/>
     * <b>Clients of this method SHOULD NOT hold on to references of the returned IPersonAttributeDao. This method should
     * be called each time the dao is needed (within reason, one call for the lifetime of a method is OK). This ensures that
     * the object changing due to a context reload will not cause problems.</b> 
     * 
     * @return The PortalApplicationContextFacade configured {@link IPersonAttributeDao} implementation.
     * @throws IllegalStateException If WebApplicationContext does not supply the IPersonAttributeDao instance.
     */
    public static IPersonAttributeDao getPersonAttributeDao() {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final IPersonAttributeDao delegate = (IPersonAttributeDao)applicationContext.getBean(PADAO_BEAN_NAME, IPersonAttributeDao.class);
        
        if (delegate == null) {
            throw new IllegalStateException("A IPersonAttributeDao bean named '" + PADAO_BEAN_NAME + "' does not exist in the Spring WebApplicationContext.");
        }
                
        return delegate;
    }
}