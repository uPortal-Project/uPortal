<%@ page errorPage="error.jsp" %>
<%@ page import="org.jasig.portal.*" %>

<jsp:useBean id="authBean" class="org.jasig.portal.AuthorizationBean"
                           type="org.jasig.portal.IAuthorizationBean"/>

<%
String sUserName = request.getParameter ("userName");
String sPassword = request.getParameter ("password");

boolean bAuthorized = authBean.authorize (sUserName, sPassword);

if (bAuthorized)
{
  /*** Tomcat 3.1 has a bug (http://jakarta.apache.org/bugs/show_bug.cgi?id=55)
       which prevents you from invalidating the session and then
       creating it again.  So in the meantime, we'll just
       clear out the session attributes to indicate a logoff/logon  ***/

  // Clear out session attributes
  java.util.Enumeration e = session.getAttributeNames ();

  while (e.hasMoreElements ())
  {
    session.removeAttribute ((String) e.nextElement ());
  }

  // Put the username in the session
  session.setAttribute ("userName", sUserName);
}
else
  session.setAttribute ("userName", "guest");

response.sendRedirect("layout.jsp");
%>
