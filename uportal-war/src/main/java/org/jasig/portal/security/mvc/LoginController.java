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

package  org.jasig.portal.security.mvc;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.portlets.swapper.IdentitySwapperPrincipal;
import org.jasig.portal.portlets.swapper.IdentitySwapperSecurityContext;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.Authentication;
import org.jasig.portal.utils.ResourceLoader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Receives the username and password and tries to authenticate the user.
 * The form presented by org.jasig.portal.channels.CLogin is typically used
 * to generate the post to this servlet.
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 * @version $Revision$
 * @author Don Fracapane (df7@columbia.edu)
 * Added properties in the security properties file that hold the tokens used to
 * represent the principal and credential for each security context.
 */
@Controller
@RequestMapping("/Login")
public class LoginController implements InitializingBean {
    
    public static final String SWAP_TARGET_UID = LoginController.class.getName() + ".SWAP_TARGET_UID";
    public static final String SWAP_ORIGINAL_UID = LoginController.class.getName() + ".SWAP_ORIGINAL_UID";
    
    public static final String AUTH_ATTEMPTED_KEY = "up_authenticationAttempted";
    public static final String AUTH_ERROR_KEY = "up_authenticationError";
    public static final String ATTEMPTED_USERNAME_KEY = "up_attemptedUserName";
    
    protected final Log log = LogFactory.getLog(getClass());
    protected final Log swapperLog = LogFactory.getLog("org.jasig.portal.portlets.swapper");
    
    protected HashMap<String, String> credentialTokens;
    protected HashMap<String, String> principalTokens;
    protected Authentication authenticationService = null;
    
    private IPersonManager personManager;

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setAuthenticationService(Authentication authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.credentialTokens = new HashMap<String,String>(1);
        this.principalTokens = new HashMap<String,String>(1);
        
        try {
           String key;
           // We retrieve the tokens representing the credential and principal
           // parameters from the security properties file.
           Properties props = ResourceLoader.getResourceAsProperties(getClass(), "/properties/security.properties");
           Enumeration propNames = props.propertyNames();
           while (propNames.hasMoreElements()) {
              String propName = (String)propNames.nextElement();
              String propValue = props.getProperty(propName);
              if (propName.startsWith("credentialToken.")) {
                 key = propName.substring(16);
                 this.credentialTokens.put(key, propValue);
              }
              if (propName.startsWith("principalToken.")) {
                 key = propName.substring(15);
                 this.principalTokens.put(key, propValue);
              }
           }
        } catch(PortalException pe) {
            log.error("LoginServlet::static ", pe);
        } catch(IOException ioe) {
            log.error("LoginServlet::static ", ioe);
        }
    }

