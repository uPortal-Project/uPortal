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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
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


/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 */
public class AuthenticationServlet extends HttpServlet {
  private static final String redirectString = "render.uP";
  private Authentication m_authenticationService = null;

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
   * @exception ServletException, IOException
   */
  public void doPost (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    // Just handle this request in the doGet() method
    doGet(req, res);
  }

  /**
   * Process the incoming HttpServletRequest
   * @param req
   * @param res
   * @exception ServletException, IOException
   */
  public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // Clear out the existing session for the user
    request.getSession().invalidate();
    // Retrieve the user's session
    HttpSession session = request.getSession(true);
    IPerson person = null;
    try {
      // Get the person object associated with the request
      person = PersonManagerFactory.getPersonManagerInstance().getPerson(request);
      // Grab all of the principals from the request
      // NOTE: This should refer to a properties file
      HashMap principals = new HashMap(1);
      principals.put("username", request.getParameter("userName"));
      // Grab all of the credentials from the request
      // NOTE: This should refer to a properties file
      HashMap credentials = new HashMap(1);
      credentials.put("password", request.getParameter("password"));
      // Attempt to authenticate using the incoming request
      m_authenticationService.authenticate(principals, credentials, person);
    } catch (Exception e) {
      // Log the exception
      LogService.log(LogService.ERROR, e);
      // Store the error in the session
      session.setAttribute("up_authorizationError", "true");
    }
    // Check to see if the person has been authenticated
    if (person != null && person.getSecurityContext().isAuthenticated()) {
      // Send the now authenticated user back to the PortalSessionManager servlet
      response.sendRedirect("http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath()
          + "/" + redirectString);
    } 
    else {
      // Store the fact that this user has attempted authorization in the session
      session.setAttribute("up_authorizationAttempted", "true");
      // Send the unauthenticated to the baseActionURL
      response.sendRedirect(request.getParameter("baseActionURL") + "?userName=" + request.getParameter("userName"));
    }
  }
}



