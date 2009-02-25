/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package  org.jasig.portal.security.provider;

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;


/**
 * <p>The factory class for the cache LDAP security context. 
 * Just returns a new instance of the CacheLdapSecurityContext.</p>
 *
 * @author Russell Tokuyama (University of Hawaii)
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated As of uPortal 2.1.3, use {@link SimpleLdapSecurityContextFactory} chained with {@link CacheSecurityContextFactory} instead
 */
public class CacheLdapSecurityContextFactory implements ISecurityContextFactory {

  /**
   * Returns a new CacheLdapSecurityContext
   * @return a new CacheLdapSecurityContext
   */
  public ISecurityContext getSecurityContext () {
    return new CacheLdapSecurityContext();
  }
}



