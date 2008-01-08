/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PersonManagerFactory;
import org.jasig.portal.security.PortalSecurityException;

/**
 * Determines which user instance object to use for a given user.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision 1.1$
 */
public class UserInstanceManager {
    private static final Log log = LogFactory.getLog(UserInstanceManager.class);
    
    private static Map<Integer, GuestUserPreferencesManager> guestUserPreferencesManagers = new HashMap<Integer, GuestUserPreferencesManager>();

    /**
     * Returns the UserInstance object that is associated with the given request.
     * @param request Incoming HttpServletRequest
     * @return UserInstance object associated with the given request
     */
    public static IUserInstance getUserInstance(HttpServletRequest request) throws PortalException {
        final IPerson person;
        try {
            // Retrieve the person object that is associated with the request
            final IPersonManager personManager = PersonManagerFactory.getPersonManagerInstance();
            person = personManager.getPerson(request);
        }
        catch (Exception e) {
            log.error("Exception while retrieving IPerson!", e);
            throw new PortalSecurityException("Could not retrieve IPerson", e);
        }
        
        if (person == null) {
            throw new PortalSecurityException("PersonManager returned null person for this request.  With no user, there's no UserInstance.  Is PersonManager misconfigured?  RDBMS access misconfigured?");
        }

        final HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalStateException("An existing HttpSession is required while retrieving a UserInstance for a HttpServletRequest");
        }

        // Return the UserInstance object if it's in the session
        UserInstanceHolder userInstanceHolder = (UserInstanceHolder) session.getAttribute(UserInstanceHolder.KEY);
        if (userInstanceHolder != null) {
            final IUserInstance userInstance = userInstanceHolder.getUserInstance();
            
            if (userInstance != null) {
                return userInstance;
            }
        }

        // Create either a UserInstance or a GuestUserInstance
        final IUserInstance userInstance;
        if (person.isGuest()) {
            final Integer personId = person.getID();
            
            //Get or Create a shared GuestUserPreferencesManager for the Guest IPerson
            //sync so multiple managers aren't created for a single guest
            GuestUserPreferencesManager guestUserPreferencesManager;
            synchronized (guestUserPreferencesManagers) {
                guestUserPreferencesManager = guestUserPreferencesManagers.get(personId);
                if (guestUserPreferencesManager == null) {
                    guestUserPreferencesManager = new GuestUserPreferencesManager(person);
                    guestUserPreferencesManagers.put(personId, guestUserPreferencesManager);
                }
            }
            
            userInstance = new GuestUserInstance(person, guestUserPreferencesManager, request);
        }
        else {
            final ISecurityContext securityContext = person.getSecurityContext();
            if (securityContext.isAuthenticated()) {
                userInstance = new UserInstance(person, request);
            }
            else {
                // we can't allow for unauthenticated, non-guest user to come into the system
                throw new PortalSecurityException("System does not allow for unauthenticated non-guest users.");
            }
        }

        //Ensure the newly created UserInstance is cached in the session
        if (userInstanceHolder == null) {
            userInstanceHolder = new UserInstanceHolder();
        }
        userInstanceHolder.setUserInstance(userInstance);
        session.setAttribute(UserInstanceHolder.KEY, userInstanceHolder);

        // Return the new UserInstance
        return userInstance;
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
        private static final long serialVersionUID = 1L;

        public transient static final String KEY = UserInstanceHolder.class.getName();

        private transient IUserInstance ui = null;

        /**
         * @return Returns the userInstance.
         */
        protected IUserInstance getUserInstance() {
            return this.ui;
        }

        /**
         * @param userInstance The userInstance to set.
         */
        protected void setUserInstance(IUserInstance userInstance) {
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
