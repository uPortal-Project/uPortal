<%@ page errorPage="error.jsp" %>

<%
if (session != null)
  session.invalidate();
        
response.sendRedirect("layout.jsp");
%>

