<%@ page errorPage="error.jsp" %>
<%@ page import="org.jasig.portal.*" %>

<jsp:useBean id="authBean" class="org.jasig.portal.AuthorizationBean" 
                           type="org.jasig.portal.IAuthorizationBean"/>

<%
String sUserName = request.getParameter ("userName");
String sPassword = request.getParameter ("password");
    
// Authenticate anyone automatically for now
boolean bAuthorized = authBean.authorize (sUserName, sPassword);
    
if (bAuthorized)
{
  if (session != null)
    session.invalidate();
        
  session = request.getSession (true);
  session.setAttribute ("userName", sUserName);      
}
else
{
  session.setAttribute ("logonStatus", "true");
}

response.sendRedirect("layout.jsp");
%>
