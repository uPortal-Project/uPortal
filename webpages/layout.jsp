

<%@ page errorPage="error.jsp" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.UtilitiesBean" %>
<jsp:useBean id="xmlLayoutBean" type="org.jasig.portal.XMLLayoutBean" class="org.jasig.portal.XMLLayoutBean" scope="session" />

<% String sUserName = (String) session.getAttribute ("userName"); %>
<% UtilitiesBean.preventPageCaching (response); 
   org.jasig.portal.Logger.setSession(session);
%>


<%
xmlLayoutBean.writeChannels (request, response, out);
%>
