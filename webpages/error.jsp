<%@ page isErrorPage="true" %>
<%@ page import="org.jasig.portal.Logger" %>
<html>
<head>
<title>Portal: An error has occured</title>
<link rel=stylesheet href="stylesheets/general.css" TYPE="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<body>

<%
if (exception != null)
{ 
  Logger.log (Logger.ERROR, exception);
%>

<%-- Error message that user will see when an exception is thrown --%>
<p><strong>An error has occurred.  Check the portal log for details.</strong>

<%
}
else
{
%>

<%-- Display text when page is loaded without an exception --%>
<p>You are looking at <code>error.jsp</code>.  If an exception had been thrown 
in one of the <code>.jsp</code> files, this page will be loaded and the stack trace
will be logged.

<%
}
%>
</body>
</html>
