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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import java.util.Enumeration;


/*
 * This class contains basic information about the browser.
 * It stores all of the headers and cookies
 */
public class BrowserInfo {
  protected HttpServletRequest m_request = null;

  /**
   * put your documentation comment here
   * @param request
   */
  public void setRequest (HttpServletRequest request) {
    m_request = request;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public Cookie[] getCookies () {
    return  (m_request.getCookies());
  }

  /**
   * put your documentation comment here
   * @param hName
   * @return 
   */
  public String getHeader (String headerName) {
    return  (m_request.getHeader(headerName));
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public Enumeration getHeaderNames () {
    return  (m_request.getHeaderNames());
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public String getUserAgent () {
    return  (m_request.getHeader("user-agent"));
  }
}



