<%--
 * Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 *
--%>

<%@ page isErrorPage="true" %>
<% org.apache.commons.logging.Log logger = org.apache.commons.logging.LogFactory.getLog("org.jasig.portal.jsp.Error"); %>
<html>
<head>
<title>Portal: An error has occured</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<body>

<%
if (exception != null)
{
    logger.error(exception.getMessage(), exception);
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
