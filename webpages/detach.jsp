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

<jsp:useBean id="layoutBean" class="org.jasig.portal.LayoutBean" scope="session" />

<%
int iTab = Integer.parseInt (request.getParameter ("tab"));
int iCol = Integer.parseInt (request.getParameter ("column"));
int iChan = Integer.parseInt (request.getParameter ("channel"));
IChannel channel = layoutBean.getChannel (request, iTab, iCol, iChan);
org.jasig.portal.IChannel ch = layoutBean.getChannelInstance (channel);
%>

<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title><%= ch.getName () %></title>
<link rel=stylesheet href="stylesheets/general.css" TYPE="text/css">
</head>

<body bgcolor=ffffff>

<% ch.render (request, response, out); %>

<center><br>
<form><input type="button" value="Close" onClick="self.close()"></form>
</center>

</body>
</html>
