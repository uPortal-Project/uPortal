/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonManagerFactory;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.services.LogService;



/**
 * Determines which user instance object to use for a given user.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision 1.1$
 */
public class UserInstanceManager {

    // a table to keep guestUserInstance objects
    static Hashtable guestUserInstances=new Hashtable();

  /**
   * Returns the UserInstance object that is associated with the given request.
   * @param request Incoming HttpServletRequest
   * @return UserInstance object associated with the given request
   */
  public static UserInstance getUserInstance(HttpServletRequest request) throws PortalException {
    IPerson person = null;
    try {
      // Retrieve the person object that is associated with the request
      person = PersonManagerFactory.getPersonManagerInstance().getPerson(request);
    } catch (Exception e) {
      LogService.log(LogService.ERROR, "UserInstanceManager: Unable to retrieve IPerson!", e);
      throw  (new PortalSecurityException("Could not retrieve IPerson"));
    }
    // Return the UserInstance object if it's in the session
    UserInstance userInstance = (UserInstance)request.getSession(false).getAttribute("org.jasig.portal.UserInstance");
    if (userInstance != null) {
      return  (userInstance);
    }
    // Create either a UserInstance or a GuestUserInstance
    if (person.isGuest()) {
        GuestUserInstance guestUserInstance = (GuestUserInstance) guestUserInstances.get(new Integer(person.getID()));
        if(guestUserInstance==null) {
            guestUserInstance = new GuestUserInstance(person);
            guestUserInstances.put(new Integer(person.getID()),guestUserInstance);
        }
        guestUserInstance.registerSession(request);
        userInstance = guestUserInstance;
    } else {
        if(person.getSecurityContext().isAuthenticated()) {
            userInstance = new UserInstance(person);
        } else {
            // we can't allow for unauthenticated, non-guest user to come into the system
            throw new PortalSecurityException("System does not allow for unauthenticated non-guest users.");
        }
    }
    // Put the user instance in the user's session
    request.getSession(false).setAttribute("org.jasig.portal.UserInstance", userInstance);
    // Return the new UserInstance
    return  (userInstance);
  }
}



