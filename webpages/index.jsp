

<%@ page errorPage="error.jsp" %>

<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.UtilitiesBean" %>

<%
String sPortalBaseDir = "/home/bw/ibs/portal/";
org.jasig.portal.GenericPortalBean.setPortalBaseDir ("/home/bw/ibs/portal/");
%>

<jsp:useBean id="layoutBean" type="org.jasig.portal.LayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />

<% String sUserName = (String) session.getAttribute ("userName"); %>
<% UtilitiesBean.preventPageCaching (response); %>


<%
layoutBean.writeContent (request, response, out);
%>

