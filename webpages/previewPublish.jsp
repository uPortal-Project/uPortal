<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page import="com.objectspace.xml.*" %>
<%@ page import= "java.sql.*" %>
<%@ page errorPage="error.jsp" %>

<jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />
<jsp:useBean id="publish" class="org.jasig.portal.PublisherBean" scope="session" />

<%
String sAction = request.getParameter ("action");


if (sAction != null)
{

  // Ignore changes and return to the layout
  if (sAction.equals ("cancel"))
  {
    response.sendRedirect ("publish.jsp");
  }
}
%>

<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title>Publish Channel</title>

</head>

<% layoutBean.writeBodyTag (request, response, out); %>

<%-- Header --%>
<% session.setAttribute ("headerTitle", "Publish Channel"); %>
<%@ include file="header.jsp" %>

<% 
if ((sAction != null) && (sAction.equals("register"))) {
 	if (publish.registerChannel()) {%>
	<h2>Congratulations!</h2><br>
	You have successfully published your channel. Once it has been approved, others will be able to
	subscribe to it and view its content.
	<%}
	else { %>
	<h2>Error!</h2>
	Your channel was not successfully registered. Please go back and retry. Notify administrator
	if problem persists.
	<% }
}
else { %>
<%-- Finished and Cancel Changes buttons --%>
<form>
<table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
  <input type=button name=add value="Finished" onClick="location='previewPublish.jsp?action=register'">
  <input type=button name=cancel value="Cancel" onClick="location='publish.jsp'">
</td></tr></table>
<FONT FACE="Helvetica, Arial" SIZE=4 COLOR=#003333><B>Preview Channel for Registration</B></FONT>
</form>

<table border=0 cellpadding=3 cellspacing=3>
This is a preview of the channel you are registering. If it is rendered correctly then click 'Finished'
button to register. Otherwise cancel or go back and make necessary corrections.
<tr bgcolor=#ffffff>
<td>
<% publish.previewChannel(request, response, out); %>
</td>
</tr>
</table>


<%-- Finished and Cancel Changes buttons --%>
<form>
<table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
  <input type=button name=add value="Finish" onClick="location='previewPublish.jsp?action=register'">
  <input type=button name=cancel value="Cancel" onClick="location='publish.jsp'">
</td></tr></table>
</form>
<% } %>

<jsp:include page="footer.jsp" flush="true" />

</body>
</html>