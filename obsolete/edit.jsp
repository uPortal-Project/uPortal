<%@ page errorPage="error.jsp" %>
<%@ page import="org.jasig.portal.layout.*" %>
<%@ page import="org.jasig.portal.UtilitiesBean" %>
<jsp:useBean id="editBean" class="org.jasig.portal.EditBean" scope="session" />

<% org.jasig.portal.IChannel ch = editBean.getChannelToEdit (request); %>
<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title>Edit</title>
</head>

<body bgcolor=ffffff>

<!-- Header -->
<% String sTitle = "Edit " +  ch.getName (); %>
<jsp:include page="header.jsp" flush="true">
  <jsp:param name="title" value="<%= sTitle %>" />
</jsp:include>

<% ch.edit (request, response, out); %>

<!-- Footer -->
<jsp:include page="footer.jsp" flush="true" />

</body>
</html>