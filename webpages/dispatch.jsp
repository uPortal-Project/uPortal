<%@ page errorPage="error.jsp" %>
<%@ page import="org.jasig.portal.layout.*" %>
<%@ page import="org.jasig.portal.UtilitiesBean" %>
<jsp:useBean id="dispatchBean" class="org.jasig.portal.DispatchBean" scope="session" />

<% org.jasig.portal.IChannel ch = dispatchBean.getChannel (request); %>
<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title>Dispatch</title>
</head>

<body bgcolor=ffffff>

<%
String sMethodName = request.getParameter ("method");
String sTitle = ch.getName ();
session.setAttribute ("headerTitle", sTitle);
%>

<%-- Header --%>
<%@ include file="header.jsp" %>

<%
Class[] paramTypes = new Class[] {Class.forName ("javax.servlet.http.HttpServletRequest"), Class.forName ("javax.servlet.http.HttpServletResponse"), Class.forName ("javax.servlet.jsp.JspWriter")};
Object[] methodParams = new Object[] {request, response, out};
java.lang.reflect.Method method = ch.getClass ().getMethod (sMethodName, paramTypes);
method.invoke (ch, methodParams);
%>

<%-- Footer --%>
<jsp:include page="footer.jsp" flush="true" />

</body>
</html>