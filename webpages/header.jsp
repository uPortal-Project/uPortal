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

<%@ page import="org.jasig.portal.security.IPerson" %>
<%@ page import="org.jasig.portal.UtilitiesBean" %>

<%
  IPerson person    = (IPerson)session.getAttribute("Person");
  String sUserName  = null;
  String sFirstName = null;
  if(person != null)
  {
    sUserName  = person.getID();
  }
%>

<!-- Header -->
<table border="0" cellpadding="0" cellspacing="1" width="100%">
  <tr>
    <td width="25%"><span class="PortalHeaderText"><img src="images/MyIBS.gif" width="128" height="47" border="0" alt="MyIBS"></span></td>
    <td width="300"><span class="PortalHeaderText">Hello <%= sUserName == null ? "guest" : sUserName %>, Welcome to MyIBS!<br>
                                             <%= UtilitiesBean.getDate () %><br>
                                             <%= person == null || sUserName.equals ("guest") ? "&nbsp;" : "<a href=\"logout.jsp\">Logout</a>" %></span></td>
    <td align="right"><span class="PortalHeaderText"><a href="layout.jsp">Home</a> > <%= session.getAttribute ("headerTitle") != null ? session.getAttribute ("headerTitle") : "" %></span></td>
  </tr>
  <tr bgcolor="<%= layoutBean.getActiveTabColor(request, response, out) %>">
    <td colspan="3">&nbsp;</td>
  </tr>
</table>
<br />
