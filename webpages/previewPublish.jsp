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

<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page import="com.objectspace.xml.*" %>
<%@ page import= "java.sql.*" %>
<%@ page errorPage="error.jsp" %>
<%@ include file="checkinit.jsp" %>
<%@ include file="checkGuest.jsp" %>

<jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />
<jsp:useBean id="publish" class="org.jasig.portal.PublisherBean" scope="session" />

<%
String sAction = request.getParameter ("action");


if (sAction != null)
{

  // Ignore changes and return to the layout
  if (sAction.equals ("cancel"))
  {
    response.sendRedirect ("publish.jsp");
  }
}
%>

<%-- UtilitiesBean.preventPageCaching (response); --%>

<html>
<head>
<title>Publish Channel</title>
<link rel=stylesheet href="stylesheets/general.css" TYPE="text/css">

<SCRIPT LANGUAGE="JavaScript">
<!--

function subForm(form) {
  form.submit();
  }
//-->
</SCRIPT>

</head>

<% layoutBean.writeBodyStyle (request, response, out); %>

<%-- Header --%>
<% session.setAttribute ("headerTitle", "Publish Channel"); %>
<%@ include file="header.jsp" %>

<% 
if ((sAction != null) && (sAction.equals("register"))) {
 	if (publish.registerChannel(request)) {%>
	<h2>Congratulations!</h2><br>
	You have successfully published your channel. Once it has been approved, others will be able to
	subscribe to it and view its content.
	<%}
	else { %>
	<h2>Error!</h2>
	Your channel was not successfully registered. Please go back and retry. Notify administrator
	if problem persists.
	<% }
}
else { %>
<%-- Finished and Cancel Changes buttons --%>
<form>
<table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
  <input type=button name=add value="Finished" onClick="subForm(channel)">
  <input type=button name=cancel value="Cancel" onClick="location='publish.jsp'">
</td></tr></table>
<font size=4><b>Preview Channel for Registration</b></font>
</form>


<table border=0 cellpadding=3 cellspacing=3>
This is a preview of the channel you are registering. If it is rendered correctly then pick any appropriate categories and click 'Finished'
button to register. Otherwise cancel or go back and make necessary corrections.
<tr bgcolor=#ffffff>
<td>
<% publish.previewChannel(request, response, out); %>
</td>
<td valign="top">
<b>Channel Categories</b><p>
<form method="POST" name="channel" action="previewPublish.jsp?action=register">
<% publish.writeChannelCats(request, response, out); %>
</form>
</td>
</tr>
</table>


<%-- Finished and Cancel Changes buttons --%>
<form>
<table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
  <input type=button name=add value="Finish" onClick="subForm(channel)">
  <input type=button name=cancel value="Cancel" onClick="location='publish.jsp'">
</td></tr></table>
</form>
<% } %>

<jsp:include page="footer.jsp" flush="true" />

</body>
</html>
