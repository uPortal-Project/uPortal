/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

import javax.servlet.http.HttpServletRequest;

/**
 * Manages the storage of an IPerson object in a user's session.
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 */
public interface IPersonManager {

  // Can be used to store an instance of IPerson in the user's session
  public static final String PERSON_SESSION_KEY = "org.jasig.portal.security.IPerson";

  /**
   * Returns an IPerson associated with the incoming request
   * @param request
   * @return IPerson associated with the incoming request
   */
  public IPerson getPerson(HttpServletRequest request) throws PortalSecurityException;
}



