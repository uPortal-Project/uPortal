<%@ page errorPage="error.jsp" %>
<%@ page import="org.jasig.portal.layout.*" %>
<%@ page import="org.jasig.portal.UtilitiesBean" %>
<jsp:useBean id="layoutBean" class="org.jasig.portal.LayoutBean" scope="session" />

<%
int iTab = Integer.parseInt (request.getParameter ("tab"));
int iCol = Integer.parseInt (request.getParameter ("column"));
int iChan = Integer.parseInt (request.getParameter ("channel"));
IChannel channel = layoutBean.getChannel (request, iTab, iCol, iChan);
org.jasig.portal.IChannel ch = layoutBean.getChannelInstance (channel);
%>

<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title><%= ch.getName () %></title>
<link rel=stylesheet href="stylesheets/general.css" TYPE="text/css">
</head>

<body bgcolor=ffffff>

<% ch.render (request, response, out); %>

<center><br>
<form><input type="button" value="Close" onClick="self.close()"></form>
</center>

</body>
</html>