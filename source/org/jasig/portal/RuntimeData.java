/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

import javax.servlet.http.*;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * A set of runtime data acessable by a channel.
 * Includes the following data
 *  - Channel ID (should probably be moved through  initParams() instead)
 *  - Base channel action URL
 *  - HTTP request/response
 *  - A hashtable of parameters passed to the current channel
 * 
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class RuntimeData 
{
  private HttpServletRequest request;
  private HttpServletResponse response;
  private String chanID;
  private String baseActionURL;
  private Hashtable parameters = new Hashtable();

  public RuntimeData() {};
  
  public RuntimeData (HttpServletRequest req, HttpServletResponse res, String cID, String baURL,Hashtable params) 
  {
    request = req;
    response = res;
    chanID = cID;
    baseActionURL = baURL;
    parameters = params;
  }

  public void setChannelID(String cID) { chanID = cID; }
  public void setBaseActionURL(String baURL) { baseActionURL = baURL; }
  public void setHttpRequest(HttpServletRequest req) { request = req; }
  public void setHttpResponse(HttpServletResponse res) { response = res; }
  public void setParameters(Hashtable params) { parameters = params; }

  public String getChannelID() { return chanID; }
  public String getBaseActionURL() { return baseActionURL; }
  public HttpServletRequest getHttpRequest() { return request; }
  public HttpServletResponse getHttpResponse() { return response; }

  public String getParameter(String pName) { return (String) parameters.get(pName); }
  public Enumeration getParameterNames() { return parameters.keys(); }
  public void setParameter(String pName, String pValue) { parameters.put(pName,pValue); }
}
