/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

// formatted with JxBeauty (c) johann.langhofer@nextra.at

package  org.jasig.portal;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonManagerFactory;
import org.jasig.portal.security.PortalSecurityException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Determines which user instance object to use for a given user.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision 1.1$
 */
public class UserInstanceManager {

    private static final Log log = LogFactory.getLog(UserInstanceManager.class);
    
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
      log.error( "UserInstanceManager: Unable to retrieve IPerson!", e);
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



