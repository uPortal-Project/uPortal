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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonManagerFactory;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.StatsRecorder;

/**
 * Simple servlet to handle user logout. When a user
 * logs out, their session gets invalidated and they
 * are returned to the guest page.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class LogoutServlet extends HttpServlet {

    private static final String redirectString;

    static {
      String upFile=UPFileSpec.RENDER_URL_ELEMENT+UPFileSpec.PORTAL_URL_SEPARATOR+UserInstance.USER_LAYOUT_ROOT_NODE+UPFileSpec.PORTAL_URL_SEPARATOR+UPFileSpec.PORTAL_URL_SUFFIX;
      try {
          upFile = UPFileSpec.buildUPFile(null,UPFileSpec.RENDER_METHOD,UserInstance.USER_LAYOUT_ROOT_NODE,null,null);
      } catch(PortalException pe) { 
          LogService.log(LogService.ERROR,"LogoutServlet::static "+pe);
      }
      redirectString=upFile;
    }

  /**
   * Process the incoming request and response.
   * @param req, the servlet request object
   * @param res, the servlet response object
   * @throws ServletException
   * @throws IOException
   */
  public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession(false);

    // Record that the user is requesting to log out
    try {
      IPerson person = PersonManagerFactory.getPersonManagerInstance().getPerson(request);
      StatsRecorder.recordLogout(person);    
    } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
    }
    
    // Clear out the existing session for the user
    if (session != null)
      session.invalidate();
      
    // Send the user back to the guest page
    response.sendRedirect(request.getContextPath() + '/' + redirectString);
  }
}



