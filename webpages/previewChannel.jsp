<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page import="com.objectspace.xml.*" %>
<%@ page import= "java.sql.*" %>
<%@ page errorPage="error.jsp" %>

<jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />
<jsp:useBean id="subscribe" class="org.jasig.portal.SubscriberBean" scope="session" />

<%
String sAction = request.getParameter ("action");
String sChan_id = request.getParameter ("chan_id");
String sChanName = subscribe.getChannelName(request);

if (sAction != null)
{

  // Ignore changes and return to the layout
  if (sAction.equals ("cancel"))
  {
    response.sendRedirect ("subscribe.jsp");
  }
}
%>

<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title>Preview Channel</title>

</head>

<% layoutBean.writeBodyTag (request, response, out); %>

<%-- Header --%>
<% session.setAttribute ("headerTitle", "Preview Channel"); %>
<%@ include file="header.jsp" %>

<%-- Finished and Cancel Changes buttons --%>
<form>
<table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
  <input type=button name=add value="Add" onClick="location='personalizeLayout.jsp?action=addChannel&column=0&chan_id=<%=sChan_id%>'">
  <input type=button name=cancel value="Cancel" onClick="location='subscribe.jsp'">
</td></tr></table>
<FONT FACE="Helvetica, Arial" SIZE=4 COLOR=#003333><B>Preview Channel: <%=sChanName%></B></FONT>
</form>

<table border=0 cellpadding=3 cellspacing=3>
This window shows how the <%=sChanName%> Channel will look on your active tab.
<tr bgcolor=#ffffff>
<td>
<% subscribe.previewChannel(request, response, out); %>
</td>
</tr>
</table>


<%-- Finished and Cancel Changes buttons --%>
<form>
<table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
  <input type=button name=add value="Add" onClick="location='personalizeLayout.jsp?action=addChannel&column=0&chan_id=<%=sChan_id%>'">
  <input type=button name=cancel value="Cancel" onClick="location='subscribe.jsp'">
</td></tr></table>
</form>


<jsp:include page="footer.jsp" flush="true" />

</body>
</html>