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

if (sAction != null)
{

  // Ignore changes and return to the layout
  if (sAction.equals ("cancel"))
  {
    response.sendRedirect ("layout.jsp");
  }
}
else {
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
Click on a channel to add to your tab.
</form>

<%
 try {
 ResultSet rs = subscribe.getChannels(request);
 while(rs.next()) {
 %>
 <a href="personalizeLayout.jsp?action=addChannel&column=0&chan_id=<%=rs.getString("ID")%>"><%=rs.getString("TITLE")%></a><br>
 <%
 }
 subscribe.close();
 %>


<%-- Finished and Cancel Changes buttons --%>
<form>
<table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
  <input type=button name=cancel value="Cancel" onClick="location='subscribe.jsp?action=cancel'">
</td></tr></table>
</form>

<%
 }
  catch (Exception e) {
    out.println("Subscriber:ERROR!");
    out.print(e.getMessage());
	 e.printStackTrace(System.err);
 } 
 }
%>

<jsp:include page="footer.jsp" flush="true" />

</body>
</html>