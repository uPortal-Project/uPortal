<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page import="com.objectspace.xml.*" %>
<%@ page errorPage="error.jsp" %>

<jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />
<jsp:useBean id="subscribe" class="org.jasig.portal.SubscriberBean" scope="session" />

<%
String sAction = request.getParameter ("action");

if (sAction != null)
{
  // Tabs
  if (sAction.equals ("addChannel"))
    subscribe.addChannel (request);


  // Save the layout xml
  else if (sAction.equals ("save"))
  {
    IXml layoutXml = layoutBean.getLayoutXml (request, layoutBean.getUserName (request));
    layoutBean.setLayoutXml (layoutBean.getUserName (request), layoutXml);
    session.removeAttribute ("layoutXml");
    response.sendRedirect ("layout.jsp");
  }

  // Ignore changes and return to the layout
  else if (sAction.equals ("cancel"))
  {
    session.removeAttribute ("layoutXml");
    response.sendRedirect ("layout.jsp");
  }
}
%>

<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title>Add Channels</title>

</head>

<% layoutBean.writeBodyTag (request, response, out); %>

<%-- Header --%>
<% session.setAttribute ("headerTitle", "Personalize Content"); %>
<%@ include file="header.jsp" %>

<%-- Finished and Cancel Changes buttons --%>
<form>
<table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
  <input type=button name=cancel value="Cancel" onClick="location='subscribe.jsp?action=cancel'">
</td></tr></table>
</form>

<%
 ResultSet rs = subscribe.getChannels(request);

 while(rs.next()) {
 %>
 <a href="subscribe.jsp?action=addChannel&column=0&chan_id="<%=rs.getString("ID")%>"><%=rs.getString("TITLE")%></a><br>
 <%
 }
 %>


<%-- Finished and Cancel Changes buttons --%>
<form>
<table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
  <input type=button name=cancel value="Cancel" onClick="location='subscribe.jsp?action=cancel'">
</td></tr></table>
</form>

<jsp:include page="footer.jsp" flush="true" />

</body>
</html>