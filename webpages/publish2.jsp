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
<jsp:useBean id="publish" class="org.jasig.portal.PublisherBean" scope="session" />

<%-- Get the user action passed in --%>
<% String sUserAction = request.getParameter("userAction"); %>

<%-- Set the incoming user data in the bean --%>
<% publish.setChannelData(request); %>

<%-- Always force this page to reload --%>
<%-- <% UtilitiesBean.preventPageCaching (response); %> --%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <title>Publish Channel</title>
    <link rel=stylesheet href="stylesheets/portal.css" TYPE="text/css">
    <script language="javascript">
      function submitForm(nextPage)
      {
        document.data.action = nextPage;
        document.data.submit();
      }
    </script>

  <% layoutBean.writeBodyStyle(request, response, out); %>

    <%-- Header --%>
    <% session.setAttribute ("headerTitle", "Publish Channel"); %>
    <%@ include file="header.jsp" %>

    <%-- Make sure we got a channel type --%>
    <%
      String sChanType = publish.getChanType();

      if((sChanType == null) || (sChanType.equals("")))
      {
    %>
        <p>You failed to enter required information.<br></p>
        <ul>
          <%
            if((sChanType == null) || (sChanType.equals("")))
            {
          %>
              <li><b>Channel Type</b></li>
          <%
            }
          %>
        </ul>
        <p>Please go back and enter this information and proceed.</p>
    <%
    }
    else
    {
    %>

    <form method="post" name="data">
      <table border="0" width="100%">
        <tr bgcolor="#dddddd">
          <td colspan="5"><p><font size=4><b>Specify Channel Parameters</b></font></p></td>
        </tr>
        <tr>
          <td><p><font size="3"><b>Step 2.</b></font><br></td>
        </tr>
      </table>

      <table border="0" width="100%">
        <% publish.writeParamFields(request, response, out); %>
      </table>

      <table border="0" width="100%">
        <tr>
          <td width="5%"><input type="button" value="Next" onClick="javascript:submitForm('previewPublish.jsp')"></td>
          <td>Preview and catagorize your channel</td>
        </tr>
        <tr>
          <td width="5%"><input type="button" value="Cancel" onClick="javascript:submitForm('publish.jsp')"></td>
          <td>Start the publishing process over</td>
        </tr>
        <tr bgcolor="#dddddd">
          <td colspan="2">&nbsp;</td>
        </tr>
      </table>
    <% } %>
    </form>

     <%-- Footer --%>
    <%@ include file="footer.jsp" %>

  </body>
</html>
