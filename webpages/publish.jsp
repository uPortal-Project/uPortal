<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page errorPage="error.jsp" %>
<%@ include file="checkinit.jsp" %>
<%@ include file="checkGuest.jsp" %>

<jsp:useBean id="layoutBean" class="org.jasig.portal.LayoutBean" type="org.jasig.portal.ILayoutBean" scope="session" />
<jsp:useBean id="publish" class="org.jasig.portal.PublisherBean" scope="request" />

<%
String sAction = request.getParameter ("action");
%>

<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title>Publish Channel</title>
<link rel=stylesheet href="stylesheets/general.css" TYPE="text/css">
</head>

<% layoutBean.writeBodyStyle (request, response, out); %>

<%-- Header --%>
<% session.setAttribute ("headerTitle", "Publish Channel"); %>
<%@ include file="header.jsp" %>

<form action="publish2.jsp" method=post name="">

<%--STEP 1- get email and channel type--%>


  <p>You are about to start the process of &quot;publishing&quot; a channel which 
    other portal users may subscribe to.<br>
  </p>
  <p><font size="5"><b>Step 1.</b></font> First we need a way to contact you as 
    the publishers. Please provide your email address.<br>
  </p>
  <table width="40%" border="0">
    <tr>
      <td width="19%">email:</td>
      <td width="81%"> 
        <input type="text" name="pub_email" maxlength="50" size="40">
      </td>
    </tr>
  </table>
  <p><font size="5"><b>Step 2.</b></font> Next you need to choose a channel type. 
    Below you'll find a list of available channels along with a brief description. 
    Please pick one. </p>
  <table width="75%" border="0">

  <% publish.writeChannelTypes(request, response, out); %>

  </table>
  <p> 
    <input type="submit" name="Submit" value="Next">
  </p>
</form>

<%-- Footer --%>
<%@ include file="footer.jsp" %>

</body>
</html>
