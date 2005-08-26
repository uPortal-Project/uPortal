/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

import java.io.Serializable;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

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
      throw  (new PortalSecurityException("Could not retrieve IPerson", e));
    }
    
    HttpSession session = request.getSession(false);
    
    // Return the UserInstance object if it's in the session
    UserInstance userInstance = null;
    UserInstanceHolder holder = (UserInstanceHolder)session.getAttribute(UserInstanceHolder.KEY);
    if (holder != null)
        userInstance = holder.getUserInstance();

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
    
    if (holder == null)
        holder = new UserInstanceHolder();
    holder.setUserInstance(userInstance);
    
    // Put the user instance in the user's session
    session.setAttribute(UserInstanceHolder.KEY, holder);

    // Return the new UserInstance
    return  (userInstance);
  }
  
    /**
     * <p>Serializable wrapper class so the UserInstance object can
     * be indirectly stored in the session. The manager can deal with
     * this class returning a null value and its field is transient
     * so the session can be serialized successfully with the
     * UserInstance object in it.</p>
     * <p>Implements HttpSessionBindingListener and delegates those methods to
     * the wrapped UserInstance, if present.</p>
     */
    private static class UserInstanceHolder implements Serializable, HttpSessionBindingListener {
        public transient static final String KEY = UserInstanceHolder.class.getName();
        
        private transient UserInstance ui = null;

        /**
         * @return Returns the userInstance.
         */
        protected UserInstance getUserInstance() {
            return this.ui;
        }

        /**
         * @param userInstance The userInstance to set.
         */
        protected void setUserInstance(UserInstance userInstance) {
            this.ui = userInstance;
        }

        public void valueBound(HttpSessionBindingEvent bindingEvent) {
            // delegate to contained UserInstance if there is one
            if (this.ui != null) {
                this.ui.valueBound(bindingEvent);
            }
            
        }

        public void valueUnbound(HttpSessionBindingEvent bindingEvent) {
            // delegate to contained UserInstance if there is one
            if (this.ui != null) {
                this.ui.valueUnbound(bindingEvent);
            }
            
        }
    }
}



