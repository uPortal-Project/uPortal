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
  else if (sAction.equals ("removeTab"))
    layoutBean.removeTab (request);
  else if (sAction.equals ("moveTabDown"))
    layoutBean.moveTabDown (request);
  else if (sAction.equals ("moveTabUp"))
    layoutBean.moveTabUp (request);

  // Columns
  else if (sAction.equals ("addColumn"))
    layoutBean.addColumn (request);
  else if (sAction.equals ("removeColumn"))
    layoutBean.removeColumn (request);
  else if (sAction.equals ("moveColumnRight"))
    layoutBean.moveColumnRight (request);
  else if (sAction.equals ("moveColumnLeft"))
    layoutBean.moveColumnLeft (request);

  // Channels
  else if (sAction.equals ("removeChannel"))
    layoutBean.removeChannel (request);
  else if (sAction.equals ("moveChannelUp"))
    layoutBean.moveChannelUp (request);
  else if (sAction.equals ("moveChannelDown"))
    layoutBean.moveChannelDown (request);

  // Save the layout xml
  IXml layoutXml = layoutBean.getLayoutXml (request, layoutBean.getUserName (request));
  layoutBean.setLayoutXml (layoutBean.getUserName (request), layoutXml);
  session.removeAttribute ("layoutXml");
}
%>

<% UtilitiesBean.preventPageCaching (response); %>

<html>
<head>
<title>Personalize Layout</title>
<script language="JavaScript">
<!-- hide
function getActionAndSubmit(theForm, buttonValue)
{
  theForm.action.value = buttonValue
  theForm.submit ()
}
//stop hiding-->
</script>
</head>

<% layoutBean.writeBodyTag (request, response, out); %>

<jsp:include page="header.jsp" flush="true">
  <jsp:param name="title" value="Personalize Layout" />
</jsp:include>

<input type=button name=finished value="Finished" onClick="location='layout.jsp'"><br>
<% layoutBean.writePersonalizeLayoutPage (request, response, out); %>
<input type=button name=finished value="Finished" onClick="location='layout.jsp'"><br>

<jsp:include page="footer.jsp" flush="true" />

</body>
</html>