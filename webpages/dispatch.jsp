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
<%@ page import="org.jasig.portal.layout.*" %>
<%@ page import="org.jasig.portal.UtilitiesBean" %>
<%@ include file="checkinit.jsp" %>

<jsp:useBean id="dispatchBean" class="org.jasig.portal.DispatchBean" scope="session" />
<jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />

<% org.jasig.portal.IChannel ch = dispatchBean.getChannel (request); %>
<% UtilitiesBean.preventPageCaching (response); %>

<%
String sMethodName = request.getParameter ("method");  
String sTitle = ch.getName ();
session.setAttribute ("headerTitle", sTitle);
%>


<html>
<head>
<title><%= sTitle %></title>
<link rel="stylesheet" href="stylesheets/general.css" TYPE="text/css" />
</head>

<% layoutBean.writeBodyStyle (request, response, out); %>
<body>

<%-- Header --%>
<%@ include file="header.jsp" %>

<%
Class[] paramTypes = new Class[] {Class.forName ("javax.servlet.http.HttpServletRequest"), Class.forName ("javax.servlet.http.HttpServletResponse"), Class.forName ("javax.servlet.jsp.JspWriter")};
Object[] methodParams = new Object[] {request, response, out};
java.lang.reflect.Method method = ch.getClass ().getMethod (sMethodName, paramTypes);
method.invoke (ch, methodParams);
%>

<%-- Footer --%>
<%@ include file="footer.jsp" %>

</body>
</html>
