<%@ page errorPage="error.jsp" %>

<%-- We need a way to configure the top level portal directory
     Here it is read in from web.xml and stored in GenericPortalBean. --%>
<%
String sPortalBaseDir = (String) application.getInitParameter ("portalBaseDir");

// Check if portal base directory has been set
if (sPortalBaseDir == null)
{
  String sErrorMsg = "ERROR: No value has been set for application-context parameter \"portalBaseDir\".";
  System.err.println (sErrorMsg);
  throw new Exception (sErrorMsg);
}
else
{
  java.io.File portalBaseDir = new java.io.File (sPortalBaseDir);
  
  // Make sure the portal base directory exists
  if (portalBaseDir.exists ())
    org.jasig.portal.GenericPortalBean.setPortalBaseDir (sPortalBaseDir);
  else
  {
    String sErrorMsg = "ERROR: The portal base directory specified does not exist: " + sPortalBaseDir;
    System.err.println (sErrorMsg);
    throw new Exception (sErrorMsg);
  }
}
%>

<jsp:forward page="layout.jsp" />
