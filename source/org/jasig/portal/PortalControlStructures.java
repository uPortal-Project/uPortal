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

import  javax.servlet.http.*;


/**
 * <p>This object is passed to special channels</p>
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */
public class PortalControlStructures {
  protected HttpServletRequest m_request;
  protected HttpServletResponse m_response;

  /**
   * put your documentation comment here
   * @return 
   */
  public HttpServletRequest getHttpServletRequest () {
    return  (m_request);
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public HttpServletResponse getHttpServletResponse () {
    return  (m_response);
  }

  /**
   * Convience method for getting just the HttpSession
   * @return the session
   */
  public HttpSession getHttpSession () {
    HttpSession session = null;
    if (m_request != null) {
      session = m_request.getSession(false);
    }
    return  (session);
  }

  /**
   * put your documentation comment here
   * @param r
   */
  public void setHttpServletRequest (HttpServletRequest request) {
    m_request = request;
  }

  /**
   * put your documentation comment here
   * @param r
   */
  public void setHttpServletResponse (HttpServletResponse response) {
    m_response = response;
  }
}



