<%--
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
--%>

<%@ page errorPage="error.jsp" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page import="org.jasig.portal.security.*" %>

<jsp:useBean id="auth" class="org.jasig.portal.services.Authentication" />
<jsp:useBean id="JNDIManager" class="org.jasig.portal.jndi.JNDIManager" />

<%
session.invalidate();
session = request.getSession(true);

String userName = request.getParameter("userName");
String password = request.getParameter("password");
String baseActionURL = request.getParameter("baseActionURL");
String redirectString = "render.uP";
boolean bAuthorized = false;

if (userName != null && password != null)
{
  try
  {
    bAuthorized = auth.authenticate (userName, password);
  }
  catch (PortalSecurityException pse)
  {
    session.setAttribute ("up_authenticationError", "true");
  }
  session.setAttribute("up_authorizationAttempted", "true");

  if(bAuthorized)
  {
    // Get the Person object and put it in the session
    IPerson person = auth.getPerson ();
    session.setAttribute ("up_person", person);

    // Get the SecurityContext and put it in the session
    ISecurityContext SecurityContext = auth.getSecurityContext();
    session.setAttribute ("up_SecurityContext", SecurityContext);
  }
  else
  {
    redirectString = baseActionURL + "?userName=" + userName;
  }
}

  response.sendRedirect("http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" + redirectString);
%>
