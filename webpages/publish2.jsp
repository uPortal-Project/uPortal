<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page errorPage="error.jsp" %>
<%@ include file="checkinit.jsp" %>
<%@ include file="checkGuest.jsp" %>

<jsp:useBean id="layoutBean" class="org.jasig.portal.LayoutBean" type="org.jasig.portal.ILayoutBean" scope="session" />
<jsp:useBean id="publish" class="org.jasig.portal.PublisherBean"  scope="session" />

<%
String sChan_type = request.getParameter ("chan_type");
String sPub_email = request.getParameter("pub_email");
%>

 <%-- UtilitiesBean.preventPageCaching (response); --%>

<html>
<head>
<title>Publish Channel</title>
<link rel=stylesheet href="stylesheets/general.css" TYPE="text/css">
</head>

<% layoutBean.writeBodyStyle (request, response, out); %>

<%-- Header --%>
<% session.setAttribute ("headerTitle", "Publish Channel"); %>
<%@ include file="header.jsp" %>

<form action="previewPublish.jsp" method=post name="">

<%--make sure we got an email and channel type--%>
<%
if ((sChan_type == null) || (sPub_email== null))
{
%>

  <p>You failed to enter required information.<br>
  </p>
  <ul>
  <%= sChan_type == null ? "<li><b>Channel Type</b>" : " "%>
  <%= sChan_type == null ? "<li><b>Email Address</b>" : " "%>
  </ul>

  <p>Please go back and enter this information and proceed. 
  </p>
  
<%
}
else 
{
%>
  <p><font size="5"><b>Step 3.</b></font><br>

   <% publish.writeParamFields(request, response, out); %>
   
  <p> 
    <input type="hidden" name="chan_type" value="<%= sChan_type %>">
	<input type="hidden" name="pub_email" value="<%= sPub_email %>">
    <input type="submit" name="Submit" value="Next">
  </p>
<% } %>  
</form>

<%-- Footer --%>
<%@ include file="footer.jsp" %>

</body>
</html>
