/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Create a security context and store the value of remote user.  If not null,
 * the user has authenticated.
 *@author     Pete Boysen, pboysen@iastate.edu
 *@created    November 17, 2002
 *@version    $Revision$
 */
class RemoteUserSecurityContext extends ChainingSecurityContext
	 implements ISecurityContext {
    
    private static final Log log = LogFactory.getLog(RemoteUserSecurityContext.class);
    
	private final static int REMOTEUSERSECURITYAUTHTYPE = 0xFF31;
	private String remoteUser;

	/**
	 * Constructor for the RemoteUserSecurityContext object. Store the
	 * value of user for authentication.
	 */
	RemoteUserSecurityContext() {
		this(null);
	}

	/**
	 * Constructor for the RemoteUserSecurityContext object. Store the
	 * value of user for authentication.
	 *
	 * @param  user  Description of the Parameter
	 */
	RemoteUserSecurityContext(String user) {
		super();
		remoteUser = user;
	}

	/**
	 *  Gets the authType attribute of the RemoteUserSecurityContext object
	 *
	 *@return    The authType value
	 */
	public int getAuthType() {
		return REMOTEUSERSECURITYAUTHTYPE;
	}

	/**
	 * Verify that remoteUser is not null and set the principal's UID to this value.
	 *
	 *@exception  PortalSecurityException
	 */
	public synchronized void authenticate()
		throws PortalSecurityException {
		isauth = remoteUser != null;
		if (isauth) {
			myPrincipal.setUID(remoteUser);
			super.authenticate();
		} else {
			log.info( "Authentication failed. REMOTE_USER not set");
		}
		return;
	}
}


