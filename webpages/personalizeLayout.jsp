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
<%@ page errorPage="error.jsp" %>
<%@ include file="checkinit.jsp" %>
<%@ include file="checkGuest.jsp" %>

<jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />

<%
String sAction = request.getParameter ("action");

if (sAction != null)
{
  // Tabs
  if (sAction.equals ("addTab"))
    layoutBean.addTab (request);
  else if (sAction.equals ("renameTab"))
    layoutBean.renameTab (request);
  else if (sAction.equals ("setDefaultTab"))
    layoutBean.setDefaultTab (request);

  // Columns
  else if (sAction.equals ("addColumn"))
    layoutBean.addColumn (request);
  else if (sAction.equals ("removeColumn"))
    layoutBean.removeColumn (request);
  else if (sAction.equals ("moveColumnRight"))
    layoutBean.moveColumnRight (request);
  else if (sAction.equals ("moveColumnLeft"))
    layoutBean.moveColumnLeft (request);
  else if (sAction.equals ("setColumnWidth"))
    layoutBean.setColumnWidth (request);

  // Channels
  else if (sAction.equals ("removeChannel"))
    layoutBean.removeChannel (request);
  else if (sAction.equals ("addChannel"))
    layoutBean.addChannel (request);
  else if (sAction.equals ("moveChannelLeft"))
    layoutBean.moveChannelLeft (request);
  else if (sAction.equals ("moveChannelRight"))
    layoutBean.moveChannelRight (request);
  else if (sAction.equals ("moveChannelUp"))
    layoutBean.moveChannelUp (request);
  else if (sAction.equals ("moveChannelDown"))
    layoutBean.moveChannelDown (request);

  // Go back to default layout xml
  else if (sAction.equals ("revertToDefaultLayoutXml"))
  {
    IXml layoutXml = layoutBean.getDefaultLayoutXml (request);
  }

  // Save the layout xml
  else if (sAction.equals ("save"))
  {
    IXml layoutXml = layoutBean.getLayoutXml (request, layoutBean.getUserName (request));
    layoutBean.setLayoutXml (layoutBean.getUserName (request), layoutXml);
    session.removeAttribute ("layoutXml");
    response.sendRedirect ("layout.jsp");
  }

  // Ignore changes and return to the layout
  else if (sAction.equals ("cancel"))
  {
    session.removeAttribute ("layoutXml");
    response.sendRedirect ("layout.jsp");
  }
}
%>

<% UtilitiesBean.preventPageCaching (response); %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <title>Personalize Layout</title>
    <link rel=stylesheet href="stylesheets/portal.css" TYPE="text/css">
    <script language="JavaScript">
    <!-- hide
    function getActionAndSubmit(theForm, buttonValue)
    {
      theForm.action.value = buttonValue

      if (theForm.elements[0].selectedIndex > -1)
        theForm.submit ()
      else
        alert ('Please select a channel and try again.')
    }
    //stop hiding-->
    </script>

  <% layoutBean.writeBodyStyle (request, response, out); %>

    <%-- Header --%>
    <% session.setAttribute ("headerTitle", "Personalize Layout"); %>
    <%@ include file="header.jsp" %>

    <%-- Finished and Cancel Changes buttons --%>
    <form>
    <table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
      <input type=button name=finished value="Finished" onClick="location='personalizeLayout.jsp?action=save'">
      <input type=button name=cancel value="Cancel Changes" onClick="location='personalizeLayout.jsp?action=cancel'">
    </td></tr></table>
    </form>

    <% layoutBean.writePersonalizeLayoutPage (request, response, out); %>

    <%-- Finished and Cancel Changes buttons --%>

    <table border=0 cellspacing=5 cellpadding=5 width="100%">
      <form><tr bgcolor="#dddddd"><td>
      <input type=button name=finished value="Finished" onClick="location='personalizeLayout.jsp?action=save'">
      <input type=button name=cancel value="Cancel Changes" onClick="location='personalizeLayout.jsp?action=cancel'">
    </td></tr></form></table>


    <%@ include file="footer.jsp" %>
  </body>
</html>
