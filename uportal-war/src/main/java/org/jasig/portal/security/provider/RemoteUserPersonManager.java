/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.security.provider;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.PortalSecurityException;

/**
 * When retrieving a new person, the value of the <code>REMOTEUSER</code> environment variable
 * is passed to the security context.  If it is set then the server has authenticated
 * the user and the username may be used for login.
 *
 * @author Pete Boysen (pboysen@iastate.edu)
 * @version $Revision$
 */
public class RemoteUserPersonManager extends AbstractPersonManager {

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
	    final HttpSession session = request.getSession(false);
        IPerson person = null;
        if (session != null) {
            
            person = (IPerson) session.getAttribute(PERSON_SESSION_KEY);
            if (person != null) {
                return person;
            }
        }

		try {
			// Create a new instance of a person
			person = PersonFactory.createGuestPerson();
			
			// If the user has authenticated with the server which has implemented web authentication,
			// the REMOTE_USER environment variable will be set.			
			String remoteUser = request.getRemoteUser();

			// We don't want to ignore the security contexts which are already configured in security.properties, so we
			// retrieve the existing security contexts.  If one of the existing security contexts is a RemoteUserSecurityContext,
			// we set the REMOTE_USER field of the existing RemoteUserSecurityContext context.
			//
			// If a RemoteUserSecurityContext does not already exist, we create one and populate the REMOTE_USER field.
			
			ISecurityContext context = null;
			Enumeration subContexts = null;
			boolean remoteUserSecurityContextExists = false;
			
			// Retrieve existing security contexts.
			context = person.getSecurityContext( );			
			if ( context != null )
			    subContexts = context.getSubContexts( );			
			
			if ( subContexts != null ) {			    			
				while ( subContexts.hasMoreElements( ) ) {
				    ISecurityContext ctx = (ISecurityContext)subContexts.nextElement( );
				    // Check to see if a RemoteUserSecurityContext already exists, and set the REMOTE_USER
				    if ( ctx instanceof RemoteUserSecurityContext ) {
				        RemoteUserSecurityContext remoteuserctx = (RemoteUserSecurityContext)ctx;
				        remoteuserctx.setRemoteUser( remoteUser );
				        remoteUserSecurityContextExists = true;
				    }			        
				}			
			}
						
			// If a RemoteUserSecurityContext doesn't alreay exist, create one.  
			// This preserves the default behavior of this class.
			if ( ! remoteUserSecurityContextExists ) {
			    RemoteUserSecurityContext remoteuserctx = new RemoteUserSecurityContext(remoteUser);
				person.setSecurityContext(remoteuserctx);
			}
		}
		catch (Exception e) {
			// Log the exception
			log.error("Exception creating person for request " + request, e);
		}
        if (session != null) {
            // Add this person object to the user's session
            session.setAttribute(PERSON_SESSION_KEY, person);
        }
		// Return the new person object
		return (person);
	}
}


