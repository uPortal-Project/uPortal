<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>
<%-- include file="support.jsp" --%>

<%
    KickCriteria criteria = new KickCriteria(request);
%>

<html>
  <head>
    <title>Kick Users:  <%=request.getServerName() %>:<%=request.getServerPort()%></title>
  </head>

  
  <body bgcolor="#FFFFFF">
    <p>The criteria you selected:</p>

    <ul>

<% if (criteria.getCreationTimeCriteria() != KickCriteria.NOT_SET) { %>
      <li>Creation time <%= criteria.getCreationTime() %> minutes ago.</li>
<% } %>

<% if (criteria.getLastAccessedTimeCriteria() != KickCriteria.NOT_SET) { %>  
      <li>Last accessed time <%= criteria.getLastAccessedTime() %> minutes ago.</li>
<% } %>
      
      <li>Sessions of type <%= criteria.getSessionType() %>.</li>
 
<% if (criteria.getInactiveTimeCriteria() != KickCriteria.NOT_SET) { %>
      <li>Inactivity time is <%= criteria.getInactiveTime() %> seconds.</li>
<% } %>
 
   </ul>

   <p>And kicking users with a <%= KickCriteria.interpretKickType(criteria.getKickType()) %>
   will invalidate:
   
    <h3>204 users</h3>

    <% if(criteria.getKickType() == KickCriteria.RESET_KICK) { %>
        <p>At the end of the inactivity timeout.</p>
    <% } else if(criteria.getKickType() == KickCriteria.INVALIDATE_KICK) { %>
        <p>Immediately after pressing the CONTINUE! button below.
    <% } %>

    <p style="font-size: 80%">NOTE: This number will fluctuate
    slightly if you continue since the action has not been
    committed, and the number of users affected could have
    changed since this estimate was obtained.</p>

    <form action="kick_users_completed.jsp">
      <input type="submit" value="CONTINUE!"> <input type=
      "submit" value="CANCEL">
    </form>
  </body>
</html>


