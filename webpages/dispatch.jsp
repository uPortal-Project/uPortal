<%@ page errorPage="error.jsp" %>
<%@ page import="org.jasig.portal.layout.*" %>
<%@ page import="org.jasig.portal.UtilitiesBean" %>

<jsp:useBean id="dispatchBean" class="org.jasig.portal.DispatchBean" scope="session" />
<jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />

<% org.jasig.portal.IChannel ch = dispatchBean.getChannel (request); %>
<% UtilitiesBean.preventPageCaching (response); %>

<%
String sMethodName = request.getParameter ("method");  
String sTitle = ch.getName ();
session.setAttribute ("headerTitle", sTitle);
%>


<html>
<head>
<title><%= sTitle %></title>
<link rel="stylesheet" href="stylesheets/general.css" TYPE="text/css" />
</head>

<% layoutBean.writeBodyStyle (request, response, out); %>
<body>

<%-- Header --%>
<%@ include file="header.jsp" %>

<%
Class[] paramTypes = new Class[] {Class.forName ("javax.servlet.http.HttpServletRequest"), Class.forName ("javax.servlet.http.HttpServletResponse"), Class.forName ("javax.servlet.jsp.JspWriter")};
Object[] methodParams = new Object[] {request, response, out};
java.lang.reflect.Method method = ch.getClass ().getMethod (sMethodName, paramTypes);
method.invoke (ch, methodParams);
%>

<%-- Footer --%>
<%@ include file="footer.jsp" %>

</body>
</html>