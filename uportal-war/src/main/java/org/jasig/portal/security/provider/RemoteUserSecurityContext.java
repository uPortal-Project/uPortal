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
	 * Verify that remoteUser is not null and set the principal's UID to this
	 * value.
	 * 
	 * @exception PortalSecurityException
	 */
   public synchronized void authenticate() throws PortalSecurityException {
        if (this.remoteUser != null) {
            // Set the UID for the principal
        	 this.myPrincipal.setUID(this.remoteUser);

             // Check that the principal UID matches the remote user
        	 final String newUid = this.myPrincipal.getUID();
        	 
        	 if (this.remoteUser.equals(newUid)) {
                if (log.isInfoEnabled()) {
                    log.info("Authentication REMOTE_USER(" + this.remoteUser + ").");
                }

                this.isauth = true;
            }
            else if (log.isInfoEnabled()) {
                log.info("Authentication failed. REMOTE_USER(" + this.remoteUser + ") != user(" + newUid + ").");
            }
        }
        else if (log.isInfoEnabled()) {
            log.info("Authentication failed. REMOTE_USER not set for(" + this.myPrincipal.getUID() + ").");
        }

        super.authenticate();
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


