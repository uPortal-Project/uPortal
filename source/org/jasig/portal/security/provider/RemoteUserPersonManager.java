/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.PortalSecurityException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * When retrieving a new person, the value of the <code>REMOTEUSER</code> environment variable
 * is passed to the security context.  If it is set then the server has authenticated
 * the user and the username may be used for login.
 *
 * @author Pete Boysen (pboysen@iastate.edu)
 * @version $Revision$
 */
public class RemoteUserPersonManager implements IPersonManager {

    private static final Log log = LogFactory.getLog(RemoteUserPersonManager.class);
    
	/**
	 *  Description of the Field
	 */
	public final static String REMOTE_USER = "remote_user";

	/**
	 * Retrieve an IPerson object for the incoming request
	 *
	 * @param request
	 * @return IPerson object for the incoming request
	 * @exception PortalSecurityException Description of the Exception
	 */
	public IPerson getPerson(HttpServletRequest request)
		throws PortalSecurityException {
		// Return the person object if it exists in the user's session
		IPerson person = (IPerson) request.getSession(false).getAttribute(PERSON_SESSION_KEY);
		if (person != null)
			return person;
		try {
			// Create a new instance of a person
			person = PersonFactory.createGuestPerson();
			// If the user has authenticated with the server which has implemented web authentication,
			// the REMOTEUSER environment variable will be set.
			String remoteUser = request.getRemoteUser();
			RemoteUserSecurityContext context = new RemoteUserSecurityContext(remoteUser);
			person.setSecurityContext(context);
		}
		catch (Exception e) {
			// Log the exception
			log.error("Exception creating person for request " + request, e);
		}
		// Add this person object to the user's session
		request.getSession(false).setAttribute(PERSON_SESSION_KEY, person);
		// Return the new person object
		return (person);
	}
}


