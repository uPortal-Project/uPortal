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
<%@ page import="org.jasig.portal.layout.*" %>
<%@ page import="com.objectspace.xml.*" %>
<%@ page errorPage="error.jsp" %>
<%@ include file="checkinit.jsp" %>
<%@ include file="checkGuest.jsp" %>


<%
  // this is how you MUST get the layout, otherwise, all guests will recieve their own layout, which WILL CRASH YOUR SERVER!
  org.jasig.portal.ILayoutBean layoutBean = org.jasig.portal.LayoutBean.findLayoutInstance(application, session);
%> 

<%
String sAction = request.getParameter ("action");

if (sAction != null)
{
  // Tabs
  if (sAction.equals ("addTab"))
    layoutBean.addTab (request);
  else if (sAction.equals ("removeTab"))
    layoutBean.removeTab (request);
  else if (sAction.equals ("moveTabDown"))
    layoutBean.moveTabDown (request);
  else if (sAction.equals ("moveTabUp"))
    layoutBean.moveTabUp (request);

  // Go back to default layout xml
  else if (sAction.equals ("revertToDefaultLayoutXml"))
  {
    IXml layoutXml = layoutBean.getDefaultLayoutXml (request);
  }
  else if (sAction.equals ("changeLayout"))
  {
    IXml layoutXml = layoutBean.getLayoutXml (request, layoutBean.getUserName (request));
    layoutBean.setLayoutXml (layoutBean.getUserName (request), layoutXml);
    response.sendRedirect ("personalizeLayout.jsp?tab=" + request.getParameter ("tab"));
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
    <title>Personalize Tabs</title>
    <link rel=stylesheet href="stylesheets/portal.css" TYPE="text/css">
    <script language="JavaScript">
    <!-- hide
    function getActionAndSubmit(theForm, buttonValue)
    {
      theForm.action.value = buttonValue

      if (theForm.tab.selectedIndex > -1)
        theForm.submit ()
      else
        alert ('Please select a tab and try again.')
    }
    //stop hiding-->
    </script>

  <% layoutBean.writeBodyStyle (request, response, out); %>

    <%-- Header --%>
    <% session.setAttribute ("headerTitle", "Personalize Tabs"); %>
    <%@ include file="header.jsp" %>

    <%-- Finished and Cancel Changes buttons --%>
    <form>
    <table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
      <input type=button name=finished value="Finished" onClick="location='personalizeTabs.jsp?action=save'">
      <input type=button name=cancel value="Cancel Changes" onClick="location='personalizeTabs.jsp?action=cancel'">
    </table>
    </form>

    <%-- Add a new tab --%>
    <form name="addTab" action="personalizeTabs.jsp" method=post>
    <input type=hidden name="action" value="addTab">
    <input type=submit name="submit" value="Add">
    new tab
    <select name="tab">
      <%
        ITab[] tabs = layoutBean.getTabs (request);

        for (int iTab = 0; iTab < tabs.length; iTab++)
        {
      %>
        <option value="<%= iTab %>">before '<%= tabs[iTab].getNameAttribute () %>'</option>
      <%
        }
      %>
      <option value="<%= tabs.length %>" selected>at the end</option>
    </select>
    </form>

    <form name="tabs" action="personalizeTabs.jsp" method=post>
    <input type=hidden name="action" value="none">
    <table border=0 cellspacing=2 cellpadding=3>
      <tr>
        <td>
          <select name="tab" size=10>
          <%
          for (int iTab = 0; iTab < tabs.length; iTab++)
          {
          %>
                <option value="<%= iTab %>"><%= tabs[iTab].getNameAttribute () %></option>
          <%
          }
          %>
          </select>
        </td>
        <td align="center">
          <%-- Edit (rename/change layout) tab --%>
          <a href="javascript:getActionAndSubmit (document.tabs, 'changeLayout')">
          <img src="images/edit.gif" border=0 alt="Rename/change tab layout"></a><br><br>

          <%-- Move tab up --%>
          <a href="javascript:getActionAndSubmit (document.tabs, 'moveTabUp')">
          <img src="images/up.gif" border=0 alt="Move tab up"></a><br><br>

          <%-- Remove tab --%>
          <a href="javascript:getActionAndSubmit (document.tabs, 'removeTab')">
          <img src="images/remove.gif" border=0 alt="Remove tab"></a><br><br>

          <%-- Move tab down --%>
          <a href="javascript:getActionAndSubmit (document.tabs, 'moveTabDown')">
          <img src="images/down.gif" border=0 alt="Move tab down"></a><br>
        </td>
      </tr>
    </table>
    </form>

    <%-- Revert to default layout xml --%>
    [<a href="personalizeTabs.jsp?action=revertToDefaultLayoutXml">Revert to default layout</a>]

    <%-- Finished and Cancel Changes buttons --%>
    <form>
    <table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
      <input type=button name=finished value="Finished" onClick="location='personalizeTabs.jsp?action=save'">
      <input type=button name=cancel value="Cancel Changes" onClick="location='personalizeTabs.jsp?action=cancel'">
    </table>
    </form>

    <%-- Footer --%>
    <%@ include file="footer.jsp" %>
  </body>
</html>