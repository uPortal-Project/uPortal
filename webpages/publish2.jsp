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
<%@ page errorPage="error.jsp" %>
<%@ include file="checkinit.jsp" %>
<%@ include file="checkGuest.jsp" %>

<jsp:useBean id="layoutBean" class="org.jasig.portal.LayoutBean" type="org.jasig.portal.ILayoutBean" scope="session" />
<jsp:useBean id="publish" class="org.jasig.portal.PublisherBean"  scope="session" />

<%
String sChan_type = request.getParameter ("chan_type");
String sPub_email = request.getParameter("pub_email");
%>

 <%-- UtilitiesBean.preventPageCaching (response); --%>

<html>
<head>
<title>Publish Channel</title>
<link rel=stylesheet href="stylesheets/general.css" TYPE="text/css">
</head>

<% layoutBean.writeBodyStyle (request, response, out); %>

<%-- Header --%>
<% session.setAttribute ("headerTitle", "Publish Channel"); %>
<%@ include file="header.jsp" %>

<form action="previewPublish.jsp" method=post name="">

<%--make sure we got an email and channel type--%>
<%
if ((sChan_type == null) || (sPub_email== null))
{
%>

  <p>You failed to enter required information.<br>
  </p>
  <ul>
  <%= sChan_type == null ? "<li><b>Channel Type</b>" : " "%>
  <%= sChan_type == null ? "<li><b>Email Address</b>" : " "%>
  </ul>

  <p>Please go back and enter this information and proceed. 
  </p>
  
<%
}
else 
{
%>
  <p><font size="5"><b>Step 3.</b></font><br>

   <% publish.writeParamFields(request, response, out); %>
   
  <p> 
    <input type="hidden" name="chan_type" value="<%= sChan_type %>">
	<input type="hidden" name="pub_email" value="<%= sPub_email %>">
    <input type="submit" value="Next">
  </p>
<% } %>  
</form>

<%-- Footer --%>
<%@ include file="footer.jsp" %>

</body>
</html>
