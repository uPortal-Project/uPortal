/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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



