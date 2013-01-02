<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

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
