/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

 import org.jasig.portal.AuthorizationException;
 
/**
 * <p>A context-specific factory class interface that should be implemented
 * by factory classes defined for each context provider. The provider's
 * constructor should not be public to discourage it's instantiation through
 * means other than the corresponding factory. This formalism should be
 * followed for consistency even when the factory performs no additional
 * value-add than instantiating the appropriate context class.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */

public interface IAuthorizationServiceFactory
{
  public IAuthorizationService getAuthorization() throws AuthorizationException;
}
