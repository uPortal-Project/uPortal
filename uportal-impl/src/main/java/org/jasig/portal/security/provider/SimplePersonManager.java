/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.PortalSecurityException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages the storage of an IPerson object in a user's session.
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 */
public class SimplePersonManager implements IPersonManager {
    
    private static final Log log = LogFactory.getLog(SimplePersonManager.class);
    
  /**
   * Retrieve an IPerson object for the incoming request
   * @param request the servlet request object
   * @return the IPerson object for the incoming request
   */
  public IPerson getPerson (HttpServletRequest request) throws PortalSecurityException {
    HttpSession session = request.getSession(false);
    IPerson person = null;
    // Return the person object if it exists in the user's session
    if (session != null)
      person = (IPerson)session.getAttribute(PERSON_SESSION_KEY);
    if (person == null) {
      try {
        // Create a guest person
        person = PersonFactory.createGuestPerson();
      } catch (Exception e) {
        // Log the exception
        log.error("Exception creating guest person.", e);
      }
      // Add this person object to the user's session
      if (person != null && session != null)
        session.setAttribute(PERSON_SESSION_KEY, person);
    }
    return person;
  }
}



