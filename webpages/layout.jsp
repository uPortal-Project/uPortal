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

<%-- If the portal base directory hasn't been set, forward back to
  -- index.jsp, which retrives it from the ServletContext.  The portal
  -- base directory is an application-wide parameter set in web.xml.
  --%>
<% if (org.jasig.portal.GenericPortalBean.getPortalBaseDir() == null) { %>
<jsp:forward page="index.jsp"/>
<% } %>

<jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />

<% String sUserName = (String) session.getAttribute ("userName"); %>
<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title>Portal Framework</title>
<script language="JavaScript">
<!--hide

function openWin(url, title, width, height)
{
  var newWin = window.open(url, title, 'width=' + width + ',height=' + height +',resizable=yes,scrollbars=yes')
}

//stop hiding-->
</script>
<% layoutBean.writeBodyStyle (request, response, out); %>

<!-- Header -->
<table border="0" cellpadding="0" cellspacing="1" width="100%">
  <tr>
    <td width="100"><img src="images/MyIBS.gif" width="100" height="50" border="0"></td>
    <td width="300"><font size="2" color="blue">Hello <%= sUserName == null ? "guest" : sUserName %>, Welcome to MyIBS!</font><br>
        <font size="1" color="#444444"><%= UtilitiesBean.getDate () %></font><br>
        <%= sUserName == null || sUserName.equals ("guest") ? "&nbsp;" : "<a href=\"logout.jsp\">Logout</a>" %></td>
    <td align="right">
      <% if (sUserName == null || sUserName.equals ("guest")) { %>
      &nbsp;
      <% } else { %>
      Channels -> <a href="publish.jsp">Publish</a> | <a href="subscribe.jsp">Subscribe</a><br>
      Personalize -> <a href="personalizeColors.jsp">Colors</a> | <a href="personalizeTabs.jsp">Tabs</a> | <a href="personalizeLayout.jsp">Layout</a>
      <% } %>
    </td>
  </tr>
</table>

<%
layoutBean.writeTabs (request, response, out);
layoutBean.writeChannels (request, response, out);
%>

<jsp:include page="footer.jsp" flush="true" />

</body>
</html>
