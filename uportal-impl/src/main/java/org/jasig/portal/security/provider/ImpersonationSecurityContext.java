/* Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.security.provider;

import java.util.Enumeration;
import java.util.Vector;

import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.jasig.portal.ldap.ILdapServer;
import org.jasig.portal.ldap.LdapServices;
import org.jasig.portal.security.IAdditionalDescriptor;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.IPrincipal;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.security.provider.ChainingSecurityContext.ChainingPrincipal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImpersonationSecurityContext implements ISecurityContext {
    
	// Static Members.
	private static final Vector EMPTY_VECTOR = new Vector();
	
	// Instance Members.
	private boolean authenticated;
	private PrincipalImpl principal = new PrincipalImpl();
	private final OpaqueCredentialsImpl credentials = new OpaqueCredentialsImpl();
	private final Log log = LogFactory.getLog(ImpersonationSecurityContext.class);

	/*
	 * Public API.
	 */
	
	public int getAuthType() {
		throw new UnsupportedOperationException();
	}

	public IPrincipal getPrincipal() {
	    if (authenticated) {
			// Odd  thing to do, but ChainingSecurityContext does it...
	        return principal;
	    } else {
	        return null;
	    }
	}

	public IPrincipal getPrincipalInstance() {
		if (authenticated) {
			// Odd  thing to do, but ChainingSecurityContext does it...
			return new PrincipalImpl();
		} else {
		    return principal;
		}
	}

	public IOpaqueCredentials getOpaqueCredentials() {
	    if (authenticated) {
	        return credentials;
	    } else {
			// Odd  thing to do, but ChainingSecurityContext does it...
	        return null;
	    }
	}

	public IOpaqueCredentials getOpaqueCredentialsInstance() {
		if (authenticated) {
			// Odd  thing to do, but ChainingSecurityContext does it...
			return new OpaqueCredentialsImpl();
		} else {
			return credentials;
		}
	}
		
	public synchronized void authenticate() throws PortalSecurityException {

		String ticket = principal.getTicket();
		
		if (log.isDebugEnabled()) {
			log.debug("Attempting to authenticate() with the following ticket:  " + ticket);
		}
		
		if (ticket != null && ticket.trim().length() > 0) {
		    
	        String username = ImpersonationFilter.getUsername(ticket);
	        if (username != null) {
	            
	            // This is a valid impersonation request...
	            authenticated = true;
	            principal.setUsername(username);
	            
	            if (log.isDebugEnabled()) {
	                log.debug("Successfully impersonating the following user:  " + username);
	            }
	            
	        }

		}		

	}
		
	public IAdditionalDescriptor getAdditionalDescriptor() {
		return null;
	}
	
	public boolean isAuthenticated() {
		return principal != null;
	}
	
	public ISecurityContext getSubContext(String ctx) throws PortalSecurityException {
		return null;
	}
	
	public Enumeration getSubContexts() {
		return EMPTY_VECTOR.elements();
	}
	
	public Enumeration getSubContextNames() {
		return EMPTY_VECTOR.elements();
	}
	
	public void addSubContext(String name, ISecurityContext ctx) throws PortalSecurityException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * Nested Types.
	 */
	
	public static final class Factory implements ISecurityContextFactory {
		
		/*
		 * Public API.
		 */

		public ISecurityContext getSecurityContext() {
			return new ImpersonationSecurityContext();
		}

	}
	
	private static final class PrincipalImpl implements IPrincipal {
		
		// Instance Members.
		private String ticket = null;
		private String username = null;

		/*
		 * Public API.
		 */
		
		public String getUID() {
			return username;
		}
				
		public String getGlobalUID() {
			return username;
		}
		
		public String getFullName() {
			return username;
		}
		
		public void setUID(String UID) {
			ticket = UID;
		}
		
		public String getTicket() {
			return ticket;
		}

		public void setUsername(String username) {
			this.username = username;
		}

	}
	
	private static final class OpaqueCredentialsImpl implements IOpaqueCredentials {
		
		// Instance Members.
		private String value;
		
		/*
		 * Public API.
		 */

		public void setCredentials(byte[] credentials) {
			value = new String(credentials);
		}

		public void setCredentials(String credentials) {
			value = credentials;
		}
		
		public String getValue() {
			return value;
		}

	}

	/*
	 * Private Stuff
	 */
	
	private ImpersonationSecurityContext() {}	

}
