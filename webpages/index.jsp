<%@ page errorPage="error.jsp" %>

<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.UtilitiesBean" %>

<%-- We need a way to configure the top level portal directory
     Here it is read in from web.xml and stored in GenericPortalBean. --%>

<%

String sPortalBaseDir = (String) application.getInitParameter ("portalBaseDir");
if (sPortalBaseDir == null)
{
  String sErrorMsg = "ERROR: No value has been set for application-context parameter \"portalBaseDir\".";
  System.err.println (sErrorMsg);
  throw new Exception (sErrorMsg);
}
else
{
  java.io.File portalBaseDir = new java.io.File (sPortalBaseDir);
  if (portalBaseDir.exists ()) {
    org.jasig.portal.GenericPortalBean.setPortalBaseDir (sPortalBaseDir);
  }
  else
  {
    String sErrorMsg = "ERROR: The portal base directory specified does not exist: " + sPortalBaseDir;
    System.err.println (sErrorMsg);
    throw new Exception (sErrorMsg);
  }
}
%>

<jsp:useBean id="layoutBean" type="org.jasig.portal.LayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />


<% UtilitiesBean.preventPageCaching (response); %>


<%
layoutBean.writeContent (request, response, out);
%>