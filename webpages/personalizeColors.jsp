<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page errorPage="error.jsp" %>

<jsp:useBean id="layoutBean" class="org.jasig.portal.LayoutBean" type="org.jasig.portal.ILayoutBean" scope="session" />

<%
String sAction = request.getParameter ("action");

if (sAction != null && sAction.equals ("saveColors"))
{
  layoutBean.setColors (request, response, out);
  response.sendRedirect ("layout.jsp");
}
%>

<html>
<head>
<title>Personalize Colors</title>
<link rel=stylesheet href="stylesheets/general.css" TYPE="text/css">
<META HTTP-EQUIV="expires" CONTENT="Tue, 20 Aug 1996 14:25:27 GMT">
</head>

<% layoutBean.writeBodyStyle (request, response, out); %>
<body>

<%-- Header --%>
<% session.setAttribute ("headerTitle", "Personalize Colors"); %>
<%@ include file="header.jsp" %>

<form action="personalizeColors.jsp" method=post>
<table border=0 cellspacing=3 cellpadding=3>
  <tr>
    <td>Background Color</td>
    <td><input type=text name="bgColor" value="<%= layoutBean.getBackgroundColor (request, response, out) %>"></td>
  </tr>
  <tr>
    <td>Foreground Color</td>
    <td><input type=text name="fgColor" value="<%= layoutBean.getForegroundColor (request, response, out) %>"></td>
  </tr>
  <tr>
    <td>Tab Color</td>
    <td><input type=text name="tabColor" value="<%= layoutBean.getTabColor (request, response, out) %>"></td>
  </tr>
  <tr>
    <td>Active Tab Color</td>
    <td><input type=text name="activeTabColor" value="<%= layoutBean.getActiveTabColor (request, response, out) %>"></td>
  </tr>
  <tr>
    <td>Channel Heading Color</td>
    <td><input type=text name="channelHeadingColor" value="<%= layoutBean.getChannelHeadingColor (request, response, out) %>"></td>
  </tr>
</table>
<input type=submit name=submit value="Save">
<input type=hidden name=action value="saveColors">
</form>

<%-- Footer --%>
<%@ include file="footer.jsp" %>

</body>
</html>