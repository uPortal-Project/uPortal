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
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.UtilitiesBean" %>
<%@ page import="org.jasig.portal.security.IPerson" %>

<%-- If the portal base directory hasn't been set, forward back to
  -- index.jsp, which retrives it from the ServletContext.  The portal
  -- base directory is an application-wide parameter set in web.xml.
  --%>
<% if (org.jasig.portal.GenericPortalBean.getPortalBaseDir() == null) { %>
  <jsp:forward page="index.jsp"/>
<% } %>

<jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />

<%
  response.setContentType("text/html");
  UtilitiesBean.preventPageCaching(response);

  IPerson person    = (IPerson)session.getAttribute("Person");
  String sUserName  = null;
  String sFirstName = null;
  if(person != null)
  {
    sUserName = person.getID();
  }
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <title>MyIBS</title>
    <link rel="stylesheet" href="stylesheets/portal.css" type="text/css">
    <script language="JavaScript" type="text/javascript">
      <!--hide
        function openWin(url, title, width, height)
        {
          var newWin = window.open(url, title, 'width=' + width + ', height=' + height +', resizable=yes, scrollbars=yes')
        }
      //stop hiding-->
    </script>

  <%-- This method call writes the opening body tag and closing head tag --%>
  <% layoutBean.writeBodyStyle(request, response, out); %>

    <table border="0" cellpadding="0" cellspacing="1" width="100%">
      <tr>
        <td width="25%"><img src="images/MyIBS.gif" width="100" height="50" border="0" alt="MyIBS"></td>
        <td width="300">
          <span class="PortalHeaderText">
            Hello <%= sUserName == null ? "guest" : sUserName %>, Welcome to MyIBS!<br>
            <%= UtilitiesBean.getDate () %><br>
            <%= person == null || sUserName.equals ("guest") ? "&nbsp;" : "<a href=\"logout.jsp\">Logout</a>" %>
          </span>
        </td>
        <% if (person == null || sUserName.equals ("guest")) { %>
        <td align="right">&nbsp;</td>
        <% } else { %>
        <td align="right"><span class="PortalHeaderText">Channels ->
          <%
            if(layoutBean.canUserPublish(person)) {
          %>
          <a href="publish.jsp">Publish</a> |
          <%
            }
          %>
          <a href="subscribe.jsp">Subscribe</a><br>
          Personalize -> <a href="personalizeColors.jsp">Colors</a> | <a href="personalizeTabs.jsp">Tabs</a> | <a href="personalizeLayout.jsp">Layout</a></span></td>
        <% } %>
      </tr>
    </table>

    <%
      layoutBean.writeTabs(request, response, out);
      layoutBean.writeChannels(request, response, out);
    %>

    <%-- Footer --%>
    <%@ include file="footer.jsp" %>

  </body>
</html>
