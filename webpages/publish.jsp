<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page errorPage="error.jsp" %>

<jsp:useBean id="layoutBean" class="org.jasig.portal.LayoutBean" type="org.jasig.portal.ILayoutBean" scope="session" />

<%
String sAction = request.getParameter ("action");
%>

<html>
<head>
<title>Publish Channel</title>
<META HTTP-EQUIV="expires" CONTENT="Tue, 20 Aug 1996 14:25:27 GMT">
</head>

<% layoutBean.writeBodyTag (request, response, out); %>

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
    <tr valign="top"> 
      <td width="26%" height="63"> 
        <input type="radio" name="chan_type" value="CRSSChannel">
        RSS Channel</td>
      <td width="3%" height="63">&nbsp;</td>
      <td width="71%" height="63"><font size="2">RSS or Rich Site Summary, the 
        most common channel format, is a channel which has its content defined 
        through an XML document. Anyone may publish an RSS channel as long as 
        the specification is adhered to and the file is stored on a publicly accessible 
        web server. You will need to provide a URL for the RSS file.</font></td>
    </tr>
    <tr>
      <td width="26%" height="2">&nbsp;</td>
      <td width="3%" height="2">&nbsp;</td>
      <td width="71%" height="2">&nbsp;</td>
    </tr>
    <tr valign="top"> 
      <td width="26%" height="37"> 
        <input type="radio" name="chan_type" value="CPageRenderer">
        HTML Channel</td>
      <td width="3%" height="37">&nbsp;</td>
      <td width="71%" height="37"><font size="2">HTML is a quick and easy way 
        to create a channel. Anyone may publish a HTML channel as long as the 
        HTML is valid and stored on a publicly accessible web server. You will 
        need to provide a URL for the HTML file.</font></td>
    </tr>
  </table>
  <p> 
    <input type="submit" name="Submit" value="Next">
  </p>
</form>

<%-- Footer --%>
<%@ include file="footer.jsp" %>

</body>
</html>