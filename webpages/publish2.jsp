<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page errorPage="error.jsp" %>
<%@ include file="checkinit.jsp" %>

<jsp:useBean id="layoutBean" class="org.jasig.portal.LayoutBean" type="org.jasig.portal.ILayoutBean" scope="session" />

<%
String sChan_type = request.getParameter ("chan_type");
String sPub_email = request.getParameter("pub_email");
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
else if ((sChan_type.equals("CRSSChannel")) || (sChan_type.equals("CPageRenderer")))
{
%>
  <p><font size="5"><b>Step 3.</b></font> You have chosen to publish a channel 
    that requires you to provide a URL. Please enter the URL for the channel you 
    wish to publish below.<br>
  </p>
  <table width="40%" border="0">
    <tr> 
      <td width="19%">URL:</td>
      <td width="81%"> 
        <input type="text" name="url" maxlength="70" size="50">
      </td>
    </tr>
  </table>
  <%
  if (sChan_type.equals("CPageRenderer")) { %>
  <p><font size="5"><b>Step 4.</b></font> Please provide a name which will be used to 
	  describe the channel to possible subscribers.<br>
  </p>
  <table width="40%" border="0">
    <tr> 
      <td width="19%">Name:</td>
      <td width="81%"> 
        <input type="text" name="name" maxlength="70" size="50">
      </td>
    </tr>
  </table>
  <% } %>
  <p> 
    <input type="hidden" name="chan_type" value="<%= sChan_type %>">
	<input type="hidden" name="pub_email" value="<%= sPub_email %>">
    <input type="submit" name="Submit" value="Next">
  </p>
<% } %>  
</form>

<%-- Footer --%>
<%@ include file="footer.jsp" %>

</body>
</html>
