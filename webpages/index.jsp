<%@ page errorPage="error.jsp" %>

<%-- We need a way to configure the top level portal directory
     For now, it will be specified here and stored in the GenericPortalBean. --%>
<%
String sPortalBaseDir = "D:\\Projects\\JA-SIG\\Portal\\";
org.jasig.portal.GenericPortalBean.setPortalBaseDir (sPortalBaseDir);
%>

<jsp:forward page="layout.jsp" />
