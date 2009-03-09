/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.services;

import org.jasig.portal.spring.locator.PersonAttributeDaoLocator;
import org.jasig.services.persondir.IPersonAttributeDao;

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
        return PersonAttributeDaoLocator.getPersonAttributeDao();
    }
}