  /**
   * Process the incoming HttpServletRequest
   * @param request
   * @param response
   * @exception ServletException
   * @exception IOException
   */
  @RequestMapping
  public void service (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setHeader("Pragma","no-cache");
      response.setHeader("Cache-Control","no-cache");
      response.setDateHeader("Expires",0);

    // Clear out the existing session for the user if they have one
    String targetUid = null;
    String originalUid = null;
    boolean swap = false;
    if (request.isRequestedSessionIdValid()) {
    	try {
            HttpSession s = request.getSession(false);
    	    //Check if this is a swapped user hitting the Login servlet
    	    originalUid = (String)s.getAttribute(SWAP_ORIGINAL_UID);

    	    //No original person in session so check for swap request
    	    if (originalUid == null) {
        	    targetUid = (String)s.getAttribute(SWAP_TARGET_UID);
        	    if (targetUid != null) {
        	        final IPerson person = personManager.getPerson(request);
        	        originalUid = person.getName();
        	        swap = true;
        	    }
    	    }
    	    else {
    	        final IPerson person = personManager.getPerson(request);
    	        targetUid = person.getName();
    	    }

            s.invalidate();
    	} catch (IllegalStateException ise) {
    		// ISE indicates session was already invalidated.
    		// This is fine.  This servlet trying to guarantee that the session has been invalidated;
    		// it doesn't have to insist that it is the one that invalidated it.
    		if (log.isTraceEnabled()) {
    			log.trace("LoginServlet attempted to invalidate an already invalid session.", ise);
    		}
    	}
    }

  	//  Create the user's session
    HttpSession s = request.getSession(true);
    
  	IPerson person = null;
    try {
        final HashMap<String,String> principals;
        final HashMap<String,String> credentials;
        
        // Get the person object associated with the request
        person = personManager.getPerson(request);
        
        //If doing an identity swap
        if (targetUid != null && originalUid != null) {
            if (swap) {
                swapperLog.warn("Swapping identity for '" + originalUid + "' to '" + targetUid + "'");

                //Track the originating user
                s.setAttribute(SWAP_ORIGINAL_UID, originalUid);
                
                //Setup the swapped person
                person.setUserName(targetUid);
                personManager.setImpersonating(request, true);
            }
            else {
                swapperLog.warn("Reverting swapped identity from '" + targetUid + "' to '" + originalUid + "'");
                
                person.setUserName(originalUid);
                personManager.setImpersonating(request, false);
            }
            
            //Setup the custom security context
            final IdentitySwapperPrincipal identitySwapperPrincipal = new IdentitySwapperPrincipal(person);
            final IdentitySwapperSecurityContext identitySwapperSecurityContext = new IdentitySwapperSecurityContext(identitySwapperPrincipal);
            person.setSecurityContext(identitySwapperSecurityContext);
            
            principals = new HashMap<String,String>();
            credentials = new HashMap<String,String>();
        }
        //Norm authN path
        else {
          // WE grab all of the principals and credentials from the request and load
          // them into their respective HashMaps.
          principals = getPropertyFromRequest (principalTokens, request);
          credentials = getPropertyFromRequest (credentialTokens, request);
        }

      // Attempt to authenticate using the incoming request
      authenticationService.authenticate(request, principals, credentials, person);
    } catch (Exception e) {
      // Log the exception
      log.error("Exception authenticating the request", e);
      // Reset everything
      request.getSession(false).invalidate();
      // Add the authentication failure
      request.getSession(true).setAttribute("up_authenticationError", "true");
      person = null;
    }
    
    
    
    // create the redirect URL, adding fname and args parameters if necessary
    String redirectTarget = null;

    final String refUrl = request.getParameter("refUrl");
    if (refUrl != null) {
        if (refUrl.startsWith("/")) {
            redirectTarget = refUrl;
        }
        else {
            log.warn("Refernce URL passed in does not start with a / and will be ignored: " + refUrl);
        }
    }
    
    if (redirectTarget == null) {
        /* Grab the target functional name, if any, off the login request.
         * Also any arguments for the target
         * We will pass them  along after authentication.
         */
        String targetFname = request.getParameter("uP_fname");
        
    	if (targetFname == null){
    	    redirectTarget = request.getContextPath() + "/";
    	} else {
    		StringBuilder sb = new StringBuilder();
    		sb.append(request.getContextPath());
    		sb.append("?uP_fname=");
    		sb.append(URLEncoder.encode(targetFname, "UTF-8"));
    		
    		char separator = '&';
    		@SuppressWarnings("unchecked")
            Enumeration<String> e = request.getParameterNames();
    		while(e.hasMoreElements()){
    			String paramName = e.nextElement();
    			if(!paramName.equals("uP_fname")){
    				sb.append(separator);
    				sb.append(paramName);
    				sb.append('=');
    				sb.append(URLEncoder.encode(request.getParameter(paramName),"UTF-8"));
    			}
    		}		
    		redirectTarget = sb.toString();
    	}
    }

	if (person == null || !person.getSecurityContext().isAuthenticated()) {
     if ( request.getMethod().equals("POST") )
         request.getSession(false).setAttribute("up_authenticationAttempted", "true");
     // Preserve the attempted username so it can be redisplayed to the user by CLogin
     String attemptedUserName = request.getParameter("userName");
     if (attemptedUserName != null)
     	request.getSession(false).setAttribute("up_attemptedUserName", request.getParameter("userName"));		
	}
	
	final String encodedRedirectURL = response.encodeRedirectURL(redirectTarget);
    response.sendRedirect(encodedRedirectURL);

  }

  /**
   * Get the values represented by each token from the request and load them into a
   * HashMap that is returned.
   * @param tokens
   * @param request
   * @return HashMap of properties
   */
  private HashMap<String,String> getPropertyFromRequest (HashMap<String,String> tokens, HttpServletRequest request) {
   // Iterate through all of the other property keys looking for the first property
   // named like propname that has a value in the request
    HashMap<String,String> retHash = new HashMap<String,String>(1);
    
    for (Map.Entry<String, String> entry : tokens.entrySet()) {
      String contextName = entry.getKey();
      String parmName = entry.getValue();
      String parmValue = null;
      if (request.getAttribute(parmName) != null) {
    	  // Upstream components (like servlet filters) may supply information 
    	  // for the authentication process using request attributes.
    	  try {
    		  parmValue = (String) request.getAttribute(parmName);
    	  } catch (ClassCastException cce) {
    		  String msg = "The request attribute '" + parmName + "' must be a String.";
    		  throw new RuntimeException(msg, cce);
    	  }
      } else {
    	  // If a configured parameter isn't provided by a request attribute, 
    	  // check request parameters (i.e. querystring, form fields).
    	  parmValue = request.getParameter(parmName);
      }
      // null value causes exception in context.authentication
      // alternately we could just not set parm if value is null
      if("password".equals(parmName)){
    	  // make sure we don't trim passwords, since they might have
    	  // leading or trailing spaces
    	  parmValue = (parmValue == null ? "" : parmValue);
   	  } else {
    	  parmValue = (parmValue == null ? "" : parmValue).trim();
   	  }

      // The relationship between the way the properties are stored and the way
      // the subcontexts are named has to be closely looked at to make this work.
      // The keys are either "root" or the subcontext name that follows "root.". As
      // as example, the contexts ["root", "root.simple", "root.cas"] are represented
      // as ["root", "simple", "cas"].
      String key = (contextName.startsWith("root.") ? contextName.substring(5) : contextName);
      retHash.put(key, parmValue);
    }
    return (retHash);
  }


}
