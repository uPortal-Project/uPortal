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
<%@ include file="checkGuest.jsp" %>

<%
  org.jasig.portal.GenericPortalBean.initialize(application);
  org.jasig.portal.ILayoutBean layoutBean = org.jasig.portal.LayoutBean.findLayoutInstance(application, session);
%> 

<jsp:useBean id="subscribe" class="org.jasig.portal.SubscriberBean" scope="session" />

<%
String sAction = request.getParameter ("action");
String sChan_id = request.getParameter ("chan_id");
String sChanName = null;

if (sAction != null)
{

  // Ignore changes and return to the layout
  if (sAction.equals ("cancel"))
  {
    session.removeAttribute("subscribe");
    response.sendRedirect ("subscribe.jsp");
  }
}
else {
 subscribe.setChannel(request);
 sChanName = subscribe.getChannelName();
 }
%>

<% UtilitiesBean.preventPageCaching (response); %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <title>Preview Channel</title>
    <link rel=stylesheet href="stylesheets/portal.css" TYPE="text/css">

  <% layoutBean.writeBodyStyle (request, response, out); %>

    <%-- Header --%>
    <% session.setAttribute ("headerTitle", "Preview Channel"); %>
    <%@ include file="header.jsp" %>

    <%-- Finished and Cancel Changes buttons --%>
    <form>
    <table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
      <input type=button name=add value="Add" onClick="location='personalizeLayout.jsp?action=addChannel&column=0&chan_id=<%=sChan_id%>'">
      <input type=button name=cancel value="Cancel" onClick="location='previewChannel.jsp?action=cancel'">
    </td></tr></table>
    <FONT FACE="Helvetica, Arial" SIZE=4 COLOR=#003333><B>Preview Channel: <%=sChanName%></B></FONT>
    </form>

    <table border=0 cellpadding=3 cellspacing=3>
    This window shows how the <%=sChanName%> Channel will look on your active tab.
    <tr bgcolor=#ffffff>
    <td>
    <% subscribe.previewChannel(request, response, out); %>
    </td>
    </tr>
    </table>


    <%-- Finished and Cancel Changes buttons --%>
    <form>
    <table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
      <input type=button name=add value="Add" onClick="location='personalizeLayout.jsp?action=addChannel&column=0&chan_id=<%=sChan_id%>'">
      <input type=button name=cancel value="Cancel" onClick="location='previewChannel.jsp?action=cancel'">
    </td></tr></table>
    </form>


    <%@ include file="footer.jsp" %>

  </body>
</html>
