<%@ page errorPage="error.jsp" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.UtilitiesBean" %>

<%-- If the portal base directory hasn't been set, forward back to
  -- index.jsp, which retrives it from the ServletContext.  The portal
  -- base directory is an application-wide parameter set in web.xml.
  --%>
<% if (org.jasig.portal.GenericPortalBean.getPortalBaseDir() == null) { %>
<jsp:forward page="index.jsp"/>
<% } %>

<jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />

<% String sUserName = (String) session.getAttribute ("userName"); %>
<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title>Portal Framework</title>
<link rel=stylesheet href="stylesheets/general.css" TYPE="text/css">
<script language="JavaScript">
<!--hide

function openWin(url, title, width, height)
{
  var newWin = window.open(url, title, 'width=' + width + ',height=' + height +',resizable=yes,scrollbars=yes')
}

//stop hiding-->
</script>
</head>

<% layoutBean.writeBodyStyle (request, response, out); %>
<body>

<!-- Header -->
<table border=0 cellpadding=0 cellspacing=1 width=100%>
  <tr>
    <td width=100><img src="images/MyIBS.gif" width=100 height=50 border=0></td>
    <td width=300><font size=2 color=blue>Hello <%= sUserName == null ? "guest" : sUserName %>, Welcome to MyIBS!</font><br>
        <font size=1 color=#444444><%= UtilitiesBean.getDate () %></font></td>
    <td align=right><%= sUserName == null || sUserName.equals ("guest") ? "&nbsp;" : "<a href=\"logout.jsp\">Logout</a>" %></td>
  </tr>
</table>

<%
layoutBean.writeTabs (request, response, out);
layoutBean.writeChannels (request, response, out);
%>

<jsp:include page="footer.jsp" flush="true" />

</body>
</html>
