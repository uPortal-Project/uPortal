<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page import="com.objectspace.xml.*" %>
<%@ page errorPage="error.jsp" %>

<jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" />

<%
String sAction = request.getParameter ("action");

if (sAction != null)
{
  // Tabs
  if (sAction.equals ("addTab"))
    layoutBean.addTab (request);
  else if (sAction.equals ("renameTab"))
    layoutBean.renameTab (request);
  else if (sAction.equals ("setDefaultTab"))
    layoutBean.setDefaultTab (request);

  // Columns
  else if (sAction.equals ("addColumn"))
    layoutBean.addColumn (request);
  else if (sAction.equals ("removeColumn"))
    layoutBean.removeColumn (request);
  else if (sAction.equals ("moveColumnRight"))
    layoutBean.moveColumnRight (request);
  else if (sAction.equals ("moveColumnLeft"))
    layoutBean.moveColumnLeft (request);
  else if (sAction.equals ("setColumnWidth"))
    layoutBean.setColumnWidth (request);

  // Channels
  else if (sAction.equals ("removeChannel"))
    layoutBean.removeChannel (request);
  else if (sAction.equals ("addChannel"))
    layoutBean.addChannel (request);
  else if (sAction.equals ("moveChannelLeft"))
    layoutBean.moveChannelLeft (request);
  else if (sAction.equals ("moveChannelRight"))
    layoutBean.moveChannelRight (request);
  else if (sAction.equals ("moveChannelUp"))
    layoutBean.moveChannelUp (request);
  else if (sAction.equals ("moveChannelDown"))
    layoutBean.moveChannelDown (request);

  // Go back to default layout xml
  else if (sAction.equals ("revertToDefaultLayoutXml"))
  {
    IXml layoutXml = layoutBean.getLayoutXml (request, layoutBean.getUserName (request));
    layoutXml = layoutBean.getDefaultLayoutXml (request, layoutBean.getUserName (request));
  }

  // Save the layout xml
  else if (sAction.equals ("save"))
  {
    IXml layoutXml = layoutBean.getLayoutXml (request, layoutBean.getUserName (request));
    layoutBean.setLayoutXml (layoutBean.getUserName (request), layoutXml);
    session.removeAttribute ("layoutXml");
    response.sendRedirect ("layout.jsp");
  }

  // Ignore changes and return to the layout
  else if (sAction.equals ("cancel"))
  {
    session.removeAttribute ("layoutXml");
    response.sendRedirect ("layout.jsp");
  }
}
%>

<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title>Personalize Layout</title>
<link rel=stylesheet href="stylesheets/general.css" TYPE="text/css">
<script language="JavaScript">
<!-- hide
function getActionAndSubmit(theForm, buttonValue)
{
  theForm.action.value = buttonValue

  if (theForm.elements[0].selectedIndex > -1)
    theForm.submit ()
  else
    alert ('Please select a channel and try again.')
}
//stop hiding-->
</script>
</head>

<% layoutBean.writeBodyStyle (request, response, out); %>
<body>

<%-- Header --%>
<% session.setAttribute ("headerTitle", "Personalize Layout"); %>
<%@ include file="header.jsp" %>

<%-- Finished and Cancel Changes buttons --%>
<form>
<table border=0 cellspacing=5 cellpadding=5 width="100%"><tr bgcolor="#dddddd"><td>
  <input type=button name=finished value="Finished" onClick="location='personalizeLayout.jsp?action=save'">
  <input type=button name=cancel value="Cancel Changes" onClick="location='personalizeLayout.jsp?action=cancel'">
</td></tr></table>
</form>

<% layoutBean.writePersonalizeLayoutPage (request, response, out); %>

<%-- Finished and Cancel Changes buttons --%>

<table border=0 cellspacing=5 cellpadding=5 width="100%">
  <form><tr bgcolor="#dddddd"><td>
  <input type=button name=finished value="Finished" onClick="location='personalizeLayout.jsp?action=save'">
  <input type=button name=cancel value="Cancel Changes" onClick="location='personalizeLayout.jsp?action=cancel'">
</td></tr></form></table>


<jsp:include page="footer.jsp" flush="true" />

</body>
</html>