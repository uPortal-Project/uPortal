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
 *
 */


package  org.jasig.portal;

import  javax.servlet.http.HttpServlet;
import  javax.servlet.http.HttpServletRequest;
import  javax.servlet.http.HttpServletResponse;
import  javax.servlet.http.HttpSession;
import  javax.servlet.ServletException;
import  java.io.IOException;
import  java.util.Enumeration;
import  java.util.HashMap;
import  org.jasig.portal.services.Authentication;
import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.security.PersonManagerFactory;
import  org.jasig.portal.services.LogService;
import  org.jasig.portal.utils.ResourceLoader;
import  java.util.Properties;

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
public class AuthenticationServlet extends HttpServlet {
  private static final String redirectString;
  private Authentication m_authenticationService = null;

    static {
      String upFile=UPFileSpec.RENDER_URL_ELEMENT+UPFileSpec.PORTAL_URL_SEPARATOR+UserInstance.USER_LAYOUT_ROOT_NODE+UPFileSpec.PORTAL_URL_SEPARATOR+UPFileSpec.PORTAL_URL_SUFFIX;
      try {
          upFile = UPFileSpec.buildUPFile(null,UPFileSpec.RENDER_METHOD,UserInstance.USER_LAYOUT_ROOT_NODE,null,null);
      } catch(PortalException pe) {
          LogService.log(LogService.ERROR,"AuthenticationServlet::static "+pe);
      }
      redirectString=upFile;
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
    // Clear out the existing session for the user
    request.getSession().invalidate();
    // Retrieve the user's session
    request.getSession(true);
    IPerson person = null;
    try {
      // Get the person object associated with the request
      person = PersonManagerFactory.getPersonManagerInstance().getPerson(request);
      // We retrieve the tokens representing the credential and userid parameters
      // from the security properties file and then grab all of the principals and
      // credentials from the request and load them into their respective HashMaps.
      Properties props =
      ResourceLoader.getResourceAsProperties(this.getClass(), "/properties/security.properties");
      HashMap principals = getPropertyFromRequest (props, "principalToken", request);
      HashMap credentials = getPropertyFromRequest (props, "credentialToken", request);

      // Attempt to authenticate using the incoming request
      m_authenticationService.authenticate(principals, credentials, person);
    } catch (Exception e) {
      // Log the exception
      LogService.log(LogService.ERROR, e);
      // Reset everything
      person = null;
      request.getSession(false).invalidate();
      // Add the authentication failure
      request.getSession(true).setAttribute("up_authenticationError", "true");
    }
    // Check to see if the person has been authenticated
    if (person != null && person.getSecurityContext().isAuthenticated()) {
      // Send the now authenticated user back to the PortalSessionManager servlet
      response.sendRedirect(request.getContextPath() + '/' + redirectString);
    }
    else {
      // Store the fact that this user has attempted authentication in the session
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
   * Gather all properties from a properties file matching a name and get their
   * values from the request and load them into a HashMap that is returned.
   * @param props
   * @param propName
   * @param request
   * @return HashMap of properties
   */
  private HashMap getPropertyFromRequest (Properties props, String propName, HttpServletRequest request) {
   // Iterate through all of the other property keys looking for the first property
   // named like propname that has a value in the request
    HashMap retHash = new HashMap(1);
    Enumeration propNames = props.propertyNames();
    while (propNames.hasMoreElements()) {
      String candidate = (String)propNames.nextElement();
      //LogService.instance().log(LogService.DEBUG, "AuthenticationServlet::getPropertyFromRequest() Candidate = " + candidate);
      String candidateValue = props.getProperty(candidate);
      if (candidate.startsWith(propName)) {
         String propValue = request.getParameter(candidateValue);
         // null value causes exception in context.authentication
         // alternately we could just not set parm if value is null
         propValue = (propValue == null ? "" : propValue);
         // The relationship between the way the properties are stored and the way
         // the subcontexts are named has to be closely looked at to make this work.
         // Do we want to strip off the "root." or even the "root.simple."?
         String key = candidate.substring(propName.length() + 1);
         // 2nd part assumes prefix of "root.". This obviously is weak and ready to break.
         key = (key.equals("root") ? key : key.substring(5));
         retHash.put(key, propValue);
      }
    }
    return (retHash);
  }
}
