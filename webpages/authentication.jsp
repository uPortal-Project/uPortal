<%--
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
--%>

<%@ page errorPage="error.jsp" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page import="org.jasig.portal.security.IPerson" %>
<%@ include file="checkinit.jsp" %>

<jsp:useBean id="auth" class="org.jasig.portal.services.Authentication" />

<jsp:useBean id="JNDIManager" class="org.jasig.portal.jndi.JNDIManager" />

<%
String sUserName = request.getParameter("userName");
String sPassword = request.getParameter("password");
String baseActionURL = request.getParameter("baseActionURL");
String redirectString = "render.uP";

boolean bAuthorized = auth.authenticate(sUserName, sPassword);
session.setAttribute("up_authorizationAttempted", "true");

if(bAuthorized)
{ 
  /*
    Tomcat 3.1 has a bug (http://jakarta.apache.org/bugs/show_bug.cgi?id=55)
    which prevents you from invalidating the session and then
    creating it again.  So in the meantime, well just
    clear out the session attributes to indicate a logoff/logon
  */

  // Clear out session attributes
  java.util.Enumeration e = session.getAttributeNames ();

  while (e.hasMoreElements ())
  {
    session.removeAttribute ((String) e.nextElement ());
  }
  
  // Get the Person object and put it in the session
  IPerson person = auth.getPerson ();
  session.setAttribute ("up_person", person);
}
else
{
  redirectString = baseActionURL + "?userName=" + sUserName;
  System.out.println("redstr=" + redirectString);
}

response.sendRedirect(redirectString);
%>
