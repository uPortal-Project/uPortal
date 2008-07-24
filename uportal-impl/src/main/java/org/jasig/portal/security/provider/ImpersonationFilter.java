/* Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.security.provider;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonManagerFactory;
import org.jasig.portal.security.provider.AuthorizationImpl;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ImpersonationFilter implements Filter {
	
	// Static Members.
	private static final String REAL_USERNAME_KEY = ImpersonationFilter.class.getName() + ".realUsername";
    private static Map<String,String> TICKETS = null;

    // Instance Members.
    private String principalToken = null;
    private final Log log = LogFactory.getLog(this.getClass());
    

    /*
	 * Public API.
	 */

    /**
     * Pass this value as the 'impersonateUser' request parameter to exit 
     * impersonation.
     */
    public static final String EXIT_SIGNAL = "EXIT_FRAGMENT_ADMINISTRATION";
    
    /**
     * Provides the username associated with the specified <code>ticket</code> 
     * or <code>null</code> if the ticket isn't recognized.
     * 
     * @param ticket A token provided as a <code>principalToken</code> by this 
     * class to the <code>ImpersonationSecurityContext</code>
     * @return A username to impersonate or <code>null</code>
     */
    public static String getUsername(String ticket) {
    	
    	// Assertions.
    	if (ticket == null) {
    		String msg = "Argument 'ticket' cannot be null.";
    		throw new IllegalArgumentException(msg);
    	}
    	
    	if (TICKETS == null) {
    		// The filter is not deployed...
    		String msg = "The TICKETS collection has not been initialized by " +
    						"the init() method;  probably ImpersonationFilter " +
    						"isn't defined properly in web.xml.";
    		throw new IllegalStateException(msg);
    	}
    	
    	return TICKETS.get(ticket);
    	
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    	
    	// Prep the TICKETS collection...
    	initTickets();
    	
    	// Figure out the principalToken...
    	InputStream inpt = null;
    	try {
    		
    		// Get properties into a workable interface...
    		Map<String,String> sprops = new HashMap<String,String>();
        	Properties p = new Properties();
        	inpt = getClass().getResourceAsStream("/properties/security.properties");
        	p.load(inpt);
    		for (Map.Entry<Object,Object> y : p.entrySet()) {
    			sprops.put((String) y.getKey(), (String) y.getValue());
    		}
    		
    		// Find the name under which the factory is defined...
    		String facName = null;
        	for (Map.Entry<String,String> y : sprops.entrySet()) {
        		if (y.getValue().equals(ImpersonationSecurityContext.Factory.class.getName())) {
        			facName = y.getKey();
        		}
        	}
        	
        	if (log.isDebugEnabled()) {
        		log.debug("Found the following factory name for " +
        				"ImpersonationSecurityContext.Factory:  " + 
        				facName);
        	}
        	
        	// Find the best principalToken...
        	String token = null;
        	List<String> nameParts = new ArrayList(Arrays.asList(facName.split("\\.")));
        	nameParts.add(0, "principalToken");
        	
        	while (token == null && nameParts.size() > 1) {
        		
        		// Prepare the next-best name to try...
        		StringBuilder tryMe = new StringBuilder();
        		for (String part : nameParts) {
        			tryMe.append(part).append(".");
        		}
        		tryMe.setLength(tryMe.length() - 1);	// remove the last '.' character
        		
        		if (log.isDebugEnabled()) {
        			log.debug("Looking for a factory name of '" + tryMe.toString() + "'");
        		}
        		
        		// See if we have a match...
        		if (sprops.containsKey(tryMe.toString())) {
        			token = sprops.get(tryMe.toString());
        		}
        		
        	}
        	
        	// Make sure we have principalToken by now...
        	if (token == null) {
        		String msg = "Unable to locate a principalToken that applies to " +
        						"ImpersonationSecurityContext.Factory.";
        		throw new RuntimeException(msg);
        	}
        	
        	principalToken = token;
        	
    	} catch (Throwable t) {
    		log.error("Falied to read security.properties", t);
    		throw new ServletException("Falied to read security.properties", t);
    	} finally {
    		if (inpt != null) {
    			try { inpt.close(); } catch (Throwable t) { throw new RuntimeException(t); }
    		}
    	}

    }

	public void destroy() { /* Nothing to do...*/ }
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
										throws IOException, ServletException {
		
		if (log.isDebugEnabled()) {
			log.debug("Entering ImpersonationFilter.doFilter().");
		}

		// We need method-scoped references for these...
		String ticket = null;
		String preserveUsername = null;
		
		// Determine if the user is requesting impersonation...
		String targetUser = req.getParameter("impersonateUser");
		if (targetUser != null && targetUser.trim().length() != 0) {
			
			if (log.isDebugEnabled()) {
				log.debug("A user is requesting impersonation of the '" 
										+ targetUser + "' user.");
			}
			
			if (!targetUser.equals(EXIT_SIGNAL)) {
				
				// The user is requesting to enter fragment administration...
				boolean permitIt = false;	// Assume no until we prove otherwise...

				/*
				 * The role of this filter is to determine whether the requester should 
				 * be permitted to become the user he's requesting to become.  This 
				 * determination rests on 2 factors:
				 *  - (1) Is the user authenticated?
				 *  - (2) Is the user permitted to become the specified user?
				 */
				
				// Obtain a reference to the IPerson, if present...
				IPerson person = PersonManagerFactory.getPersonManagerInstance()
									.getPerson((HttpServletRequest) req);
				if (person != null && person.getSecurityContext().isAuthenticated()) {
					
					if (log.isDebugEnabled()) {
						log.debug("User '" + person.getAttribute(IPerson.USERNAME) + "' is authenticated, and "
											+ "is requesting impersonation of the '"  
											+ targetUser + "' user.");
					}

					// Now we know the requester is authenticated, let's 
					// make sure he's authorized to do what he's trying to do...
					IAuthorizationService authServ = AuthorizationImpl.singleton();
					IAuthorizationPrincipal principal = authServ.newPrincipal((String) person.getAttribute(IPerson.USERNAME), IPerson.class);
					IPermission[] grants = authServ.getAllPermissionsForPrincipal(principal, null, "IMPERSONATE", null);
					for (IPermission p : grants) {
						if (p.getType().equals(IPermission.PERMISSION_TYPE_GRANT) && targetUser.matches(p.getTarget())) {
							permitIt = true;
							break;
						}
					}
					
					if (log.isDebugEnabled()) {
						log.debug("User '" + person.getAttribute(IPerson.USERNAME) + "' is " 
								+ (permitIt? "" : "*NOT* ") + "authorized to " 
								+ "impersonate the '" + targetUser + "' user.");
					}
					
				}
				
				if (permitIt) {
					preserveUsername = (String) person.getAttribute(IPerson.USERNAME);
					do {
						ticket = this.toString() + "." + System.currentTimeMillis();
					} while (TICKETS.containsKey(ticket));
					TICKETS.put(ticket, targetUser);
					req.setAttribute(principalToken, ticket);
				}

			} else {
				
				// The user is requesting to exit fragment administration...
				String realUsername = (String) ((HttpServletRequest) req).getSession(true).getAttribute(REAL_USERNAME_KEY);
				
				if (realUsername != null && realUsername.trim().length() != 0) {
					
					if (log.isDebugEnabled()) {
						log.debug("User '" +realUsername + "' is exiting impersonation.");
					}

					do {
						ticket = this.toString() + "." + System.currentTimeMillis();
					} while (TICKETS.containsKey(ticket));
					TICKETS.put(ticket, realUsername);
					req.setAttribute(principalToken, ticket);

				} else {
					log.error("There was a request to exit impersonation, " +
									"but the realUsername was missing.");
				}
				
			}
			
		
		}
		
		// Proceed to login...
		try {
			chain.doFilter(req, res);
		} finally {
			
			// Clean data from the TICKETS map...
			if (ticket != null) {
				// Remove from the collection lest we introduce a memory leak...
				TICKETS.remove(ticket);
			}

		}
		
		if (preserveUsername != null) {
			// We entered impersonation;  hold onto the real username to exit later...
			((HttpServletRequest) req).getSession(true).setAttribute(REAL_USERNAME_KEY, preserveUsername);
		}			
				
	}
	
	/*
	 * Private Stuff.
	 */
	
	private static synchronized void initTickets() {
		if (TICKETS == null) {
			TICKETS = new ConcurrentHashMap<String,String>();
		}
	}

}
