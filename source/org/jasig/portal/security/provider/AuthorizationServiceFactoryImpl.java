/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

/**
 * <p>The factory class for the uPortal reference authorization class.</p>
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IAuthorizationServiceFactory;

public class AuthorizationServiceFactoryImpl implements IAuthorizationServiceFactory
{
  public IAuthorizationService getAuthorization() throws AuthorizationException
  {
    return AuthorizationImpl.singleton();
  }
}
