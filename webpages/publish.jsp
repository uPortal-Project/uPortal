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
<jsp:useBean id="publish" class="org.jasig.portal.PublisherBean" scope="request" />

<%
String sAction = request.getParameter ("action");
%>

<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title>Publish Channel</title>
<link rel=stylesheet href="stylesheets/general.css" TYPE="text/css">
</head>

<% layoutBean.writeBodyStyle (request, response, out); %>

<%-- Header --%>
<% session.setAttribute ("headerTitle", "Publish Channel"); %>
<%@ include file="header.jsp" %>

<form action="publish2.jsp" method=post name="">

<%--STEP 1- get email and channel type--%>


  <p>You are about to start the process of &quot;publishing&quot; a channel which 
    other portal users may subscribe to.<br>
  </p>
  <p><font size="5"><b>Step 1.</b></font> First we need a way to contact you as 
    the publishers. Please provide your email address.<br>
  </p>
  <table width="40%" border="0">
    <tr>
      <td width="19%">email:</td>
      <td width="81%"> 
        <input type="text" name="pub_email" maxlength="50" size="40">
      </td>
    </tr>
  </table>
  <p><font size="5"><b>Step 2.</b></font> Next you need to choose a channel type. 
    Below you'll find a list of available channels along with a brief description. 
    Please pick one. </p>
  <table width="75%" border="0">

  <% publish.writeChannelTypes(request, response, out); %>

  </table>
  <p> 
    <input type="submit" value="Next">
  </p>
</form>

<%-- Footer --%>
<%@ include file="footer.jsp" %>

</body>
</html>
