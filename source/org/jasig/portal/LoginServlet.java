/**
 * Copyright � 2001 The JA-SIG Collaborative.  All rights reserved.
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
 *
 */


package  org.jasig.portal;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonManagerFactory;
import org.jasig.portal.services.Authentication;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.CommonUtils;

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
public class LoginServlet extends HttpServlet {
  private static final String redirectString;
  private static HashMap credentialTokens;
  private static HashMap principalTokens;
  private Authentication m_authenticationService = null;

    static {
      String upFile=UPFileSpec.RENDER_URL_ELEMENT+UPFileSpec.PORTAL_URL_SEPARATOR+UserInstance.USER_LAYOUT_ROOT_NODE+UPFileSpec.PORTAL_URL_SEPARATOR+UPFileSpec.PORTAL_URL_SUFFIX;
      HashMap cHash = new HashMap(1);
      HashMap pHash = new HashMap(1);
      try {
         upFile = UPFileSpec.buildUPFile(null,UPFileSpec.RENDER_METHOD,UserInstance.USER_LAYOUT_ROOT_NODE,null,null);
         String key;
         // We retrieve the tokens representing the credential and principal
         // parameters from the security properties file.
         Properties props = ResourceLoader.getResourceAsProperties(LoginServlet.class, "/properties/security.properties");
         Enumeration propNames = props.propertyNames();
         while (propNames.hasMoreElements()) {
            String propName = (String)propNames.nextElement();
            String propValue = props.getProperty(propName);
            if (propName.startsWith("credentialToken.")) {
               key = propName.substring(16);
               cHash.put(key, propValue);
            }
            if (propName.startsWith("principalToken.")) {
               key = propName.substring(15);
               pHash.put(key, propValue);
            }
         }
      } catch(PortalException pe) {
          LogService.log(LogService.ERROR,"LoginServlet::static "+pe);
      } catch(IOException ioe) {
          LogService.log(LogService.ERROR,"LoginServlet::static "+ioe);
      }
      redirectString=upFile;
      credentialTokens=cHash;
      principalTokens=pHash;
    }

  /**
   * Initializes the servlet
   * @exception ServletException
   */
  public void init () throws ServletException {
    m_authenticationService = new Authentication();

  }

  /**
   * Forwards request to doGet() method
   * @param req
   * @param res
   * @exception ServletException
   * @exception IOException
   */
  public void doPost (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    // Just handle this request in the doGet() method
    doGet(req, res);
  }

  /**
   * Process the incoming HttpServletRequest
   * @param request
   * @param response
   * @exception ServletException
   * @exception IOException
   */
  public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  	CommonUtils.setNoCache(response);
  	// Clear out the existing session for the user
    request.getSession().invalidate();
    // Retrieve the user's session
    request.getSession(true);
    IPerson person = null;
    try {
      // Get the person object associated with the request
      person = PersonManagerFactory.getPersonManagerInstance().getPerson(request);
      // WE grab all of the principals and credentials from the request and load
      // them into their respective HashMaps.
      HashMap principals = getPropertyFromRequest (principalTokens, request);
      HashMap credentials = getPropertyFromRequest (credentialTokens, request);

      // Attempt to authenticate using the incoming request
      if ( person != null )
       m_authenticationService.authenticate(principals, credentials, person);
    } catch (Exception e) {
      // Log the exception
      LogService.log(LogService.ERROR, e);
      // Reset everything
      request.getSession(false).invalidate();
      // Add the authentication failure
      if ( person != null )
        request.getSession(true).setAttribute("up_authenticationError", "true");
    }
    // Check to see if the person has been authenticated
    if (person != null && person.getSecurityContext().isAuthenticated()) {
      // Send the now authenticated user back to the PortalSessionManager servlet
      response.sendRedirect(request.getContextPath() + '/' + redirectString);
    }
    else {
      if ( person != null )
         request.getSession(false).setAttribute("up_authenticationAttempted", "true");
      // Preserve the attempted username so it can be redisplayed to the user by CLogin
      String attemptedUserName = request.getParameter("userName");
      if (attemptedUserName != null)
        request.getSession(false).setAttribute("up_attemptedUserName", request.getParameter("userName"));
      // Send the unauthenticated user back to the PortalSessionManager servlet
      response.sendRedirect(request.getContextPath() + '/' + redirectString);
    }
  }

  /**
   * Get the values represented by each token from the request and load them into a
   * HashMap that is returned.
   * @param propNames
   * @param request
   * @return HashMap of properties
   */
  private HashMap getPropertyFromRequest (HashMap tokens, HttpServletRequest request) {
   // Iterate through all of the other property keys looking for the first property
   // named like propname that has a value in the request
    HashMap retHash = new HashMap(1);
    Iterator tokenItr = tokens.keySet().iterator();
    while (tokenItr.hasNext()) {
      String ctxName = (String)tokenItr.next();
      String parmName = (String)tokens.get(ctxName);
      String parmValue = request.getParameter(parmName);
      // null value causes exception in context.authentication
      // alternately we could just not set parm if value is null
      parmValue = (parmValue == null ? "" : parmValue).trim();
      // The relationship between the way the properties are stored and the way
      // the subcontexts are named has to be closely looked at to make this work.
      // The keys are either "root" or the subcontext name that follows "root.". As
      // as example, the contexts ["root", "root.simple", "root.cas"] are represented
      // as ["root", "simple", "cas"].
      String key = (ctxName.startsWith("root.") ? ctxName.substring(5) : ctxName);
      retHash.put(key, parmValue);
    }
    return (retHash);
  }
}
