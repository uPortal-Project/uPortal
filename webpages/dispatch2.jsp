<%@ page import="org.jasig.portal.layout.*" %><%@ page import="org.jasig.portal.UtilitiesBean" %><jsp:useBean id="dispatchBean" class="org.jasig.portal.DispatchBean" scope="session" /><jsp:useBean id="layoutBean" type="org.jasig.portal.ILayoutBean" class="org.jasig.portal.LayoutBean" scope="session" /><%
org.jasig.portal.IChannel ch = dispatchBean.getChannel (request);
if (ch == null) {
  return;
}
/*
  Have to disable caching since it causes problems with downloading
  attachments and zip files.

  UtilitiesBean.preventPageCaching (response);
*/

String sMethodName = request.getParameter ("method");  

Class[] paramTypes = new Class[] {Class.forName ("javax.servlet.http.HttpServletRequest"), Class.forName ("javax.servlet.http.HttpServletResponse"), Class.forName ("javax.servlet.jsp.JspWriter")};
Object[] methodParams = new Object[] {request, response, out};
java.lang.reflect.Method method = ch.getClass ().getMethod (sMethodName, paramTypes);
method.invoke (ch, methodParams);
%>