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
	
	/**
	 * Set the remote user for this security context.
	 * 
	 * @param remoteuser the REMOTE_USER environment variable.
	 */
	public void setRemoteUser( String remoteUser ) {
	    this.remoteUser = remoteUser;
	}
}


